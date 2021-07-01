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
import soot.JastAddJ.CastExpr;
import soot.jimple.AssignStmt;
import soot.jimple.ConditionExpr;
import soot.jimple.Expr;
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
import soot.jimple.internal.AbstractBinopExpr;
import soot.jimple.internal.JAddExpr;
import soot.jimple.internal.JCastExpr;
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
						v1 = ((IdentityStmt) s).getLeftOp();
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
		        List<Stmt> newStmtBody = new ArrayList<Stmt>();
		        for(Local l : localsBlock) {
		        	newStmtBody.addAll(getStmtByLocal(oldBody,localsBlock,l));
		        }
		        System.out.println(localsBlock);
		        System.out.println(oldBody.getLocals());
		        printNewBody(newStmtBody);
			}
		}));
		PackManager.v().runPacks();
		//PackManager.v().writeOutput();
	}
	
	private static List<Stmt> getStmtByLocal(Body b, Set<Local >localsBlock, Local loc) {
		List<Stmt> res = new ArrayList<Stmt>();
		Chain<Local> cl = b.getLocals();
		Value v1,v2;
		for(Unit u : b.getUnits()) {
			Stmt s = (Stmt) u;
			/* Assignement */
			if(s instanceof AssignStmt) {
				v1 = ((AssignStmt) s).getLeftOp();
				v2 = ((AssignStmt) s).getRightOp();
				if(v1.equals(loc) && v1 instanceof Local) {
					System.out.println("---V2---");
					System.out.println("v1 -> " + v2);
					System.out.println("v2 -> "  + v2);
					System.out.println("Type de v2 : " + v2.getClass());
					if(v2 instanceof JCastExpr) {
						Value imm = (Value) ((JCastExpr) v2).getOp();
						if(imm instanceof Local && cl.contains(imm) && !imm.equals(v1)) {
							res.addAll(0,getStmtByLocal(b,localsBlock,(Local)imm));
						}
					} else if(v2 instanceof AbstractBinopExpr) {
						Value imm1 = ((AbstractBinopExpr) v2).getOp1();
						Value imm2 = ((AbstractBinopExpr) v2).getOp2();
						System.out.println("imm : " + imm1 + " ; " + imm2);
						System.out.println();
						if(imm1 instanceof Local && cl.contains(imm1) && !imm1.equals(v1)) {
							res.addAll(0,getStmtByLocal(b,localsBlock,(Local)imm1));
						}
						if(imm2 instanceof Local && cl.contains(imm2) && !imm2.equals(v1)) {
							res.addAll(0,getStmtByLocal(b,localsBlock,(Local)imm2));
						}
					}
					res.add(s);
				}
			} 
			/* Identification */
			else if(s instanceof IdentityStmt) {
				v1 = ((IdentityStmt) s).getLeftOp();
				if(v1.equals(loc) && v1 instanceof Local) {
					res.add(s);
				}
			} 
			/* if */
			else if(s instanceof IfStmt) {
				ConditionExpr cond = (ConditionExpr) ((IfStmt) s).getCondition();
				v1 = cond.getOp1();
				v2 = cond.getOp2();
				if((v1.equals(loc) && v1 instanceof Local) || (v2.equals(loc) && v2 instanceof Local)) {
					res.add(s);
				}
			} 
			/* invoke */
			else if(s instanceof InvokeStmt) {
				InvokeStmt invoke = ((InvokeStmt) s);
				InvokeExpr expr = invoke.getInvokeExpr();
				if(expr instanceof SpecialInvokeExpr) {
					v1 = ((SpecialInvokeExpr) expr).getBase();
					if(v1.equals(loc) && v1 instanceof Local) {
						res.add(s);
					}
				} else if(expr instanceof InterfaceInvokeExpr) {
					v1 = ((InterfaceInvokeExpr) expr).getBase();
					if(v1.equals(loc) && v1 instanceof Local) {
						res.add(s);
					}
				} else if(expr instanceof VirtualInvokeExpr) {
					v1 = ((VirtualInvokeExpr) expr).getBase();
					if(v1.equals(loc) && v1 instanceof Local) {
						res.add(s);
					}
				}
				/*for(Value v : expr.getArgs()) {
					if(locals.contains(v) && v instanceof Local) {
						localsBlock.add((Local)v);
					}
				}*/
			} 
			/* switch */
			else if(s instanceof SwitchStmt) {
				v1 = ((SwitchStmt) s).getKey();
				if(v1.equals(loc) && v1 instanceof Local) {
					res.add(s);
				}
			} 
			/* return */
			else if(s instanceof ReturnStmt) {
				v1 = ((ReturnStmt) s).getOp();
				if(v1.equals(loc) && v1 instanceof Local) {
					res.add(s);
				}
			} 
			/* Monitor */
			else if(s instanceof MonitorStmt) {
				v1 = ((MonitorStmt) s).getOp();
				if(v1.equals(loc) && v1 instanceof Local) {
					res.add(s);
				}
			} 
			/* throw */
			else if(s instanceof ThrowStmt) {
				v1 = ((ThrowStmt) s).getOp();
				if(v1.equals(loc) && v1 instanceof Local) {
					res.add(s);
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
