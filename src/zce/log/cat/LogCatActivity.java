package zce.log.cat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import zce.log.cat.crash.LogCatApplication;
import zce.log.cat.widget.AutoCompleteTextView;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.text.ClipboardManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.Toast;

@SuppressWarnings("deprecation")
public class LogCatActivity extends Activity implements LogCatInterface,
		OnClickListener, OnCheckedChangeListener, OnItemClickListener,
		OnScrollListener, TextWatcher {

	private ListView listView;// log列表
	private AutoCompleteTextView package_name_edit;// 包名输入框
	private EditText save_path_edit;// 保存路径输入框
	private CheckBox tag_vCheckBox, tag_dCheckBox, tag_iCheckBox,
			tag_wCheckBox, tag_eCheckBox, tag_rCheckBox;// tag复选框
	private Button startButton;// 开始按钮
	private Button clearButton;// 清除按钮
	private Button exitButton;// 退出按钮
	private Button copy;// 复制
	private Button search;// 搜索
	private Toast toast;
	private Intent intent;
	private TableLayout toollayout;
	private RelativeLayout rltool;//
	private ArrayList<String> appname;
	private ArrayAdapter<String> adapter;
	private LogCatAdapter<String> simpleAdapter;// log适配器
	private MyReceiver receiver;// 广播接收
	private ClipboardManager cm;
	private LogCatService lcs;
	private String SdPath;
	// private final String tag = "LogCatActivity";
	private String logdata;
	private boolean isPause = false;
	private boolean isError = false;

	/**
	 * onCreate
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		// WindowManager.LayoutParams.FLAG_FULLSCREEN);//全屏
		setContentView(R.layout.main);
		FindViewById();

		getInstalledApps(true);
		SdPath = ((LogCatApplication) getApplication()).getSdPath();
		setCheckBoxValue();// 把checkb的值保存到全局Tag中
		receiver = new MyReceiver();
		cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
		IntentFilter filter = new IntentFilter();
		filter.addAction("zce.log.cat.LogCatService");// 广播过滤
		registerReceiver(receiver, filter);// 注册广播
		bindService(new Intent(this, LogCatService.class), connection,
				Context.BIND_AUTO_CREATE);// 绑定服务

		intent = new Intent();
		intent.setAction("android.intent.action.VIEW");

		sendErrorEmail();
		super.onCreate(savedInstanceState);
		new LogCatCheck(this, getResources().openRawResource(R.raw.data)).run();// 检测签名

	}

	/**
	 * onDestroy
	 */
	@Override
	protected void onDestroy() {
		// Log.i(tag, "onDestroy");
		unregisterReceiver(receiver);// 取消广播注册
		unbindService(connection);
		stopService(new Intent(this, LogCatService.class));
		try {
			Thread.sleep(2);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.exit(0);
		super.onDestroy();
	}

	/**
	 * onPause
	 */
	@Override
	protected void onPause() {
		// Log.v(tag, "onPause");
		setPause(true);// 暂停
		super.onPause();
	}

	/**
	 * onResume
	 */
	@Override
	protected void onResume() {
		super.onResume();
		// Log.v(tag, "onResume");
		setPause(false);// 不暂停
		listView.post(new Runnable() {
			@Override
			public void run() {
				simpleAdapter.notifyDataSetChanged();
				listView.setSelection(listView.getBottom()
						+ listView.getCount());
			}
		});
	}

	/**
	 * afterTextChanged
	 */
	@Override
	public void afterTextChanged(Editable s) {
		String str = s.toString();
		int apos = str.indexOf("\n\t");
		int ppos = str.indexOf("\n\t\t");
		if (ppos > -1) {
			s.delete(ppos, str.length());
		} else if (apos > -1) {
			s.delete(0, apos + 2);
		}
	}

	/**
	 * beforeTextChanged
	 */
	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		// TODO Auto-generated method stub
	}

	/**
	 * onTextChanged
	 */
	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		// TODO Auto-generated method stub
	}

	/**
	 * onScroll
	 */
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		// TODO Auto-generated method stub
	}

	/**
	 * onScrollStateChanged
	 */
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		switch (scrollState) {
		case SCROLL_STATE_FLING:
			setPause(false);// 不暂停
			// Log.w(tag, "SCROLL_STATE_FLING");
			break;
		case SCROLL_STATE_IDLE:
			setPause(false);// 不暂停
			// Log.w(tag, "SCROLL_STATE_IDLE");
			break;
		case SCROLL_STATE_TOUCH_SCROLL:
			setPause(true);// 暂停
			// Log.w(tag, "SCROLL_STATE_TOUCH_SCROLL");
			break;
		}
	}

	/**
	 * onItemClick
	 */
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		String data = LogList.get(arg2);
		if (data.length() <= 0) {
			return;
		}
		String[] split = data.split(":");
		if (split.length > 3) {
			try {
				logdata = split[3];
				logdata = logdata.substring(0, logdata.length() - 7);
				// Log.i(tag, logdata);
				intent.setData(Uri.parse(getString(R.string.search) + logdata));
			} catch (Exception e) {

			}
		} else {
			Toast.makeText(this, getString(R.string.ftgtd), Toast.LENGTH_SHORT)
					.show();
			return;
		}
		showTool();
	}

	/**
	 * onCheckedChanged
	 */
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch (buttonView.getId()) {
		case R.id.tag_v:
			Tag.put("V", isChecked);
			// Log.v(tag, "V");
			break;
		case R.id.tag_d:
			Tag.put("D", isChecked);
			// Log.v(tag, "D");
			break;
		case R.id.tag_i:
			Tag.put("I", isChecked);
			// Log.v(tag, "I");
			break;
		case R.id.tag_w:
			Tag.put("W", isChecked);
			// Log.v(tag, "W");
			break;
		case R.id.tag_e:
			Tag.put("E", isChecked);
			// Log.v(tag, "E");
			break;
		case R.id.tag_r:
			Tag.put("R", isChecked);
			// Log.v(tag, "R");
			break;
		}
	}

	/**
	 * 点击事件 onClick
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.start:
			// Log.v(tag, "start");
			// LogList.clear();
			String packageName = package_name_edit.getText().toString().trim();
			for (String aname : appname) {
				int apos = aname.indexOf("\n\t");
				int ppos = aname.indexOf("\n\t\t");
				if (ppos > -1) {
					aname = aname.substring(0, ppos);
					// System.out.println(aname);
				} else if (apos > -1) {
					aname = aname.substring(apos + 2, aname.length());
					// System.out.println(aname);
				}
				if (aname.equals(packageName)) {
					String path = save_path_edit.getText().toString().trim();
					if (sdk > 15) {
						if (Tag.get("R")) {
							lcs.setParams(packageName,
									"su ; logcat -v threadtime", path);
							lcs.startLogCat();
							startActivityByName(packageName);
						} else {
							lcs.setParams(packageName, "logcat -v threadtime",
									path);
							lcs.startLogCat();
							Toast.makeText(
									this,
									getResources()
											.getString(R.string.sdknotice),
									Toast.LENGTH_SHORT).show();
						}
					} else {
						lcs.setParams(packageName, "logcat -v threadtime", path);
						lcs.startLogCat();
						startActivityByName(packageName);
					}
					showMenu();
					return;
				}
			}
			toast.setText(getString(R.string.notfound));
			toast.show();
			break;
		case R.id.clear:
			// Log.v(tag, "clear");
			LogList.clear();
			simpleAdapter.notifyDataSetChanged();
			showMenu();
			break;
		case R.id.exit:
			// Log.v(tag, "exit");
			showMenu();
			finish();
			break;
		case R.id.copy:
			showTool();
			cm.setText(logdata);
			Toast.makeText(this, getString(R.string.copynotice),
					Toast.LENGTH_SHORT).show();
			break;
		case R.id.search:
			showTool();
			try {
				intent.setClassName("com.UCMobile",
						"com.UCMobile.main.UCMobile");
				startActivity(intent);
			} catch (Exception e) {
				e.printStackTrace();
				try {
					intent.setClassName("com.android.browser",
							"com.android.browser.BrowserActivity");
					startActivity(intent);
				} catch (Exception ee) {
					ee.printStackTrace();
					Toast.makeText(this, getString(R.string.bnotfound),
							Toast.LENGTH_SHORT).show();
				}
			}
			break;
		}
	}

	/**
	 * 返回键事件 onKeyDown
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (rltool.getVisibility() == RelativeLayout.VISIBLE) {
				rltool.setVisibility(RelativeLayout.GONE);
				return true;
			}
			if (toollayout.getVisibility() == TableLayout.VISIBLE) {
				toollayout.setVisibility(TableLayout.GONE);
				return true;
			}
			if (isError) {
				new File(SdPath + "/LogCatError.log").delete();
				isError = false;
				return true;
			}
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.addCategory(Intent.CATEGORY_HOME);
			startActivity(intent);
			Toast.makeText(this, getString(R.string.backstr),
					Toast.LENGTH_SHORT).show();
			return true;
		}
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			if (rltool.getVisibility() == TableLayout.VISIBLE) {
				rltool.setVisibility(TableLayout.GONE);
			}
			showMenu();
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * 广播接收器 MyReceiver
	 */
	private class MyReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// String action = intent.getAction();
			// Log.v(tag, action);
			if (!getPause()
					&& intent.getAction().equals("zce.log.cat.LogCatService")) {
				listView.post(new Runnable() {
					@Override
					public void run() {
						simpleAdapter.notifyDataSetChanged();
						listView.setSelection(listView.getBottom());
					}
				});
			} else {
				// Log.w(tag, "程序后台运行中..暂停更新界面..");
			}
		}
	}

	private ServiceConnection connection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			lcs = ((LogCatService.LogCatBinder) service).getService();
			// Log.v(tag, "得到Service对象");
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			lcs = null;
		}
	};

	/**
	 * 获取已安装程序 getSysPackages
	 */
	private void getInstalledApps(final boolean getSysPackages) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				List<PackageInfo> packs = getPackageManager()
						.getInstalledPackages(0);
				for (int i = 0; i < packs.size(); i++) {
					PackageInfo p = packs.get(i);
					if ((!getSysPackages) && (p.versionName == null)) {
						continue;
					}
					appname.add(p.applicationInfo
							.loadLabel(getPackageManager()).toString()
							+ "\n\t"
							+ p.packageName);
					appname.add(p.packageName
							+ "\n\t\t"
							+ p.applicationInfo.loadLabel(getPackageManager())
									.toString());
				}
				rltool.post(new Runnable() {
					@Override
					public void run() {
						adapter.notifyDataSetChanged();
						// Log.i(tag, "获取程序列表结束");
					}
				});
			}
		}).start();
	}

	/**
	 * 设置复选框值 setCheckBoxValue
	 */
	public void setCheckBoxValue() {
		Tag.put("V", true);
		Tag.put("D", true);
		Tag.put("I", true);
		Tag.put("W", true);
		Tag.put("E", true);
		if (sdk > 15) {
			Tag.put("R", true);
			tag_rCheckBox.setChecked(true);
		} else {
			Tag.put("R", false);
		}
		if (SdPath == null || SdPath.length() == 0)
			save_path_edit.setText(getFilesDir().toString() + "/LogCat.log");
		else
			save_path_edit.setText(SdPath + "/LogCat.log");
	}

	/**
	 * 通过包名启动应用程序 startActivityByName
	 */
	public void startActivityByName(String name) {
		try {
			Intent intent = getPackageManager().getLaunchIntentForPackage(name);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			startActivity(intent);
			Toast.makeText(this, getString(R.string.runstr) + name,
					Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			Toast.makeText(this, getString(R.string.autorun),
					Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * 初始化控件 FindViewById
	 */
	@SuppressLint("ShowToast")
	public void FindViewById() {
		toollayout = (TableLayout) findViewById(R.id.toollayout);
		rltool = (RelativeLayout) findViewById(R.id.tool);
		listView = (ListView) findViewById(R.id.list);
		copy = (Button) findViewById(R.id.copy);
		search = (Button) findViewById(R.id.search);
		startButton = (Button) findViewById(R.id.start);
		clearButton = (Button) findViewById(R.id.clear);
		exitButton = (Button) findViewById(R.id.exit);
		package_name_edit = (AutoCompleteTextView) findViewById(R.id.package_name_edit);
		save_path_edit = (EditText) findViewById(R.id.save_path_edit);
		tag_vCheckBox = (CheckBox) findViewById(R.id.tag_v);
		tag_dCheckBox = (CheckBox) findViewById(R.id.tag_d);
		tag_iCheckBox = (CheckBox) findViewById(R.id.tag_i);
		tag_wCheckBox = (CheckBox) findViewById(R.id.tag_w);
		tag_eCheckBox = (CheckBox) findViewById(R.id.tag_e);
		tag_rCheckBox = (CheckBox) findViewById(R.id.tag_r);

		// 自定义toast
		toast = Toast.makeText(this, R.string.explainstr, Toast.LENGTH_LONG);
		LinearLayout toastView = (LinearLayout) toast.getView();
		// toastView.setBackgroundResource(R.drawable.v_bg);
		toastView.setOrientation(LinearLayout.HORIZONTAL);
		ImageView toastImage = new ImageView(this);
		toastImage.setImageResource(R.drawable.icon);
		toastView.addView(toastImage, 0);

		appname = new ArrayList<String>();
		simpleAdapter = new LogCatAdapter<String>(this, R.layout.simple_item,
				LogList);
		adapter = new ArrayAdapter<String>(this, R.layout.simple_dropdown_item,
				appname);
		listView.setAdapter(simpleAdapter);
		listView.setOnItemClickListener(this);
		listView.setOnScrollListener(this);
		package_name_edit.setAdapter(adapter);
		package_name_edit.addTextChangedListener(this);
		tag_vCheckBox.setOnCheckedChangeListener(this);
		tag_dCheckBox.setOnCheckedChangeListener(this);
		tag_iCheckBox.setOnCheckedChangeListener(this);
		tag_wCheckBox.setOnCheckedChangeListener(this);
		tag_eCheckBox.setOnCheckedChangeListener(this);
		tag_rCheckBox.setOnCheckedChangeListener(this);
		startButton.setOnClickListener(this);
		clearButton.setOnClickListener(this);
		exitButton.setOnClickListener(this);
		copy.setOnClickListener(this);
		search.setOnClickListener(this);

	}

	/**
	 * 获取isPause值 getPause
	 * 
	 * @return
	 */
	private boolean getPause() {
		return isPause;
	}

	/**
	 * 设置isPause值 setPause
	 */
	private void setPause(boolean isPause) {
		this.isPause = isPause;
	}

	/**
	 * 发送邮件 sendErrorEmail
	 */
	private void sendErrorEmail() {
		if (SdPath == null || SdPath.length() == 0)
			return;
		final File file = new File(SdPath + "/LogCatError.log");
		if (file.exists()) {
			isError = true;
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(getString(R.string.wnotice));
			builder.setMessage(getString(R.string.wmsg));
			builder.setIcon(R.drawable.icon);
			builder.setPositiveButton(getString(R.string.send),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Intent intent = new Intent(
									"android.intent.action.SEND");
							intent.setType("plain/text");
							intent.putExtra(Intent.EXTRA_EMAIL,
									new String[] { "892768447@qq.com" });
							intent.putExtra(Intent.EXTRA_SUBJECT,
									getString(R.string.report));
							intent.putExtra(Intent.EXTRA_TEXT,
									getString(R.string.report));
							intent.putExtra(
									Intent.EXTRA_STREAM,
									Uri.parse("file://" + SdPath
											+ "/LogCatError.log"));
							Intent.createChooser(intent,
									getString(R.string.report));
							startActivity(intent);
							isError = false;
						}
					});
			builder.setNegativeButton(getString(R.string.cancle),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							isError = false;
							file.delete();
						}
					});
			builder.show();
		}
	}

	/**
	 * 显示菜单 showMenu
	 */
	public void showMenu() {
		if (toollayout.getVisibility() == TableLayout.GONE) {
			toollayout.setVisibility(TableLayout.VISIBLE);
			toast.setText(R.string.explainstr);
			toast.show();
		} else {
			toollayout.setVisibility(TableLayout.GONE);
		}
	}

	/**
	 * 显示tool showTool
	 */
	public void showTool() {
		if (rltool.getVisibility() == RelativeLayout.GONE) {
			rltool.setVisibility(RelativeLayout.VISIBLE);
		} else {
			rltool.setVisibility(RelativeLayout.GONE);
		}
	}
}
