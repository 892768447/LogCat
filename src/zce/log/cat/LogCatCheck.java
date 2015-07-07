package zce.log.cat;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Looper;
import android.widget.Toast;

public class LogCatCheck {

	//private final String tag = "LogCatCheck";
	private List<PackageInfo> apps;
	private Iterator<PackageInfo> iter;
	private PackageInfo info;
	private BufferedReader br;
	private StringBuffer sb;
	private LogCatActivity lca;
	private LogCatEncrypt lce;
	private String myPackageName;

	public LogCatCheck(LogCatActivity lca, InputStream in) {
		this.lca = lca;
		this.br = new BufferedReader(new InputStreamReader(in));
		sb = new StringBuffer();
		lce = new LogCatEncrypt();
		this.myPackageName = lca.getPackageName();
		this.apps = lca.getPackageManager().getInstalledPackages(
				PackageManager.GET_SIGNATURES);
	}

	private void exit() {
		lca.finish();
	}

	public void run() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				boolean flag = true;
				try {
					String line;
					while ((line = br.readLine()) != null) {
						sb.append(line.replace("\r\n", "").replace("\n", "")
								.trim());
					}
					br.close();
					// Log.v(tag, "读取data.dat完毕");
					iter = apps.iterator();
					while (iter.hasNext()) {
						info = iter.next();
						if (info.packageName.equals(myPackageName)) {
							String sign = info.signatures[0].toCharsString();
							if (sign.equals(lce.de(sb.toString(), myPackageName))) {
								flag = false;
								// Log.v(tag, "检测通过");
								break;
							}
						}
					}
					if (flag) {
						// Log.v(tag, "检测未通过");
						Looper.prepare();
						Toast.makeText(lca, "程序被非法修改!", Toast.LENGTH_LONG)
								.show();
						exit();
						Looper.loop();
					}
				} catch (Exception e) {
					// Log.v(tag, e.getMessage());
				}
			}
		}).start();
	}
}
