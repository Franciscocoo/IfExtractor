package apkGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import soot.Body;
import soot.Local;
import soot.MethodOrMethodContext;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.UnitPatchingChain;
import soot.Value;
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
		List<Stmt> newStmtBody = dependenciesSolver.getNewStmtBody(m, oldBody, ifStmt, blockToAnalyse, c);
		/* Getting Local from Method */
		Set<Local> newLocalsBody = dependenciesSolver.getLocalIfBlock(newStmtBody, oldBody.getLocals());
		/* Getting all ref from original class */
		List<FieldRef> internRefs = dependenciesSolver.getRefs(ifStmt, c);
		
		/* Creating new Class */
		SootClass ifClass = new SootClass("ifClass");
		Scene.v().addClass(ifClass);
		/* Creating new Method and the method to the Class*/
		/* TODO : marquer dans le carnet
		 *  */
		SootMethod ifMethod = new SootMethod("ifMethod", m.getParameterTypes(), m.getReturnType(), m.getModifiers(), m.getExceptions());
		ifClass.addMethod(ifMethod);
		
		/* Creating Body */
		JimpleBody b = new JimpleBody(ifMethod);
		/* Adding Locals and Stmt */
		addLocals(newLocalsBody, b);
		addStmt(b, newStmtBody);
		/* PRINT TEST ZONE */
		
		//utils.saveJimple(Scene.v().getClasses(), dirOutput);
		Options.v().set_output_format(Options.output_format_dex);
		Options.v().set_output_dir(dirOutput);
		
	}
	
	private static void addLocals(Set<Local> s, JimpleBody b) {
		for(Local loc : s) {
			Local tmp = Jimple.v().newLocal(loc.getName(), loc.getType());
			b.getLocals().add(tmp);
		}
	}
	
	private static void addStmt(JimpleBody b, List<Stmt> l) {
		UnitPatchingChain units = b.getUnits();
        List<Unit> generatedUnits = new ArrayList<>();
		for(Stmt s : l) {
			System.out.println(s);
			/* Assignement */
			if (s instanceof AssignStmt) {
				generatedUnits.add(stmtCreator.createAssignStmt(s, b));
			}
			/* Identification */
			else if (s instanceof IdentityStmt) {
				generatedUnits.add(stmtCreator.createIdentity(s, b));
			}
			/* Go to */
			else if(s instanceof GotoStmt) {
				generatedUnits.add(stmtCreator.createGoToStmt(s, b));
			}
			/* if */
			else if (s instanceof IfStmt) {
				generatedUnits.add(stmtCreator.createIfStmt(s, b));
			}
			/* invoke */
			else if (s instanceof InvokeStmt) {
				generatedUnits.add(stmtCreator.createInvokeStmt(s, b));
			}
			/* switch */
			else if (s instanceof SwitchStmt) {
				generatedUnits.add(stmtCreator.createSwitchStmt(s, b));
			}
			/* Monitor */
			else if (s instanceof MonitorStmt) {
				generatedUnits.add(stmtCreator.createMonitorStmt(s, b));
			}
			/* return */
			else if (s instanceof ReturnStmt) {
				generatedUnits.add(stmtCreator.createReturnStmt(s, b));
			}
			/* throw */
			else if (s instanceof ThrowStmt) {
				generatedUnits.add(stmtCreator.createThrowStmt(s, b));
			}
			/* Breakpoint */
			else if(s instanceof BreakpointStmt) {
				generatedUnits.add(Jimple.v().newBreakpointStmt());
			}
			/* Nop */
			else if(s instanceof NopStmt) {
				generatedUnits.add(stmtCreator.createNopStmt());
			}
		}
		units.addAll(generatedUnits);
		solveTargets(b,l);
		System.out.println(units);
		b.validate();
	}
	
	private static void solveTargets(JimpleBody b, List<Stmt> block) {
		for(Unit u : b.getUnits()) {
			Stmt s = (Stmt) u;
			if(s instanceof IfStmt) {
				Unit target = ((IfStmt) s).getTarget();
				for(Unit us : b.getUnits()) {
					if(target.toString().equals(us.toString())) {
						((IfStmt) s).setTarget(target);
					}
				}
			} else if(s instanceof TableSwitchStmt) {
				TableSwitchStmt st = (TableSwitchStmt) s;
				for(Unit target : st.getTargets()) {
					for(Unit us : b.getUnits()) {
						if(target.toString().equals(us.toString())) {
							((TableSwitchStmt) s).setDefaultTarget(target);
						}
					}
				}
			} else if(s instanceof LookupSwitchStmt) {
				LookupSwitchStmt st = (LookupSwitchStmt) s;
				for(Unit target : st.getTargets()) {
					for(Unit us : b.getUnits()) {
						if(target.toString().equals(us.toString())) {
							((LookupSwitchStmt) s).setDefaultTarget(target);
						}
					}
				}
			} else if(s instanceof GotoStmt) {
				GotoStmt st = (GotoStmt) s;
				Unit target = st.getTarget();
				for(Unit us : b.getUnits()) {
					if(target.toString().equals(us.toString())) {
						((GotoStmt) s).setTarget(target);
					}
				}
			}
		}
	}
	
	private static Local getLocal(String name, Body b) {
		for(Local l : b.getLocals()) {
			if(l.getName().equals(name)) {
				return l;
			}
		}
		System.out.println("ERROR ALED");
		return null;
	}
	
}
