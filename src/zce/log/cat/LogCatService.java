package zce.log.cat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

@SuppressLint("SdCardPath")
public class LogCatService extends Service implements LogCatInterface {

	// private final static String tag = "LogCatService";
	private final static String grey = "<font color=\"#CCCCCC\">%s</font>";
	private final static String blue = "<font color=\"#0066CC\">%s</font>";
	private final static String green = "<font color=\"#00FF00\">%s</font>";
	private final static String orange = "<font color=\"#FF9900\">%s</font>";
	private final static String red = "<font color=\"#FF0000\">%s</font>";
	private final IBinder binder = new LogCatBinder();
	private ActivityManager activityManager;
	private List<RunningAppProcessInfo> procList;
	private int size;
	private String command;
	private String name;
	private String path;
	private Intent inten;
	private boolean isPath;
	private File file;
	private BufferedWriter bw;
	private BufferedReader br;
	private Process process;
	private Handler handler;
	private Context context;
	private Thread thread;
	private Matcher m;
	private Pattern p;

	public class LogCatBinder extends Binder {
		public LogCatService getService() {
			return LogCatService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		// Log.v(tag, "Service onBind--->");
		return binder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		// Log.v(tag, "Service onUnbind--->");
		if (thread != null) {
			thread.interrupt();
		}
		stopSelf();
		return super.onUnbind(intent);
	}

	@SuppressLint("HandlerLeak")
	@Override
	public void onCreate() {
		// Log.v(tag, "Service onCreate--->");
		context = getApplicationContext();
		p = Pattern
				.compile("\\s+V\\s+|\\s+D\\s+|\\s+I\\s+|\\s+W\\s+|\\s+E\\s+");
		activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		inten = new Intent();
		inten.setAction("zce.log.cat.LogCatService");
		handler = new Handler() {
			public void handleMessage(Message msg) {
				context.sendBroadcast(inten);// 用于线程中发送广播
			}
		};
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		// Log.v(tag, "Service onDestroy--->");
		super.onDestroy();
	}

	public void setParams(String name, String command, String path) {
		// Log.v(tag,
		// String.format("setParams name: %s command: %s path: %s",name,
		// command, path));
		this.name = name;
		this.command = command;
		this.path = path;
		if (path == null || path.length() < 1) {
			isPath = false;
		} else {
			isPath = true;
		}
	}

	public void startLogCat() {
		if (thread == null) {// 如果线程为空
			// Log.v(tag, "新建线程");
			thread = new Thread(new LogCat());
			thread.setDaemon(true);
			thread.start();
		} else {
			// Log.v(tag, "退出原来线程,启动新线程");
			thread.interrupt();
			thread = new Thread(new LogCat());
			thread.setDaemon(true);
			thread.start();
		}
	}

	private class LogCat implements Runnable {
		public void run() {
			// Log.v(tag, "... name: " + name);
			// Log.v(tag, "... command: " + command);
			// Log.v(tag, "... path: " + path);
			String pid = "0";
			boolean running = true;
			while (running) {// 非全部过滤才执行
				procList = activityManager.getRunningAppProcesses();
				for (int i = 0; i < procList.size(); i++) {
					RunningAppProcessInfo procInfo = procList.get(i);
					if (procInfo.processName.equals(name)) {
						pid = Integer.toString(procInfo.pid);// 根据传入的包名获得该程序的进程
						// Log.i(tag, "pid: " + pid);
						running = false;
						break;
					}
				}
				if (!running) {
					break;
				}
			}
			// Log.i(tag, "退出进程id获取");
			if (isPath) {
				// 判断是否要保存为文件
				file = new File(path);
				try {
					bw = new BufferedWriter(new FileWriter(file));
				} catch (IOException e) {
					e.printStackTrace();
					isPath = false;
					System.out.println("new FileWriter错误不保存文件");
				}
			}

			try {
				process = Runtime.getRuntime().exec(command);

				br = new BufferedReader(new InputStreamReader(
						process.getInputStream()));
				String line;
				LogList.clear();
				while (!Thread.currentThread().isInterrupted()
						&& ((line = br.readLine()) != null)) {
					String[] strs = line.split("\\s+");
					if (strs.length > 3) {
						if (strs[2].trim().replace("\t", "").equals(pid)) {// 找到log中的pid
							size = LogList.size();
							if (size > 50) {
								for (int i = 0; i <= size - 50; i++) {
									LogList.remove(size - 50);
								}
							}
							m = p.matcher(line);
							if (m.find()) {
								String tag = m.group(0).trim()
										.replace("\t", "");
								if (tag.equals("V") && Tag.get("V")) {
									LogList.add(String.format(grey, line));
									handler.sendEmptyMessage(1);
									if (isPath && bw != null) {
										bw.write(line + "\n\n");
										bw.flush();
									}
									continue;
								}
								if (tag.equals("D") && Tag.get("D")) {
									LogList.add(String.format(blue, line));
									handler.sendEmptyMessage(1);
									if (isPath && bw != null) {
										bw.write(line + "\n\n");
										bw.flush();
									}
									continue;
								}
								if (tag.equals("I") && Tag.get("I")) {
									LogList.add(String.format(green, line));
									handler.sendEmptyMessage(1);
									if (isPath && bw != null) {
										bw.write(line + "\n\n");
										bw.flush();
									}
									continue;
								}
								if (tag.equals("W") && Tag.get("W")) {
									LogList.add(String.format(orange, line));
									handler.sendEmptyMessage(1);
									if (isPath && bw != null) {
										bw.write(line + "\n\n");
										bw.flush();
									}
									continue;
								}
								if (tag.equals("E") && Tag.get("E")) {
									LogList.add(String.format(red, line));
									handler.sendEmptyMessage(1);
									if (isPath && bw != null) {
										bw.write(line + "\n\n");
										bw.flush();
									}
									continue;
								}
							}
						}
					}
				}
				if (bw != null)
					bw.close();
				if (br != null)
					br.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
