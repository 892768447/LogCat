package zce.log.cat;

public class LogCatEncrypt {

	/*public String en(String s, String key) {
		String str = "";
		int ch;
		for (int i = 0, j = 0; i < s.length(); i++, j++) {
			if (j > key.length() - 1) {
				j = j % key.length();
			}
			ch = s.codePointAt(i) + key.codePointAt(j);
			if (ch > 65535) {
				ch = ch % 65535;
			}
			str += (char) ch;
		}
		return str;
	}*/

	public String de(String s, String key) {
		String str = "";
		int ch;
		for (int i = 0, j = 0; i < s.length(); i++, j++) {
			if (j > key.length() - 1) {
				j = j % key.length();
			}
			ch = (s.codePointAt(i) + 65535 - key.codePointAt(j));
			if (ch > 65535) {
				ch = ch % 65535;
			}
			str += (char) ch;
		}
		return str;
	}
	
}
