package analyseSoot.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import soot.SootClass.*;
import soot.options.Options;
import soot.util.Chain;
import soot.SootMethod;
import soot.Body;
import soot.G;
import soot.Scene;
import soot.SootClass;

public class utils {
	
	public static boolean isSystemClass(String className) {
		return (className.startsWith("android.") || className.startsWith("java.") || className.startsWith("javax.") || 
				className.startsWith("sun.") || className.startsWith("org.omg.") || className.startsWith("org.w3c.dom.") ||
				className.startsWith("com.google.") || className.startsWith("com.android.")) || className.startsWith("androidx") ||
				className.startsWith("com.example.demo.R"); //Dernière à supprimer
	}
	
	public static void initSoot(String dirAndroid, String dirApk) {
		G.v().reset();
		Options.v().set_allow_phantom_refs(true);
		Options.v().set_android_jars(dirAndroid);
		List<String> l = new ArrayList<String>();
		l.add(dirApk);
		Options.v().set_process_dir(l);
		Options.v().set_src_prec(Options.src_prec_apk);
		Options.v().set_whole_program(true);
		Scene.v().loadNecessaryClasses();
		Scene.v().loadBasicClasses();
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
}
