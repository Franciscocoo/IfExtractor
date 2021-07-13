package apkGenerator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import soot.Body;
import soot.Local;
import soot.Value;
import soot.ValueBox;
import soot.jimple.AssignStmt;
import soot.jimple.ConditionExpr;
import soot.jimple.IdentityStmt;
import soot.jimple.IfStmt;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.MonitorStmt;
import soot.jimple.ReturnStmt;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.SwitchStmt;
import soot.jimple.ThrowStmt;
import soot.jimple.VirtualInvokeExpr;
import soot.util.Chain;

public class intraDependencies {

	public static Set<Local> getLocalIfBlock(List<Stmt> l, Chain<Local> locals) {
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
	
	public static List<Stmt> getStmtByLocal(Body b, List<Stmt> l, Local loc) {
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
}
