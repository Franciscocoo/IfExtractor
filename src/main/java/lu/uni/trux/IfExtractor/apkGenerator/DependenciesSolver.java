package lu.uni.trux.IfExtractor.apkGenerator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lu.uni.trux.IfExtractor.utils.Utils;
import soot.Body;
import soot.Local;
import soot.SootClass;
import soot.SootMethod;
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

/**
 * Methods used to solves Intra Procedural Dependencies
 * @author François JULLION
 */
public class DependenciesSolver {
	
	/**
	 * Generate a new List of Stmt, based on the ifBlock.
	 * @param m, the SootMethod containing the Logical Bomb
	 * @param oldBody, the Body containing the Logical Bomb
	 * @param ifStmt,the ifBlock
	 * @param blockToAnalyse, all Stmt not contain in the ifBlock.
	 * @param c, the SootClass containing m
	 * @return a list of all Stmt usefull for compile the method with the ifBlock
	 */
	public static List<Stmt> getNewStmtBody(SootMethod m, Body oldBody, List<Stmt> ifStmt,
			List<Stmt> blockToAnalyse, SootClass c) {
		/* Retrieve all locals from old body */
		Chain<Local> locals = oldBody.getLocals();
		/* Get all locals from if Block */
		Set<Local> localsIf = getLocalIfBlock(ifStmt, locals);
		/* Retrieve all Stmt */
		List<Stmt> newStmtBody = new ArrayList<Stmt>();
		for (Local l : localsIf) {
			newStmtBody.addAll(getStmtByLocal(oldBody, blockToAnalyse, l)); // CHANGE TO BLOCK
		}
		/* Adding the IfBlock and order it */
		newStmtBody.addAll(ifStmt);
		newStmtBody = Utils.orderList(oldBody, newStmtBody);
		return newStmtBody;
	}
	
	/**
	 * Retrieve all used locals in the ifBlock 
	 * @param l, list of Stmt
	 * @param locals, chain of Locals from a Body
	 * @return Set of local located in the ifBlock
	 */
	public static Set<Local> getLocalIfBlock(List<Stmt> l, Chain<Local> locals) {
		Set<Local> localsBlock = new HashSet<Local>();
		Value v1, v2;
		for (Stmt s : l) {
			/* Assignement */
			if (s instanceof AssignStmt) {
				/* Getting the Values */
				v1 = ((AssignStmt) s).getLeftOp();
				v2 = ((AssignStmt) s).getRightOp();
				/* Check if the Values are Local from the Body */
				if (locals.contains(v1) && v1 instanceof Local) {
					localsBlock.add((Local) v1);
				}
				if (locals.contains(v2) && v2 instanceof Local) {
					localsBlock.add((Local) v2);
				}
			}
			/* Identification */
			else if (s instanceof IdentityStmt) {
				/* Getting the Value */
				v1 = ((IdentityStmt) s).getLeftOp();
				/* Check if the Value is Local from the Body */
				if (locals.contains(v1) && v1 instanceof Local) {
					localsBlock.add((Local) v1);
				}
			}
			/* if */
			else if (s instanceof IfStmt) {
				/* Getting the Values */
				ConditionExpr cond = (ConditionExpr) ((IfStmt) s).getCondition();
				v1 = cond.getOp1();
				v2 = cond.getOp2();
				/* Check if the Values are Local from the Body */
				if (locals.contains(v1) && v1 instanceof Local) {
					localsBlock.add((Local) v1);
				}
				if (locals.contains(v2) && v2 instanceof Local) {
					localsBlock.add((Local) v2);
				}
			}
			/* invoke */
			else if (s instanceof InvokeStmt) {
				/* Getting the type of InvokeExpr */
				InvokeStmt invoke = ((InvokeStmt) s);
				InvokeExpr expr = invoke.getInvokeExpr();
				if (expr instanceof SpecialInvokeExpr) {
					/* Getting the Value */
					v1 = ((SpecialInvokeExpr) expr).getBase();
					/* Checking if the value is a Local from the Body */
					if (locals.contains(v1) && v1 instanceof Local) {
						localsBlock.add((Local) v1);
					}
				} else if (expr instanceof InterfaceInvokeExpr) {
					/* Getting the Value */
					v1 = ((InterfaceInvokeExpr) expr).getBase();
					/* Checking if the value is a Local from the Body */
					if (locals.contains(v1) && v1 instanceof Local) {
						localsBlock.add((Local) v1);
					}
				} else if (expr instanceof VirtualInvokeExpr) {
					/* Getting the Value */
					v1 = ((VirtualInvokeExpr) expr).getBase();
					/* Checking if the value is a Local from the Body */
					if (locals.contains(v1) && v1 instanceof Local) {
						localsBlock.add((Local) v1);
					}
				}
				/* Getting list of Values (args) */
				for (Value v : expr.getArgs()) {
					/* Checking if the arg is a Local from the Body */
					if (locals.contains(v) && v instanceof Local) {
						localsBlock.add((Local) v);
					}
				}
			}
			/* switch */
			else if (s instanceof SwitchStmt) {
				/* Getting the Value */
				v1 = ((SwitchStmt) s).getKey();
				/* Checking if the value is a Local from the Body */
				if (locals.contains(v1) && v1 instanceof Local) {
					localsBlock.add((Local) v1);
				}
			}
			/* return */
			else if (s instanceof ReturnStmt) {
				/* Getting the Value */
				v1 = ((ReturnStmt) s).getOp();
				/* Checking if the value is a Local from the Body */
				if (locals.contains(v1) && v1 instanceof Local) {
					localsBlock.add((Local) v1);
				}
			}
			/* Monitor */
			else if (s instanceof MonitorStmt) {
				/* Getting the Value */
				v1 = ((MonitorStmt) s).getOp();
				/* Checking if the value is a Local from the Body */
				if (locals.contains(v1) && v1 instanceof Local) {
					localsBlock.add((Local) v1);
				}
			}
			/* throw */
			else if (s instanceof ThrowStmt) {
				/* Getting the Value */
				v1 = ((ThrowStmt) s).getOp();
				/* Checking if the value is a Local from the Body */
				if (locals.contains(v1) && v1 instanceof Local) {
					localsBlock.add((Local) v1);
				}
			}
		}
		return localsBlock;
	}
	
	/**
	 * Retrieve all stmt containing the local loc
	 * @param b, the Body of the ifBlock
	 * @param l, the list of Stmt to analyse
	 * @param loc, the local value
	 * @return list of stmt containing the local
	 */
	private static List<Stmt> getStmtByLocal(Body b, List<Stmt> l, Local loc) {
		List<Stmt> res = new ArrayList<Stmt>();
		Chain<Local> cl = b.getLocals();
		Value v1, v2;
		for (Stmt s : l) {
			/* Assignement */
			if (s instanceof AssignStmt) {
				/* Getting the Values */
				v1 = ((AssignStmt) s).getLeftOp();
				v2 = ((AssignStmt) s).getRightOp();
				/* Checking if the Stmt is using the Local loc */
				if (v1.equals(loc) && v1 instanceof Local) {
					/* Getting all the Values from the Expr */
					for (ValueBox v : v2.getUseBoxes()) {
						Value imm = v.getValue();
						/* Recursive call */
						if (imm instanceof Local && cl.contains(imm) && !imm.equals(v1)) {
							res.addAll(0, getStmtByLocal(b, l, (Local) imm));
						}
					}
					res.add(s);
				}
			}
			/* Identification */
			else if (s instanceof IdentityStmt) {
				/* Getting the Value */
				v1 = ((IdentityStmt) s).getLeftOp();
				/* Checking if the Stmt is using the Local loc */
				if (v1.equals(loc) && v1 instanceof Local) {
					res.add(s);
				}
			}
			/* if */
			else if (s instanceof IfStmt) {
				/* Getting the Values */
				ConditionExpr cond = (ConditionExpr) ((IfStmt) s).getCondition();
				v1 = cond.getOp1();
				v2 = cond.getOp2();
				/* Checking if the Stmt is using the Local loc */
				if ((v1.equals(loc) && v1 instanceof Local) || (v2.equals(loc) && v2 instanceof Local)) {
					res.add(s);
				}
			}
			/* invoke */
			else if (s instanceof InvokeStmt) {
				/* Getting the type of InvokeExpr */
				InvokeStmt invoke = ((InvokeStmt) s);
				InvokeExpr expr = invoke.getInvokeExpr();
				if (expr instanceof SpecialInvokeExpr) {
					/* Getting the Value */
					v1 = ((SpecialInvokeExpr) expr).getBase();
					/* Checking if the Stmt is using the Local loc */
					if (v1.equals(loc) && v1 instanceof Local) {
						res.add(s);
					}
				} else if (expr instanceof InterfaceInvokeExpr) {
					/* Getting the Value */
					v1 = ((InterfaceInvokeExpr) expr).getBase();
					/* Checking if the Stmt is using the Local loc */
					if (v1.equals(loc) && v1 instanceof Local) {
						res.add(s);
					}
				} else if (expr instanceof VirtualInvokeExpr) {
					/* Getting the Value */
					v1 = ((VirtualInvokeExpr) expr).getBase();
					/* Checking if the Stmt is using the Local loc */
					if (v1.equals(loc) && v1 instanceof Local) {
						res.add(s);
					}
				}
				/* Getting list of Values (args) */
				for(Value v : expr.getArgs()) { 
					/* Checking if the Stmt is using the Local loc */
					if(v.equals(loc) && v instanceof Local){ 
						res.add(s); 
					} 
				}
			}
			/* switch */
			else if (s instanceof SwitchStmt) {
				/* Getting the Value */
				v1 = ((SwitchStmt) s).getKey();
				/* Checking if the Stmt is using the Local loc */
				if (v1.equals(loc) && v1 instanceof Local) {
					res.add(s);
				}
			}
			/* return */
			else if (s instanceof ReturnStmt) {
				/* Getting the Value */
				v1 = ((ReturnStmt) s).getOp();
				/* Checking if the Stmt is using the Local loc */
				if (v1.equals(loc) && v1 instanceof Local) {
					res.add(s);
				}
			}
			/* Monitor */
			else if (s instanceof MonitorStmt) {
				/* Getting the Value */
				v1 = ((MonitorStmt) s).getOp();
				/* Checking if the Stmt is using the Local loc */
				if (v1.equals(loc) && v1 instanceof Local) {
					res.add(s);
				}
			}
			/* throw */
			else if (s instanceof ThrowStmt) {
				/* Getting the Value */
				v1 = ((ThrowStmt) s).getOp();
				/* Checking if the Stmt is using the Local loc */
				if (v1.equals(loc) && v1 instanceof Local) {
					res.add(s);
				}
			}
		}
		return res;
	}
	
}
