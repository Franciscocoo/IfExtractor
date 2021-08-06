package lu.uni.trux.IfExtractor.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import soot.SootClass.*;
import soot.options.Options;
import soot.util.Chain;
import soot.SootMethod;
import soot.Unit;
import soot.UnitPatchingChain;
import soot.jimple.Stmt;
import soot.jimple.infoflow.InfoflowConfiguration;
import soot.jimple.infoflow.InfoflowConfiguration.CallgraphAlgorithm;
import soot.jimple.infoflow.android.InfoflowAndroidConfiguration;
import soot.jimple.infoflow.android.SetupApplication;
import soot.Body;
import soot.G;
import soot.Scene;
import soot.SootClass;

public class Utils {
	
	public static boolean isSystemClass(String className) {
		return (className.startsWith("android.") || className.startsWith("java.") || className.startsWith("javax.") || 
				className.startsWith("sun.") || className.startsWith("org.omg.") || className.startsWith("org.w3c.dom.") ||
				className.startsWith("com.google.") || className.startsWith("com.android.")) || className.startsWith("androidx");
	}
	
	public static void initSoot(String dirAndroid, String dirApk, String dirOutput) {
		final InfoflowAndroidConfiguration config = new InfoflowAndroidConfiguration();
		config.getAnalysisFileConfig().setAndroidPlatformDir(dirAndroid);
		config.getAnalysisFileConfig().setTargetAPKFile(dirApk);
        config.setCodeEliminationMode(InfoflowConfiguration.CodeEliminationMode.NoCodeElimination);
        config.setCallgraphAlgorithm(CallgraphAlgorithm.CHA);
        SetupApplication app = new SetupApplication(config);
        app.constructCallgraph();
		Options.v().set_output_format(Options.output_format_dex);
		Options.v().set_output_dir(dirOutput);
	}
	
	public static void saveJimple(Chain<SootClass> appClasses, String dirOutput) {
		for(SootClass c : appClasses) {
			if(!isSystemClass(c.getName())) {
				try {
					System.out.println(c.getName());
					stringToJimple(c,dirOutput);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private static void stringToJimple(SootClass cls, String dirOutput) throws IOException{
		String filename = dirOutput + "/" + cls.getName() + ".jmpl";
		FileWriter filewrite = new FileWriter(filename);
		PrintWriter printwrite = new PrintWriter(filewrite);
		cls.setApplicationClass();
		for(SootMethod meth: cls.getMethods()) {
			if(meth.isConcrete()) {
				Body b = meth.retrieveActiveBody();
				printwrite.write(b.toString());
			}
		}
		printwrite.close();
	}
	
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
	
	private static void printNewBody(List<Stmt> l) {
		System.out.println("---PRINTING NEW BODY ---");
		for (Stmt st : l) {
			System.out.println(st);
		}
	}
	
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
