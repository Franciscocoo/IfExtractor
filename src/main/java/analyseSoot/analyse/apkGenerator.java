package analyseSoot.analyse;

import analyseSoot.utils.*;

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

public class apkGenerator {

	private static String home = System.getProperty("user.home");
	private static String dirAndroid = home + "/Android/Sdk/platforms";
	private static String directory = System.getProperty("user.dir");
	private static String apkName = "dependencies2";
	private static String dirApk = directory + "/apk/" + apkName + ".apk";

	public static void constructApk(final SootMethod m, final Body oldBody, final List<Stmt> stmtBlock,
			final SootClass c, final String dirOutput) {
		/* Setup de l'output apk */
		Options.v().set_output_dir(dirOutput);
		Options.v().set_output_format(Options.output_format_dex);

		/* Creation de la classe */
		SootClass cls = new SootClass("Isolate", Modifier.PUBLIC);
		cls.setSuperclass(Scene.v().getSootClass("java.lang.Object"));
		cls.setApplicationClass();
		Scene.v().addClass(cls);

		/*
		 * Création de la méthode
		 */
		SootMethod n = new SootMethod("mainActivity", Arrays.asList(new Type[] {}), VoidType.v(),
				Modifier.PUBLIC | Modifier.STATIC);

		/*
		 * Organisation du if block
		 */
		List<Stmt> ifStmt = utils.orderList(oldBody, stmtBlock);

		/*
		 * Coupage du code après le if (Partie Haute)
		 */
		List<Stmt> blockToAnalyse = cutBottomBody(oldBody.getUnits(), ifStmt);

		/*
		 * Récupération de la liste des Local
		 */
		Chain<Local> locals = oldBody.getLocals();

		/*
		 * Identification des locals dans le block
		 */
		Set<Local> localsIf = getLocalIfBlock(ifStmt, locals);

		/* Récupération des statements nécéssaire */
		List<Stmt> newStmtBody = new ArrayList<Stmt>();
		for (Local l : localsIf) {
			newStmtBody.addAll(getStmtByLocal(oldBody, blockToAnalyse, l)); // CHANGE TO BLOCK
		}
		newStmtBody.addAll(ifStmt);
		newStmtBody = utils.orderList(oldBody, newStmtBody);

		/* Initialisation de la liste des Classes */
		List<SootClass> listClass = new ArrayList<SootClass>();

		/* Getting all ref from original class */
		List<FieldRef> localRefs = getLocalRefs(ifStmt, c);
		System.out.println(localRefs);

		/*
		 * Sources -> Source Methods of Edge (Father) Units -> Source Statemnt of Edge
		 * Targets -> Target Methods of edge (Son) On veut Target de notre méthode
		 * contenant le IF
		 */

		final InfoflowAndroidConfiguration config = new InfoflowAndroidConfiguration();
		config.getAnalysisFileConfig().setTargetAPKFile(dirApk);
		config.getAnalysisFileConfig().setAndroidPlatformDir(dirAndroid);
        config.setEnableReflection(true);
		config.setCodeEliminationMode(InfoflowConfiguration.CodeEliminationMode.NoCodeElimination);
		SetupApplication app = new SetupApplication(config);
		app.constructCallgraph();
		CallGraph cg = Scene.v().getCallGraph();
		Iterator<MethodOrMethodContext> ctargets = new Targets(cg.edgesOutOf(m));
		System.out.println(ctargets);
	}

	private static Set<Local> getLocalIfBlock(List<Stmt> l, Chain<Local> locals) {
		Set<Local> localsBlock = new HashSet<Local>();
		Value v1, v2;
		for (Stmt s : l) {
			/* Assignement */
			if (s instanceof AssignStmt) {
				v1 = ((AssignStmt) s).getLeftOp();
				v2 = ((AssignStmt) s).getRightOp();
				if (locals.contains(v1) && v1 instanceof Local) {
					localsBlock.add((Local) v1);
				}
				if (locals.contains(v2) && v2 instanceof Local) {
					localsBlock.add((Local) v2);
				}
			}
			/* Identification */
			else if (s instanceof IdentityStmt) {
				v1 = ((IdentityStmt) s).getLeftOp();
				if (locals.contains(v1) && v1 instanceof Local) {
					localsBlock.add((Local) v1);
				}
			}
			/* if */
			else if (s instanceof IfStmt) {
				ConditionExpr cond = (ConditionExpr) ((IfStmt) s).getCondition();
				v1 = cond.getOp1();
				v2 = cond.getOp2();
				if (locals.contains(v1) && v1 instanceof Local) {
					localsBlock.add((Local) v1);
				}
				if (locals.contains(v2) && v2 instanceof Local) {
					localsBlock.add((Local) v2);
				}
			}
			/* invoke */
			else if (s instanceof InvokeStmt) {
				InvokeStmt invoke = ((InvokeStmt) s);
				InvokeExpr expr = invoke.getInvokeExpr();
				if (expr instanceof SpecialInvokeExpr) {
					v1 = ((SpecialInvokeExpr) expr).getBase();
					if (locals.contains(v1) && v1 instanceof Local) {
						localsBlock.add((Local) v1);
					}
				} else if (expr instanceof InterfaceInvokeExpr) {
					v1 = ((InterfaceInvokeExpr) expr).getBase();
					if (locals.contains(v1) && v1 instanceof Local) {
						localsBlock.add((Local) v1);
					}
				} else if (expr instanceof VirtualInvokeExpr) {
					v1 = ((VirtualInvokeExpr) expr).getBase();
					if (locals.contains(v1) && v1 instanceof Local) {
						localsBlock.add((Local) v1);
					}
				}
				for (Value v : expr.getArgs()) {
					if (locals.contains(v) && v instanceof Local) {
						localsBlock.add((Local) v);
					}
				}
			}
			/* switch */
			else if (s instanceof SwitchStmt) {
				v1 = ((SwitchStmt) s).getKey();
				if (locals.contains(v1) && v1 instanceof Local) {
					localsBlock.add((Local) v1);
				}
			}
			/* return */
			else if (s instanceof ReturnStmt) {
				v1 = ((ReturnStmt) s).getOp();
				if (locals.contains(v1) && v1 instanceof Local) {
					localsBlock.add((Local) v1);
				}
			}
			/* Monitor */
			else if (s instanceof MonitorStmt) {
				v1 = ((MonitorStmt) s).getOp();
				if (locals.contains(v1) && v1 instanceof Local) {
					localsBlock.add((Local) v1);
				}
			}
			/* throw */
			else if (s instanceof ThrowStmt) {
				v1 = ((ThrowStmt) s).getOp();
				if (locals.contains(v1) && v1 instanceof Local) {
					localsBlock.add((Local) v1);
				}
			}
		}
		return localsBlock;
	}

	private static List<Stmt> getStmtByLocal(Body b, List<Stmt> l, Local loc) {
		List<Stmt> res = new ArrayList<Stmt>();
		Chain<Local> cl = b.getLocals();
		Value v1, v2;
		for (Stmt s : l) {
			/* Assignement */
			if (s instanceof AssignStmt) {
				v1 = ((AssignStmt) s).getLeftOp();
				v2 = ((AssignStmt) s).getRightOp();
				if (v1.equals(loc) && v1 instanceof Local) {
					for (ValueBox v : v2.getUseBoxes()) {
						Value imm = v.getValue();
						if (imm instanceof Local && cl.contains(imm) && !imm.equals(v1)) {
							res.addAll(0, getStmtByLocal(b, l, (Local) imm));
						}
					}
					res.add(s);
				}
			}
			/* Identification */
			else if (s instanceof IdentityStmt) {
				v1 = ((IdentityStmt) s).getLeftOp();
				if (v1.equals(loc) && v1 instanceof Local) {
					res.add(s);
				}
			}
			/* if */
			else if (s instanceof IfStmt) {
				ConditionExpr cond = (ConditionExpr) ((IfStmt) s).getCondition();
				v1 = cond.getOp1();
				v2 = cond.getOp2();
				if ((v1.equals(loc) && v1 instanceof Local) || (v2.equals(loc) && v2 instanceof Local)) {
					res.add(s);
				}
			}
			/* invoke */
			else if (s instanceof InvokeStmt) {
				InvokeStmt invoke = ((InvokeStmt) s);
				InvokeExpr expr = invoke.getInvokeExpr();
				if (expr instanceof SpecialInvokeExpr) {
					v1 = ((SpecialInvokeExpr) expr).getBase();
					if (v1.equals(loc) && v1 instanceof Local) {
						res.add(s);
					}
				} else if (expr instanceof InterfaceInvokeExpr) {
					v1 = ((InterfaceInvokeExpr) expr).getBase();
					if (v1.equals(loc) && v1 instanceof Local) {
						res.add(s);
					}
				} else if (expr instanceof VirtualInvokeExpr) {
					v1 = ((VirtualInvokeExpr) expr).getBase();
					if (v1.equals(loc) && v1 instanceof Local) {
						res.add(s);
					}
				}
				/*
				 * for(Value v : expr.getArgs()) { if(locals.contains(v) && v instanceof Local)
				 * { localsBlock.add((Local)v); } }
				 */
			}
			/* switch */
			else if (s instanceof SwitchStmt) {
				v1 = ((SwitchStmt) s).getKey();
				if (v1.equals(loc) && v1 instanceof Local) {
					res.add(s);
				}
			}
			/* return */
			else if (s instanceof ReturnStmt) {
				v1 = ((ReturnStmt) s).getOp();
				if (v1.equals(loc) && v1 instanceof Local) {
					res.add(s);
				}
			}
			/* Monitor */
			else if (s instanceof MonitorStmt) {
				v1 = ((MonitorStmt) s).getOp();
				if (v1.equals(loc) && v1 instanceof Local) {
					res.add(s);
				}
			}
			/* throw */
			else if (s instanceof ThrowStmt) {
				v1 = ((ThrowStmt) s).getOp();
				if (v1.equals(loc) && v1 instanceof Local) {
					res.add(s);
				}
			}
		}
		return res;
	}

	private static void printNewBody(List<Stmt> l) {
		System.out.println("---PRINTING NEW BODY ---");
		for (Stmt st : l) {
			System.out.println(st);
		}
	}

	private static List<Stmt> cutBottomBody(UnitPatchingChain upc, List<Stmt> l) {
		List<Stmt> res = new ArrayList<Stmt>();
		for (Unit u : upc) {
			Stmt s = (Stmt) u;
			if (l.contains(s)) {
				return res;
			}
			res.add(s);
		}
		return res;
	}

	private static List<FieldRef> getLocalRefs(List<Stmt> l, SootClass c) {
		List<FieldRef> res = new ArrayList<FieldRef>();
		for (Stmt s : l) {
			if (s instanceof AssignStmt) {
				Value v1 = ((AssignStmt) s).getLeftOp();
				Value v2 = ((AssignStmt) s).getRightOp();
				System.out.println("Stmt : " + s);
				System.out.println("v1 : " + v1.getClass());
				System.out.println("v2 : " + v2.getClass());
				if (v1 instanceof FieldRef) {
					FieldRef rv1 = (FieldRef) v1;
					if (rv1.getField().getDeclaringClass().equals(c)) {
						res.add(rv1);
					}
				}
				if (v2 instanceof FieldRef) {
					FieldRef rv2 = (FieldRef) v2;
					if (rv2.getField().getDeclaringClass().equals(c)) {
						res.add(rv2);
					}
				}
			}
		}
		return res;
	}

	private static List<FieldRef> getExternalRefs(List<Stmt> l, SootClass c) {
		List<FieldRef> res = new ArrayList<FieldRef>();
		for (Stmt s : l) {
			if (s instanceof AssignStmt) {
				Value v1 = ((AssignStmt) s).getLeftOp();
				Value v2 = ((AssignStmt) s).getRightOp();
				System.out.println("Stmt : " + s);
				System.out.println("v1 : " + v1.getClass());
				System.out.println("v2 : " + v2.getClass());
				if (v1 instanceof FieldRef) {
					FieldRef rv1 = (FieldRef) v1;
					if (!rv1.getField().getDeclaringClass().equals(c)) {
						res.add(rv1);
					}
				}
				if (v2 instanceof FieldRef) {
					FieldRef rv2 = (FieldRef) v2;
					if (!rv2.getField().getDeclaringClass().equals(c)) {
						res.add(rv2);
					}
				}
			}
		}
		return res;
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
