package apkGenerator;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import soot.Body;
import soot.Local;
import soot.MethodOrMethodContext;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.ConditionExpr;
import soot.jimple.FieldRef;
import soot.jimple.IdentityStmt;
import soot.jimple.IfStmt;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.MonitorStmt;
import soot.jimple.ReturnStmt;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.SwitchStmt;
import soot.jimple.ThrowStmt;
import soot.jimple.VirtualInvokeExpr;
import soot.jimple.infoflow.InfoflowConfiguration;
import soot.jimple.infoflow.android.InfoflowAndroidConfiguration;
import soot.jimple.infoflow.android.SetupApplication;
import soot.jimple.infoflow.android.InfoflowAndroidConfiguration.SootIntegrationMode;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Targets;
import utils.utils;

public class apkCreator {

	public static void constructApk(SootMethod m, Body oldBody, List<Stmt> stmtBlock,
			SootClass c, String dirOutput, String dirApk, String dirAndroid) {
		/* Organisation du if block */
		List<Stmt> ifStmt = utils.orderList(oldBody, stmtBlock);
		/* Coupage du code après le if (Partie Haute) */
		List<Stmt> blockToAnalyse = utils.cutBottomBody(oldBody.getUnits(), ifStmt);
		/* Solving intra-procedural dependencies */
		List<Stmt> newStmtBody = intraDependencies.getNewStmtBody(m, oldBody, ifStmt, blockToAnalyse, c);
		
		/* Getting all ref from original class */
		List<FieldRef> internRefs = interDependencies.getRefs(ifStmt, c);
		
		/* Getting CallGraph of the app */
		CallGraph cg = interDependencies.getCallGraph(dirApk, dirAndroid);
		/* Map */
		Map<SootMethod,SootClass> methods = new HashMap<SootMethod,SootClass>();
		interDependencies.visit(cg, m, methods);
		
		/* Cleaning External Class not containing */
		for(Map.Entry<SootMethod,SootClass> entry : methods.entrySet()) {
			SootClass cla = entry.getValue();
			if(!utils.isSystemClass(cla.toString()) && methods.containsValue(cla)) {
				Scene.v().removeClass(cla);
			}
		}
		
		/* Creating new Class */
		SootClass ifClass = new SootClass("ifClass");
		addRefs(c,internRefs);
		SootMethod ifMethod = new SootMethod("ifMethod",m.getParameterTypes(), m.getReturnType());
		addStmt(ifMethod, newStmtBody);
		
	}
	
	private static void addRefs(SootClass c, List<FieldRef> l) {
		for(FieldRef f : l) {
			c.addField(f.getField());
		}
	}
	
	private static void addStmt(SootMethod m, List<Stmt> l) {
		JimpleBody j = new JimpleBody();
		for(Stmt s : l) {
			/* Assignement */
			if (s instanceof AssignStmt) {
				AssignStmt st = (AssignStmt) s;
				
			}
			/* Identification */
			else if (s instanceof IdentityStmt) {
				
			}
			/* if */
			else if (s instanceof IfStmt) {
				
			}
			/* invoke */
			else if (s instanceof InvokeStmt) {
				
			}
			/* switch */
			else if (s instanceof SwitchStmt) {
				
			}
			/* return */
			else if (s instanceof ReturnStmt) {
				
			}
			/* Monitor */
			else if (s instanceof MonitorStmt) {
				
			}
			/* throw */
			else if (s instanceof ThrowStmt) {
				
			}
		}
		m.setActiveBody(j);
	}
	
	
	
}
