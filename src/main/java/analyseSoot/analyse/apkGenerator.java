package analyseSoot.analyse;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import soot.Body;
import soot.Local;
import soot.PackManager;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.Type;
import soot.Unit;
import soot.VoidType;
import soot.jimple.AssignStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.Stmt;
import soot.options.Options;
import soot.util.Chain;

public class apkGenerator {
	
	public static void constructApk(final SootMethod m, final Body oldBody,final List<Stmt> l, final String dirOutput) {
		PackManager.v().getPack("wjtp").add(new Transform("wjtp.myTransform", new SceneTransformer() {
			@Override
			protected void internalTransform(String phaseName, Map<String, String> options) {
				/* Setup de l'output apk */
				Options.v().set_output_dir(dirOutput);
				Options.v().set_output_format(Options.output_format_dex);
		        
		        /* Creation de la classe */
		        SootClass cls = new SootClass("Isolate", Modifier.PUBLIC);
		        cls.setSuperclass(Scene.v().getSootClass("java.lang.Object"));
		        cls.setApplicationClass();
		        Scene.v().addClass(cls);
		        
		        /* Création de la méthode */
		        SootMethod n = new SootMethod("mainActivity", Arrays.asList(new Type[]{}),
		                VoidType.v(), Modifier.PUBLIC | Modifier.STATIC);
		        
		        Map<Local,Boolean> map = getMap(oldBody.getLocals());
		        System.out.println(map);
		        
		        /* Playing */
		        Body b = n.retrieveActiveBody();
		        System.out.println(b.getLocals());
			}
		}));
		PackManager.v().runPacks();
		//PackManager.v().writeOutput();
	}
	
	private static List<Stmt> getBlock(Body b, List<Stmt> l, Map<Local, Boolean> localChain) {
		
		return new ArrayList<Stmt>();
	}
	
	
	private static Map<Local, Boolean> getMap(Chain<Local> localChain){
		Map<Local, Boolean> res = new HashMap<Local,Boolean>();
		for(Local c : localChain) {
			res.put(c, false);
		}
		return res;
	}
	
}
