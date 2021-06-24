package analyseSoot.analyse;

import java.util.List;

import analyseSoot.utils.*;
import soot.PackManager;
import soot.SootMethod;
import soot.jimple.Stmt;

public class test {

	/* */
	private static String home = System.getProperty("user.home");
	private static String dirAndroid = home + "/Android/Sdk/platforms";
	/* */
	private static String directory = System.getProperty("user.dir");
	private static String apkName = "test4";
	private static String dirApk = directory + "/apk/" + apkName + ".apk";
	/* */
	private static String dirOutput = directory + "/output";
	
	public static void main(String[] args) {
		utils.initSoot(dirAndroid, dirApk);
		/* GET IF BLOCK */
		ifManager.getIf();
		//apkGenerator
		PackManager.v().runPacks();
		/* Prints */
		List<Stmt> l = ifManager.codeToIsolate;
		SootMethod m = ifManager.methodToIsolate;
		System.out.println("****************");
		System.out.println(m);
		System.out.println(l);
		System.out.println("****************");
		//apkGenerator.constructApk(m, l, dirOutput);
		//PackManager.v().runPacks();
	}
}
