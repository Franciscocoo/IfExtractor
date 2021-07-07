package analyseSoot.analyse;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import analyseSoot.utils.*;
import soot.Body;
import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.JimpleBody;
import soot.jimple.Stmt;

public class test {

	/* */
	private static String home = System.getProperty("user.home");
	private static String dirAndroid = home + "/Android/Sdk/platforms";
	/* */
	private static String directory = System.getProperty("user.dir");
	private static String apkName = "dependencies1";
	private static String dirApk = directory + "/apk/" + apkName + ".apk";
	/* */
	private static String dirOutput = directory + "/output";
	
	public static void main(String[] args) {
		
		/* Cleaning Output Folder */
		final File[] files = (new File(dirOutput)).listFiles();
		if(files != null && files.length>0) {
			Arrays.asList(files).forEach(File::delete);
		}
		
		/* GET IF BLOCK */
		utils.initSoot(dirAndroid, dirApk);
		ifManager.getIf();
		
		/* Prints */
		List<Stmt> l = ifManager.codeToIsolate;
		SootMethod m = ifManager.methodToIsolate;
		Body b = ifManager.bodyToIsolate;
		SootClass c = ifManager.classToIsolate;
		System.out.println("****************");
		System.out.println(m);
		System.out.println(l);
		System.out.println("****************");
		
		/* APK GENERATOR */
		utils.initSoot(dirAndroid, dirApk);
		apkGenerator.constructApk(m, b, l, c, dirOutput);
	}
}
