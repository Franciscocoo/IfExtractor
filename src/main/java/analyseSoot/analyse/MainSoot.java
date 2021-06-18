package analyseSoot.analyse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import analyseSoot.utils.utils;
import soot.Body;
import soot.G;
import soot.Local;
import soot.PackManager;
import soot.PatchingChain;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.Unit;
import soot.jimple.AbstractStmtSwitch;
import soot.jimple.IfStmt;
import soot.jimple.Stmt;
import soot.options.Options;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.DominatorsFinder;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.MHGDominatorsFinder;
import soot.toolkits.graph.SimpleDominatorsFinder;
import soot.toolkits.graph.MHGPostDominatorsFinder;
import soot.toolkits.graph.UnitGraph;
import soot.util.Chain;

public class MainSoot {

	/* */
	private static String home = System.getProperty("user.home");
	private static String repAndroid = home + "/Android/Sdk/platforms";
	/* */
	private static String directory = System.getProperty("user.dir");
	private static String apkName = "instagram";
	private static String repApk = directory + "/apk/" + apkName + ".apk";
	/* */
	private static String repOutput = directory + "/output";

	/**
	 * 
	 */
	static void initSoot() {
		G.v().reset();
		Options.v().set_allow_phantom_refs(true);
		Options.v().set_android_jars(repAndroid);
		List<String> l = new ArrayList<String>();
		l.add(repApk);
		Options.v().set_process_dir(l);
		Options.v().set_src_prec(Options.src_prec_apk);
		Options.v().set_whole_program(true);
		Scene.v().loadNecessaryClasses();
		Scene.v().loadBasicClasses();
	}

	private static boolean isSystemClass(String className) {
		return (className.startsWith("android.") || className.startsWith("java.") || className.startsWith("javax.") || 
				className.startsWith("sun.") || className.startsWith("org.omg.") || className.startsWith("org.w3c.dom.") ||
				className.startsWith("com.google.") || className.startsWith("com.android.")) || className.startsWith("androidx") ||
				className.startsWith("com.example.demo.R"); //Dernière à supprimer
	}
	
	public static void main(String[] args) {
		initSoot();
		PackManager.v().getPack("wjtp").add(new Transform("wjtp.myTransform", new SceneTransformer() {
			@Override
			protected void internalTransform(String phaseName, Map<String, String> options) {
				Chain<SootClass> appClasses = Scene.v().getApplicationClasses();
				for(SootClass c : appClasses) {
					if(!isSystemClass(c.getName())) {
						System.out.println(c.getName());
						for(SootMethod m : c.getMethods()) {
							if(m.isConcrete()) {
								Body b = m.retrieveActiveBody();
								System.out.println(b);
								for(Unit u : b.getUnits()) {
									Stmt s = (Stmt) u;
									if(s instanceof IfStmt) {
										System.out.println(c);
										System.out.println(m.getSignature());
										//isolate(m, (IfStmt)s);
										try {
											utils.stringToJimple(c);
										} catch (IOException e) {
											System.out.println(e);
										}
									}
								}
							}
						}
						System.out.println("**********");
					}
				}
			}
		}));
		PackManager.v().runPacks();
	}
	
	public static void saveJimple(Chain<SootClass> appClasses) {
		for(SootClass c : appClasses) {
			if(!isSystemClass(c.getName())) {
				try {
					System.out.println(c.getName());
					utils.stringToJimple(c);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void isolate(SootMethod m, IfStmt stmt) {
		/* Getting Data and grapj from Method m*/
		Body b = m.retrieveActiveBody();
		UnitGraph ug = new ExceptionalUnitGraph(b);
		SimpleDominatorsFinder sdf = new SimpleDominatorsFinder(ug);
		DirectedGraph dg = sdf.getGraph();
		/* Getting the statement of right and left branch */
		Stmt left = (Stmt) dg.getSuccsOf(stmt).get(0);
		Stmt right = (Stmt) dg.getSuccsOf(stmt).get(1);
		List<Stmt> branchLeft = new ArrayList<Stmt>();
		branchLeft = getBranch(dg, branchLeft, left);
		List<Stmt> branchRight = new ArrayList<Stmt>();
		branchRight = getBranch(dg, branchRight, right);
		System.out.println("Branch left");
		System.out.println(branchLeft);
		System.out.println("Branch Right");
		System.out.println(branchRight);
		List<Stmt> union = new ArrayList<Stmt>(branchLeft);
		union.addAll(branchRight);
		List<Stmt> intersection = new ArrayList<Stmt>(branchLeft);
		intersection.retainAll(branchRight);
		List<Stmt> res = new ArrayList<Stmt>(union);
		res.removeAll(intersection);
		System.out.println("ISOLATED IF :");
		for(Stmt s: res) {
			System.out.println(s);
		}
	}
	
	private static List<Stmt> getBranch(DirectedGraph dg, List<Stmt> l, Stmt s) {
		l.add(s);
		for(int i=0;i<dg.getSuccsOf(s).size();i++) {
			Stmt succ = (Stmt) dg.getSuccsOf(s).get(i);
			if(!l.contains(succ)) {
				getBranch(dg, l, succ);
			}
		}
		return l;
	}
	
}
