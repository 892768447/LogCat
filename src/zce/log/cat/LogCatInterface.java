package zce.log.cat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.os.Build;

public interface LogCatInterface {

	public static Map<String, Boolean> Tag = new HashMap<String, Boolean>();
	public final static int sdk = Build.VERSION.SDK_INT;
	public ArrayList<String> LogList = new ArrayList<String>();
}
