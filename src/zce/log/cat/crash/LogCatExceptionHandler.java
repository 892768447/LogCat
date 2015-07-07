package zce.log.cat.crash;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;

import android.content.Context;
import android.os.Build;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

public class LogCatExceptionHandler implements UncaughtExceptionHandler {

	private static LogCatExceptionHandler handler;
	private static Context mcontext;
	private String SdPath;

	private LogCatExceptionHandler(String SdPath) {
		this.SdPath = SdPath;
	}

	public synchronized static LogCatExceptionHandler getInstance(
			Context context, String SdPath) {
		if (handler == null) {
			handler = new LogCatExceptionHandler(SdPath);
			mcontext = context;
		}
		return handler;
	}

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		// TODO Auto-generated method stub
		if (SdPath == null || SdPath.length() == 0) {
			return;
		}
		try {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			ex.printStackTrace(pw);
			System.out.println("错误信息" + sw.toString());
			File file = null;
			file = new File(SdPath + "/LogCatError.log");
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(("time:" + System.currentTimeMillis() + "\n").getBytes());
			fos.flush();
			fos.write(sw.toString().getBytes());
			fos.flush();
			// 获取手机的版本信息
			Field[] fields = Build.class.getFields();
			for (Field field : fields) {
				field.setAccessible(true);// 暴力反射
				String key = field.getName();
				String value = field.get(null).toString();
				fos.write((key + "=" + value + "\n").getBytes());
				fos.flush();
			}
			fos.close();
			new Thread() {
				@Override
				public void run() {
					Looper.prepare();
					Toast.makeText(mcontext, "啊哦！程序崩溃鸟！", Toast.LENGTH_LONG)
							.show();
					Looper.loop();
				};
			}.start();
			Log.i("LogCatException", "程序出现异常。已被捕获");
			android.os.Process.killProcess(android.os.Process.myPid());
			System.exit(2);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
