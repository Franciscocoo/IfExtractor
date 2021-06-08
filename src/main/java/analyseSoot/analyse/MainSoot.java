package analyseSoot.analyse;

import soot.options.Options;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.util.Chain;
import soot.Body;
import soot.PackManager;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.Unit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainSoot {

	/* */
	private static String home = System.getProperty("user.home");
	private static String repAndroid = home + "/Android/Sdk/platforms";
	/* */
	private static String directory = System.getProperty("user.dir");
	private static String apkName = "app-debug";
	private static String repApk = directory + "/apk/" + apkName + ".apk";
	/* */
	private static String repOutput = directory + "/output";

	/**
	 * 
	 */
	static void initSoot() {
		Options.v().set_allow_phantom_refs(true);
		Options.v().set_android_jars(repAndroid);
		List<String> l = new ArrayList<String>();
		l.add(repApk);
		Options.v().set_process_dir(l);
		Options.v().set_src_prec(Options.src_prec_apk);
		Options.v().set_whole_program(true);
		Scene.v().loadNecessaryClasses();
		/* TODO : Add Options */
	}

	private static boolean isSystemClass(String className) {
		return (className.startsWith("android.") || className.startsWith("java.") || className.startsWith("javax.") || 
				className.startsWith("sun.") || className.startsWith("org.omg.") || className.startsWith("org.w3c.dom.") ||
				className.startsWith("com.google.") || className.startsWith("com.android.")) || className.startsWith("androidx") ||
				className.startsWith("com.example.demo.R");
	}
	public static void main(String[] args) {
		initSoot();
		PackManager.v().getPack("wjtp").add(new Transform("wjtp.myTransform", new SceneTransformer() {
			@Override
			protected void internalTransform(String phaseName, Map<String, String> options) {
				Chain<SootClass> appClasses = Scene.v().getApplicationClasses();
				for(SootClass c : appClasses) {
					if(!isSystemClass(c.getName())) {
						List<SootMethod> classMethods = c.getMethods();
						for(SootMethod m : c.getMethods()) {
							//System.out.println(m.retrieveActiveBody());
							Body b = m.retrieveActiveBody();
							UnitGraph ug = new ExceptionalUnitGraph(b);
							/* Test d'un parcours */
							System.out.println("------");
							System.out.println("Parcours du graphe de " + m.getName());
							List<Unit> iterator = ug.getHeads();
							System.out.println(iterator);
							while(!iterator.equals(ug.getTails())) {
								iterator = ug.getSuccsOf(iterator.get(0));
								System.out.println(iterator);
								System.out.println(iterator.toString());
							}
							System.out.println("------");
							/* Probleme sur mon parcours lorsque le code finit sur un if */
						}
					}
				}
			}
		}));
		PackManager.v().runPacks();
	}
}
