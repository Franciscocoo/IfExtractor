package analyseSoot.analyse;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import soot.Value;
import soot.VoidType;
import soot.jimple.AssignStmt;
import soot.jimple.ConditionExpr;
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
import soot.jimple.StaticInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.SwitchStmt;
import soot.jimple.ThrowStmt;
import soot.jimple.VirtualInvokeExpr;
import soot.options.Options;
import soot.util.Chain;
import soot.util.HashChain;

public class apkGenerator {
	
	public static void constructApk(final SootMethod m, final Body oldBody,final List<Stmt> stmtBlock, final String dirOutput) {
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
		        
		        /* Récupération de la liste des Local */
		        Chain<Local> locals = oldBody.getLocals();
		        
		        /* 
		         * Identification des locals dans le block 
		         */
		        Set<Local> localsBlock = new HashSet<Local>();
		        Value v1, v2;
		        for(Stmt s : stmtBlock) {
		        	/* Assignement */
					if(s instanceof AssignStmt) {
						v1 = ((AssignStmt) s).getLeftOp();
						v2 = ((AssignStmt) s).getRightOp();
						if(locals.contains(v1) && v1 instanceof Local) {
							localsBlock.add((Local)v1);
						}
						if(locals.contains(v2) && v2 instanceof Local) {
							localsBlock.add((Local)v2);
						}
					} 
					/* Identification */
					else if(s instanceof IdentityStmt) {
						v1 = ((AssignStmt) s).getLeftOp();
						if(locals.contains(v1) && v1 instanceof Local) {
							localsBlock.add((Local)v1);
						}
					} 
					/* if */
					else if(s instanceof IfStmt) {
						ConditionExpr cond = (ConditionExpr) ((IfStmt) s).getCondition();
						v1 = cond.getOp1();
						v2 = cond.getOp2();
						if(locals.contains(v1) && v1 instanceof Local) {
							localsBlock.add((Local)v1);
						}
						if(locals.contains(v2) && v2 instanceof Local) {
							localsBlock.add((Local)v2);
						}
					} 
					/* invoke */
					else if(s instanceof InvokeStmt) {
						InvokeStmt invoke = ((InvokeStmt) s);
						InvokeExpr expr = invoke.getInvokeExpr();
						if(expr instanceof SpecialInvokeExpr) {
							v1 = ((SpecialInvokeExpr) expr).getBase();
							if(locals.contains(v1) && v1 instanceof Local) {
								localsBlock.add((Local)v1);
							}
						} else if(expr instanceof InterfaceInvokeExpr) {
							v1 = ((InterfaceInvokeExpr) expr).getBase();
							if(locals.contains(v1) && v1 instanceof Local) {
								localsBlock.add((Local)v1);
							}
						} else if(expr instanceof VirtualInvokeExpr) {
							v1 = ((VirtualInvokeExpr) expr).getBase();
							if(locals.contains(v1) && v1 instanceof Local) {
								localsBlock.add((Local)v1);
							}
						}
						for(Value v : expr.getArgs()) {
							if(locals.contains(v) && v instanceof Local) {
								localsBlock.add((Local)v);
							}
						}
					} 
					/* switch */
					else if(s instanceof SwitchStmt) {
						v1 = ((SwitchStmt) s).getKey();
						if(locals.contains(v1) && v1 instanceof Local) {
							localsBlock.add((Local)v1);
						}
					} 
					/* return */
					else if(s instanceof ReturnStmt) {
						v1 = ((ReturnStmt) s).getOp();
						if(locals.contains(v1) && v1 instanceof Local) {
							localsBlock.add((Local)v1);
						}
					} 
					/* Monitor */
					else if(s instanceof MonitorStmt) {
						v1 = ((MonitorStmt) s).getOp();
						if(locals.contains(v1) && v1 instanceof Local) {
							localsBlock.add((Local)v1);
						}
					} 
					/* throw */
					else if(s instanceof ThrowStmt) {
						v1 = ((ThrowStmt) s).getOp();
						if(locals.contains(v1) && v1 instanceof Local) {
							localsBlock.add((Local)v1);
						}
					}
		        }
		        
		        /* Récupération des statements nécéssaire */
		        
		        
		        System.out.println(localsBlock);
		        System.out.println(oldBody.getLocals());
			}
		}));
		PackManager.v().runPacks();
		//PackManager.v().writeOutput();
	}
	
	private static List<Stmt> getStmtByLocal(Body b, String local, Set<String> localBlock, List<Stmt> stmtBlock, List<String> localName) {
		List<Stmt> res = new ArrayList<Stmt>();
		Chain<Local> cl = b.getLocals();
		for(Unit u : b.getUnits()) {
			Stmt s = (Stmt) u;
			if(!stmtBlock.contains(s)) {
				String v1;
				String value2;
        		/* Assignement */
				if(s instanceof AssignStmt) {
					v1 = ((AssignStmt) s).getLeftOp().toString();
					value2 = ((AssignStmt) s).getRightOp().toString();
					if(local.equals(v1)) {
						res.add(s);
						if(localName.contains(value2) && !localBlock.contains(value2)) {
							List<Stmt> tmp = new ArrayList<Stmt>(res);
							res = getStmtByLocal(b, local, localBlock, stmtBlock, localName);
							res.addAll(tmp);
						}
					}
					if(local.equals(value2)) {
						res.add(s);
					}
				} 
				/* Identification */
				else if(s instanceof IdentityStmt) {
					v1 = ((IdentityStmt) s).getLeftOp().toString();
					if(local.equals(v1)) {
						res.add(s);
					}
				} 
				/* invoke */
				else if(s instanceof InvokeStmt) {
					InvokeStmt invoke = ((InvokeStmt) s);
					InvokeExpr expr = invoke.getInvokeExpr();
					if(expr instanceof SpecialInvokeExpr) {
						String base = ((SpecialInvokeExpr) expr).getBase().toString();
						if(local.equals(base)) {
							res.add(s);
						}
					} else if(expr instanceof InterfaceInvokeExpr) {
						String base = ((InterfaceInvokeExpr) expr).getBase().toString();
						if(local.equals(base)) {
							res.add(s);
						}
					} else if(expr instanceof VirtualInvokeExpr) {
						String base = ((VirtualInvokeExpr) expr).getBase().toString();
						if(local.equals(base)) {
							res.add(s);
						}
					}
					for(Value v : expr.getArgs()) {
						if(local.equals(v.toString())) {
							res.add(s);
						}
					}
				}
				/* return */
				else if(s instanceof ReturnStmt) {
					v1 = ((ReturnStmt) s).getOp().toString();
					if(local.equals(v1)) {
						res.add(s);
					}
				} 
				/* Monitor */
				else if(s instanceof MonitorStmt) {
					v1 = ((MonitorStmt) s).getOp().toString();
					if(local.equals(v1)) {
						res.add(s);
					}
				} 
				/* throw */
				else if(s instanceof ThrowStmt) {
					v1 = ((ThrowStmt) s).getOp().toString();
					if(local.equals(v1)) {
						res.add(s);
					}
				}
        	}
			
		}
		return res;
	}
	
	private static void printNewBody(List<Stmt> l) {
		System.out.println("---PRINTING NEW BODY ---");
		for(Stmt st : l) {
			System.out.println(st);
		}
	}
	
	
}
