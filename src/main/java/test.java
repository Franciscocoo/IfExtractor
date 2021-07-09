

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import analyseSoot.analyse.apkGenerator;
import analyseSoot.ifIsolator.IfPackage;
import analyseSoot.ifIsolator.ifManager;
import analyseSoot.utils.*;
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

public class test {

	/* */
	private static String home = System.getProperty("user.home");
	private static String dirAndroid = home + "/Android/Sdk/platforms";
	/* */
	private static String directory = System.getProperty("user.dir");
	private static String apkName = "dependencies2";
	private static String dirApk = directory + "/apk/" + apkName + ".apk";
	/* */
	private static String dirOutput = directory + "/output";
	
	public static void main(String[] args) {
		
		/* Cleaning Output Folder */
		final File[] files = (new File(dirOutput)).listFiles();
		if(files != null && files.length>0) {
			Arrays.asList(files).forEach(File::delete);
		}
		
		utils.initSoot(dirAndroid, dirApk);
		
		PackManager.v().getPack("wjtp").add(new Transform("wjtp.myTransform", new SceneTransformer() {
			@Override
			protected void internalTransform(String phaseName, Map<String, String> options) {
				/* Get IF */
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
				
				apkGenerator.constructApk(m, b, l, c, dirOutput);
			}
		}));
		PackManager.v().runPacks();
		
	}
}
