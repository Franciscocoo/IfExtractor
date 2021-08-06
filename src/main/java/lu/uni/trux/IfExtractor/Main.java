package lu.uni.trux.IfExtractor;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.xml.crypto.dsig.Manifest;

import org.xmlpull.v1.XmlPullParserException;

import lu.uni.trux.IfExtractor.ifIsolator.IfPackage;
import lu.uni.trux.IfExtractor.ifIsolator.IfManager;
import lu.uni.trux.IfExtractor.utils.*;
import soot.Body;
import soot.PackManager;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.Unit;
import soot.jimple.IfStmt;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.Stmt;
import soot.jimple.infoflow.android.InfoflowAndroidConfiguration;
import soot.util.Chain;

import soot.jimple.infoflow.android.manifest.*;

public class Main {

	
	public static void main(String[] args) throws IOException, XmlPullParserException {

		String dirAndroid = "/home/student/Android-platforms/jars/stubs";
		
		String directory = System.getProperty("user.dir");
		String apkName = "dependencies7";
		String dirApk = directory + "/apk/" + apkName + ".apk";
		
		String dirOutput = directory + "/output";
		
		/* Cleaning Output Folder */
		final File[] files = (new File(dirOutput)).listFiles();
		if(files != null && files.length>0) {
			Arrays.asList(files).forEach(File::delete);
		}
		
		IfExtractor test = new IfExtractor(dirAndroid, dirApk, dirOutput);
		Chain<SootClass> appClasses = Scene.v().getApplicationClasses();
		//Utils.saveJimple(appClasses, dirOutput);
		for (SootClass c : appClasses) {
			if (!(Utils.isSystemClass(c.getName()))) {
				for (SootMethod m : c.getMethods()) {
					if (m.isConcrete()) {
						Body b = m.retrieveActiveBody();
						for (Unit u : b.getUnits()) {
							Stmt s = (Stmt) u;
							if (s instanceof IfStmt) {
								test.addLogicBomb((IfStmt) s);
							}
						}
					}
				}
			}
		}		
				
		test.generateApk();
		appClasses = Scene.v().getApplicationClasses();
		Utils.saveJimple(appClasses, dirOutput);
	}
}
