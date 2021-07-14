package apkGenerator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import soot.MethodOrMethodContext;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.FieldRef;
import soot.jimple.Stmt;
import soot.jimple.infoflow.InfoflowConfiguration;
import soot.jimple.infoflow.android.InfoflowAndroidConfiguration;
import soot.jimple.infoflow.android.SetupApplication;
import soot.jimple.infoflow.android.InfoflowAndroidConfiguration.SootIntegrationMode;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Targets;

public class interDependencies {
	
	public static CallGraph getCallGraph(String dirApk, String dirAndroid) {
		final InfoflowAndroidConfiguration config = new InfoflowAndroidConfiguration();
		config.getAnalysisFileConfig().setTargetAPKFile(dirApk);
        config.getAnalysisFileConfig().setAndroidPlatformDir(dirAndroid);
        config.setCodeEliminationMode(InfoflowConfiguration.CodeEliminationMode.NoCodeElimination);
		SetupApplication app = new SetupApplication(config);
		app.getConfig().setSootIntegrationMode(SootIntegrationMode.UseExistingInstance);
		app.constructCallgraph();
		CallGraph cg = Scene.v().getCallGraph();
		return cg;
	}
	
	public static void visit(CallGraph cg, SootMethod m, Map<SootMethod,SootClass> map) {
		map.put(m, m.getDeclaringClass());
		Iterator<MethodOrMethodContext> ctargets = new Targets(cg.edgesOutOf(m));
		  if (ctargets != null) {
			  while (ctargets.hasNext()) {
			        SootMethod child = (SootMethod) ctargets.next();
			        if (!map.containsKey(child)) visit(cg, child, map);
			    }
		  }
	}
	
	public static List<FieldRef> getRefs(List<Stmt> l, SootClass c) {
		List<FieldRef> res = new ArrayList<FieldRef>();
		for (Stmt s : l) {
			if (s instanceof AssignStmt) {
				Value v1 = ((AssignStmt) s).getLeftOp();
				Value v2 = ((AssignStmt) s).getRightOp();
				if (v1 instanceof FieldRef) {
					FieldRef rv1 = (FieldRef) v1;
					res.add(rv1);
				}
				if (v2 instanceof FieldRef) {
					FieldRef rv2 = (FieldRef) v2;
					res.add(rv2);
				}
			}
		}
		return res;
	}
}
