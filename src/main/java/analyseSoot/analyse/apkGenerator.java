package analyseSoot.analyse;

import utils.*;
import apkGenerator.interDependencies;
import apkGenerator.intraDependencies;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.Body;
import soot.Local;
import soot.MethodOrMethodContext;
import soot.PackManager;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.Type;
import soot.Unit;
import soot.UnitPatchingChain;
import soot.Value;
import soot.ValueBox;
import soot.VoidType;
import soot.JastAddJ.CastExpr;
import soot.jimple.AssignStmt;
import soot.jimple.ConditionExpr;
import soot.jimple.Expr;
import soot.jimple.FieldRef;
import soot.jimple.IdentityStmt;
import soot.jimple.IfStmt;
import soot.jimple.InstanceOfExpr;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.LengthExpr;
import soot.jimple.MonitorStmt;
import soot.jimple.NegExpr;
import soot.jimple.Ref;
import soot.jimple.ReturnStmt;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.SwitchStmt;
import soot.jimple.ThrowStmt;
import soot.jimple.VirtualInvokeExpr;
import soot.jimple.infoflow.InfoflowConfiguration;
import soot.jimple.infoflow.android.InfoflowAndroidConfiguration;
import soot.jimple.infoflow.android.InfoflowAndroidConfiguration.SootIntegrationMode;
import soot.jimple.infoflow.android.SetupApplication;
import soot.jimple.internal.AbstractBinopExpr;
import soot.jimple.internal.JAddExpr;
import soot.jimple.internal.JCastExpr;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.jimple.toolkits.callgraph.Targets;
import soot.options.Options;
import soot.util.Chain;
import soot.util.HashChain;
import utils.utils;

public class apkGenerator {

	private static String home = System.getProperty("user.home");
	private static String dirAndroid = home + "/Android/Sdk/platforms";
	private static String directory = System.getProperty("user.dir");
	private static String apkName = "dependencies2";
	private static String dirApk = directory + "/apk/" + apkName + ".apk";

	public static void constructApk(SootMethod m, Body oldBody, List<Stmt> stmtBlock,
			SootClass c, String dirOutput) {
		
		/* Organisation du if block */
		List<Stmt> ifStmt = utils.orderList(oldBody, stmtBlock);
		/* Coupage du code après le if (Partie Haute) */
		List<Stmt> blockToAnalyse = utils.cutBottomBody(oldBody.getUnits(), ifStmt);
		/* Solving intra-procedural dependencies */
		List<Stmt> newStmtBody = getNewStmtBody(m, oldBody, ifStmt, blockToAnalyse, c);
		
		/* Getting all ref from original class */
		List<FieldRef> localRefs = interDependencies.getRefs(ifStmt, c);
		System.out.println(localRefs);

		/*
		 * Sources -> Source Methods of Edge (Father) Units -> Source Statemnt of Edge
		 * Targets -> Target Methods of edge (Son) On veut Target de notre méthode
		 * contenant le IF
		 */

		final InfoflowAndroidConfiguration config = new InfoflowAndroidConfiguration();
		config.getAnalysisFileConfig().setTargetAPKFile(dirApk);
        config.getAnalysisFileConfig().setAndroidPlatformDir(dirAndroid);
        config.setCodeEliminationMode(InfoflowConfiguration.CodeEliminationMode.NoCodeElimination);
		SetupApplication app = new SetupApplication(config);
		app.constructCallgraph();
		CallGraph cg = Scene.v().getCallGraph();
		Iterator<MethodOrMethodContext> ctargets = new Targets(cg.edgesOutOf(m));
		System.out.println(ctargets);
	}

	
	private static List<Stmt> getNewStmtBody(SootMethod m, Body oldBody, List<Stmt> ifStmt,
			List<Stmt> blockToAnalyse, SootClass c) {
		/* Récupération de la liste des Local */
		Chain<Local> locals = oldBody.getLocals();
		/* Identification des locals dans le block */
		Set<Local> localsIf = intraDependencies.getLocalIfBlock(ifStmt, locals);
		/* Récupération des statements nécéssaire */
		List<Stmt> newStmtBody = new ArrayList<Stmt>();
		for (Local l : localsIf) {
			newStmtBody.addAll(intraDependencies.getStmtByLocal(oldBody, blockToAnalyse, l)); // CHANGE TO BLOCK
		}
		/* Adding the IF Block and order it */
		newStmtBody.addAll(ifStmt);
		newStmtBody = utils.orderList(oldBody, newStmtBody);
		return newStmtBody;
	}
	
	private static Map<SootClass, SootMethod> getExternalMethods(List<Stmt> l, SootMethod m) {
		Map<SootClass, SootMethod> res = new HashMap<SootClass, SootMethod>();
		for (Stmt s : l) {
			if (s instanceof InvokeStmt) {
				InvokeStmt invstmt = ((InvokeStmt) s);
				SootMethod newMethod = invstmt.getInvokeExpr().getMethod();
				if (newMethod.equals(m)) {
					res.put(newMethod.getDeclaringClass(), newMethod);
				}
			}
		}
		return res;

	}
}
