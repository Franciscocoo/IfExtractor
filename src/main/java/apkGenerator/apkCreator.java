package apkGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;

import soot.Body;
import soot.Local;
import soot.MethodOrMethodContext;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.UnitPatchingChain;
import soot.Value;
import soot.VoidType;
import soot.javaToJimple.LocalGenerator;
import soot.jimple.AddExpr;
import soot.jimple.AndExpr;
import soot.jimple.AssignStmt;
import soot.jimple.BinopExpr;
import soot.jimple.BreakpointStmt;
import soot.jimple.CastExpr;
import soot.jimple.CaughtExceptionRef;
import soot.jimple.CmpExpr;
import soot.jimple.CmpgExpr;
import soot.jimple.CmplExpr;
import soot.jimple.ConditionExpr;
import soot.jimple.DivExpr;
import soot.jimple.EnterMonitorStmt;
import soot.jimple.EqExpr;
import soot.jimple.ExitMonitorStmt;
import soot.jimple.FieldRef;
import soot.jimple.GeExpr;
import soot.jimple.GotoStmt;
import soot.jimple.GtExpr;
import soot.jimple.IdentityStmt;
import soot.jimple.IfStmt;
import soot.jimple.IntConstant;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.LeExpr;
import soot.jimple.LookupSwitchStmt;
import soot.jimple.LtExpr;
import soot.jimple.MonitorStmt;
import soot.jimple.MulExpr;
import soot.jimple.NeExpr;
import soot.jimple.NopStmt;
import soot.jimple.OrExpr;
import soot.jimple.ParameterRef;
import soot.jimple.RemExpr;
import soot.jimple.ReturnStmt;
import soot.jimple.ShlExpr;
import soot.jimple.ShrExpr;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.SwitchStmt;
import soot.jimple.TableSwitchStmt;
import soot.jimple.ThisRef;
import soot.jimple.ThrowStmt;
import soot.jimple.UshrExpr;
import soot.jimple.VirtualInvokeExpr;
import soot.jimple.XorExpr;
import soot.jimple.infoflow.InfoflowConfiguration;
import soot.jimple.infoflow.android.InfoflowAndroidConfiguration;
import soot.jimple.infoflow.android.SetupApplication;
import soot.jimple.infoflow.android.InfoflowAndroidConfiguration.SootIntegrationMode;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Targets;
import soot.options.Options;
import soot.util.Chain;
import utils.utils;

public class apkCreator {

	public static void constructApk(SootMethod m, Body oldBody, List<Stmt> stmtBlock,
			SootClass c, String dirOutput, String dirApk, String dirAndroid) {
		
		/* Organisation du if block */
		List<Stmt> ifStmt = utils.orderList(oldBody, stmtBlock);
		/* Coupage du code après le if (Partie Haute) */
		List<Stmt> blockToAnalyse = utils.cutBottomBody(oldBody.getUnits(), ifStmt);
		/* Solving intra-procedural dependencies */
		List<Stmt> newStmtList = dependenciesSolver.getNewStmtBody(m, oldBody, ifStmt, blockToAnalyse, c);
		/* Getting Local from Method */
		Set<Local> newLocalSet = dependenciesSolver.getLocalIfBlock(newStmtList, oldBody.getLocals());
		
		/* Creating new Class */
		SootClass ifClass = new SootClass("ifClass");
		Scene.v().addClass(ifClass);
		SootClass activity = Scene.v().loadClassAndSupport("android.app.Activity");
		ifClass.setSuperclass(activity);
		
		/* Creating init<>() */
		methodCreator.createInitMethod(ifClass);
		
		/* Creating ifMethod*/
		methodCreator.createIfMethod(ifClass, newLocalSet, newStmtList);
		
		/* Creating onCreate() */
		//methodCreator.createOnCreateMethod(ifClass);
		
		//utils.saveJimple(Scene.v().getClasses(), dirOutput);
		Options.v().set_output_format(Options.output_format_dex);
		Options.v().set_output_dir(dirOutput);
		
	}
	
}
