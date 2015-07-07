package zce.log.cat.crash;

import android.app.Application;
import android.os.Environment;

public class LogCatApplication extends Application {

	private String SdPath = "";

	// 在应用程序的进程被创建的时候执行
	@Override
	public void onCreate() {
		super.onCreate();
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			SdPath = Environment.getExternalStorageDirectory().toString();
		}else{
			SdPath = getFilesDir().toString();
		}
		Thread.setDefaultUncaughtExceptionHandler(LogCatExceptionHandler
				.getInstance(getApplicationContext(), SdPath));
	}

	public String getSdPath() {
		return SdPath;
	}
}
