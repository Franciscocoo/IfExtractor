package analyseSoot.analyse;

import java.lang.reflect.Modifier;
import java.util.List;

import soot.Body;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.Jimple;
import soot.jimple.Stmt;
import soot.options.Options;
import soot.util.Chain;

public class apkGenerator {
	
	public static void constructApk(SootMethod m, List<Stmt> l, String dirOutput) {
		/* Setup de l'output apk */
		Options.v().set_output_dir(dirOutput);
		Options.v().set_output_format(Options.output_format_dex);
        
        /* Creation de la classe */
        SootClass cls = new SootClass("Isolate", Modifier.PUBLIC);
        cls.setSuperclass(Scene.v().getSootClass("java.lang.Object"));
        cls.setApplicationClass();
        Scene.v().addClass(cls);
        
        /* Création de la méthode */
        SootMethod n = new SootMethod(m.getName(), m.getParameterTypes(), m.getReturnType());
        cls.addMethod(n);
        
        /* Boucle qui va générer le contenu de la méthode */
        Body oldBody = m.getActiveBody();
        Body newBody = Jimple.v().newBody(n);
        Chain c = newBody.getUnits();
        for(Unit u : oldBody.getUnits()) {
        	Stmt s = (Stmt) u;
        	if(l.contains(s)) {
        		c.add(s);
        	}
        }
        System.out.println(oldBody.getLocalCount());
        System.out.println(newBody.getLocalCount());
	}
}
