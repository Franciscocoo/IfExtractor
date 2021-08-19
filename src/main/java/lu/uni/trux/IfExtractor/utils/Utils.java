package lu.uni.trux.IfExtractor.utils;

import java.util.ArrayList;
import java.util.List;

import lu.uni.trux.IfExtractor.MyConfig;
import soot.Unit;
import soot.UnitPatchingChain;
import soot.jimple.Stmt;
import soot.jimple.infoflow.InfoflowConfiguration;
import soot.jimple.infoflow.InfoflowConfiguration.CallgraphAlgorithm;
import soot.jimple.infoflow.android.InfoflowAndroidConfiguration;
import soot.jimple.infoflow.android.SetupApplication;
import soot.Body;

/**
 * Utils Methods
 * @author François JULLION
 */
public class Utils {
	
	/**
	 * Return true if the class is a SystemClass
	 * @param className, String
	 * @return boolean
	 */
	public static boolean isSystemClass(String className) {
		return (className.startsWith("android.") || className.startsWith("java.") || className.startsWith("javax.") || 
				className.startsWith("sun.") || className.startsWith("org.omg.") || className.startsWith("org.w3c.dom.") ||
				className.startsWith("com.google.") || className.startsWith("com.android.")) || className.startsWith("androidx");
	}
	
	/**
	 * Initialize flowDroid and the instance of Soot
	 * @param dirAndroid, String which represents the Android sources folder path
	 * @param dirApk, String which represents the apk file path
	 * @param dirOutput, String which represents the output folder path
	 */
	public static void initSoot(String dirAndroid, String dirApk, String dirOutput) {
		final InfoflowAndroidConfiguration config = new InfoflowAndroidConfiguration();
		config.getAnalysisFileConfig().setAndroidPlatformDir(dirAndroid);
		config.getAnalysisFileConfig().setTargetAPKFile(dirApk);
        config.setCodeEliminationMode(InfoflowConfiguration.CodeEliminationMode.NoCodeElimination);
        config.setCallgraphAlgorithm(CallgraphAlgorithm.CHA);
        SetupApplication app = new SetupApplication(config);
        app.setSootConfig(new MyConfig());
        app.constructCallgraph();
	}
		
	/**
	 * Ordering a list of Stmt based on a Body
	 * @param b, Body
	 * @param l, List of Stmt
	 * @return new List of Stmt in order
	 */
	public static List<Stmt> orderList(Body b, List<Stmt> l) {
		List<Stmt> res = new ArrayList<Stmt>();
		for(Unit u : b.getUnits()) {
			Stmt s = (Stmt) u;
			if(l.contains(s)) {
				res.add(s);
			}
		}
		return res;
	}
	
	/**
	 * Cut a part of a Body using the upc and the ifBlock list
	 * @param upc, UnitPatchingChain
	 * @param l, list of Stmt
	 * @return new list of Stmt
	 */
	public static List<Stmt> cutBottomBody(UnitPatchingChain upc, List<Stmt> l) {
		List<Stmt> res = new ArrayList<Stmt>();
		for (Unit u : upc) {
			Stmt s = (Stmt) u;
			if (l.contains(s)) {
				return res;
			}
			res.add(s);
		}
		return res;
	}
}
