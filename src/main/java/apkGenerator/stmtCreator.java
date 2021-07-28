package apkGenerator;

import java.util.ArrayList;
import java.util.List;

import soot.Body;
import soot.Local;
import soot.Value;
import soot.jimple.AddExpr;
import soot.jimple.AndExpr;
import soot.jimple.AssignStmt;
import soot.jimple.BinopExpr;
import soot.jimple.BreakpointStmt;
import soot.jimple.CastExpr;
import soot.jimple.CmpExpr;
import soot.jimple.CmpgExpr;
import soot.jimple.CmplExpr;
import soot.jimple.ConditionExpr;
import soot.jimple.DivExpr;
import soot.jimple.EnterMonitorStmt;
import soot.jimple.EqExpr;
import soot.jimple.ExitMonitorStmt;
import soot.jimple.GeExpr;
import soot.jimple.GotoStmt;
import soot.jimple.GtExpr;
import soot.jimple.IdentityStmt;
import soot.jimple.IfStmt;
import soot.jimple.InstanceOfExpr;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.LeExpr;
import soot.jimple.LengthExpr;
import soot.jimple.LookupSwitchStmt;
import soot.jimple.LtExpr;
import soot.jimple.MonitorStmt;
import soot.jimple.MulExpr;
import soot.jimple.NeExpr;
import soot.jimple.NegExpr;
import soot.jimple.NewArrayExpr;
import soot.jimple.NewExpr;
import soot.jimple.NewMultiArrayExpr;
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
import soot.jimple.ThrowStmt;
import soot.jimple.UshrExpr;
import soot.jimple.VirtualInvokeExpr;
import soot.jimple.XorExpr;

public class stmtCreator {
	
	protected static AssignStmt createAssignStmt(Stmt s, Body b) {
		AssignStmt st = (AssignStmt) s;
		Local v1 = (Local) st.getLeftOp();
		v1 = getLocal(v1.getName(),b);
		Value v2 = st.getRightOp();
		if(v2 instanceof Local) {
			Local loc = (Local) v2;
			v2 = getLocal(loc.getName(),b);
		} else if(v2 instanceof BinopExpr) {
			v2 = createBinopExpr((BinopExpr) v2, b);
		} else if(v2 instanceof CastExpr) {
			CastExpr cast = (CastExpr) v2;
			Value val = cast.getOp();
			if(val instanceof Local) {
				Local loc = (Local) val;
				val = getLocal(loc.getName(), b);
			}
			v2 = Jimple.v().newCastExpr(val, cast.getCastType());
		} else if(v2 instanceof InstanceOfExpr) {
			InstanceOfExpr instanceOf = (InstanceOfExpr) v2;
			Value val = instanceOf.getOp();
			if(val instanceof Local) {
				Local loc = (Local) val;
				val = getLocal(loc.getName(),b);
			}
			v2 = Jimple.v().newInstanceOfExpr(val, instanceOf.getType());
		} else if(v2 instanceof InvokeExpr) {
			InvokeExpr e = (InvokeExpr) v2;
			if(v2 instanceof SpecialInvokeExpr) {
				v2 = createSpecialInvokeExpr(e, b);
			} else if(v2 instanceof InterfaceInvokeExpr) {
				v2 = createInterfaceInvokeExpr(e, b);
			} else if(v2 instanceof VirtualInvokeExpr) {
				v2 = createVirtualInvokeExpr(e, b);
			} else if(v2 instanceof StaticInvokeExpr) {
				v2 = createStaticInvokeExpr(e, b);
			}
		} else if(v2 instanceof NewExpr) {
			NewExpr newexp = (NewExpr) v2;
			v2 = Jimple.v().newNewExpr(newexp.getBaseType());
		} else if(v2 instanceof NewArrayExpr) {
			NewArrayExpr array = (NewArrayExpr) v2;
			Value size = array.getSize();
			if(size instanceof Local) {
				Local loc = (Local) size;
				size = getLocal(loc.getName(), b);
			}
			v2 = Jimple.v().newNewArrayExpr(array.getBaseType(), size);
		} else if(v2 instanceof NewMultiArrayExpr) {
			NewMultiArrayExpr array = (NewMultiArrayExpr) v2;
			List<Value> newSizes = new ArrayList<Value>();
			for(Value size : array.getSizes()) {
				if(size instanceof Local) {
					Local loc = (Local) size;
					Local newLocal = getLocal(loc.getName(), b);
					newSizes.add(newLocal);
				} else {
					newSizes.add(size);
				}
			}
			v2 = Jimple.v().newNewMultiArrayExpr(array.getBaseType(), newSizes);
		} else if(v2 instanceof LengthExpr) {
			LengthExpr len = (LengthExpr) v2;
			Value v = len.getOp();
			if(v instanceof Local) {
				Local loc = (Local) v;
				v = getLocal(loc.getName(), b);
			}
			v2 = Jimple.v().newLengthExpr(v);
		} else if(v2 instanceof NegExpr) {
			NegExpr neg = (NegExpr) v2;
			Value v = neg.getOp();
			if(v instanceof Local) {
				Local loc = (Local) v;
				v = getLocal(loc.getName(), b);
			}
			v2 = Jimple.v().newNegExpr(v);
		}
		return Jimple.v().newAssignStmt(v1, v2);
	}

	private static BinopExpr createBinopExpr(BinopExpr e, Body b) {
		Value left = e.getOp1();
		Value right = e.getOp2();
		if(left instanceof Local) {
			Local loc = (Local) left;
			left = getLocal(loc.getName(), b);
		}
		if(right instanceof Local) {
			Local loc = (Local) right;
			right = getLocal(loc.getName(),b);
		}
		if(e instanceof AddExpr) {
			return Jimple.v().newAddExpr(left, right);
		} else if(e instanceof AndExpr) {
			return Jimple.v().newAndExpr(left, right);
		} else if(e instanceof CmpExpr) {
			return Jimple.v().newCmpExpr(left, right);
		} else if(e instanceof CmplExpr) {
			return Jimple.v().newCmplExpr(left, right);
		} else if(e instanceof CmpgExpr) {
			return Jimple.v().newCmpgExpr(left, right);
		} else if(e instanceof DivExpr) {
			return Jimple.v().newDivExpr(left, right);
		} else if(e instanceof EqExpr) {
			return Jimple.v().newEqExpr(left, right);
		} else if(e instanceof GeExpr) {
			return Jimple.v().newGeExpr(left, right);
		} else if(e instanceof GtExpr) {
			return Jimple.v().newGtExpr(left, right);
		} else if(e instanceof LeExpr) {
			return Jimple.v().newLeExpr(left, right);
		} else if(e instanceof LtExpr) {
			return Jimple.v().newLtExpr(left, right);
		} else if(e instanceof MulExpr) {
			return Jimple.v().newMulExpr(left, right);
		} else if(e instanceof NeExpr) {
			return Jimple.v().newNeExpr(left, right);
		} else if(e instanceof OrExpr) {
			return Jimple.v().newOrExpr(left, right);
		} else if(e instanceof RemExpr) {
			return Jimple.v().newRemExpr(left, right);
		} else if(e instanceof ShlExpr) {
			return Jimple.v().newShlExpr(left, right);
		} else if(e instanceof ShrExpr) {
			return Jimple.v().newShrExpr(left, right);
		} else if(e instanceof UshrExpr) {
			return Jimple.v().newUshrExpr(left, right);
		} else if(e instanceof XorExpr) {
			return Jimple.v().newXorExpr(left, right);
		} else {
			/* IMPOSSIBLE */
			return null;
		}
	}
	/* TODO : Opti and clean */
	protected static IdentityStmt createIdentity(Stmt s, Body b) {
		IdentityStmt st = (IdentityStmt) s;
		// Gauche
		Local leftOp  = (Local) st.getLeftOp();
		Local leftLocal = getLocal(leftOp.getName(),b);
		// Droite
		Value rightOp = (Value) st.getRightOp().clone();
		if(rightOp instanceof ParameterRef) {
			ParameterRef param = (ParameterRef) rightOp;
			ParameterRef newParam = new ParameterRef(param.getType(), param.getIndex());
			return Jimple.v().newIdentityStmt(leftLocal, newParam);
		} else {
			return Jimple.v().newIdentityStmt(leftLocal, rightOp);
		}
	}
	
	protected static GotoStmt createGoToStmt(Stmt s, Body b) {
		GotoStmt st = (GotoStmt) s;
		return Jimple.v().newGotoStmt(st.getTarget());
	}
	
	protected static IfStmt createIfStmt(Stmt s, Body b) {
		IfStmt st = (IfStmt) s;
		ConditionExpr cond = (ConditionExpr) st.getCondition();
		Value v1 = cond.getOp1();
		Value v2 = cond.getOp2();
		if(v1 instanceof Local) {
			Local loc = (Local) v1;
			v1 = getLocal(loc.getName(),b);
		}
		if(v2 instanceof Local) {
			Local loc = (Local) v2;
			v2 = getLocal(loc.getName(),b);
		}
		if(cond instanceof EqExpr) {
			cond = Jimple.v().newEqExpr(v1, v2);
		} else if(cond instanceof GeExpr) {
			cond = Jimple.v().newGeExpr(v1, v2);
		} else if(cond instanceof GtExpr) {
			cond = Jimple.v().newGtExpr(v1, v2);
		} else if(cond instanceof LeExpr) {
			cond = Jimple.v().newLeExpr(v1, v2);
		} else if(cond instanceof LtExpr) {
			cond = Jimple.v().newLtExpr(v1, v2);
		} else if(cond instanceof NeExpr) {
			cond = Jimple.v().newNeExpr(v1, v2);
		}
		return Jimple.v().newIfStmt(cond, st.getTarget());
	}
	
	/* TODO : Opti and Clean */
	protected static InvokeStmt createInvokeStmt(Stmt s, Body b) {
		InvokeStmt st = (InvokeStmt) s;
		InvokeExpr expr = st.getInvokeExpr();
		if(expr instanceof SpecialInvokeExpr) {
			SpecialInvokeExpr specialExpr = createSpecialInvokeExpr(expr, b);
			return Jimple.v().newInvokeStmt(specialExpr);
		} else if(expr instanceof InterfaceInvokeExpr) {
			InterfaceInvokeExpr interfaceExpr = createInterfaceInvokeExpr(expr, b);
			return Jimple.v().newInvokeStmt(interfaceExpr);
		} else if(expr instanceof VirtualInvokeExpr) {
			VirtualInvokeExpr virtualExpr = createVirtualInvokeExpr(expr, b);
			return Jimple.v().newInvokeStmt(virtualExpr);
		} else if(expr instanceof StaticInvokeExpr) {
			StaticInvokeExpr staticExpr = createStaticInvokeExpr(expr, b);
			return Jimple.v().newInvokeStmt(staticExpr);
		} else {
			/* IMPOSSIBLE */
			return null;
		}
	}
	
	private static SpecialInvokeExpr createSpecialInvokeExpr(InvokeExpr e, Body b) {
		SpecialInvokeExpr specialExpr = (SpecialInvokeExpr) e;
		Local loc = (Local) specialExpr.getBase();
		Local newLocal = getLocal(loc.getName(),b);
		List<Value> args = new ArrayList<Value>();
		for(Value arg : e.getArgs()) {
			if(arg instanceof Local) {
				Local argLoc = (Local) arg;
				Local newArgLocal = getLocal(argLoc.getName(),b);
				args.add(newArgLocal);
			} else {
				args.add(arg);
			}
		}
		return Jimple.v().newSpecialInvokeExpr(newLocal, e.getMethodRef(),args);
	}
	
	private static InterfaceInvokeExpr createInterfaceInvokeExpr(InvokeExpr e, Body b) {
		InterfaceInvokeExpr interfaceExpr = (InterfaceInvokeExpr) e;
		Local loc = (Local) interfaceExpr.getBase();
		Local newLocal = getLocal(loc.getName(),b);
		List<Value >args = new ArrayList<Value>();
		for(Value arg : e.getArgs()) {
			if(arg instanceof Local) {
				Local argLoc = (Local) arg;
				Local newArgLocal = getLocal(argLoc.getName(),b);
				args.add(newArgLocal);
			} else {
				args.add(arg);
			}
		}
		return Jimple.v().newInterfaceInvokeExpr(newLocal, e.getMethodRef(),args);
	}
	
	private static VirtualInvokeExpr createVirtualInvokeExpr(InvokeExpr e, Body b) {
		VirtualInvokeExpr virtualExpr = (VirtualInvokeExpr) e;
		Local loc = (Local) virtualExpr.getBase();
		Local newLocal = getLocal(loc.getName(),b);
		List<Value> args = new ArrayList<Value>();
		for(Value arg : e.getArgs()) {
			if(arg instanceof Local) {
				Local argLoc = (Local) arg;
				Local newArgLocal = getLocal(argLoc.getName(),b);
				args.add(newArgLocal);
			} else {
				args.add(arg);
			}
		}
		return Jimple.v().newVirtualInvokeExpr(newLocal, e.getMethodRef(), args);
	}
	
	private static StaticInvokeExpr createStaticInvokeExpr(InvokeExpr e, Body b) {
		List<Value> args = new ArrayList<Value>();
		for(Value arg : e.getArgs()) {
			if(arg instanceof Local) {
				Local argLoc = (Local) arg;
				Local newArgLocal = getLocal(argLoc.getName(),b);
				args.add(newArgLocal);
			} else {
				args.add(arg);
			}
		}
		return Jimple.v().newStaticInvokeExpr(e.getMethodRef(), args);

	}
	 /* TODO : opti and clean */
	protected static SwitchStmt createSwitchStmt(Stmt s, Body b) {
		if(s instanceof TableSwitchStmt) {
			TableSwitchStmt st = (TableSwitchStmt) s;
			Value v = st.getKey();
			if(v instanceof Local) {
				Local loc = (Local) v;
				v = getLocal(loc.getName(),b);
			}
			return Jimple.v().newTableSwitchStmt(v, st.getLowIndex(), st.getHighIndex(), st.getTargets(), st.getDefaultTarget());
		} else if(s instanceof LookupSwitchStmt) {
			LookupSwitchStmt st = (LookupSwitchStmt) s;
			Value v = st.getKey();
			if(v instanceof Local) {
				Local loc = (Local) v;
				v = getLocal(loc.getName(),b);
			}
			return Jimple.v().newLookupSwitchStmt(v, st.getLookupValues(), st.getTargets(), st.getDefaultTarget());
		} else {
			/* IMPOSSIBLE */
			return null;
		}
	}
	
	/* TODO : opti and clean */
	protected static MonitorStmt createMonitorStmt(Stmt s, Body b) {
		MonitorStmt st = (MonitorStmt) s;
		if(s instanceof EnterMonitorStmt) {
			Value v = st.getOp();
			if(v instanceof Local) {
				Local local  = (Local) st.getOp();
				Local newLocal = getLocal(local.getName(),b);
				return Jimple.v().newEnterMonitorStmt(newLocal);
			} else {
				return Jimple.v().newEnterMonitorStmt(v);
			}
		} else if(s instanceof ExitMonitorStmt) {
			Value v = st.getOp();
			if(v instanceof Local) {
				Local local  = (Local) st.getOp();
				Local newLocal = getLocal(local.getName(),b);
				return Jimple.v().newExitMonitorStmt(newLocal);
			} else {
				return Jimple.v().newExitMonitorStmt(v);
			}
		} else {
			/* IMPOSSIBLE */
			return null;
		}
	}
	
	/* TODO : opti and clean */
	protected static ReturnStmt createReturnStmt(Stmt s, Body b) {
		ReturnStmt st = (ReturnStmt) s;
		Value v = st.getOp();
		if(v instanceof Local) {
			Local local  = (Local) st.getOp();
			Local newLocal = getLocal(local.getName(),b);
			return Jimple.v().newReturnStmt(newLocal);
		} else {
			return Jimple.v().newReturnStmt(v);
		}
	}
	
	/* TODO : opti and clean */
	protected static ThrowStmt createThrowStmt(Stmt s, Body b) {
		ThrowStmt st = (ThrowStmt) s;
		Value v = st.getOp();
		if(v instanceof Local) {
			Local local  = (Local) st.getOp();
			Local newLocal = getLocal(local.getName(),b);
			return Jimple.v().newThrowStmt(newLocal);
		} else {
			return Jimple.v().newThrowStmt(v);
		}
	}
	
	protected static BreakpointStmt createBreakpointStmt() {
		return Jimple.v().newBreakpointStmt();
	}

	protected static NopStmt createNopStmt() {
		return Jimple.v().newNopStmt();
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
