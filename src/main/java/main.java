import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import apkGenerator.apkCreator;
import utils.*;
import ifIsolator.IfPackage;
import ifIsolator.ifManager;
import soot.Body;
import soot.PackManager;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.Unit;
import soot.jimple.IfStmt;
import soot.jimple.JimpleBody;
import soot.jimple.Stmt;
import soot.util.Chain;
import utils.utils;

public class main {

	/* Directory of Android Platforms*/
	private static String home = System.getProperty("user.home");
	private static String dirAndroid = home + "/Android/Sdk/platforms";
	/* Directory of the apk put in output */
	private static String directory = System.getProperty("user.dir");
	private static String apkName = "dependencies1";
	private static String dirApk = directory + "/apk/" + apkName + ".apk";
	/* Directory of the output */
	private static String dirOutput = directory + "/output";
	
	public static void main(String[] args) {
		
		/* Cleaning Output Folder */
		final File[] files = (new File(dirOutput)).listFiles();
		if(files != null && files.length>0) {
			Arrays.asList(files).forEach(File::delete);
		}
		
		utils.initSoot(dirAndroid, dirApk);
		
		//TestPrint.test();
		Chain<SootClass>appClasses = Scene.v().getApplicationClasses();
		utils.saveJimple(appClasses, dirOutput);
		
		IfPackage p = ifManager.getIf();
		
		List<Stmt> l = p.getBlock();
		SootMethod m = p.getMethod();
		Body b = p.getBody();
		SootClass c = p.getClasse();
		
		System.out.println("***********");
		System.out.println("RESULTS : ");
		System.out.println("Class : " + c);
		System.out.println("Method : " + m);
		System.out.println("Block IF : " + l);
		System.out.println("***********");
		
		apkCreator.constructApk(m, b, l, c, dirOutput, dirApk, dirAndroid);
				
	}
}
