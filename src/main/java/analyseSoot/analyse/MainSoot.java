package analyseSoot.analyse;

import soot.options.Options;

public class MainSoot {
	
	static String home = System.getProperty("user.home");
	static String directory = System.getProperty("user.dir");
	private static String repAndroid = home + "/Android/Sdk/platforms";
	private static String apkName = "app-debug";
	
	static void initSoot() {
		Options.v().set_allow_phantom_refs(true);
		/* TODO : Add Options */
	}
	
	public static void main(String[] args) {
		// apkName = args[1];
		System.out.println(System.getProperty("user.home"));
		System.out.println(System.getProperty("user.dir"));
	}
}
