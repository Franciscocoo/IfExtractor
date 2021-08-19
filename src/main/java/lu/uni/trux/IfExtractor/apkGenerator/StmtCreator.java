package lu.uni.trux.IfExtractor.apkGenerator;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import soot.Body;
import soot.Local;
import soot.RefType;
import soot.Type;
import soot.Value;
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
import soot.jimple.GeExpr;
import soot.jimple.GotoStmt;
import soot.jimple.GtExpr;
import soot.jimple.IdentityStmt;
import soot.jimple.IfStmt;
import soot.jimple.InstanceFieldRef;
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
import soot.jimple.ShlExpr;
import soot.jimple.ShrExpr;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticFieldRef;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.SubExpr;
import soot.jimple.SwitchStmt;
import soot.jimple.TableSwitchStmt;
import soot.jimple.ThisRef;
import soot.jimple.ThrowStmt;
import soot.jimple.UshrExpr;
import soot.jimple.VirtualInvokeExpr;

/**
 * Methods used to create new Statements based on the old Statements of the same type
 * @author François JULLION
 */
public class StmtCreator {
	
	/**
	 * Create new AssignStmt
	 * @param s, the old Assign Stmt
	 * @param b, the Body
	 * @return new AssignStmt
	 */
	protected static AssignStmt createAssignStmt(Stmt s, Body b) {
		/* v1 = v2 */
		AssignStmt st = (AssignStmt) s;
		Value v1 = st.getLeftOp();
		/* Checking if v1 is a Local */
		if(v1 instanceof Local) {
			Local loc = (Local) v1;
			v1 = getLocal(loc.getName(), b);
		/* v1.field */
		} else if(v1 instanceof InstanceFieldRef) {
			InstanceFieldRef fieldRef = (InstanceFieldRef) v1;
			Local base = (Local) fieldRef.getBase();
			Local newBase = getLocal(base.getName(), b);
			v1 = Jimple.v().newInstanceFieldRef(newBase, fieldRef.getFieldRef());
		/* field */
		} else if(v1 instanceof StaticFieldRef) {
			StaticFieldRef staticRef = (StaticFieldRef) v1;
			v1 = Jimple.v().newStaticFieldRef(staticRef.getFieldRef());
		}
		Value v2 = st.getRightOp();
		/* Checking if v2 is a Local */
		if(v2 instanceof Local) {
			Local loc = (Local) v2;
			v2 = getLocal(loc.getName(),b);
		/* v2.field */
		} else if(v2 instanceof InstanceFieldRef) {
			InstanceFieldRef fieldRef = (InstanceFieldRef) v2;
			Local base = (Local) fieldRef.getBase();
			Local newBase = getLocal(base.getName(), b);
			v2 = Jimple.v().newInstanceFieldRef(newBase, fieldRef.getFieldRef());
		/* field */
		} else if(v2 instanceof StaticFieldRef) {
			StaticFieldRef staticRef = (StaticFieldRef) v2;
			v2 = Jimple.v().newStaticFieldRef(staticRef.getFieldRef());
		/* imm1 op imm2 */
		} else if(v2 instanceof BinopExpr) {
			v2 = createBinopExpr((BinopExpr) v2, b);
		/* (type) imm */
		} else if(v2 instanceof CastExpr) {
			CastExpr cast = (CastExpr) v2;
			Value val = cast.getOp();
			if(val instanceof Local) {
				Local loc = (Local) val;
				val = getLocal(loc.getName(), b);
			}
			v2 = Jimple.v().newCastExpr(val, cast.getCastType());
		/* imm instanceof type */
		} else if(v2 instanceof InstanceOfExpr) {
			InstanceOfExpr instanceOf = (InstanceOfExpr) v2;
			Value val = instanceOf.getOp();
			if(val instanceof Local) {
				Local loc = (Local) val;
				val = getLocal(loc.getName(),b);
			}
			v2 = Jimple.v().newInstanceOfExpr(val, instanceOf.getType());
		/* invokeExpr */
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
		/* new RefType */
		} else if(v2 instanceof NewExpr) {
			NewExpr newexp = (NewExpr) v2;
			v2 = Jimple.v().newNewExpr(newexp.getBaseType());
		/* newArray(type)[imm] */
		} else if(v2 instanceof NewArrayExpr) {
			NewArrayExpr array = (NewArrayExpr) v2;
			Value size = array.getSize();
			if(size instanceof Local) {
				Local loc = (Local) size;
				size = getLocal(loc.getName(), b);
			}
			v2 = Jimple.v().newNewArrayExpr(array.getBaseType(), size);
		/* newMultiArray(type)[imm][imm][]* */
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
		/* length imm*/
		} else if(v2 instanceof LengthExpr) {
			LengthExpr len = (LengthExpr) v2;
			Value v = len.getOp();
			if(v instanceof Local) {
				Local loc = (Local) v;
				v = getLocal(loc.getName(), b);
			}
			v2 = Jimple.v().newLengthExpr(v);
		/* neg imm */
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

	/**
	 * Create new BinopExpr
	 * @param e, the old BinopExpr
	 * @param b, the Body
	 * @return new BinopExpr
	 */
	private static BinopExpr createBinopExpr(BinopExpr e, Body b) {
		BinopExpr newBinopExpr;
		/* Getting the 2 Values */
		Value left = e.getOp1();
		Value right = e.getOp2();
		/* Checking if the left Value is a Local */
		if(left instanceof Local) {
			Local loc = (Local) left;
			left = getLocal(loc.getName(), b);
		}
		/* Checking if the right Value is a Local */
		if(right instanceof Local) {
			Local loc = (Local) right;
			right = getLocal(loc.getName(),b);
		}
		/* Checking the type of BinopExpr */
		if(e instanceof AddExpr) {
			newBinopExpr = Jimple.v().newAddExpr(left, right);
		} else if(e instanceof AndExpr) {
			newBinopExpr = Jimple.v().newAndExpr(left, right);
		} else if(e instanceof CmpExpr) {
			newBinopExpr = Jimple.v().newCmpExpr(left, right);
		} else if(e instanceof CmplExpr) {
			newBinopExpr = Jimple.v().newCmplExpr(left, right);
		} else if(e instanceof CmpgExpr) {
			newBinopExpr = Jimple.v().newCmpgExpr(left, right);
		} else if(e instanceof DivExpr) {
			newBinopExpr = Jimple.v().newDivExpr(left, right);
		} else if(e instanceof EqExpr) {
			newBinopExpr = Jimple.v().newEqExpr(left, right);
		} else if(e instanceof GeExpr) {
			newBinopExpr = Jimple.v().newGeExpr(left, right);
		} else if(e instanceof GtExpr) {
			newBinopExpr = Jimple.v().newGtExpr(left, right);
		} else if(e instanceof LeExpr) {
			newBinopExpr = Jimple.v().newLeExpr(left, right);
		} else if(e instanceof LtExpr) {
			newBinopExpr = Jimple.v().newLtExpr(left, right);
		} else if(e instanceof MulExpr) {
			newBinopExpr = Jimple.v().newMulExpr(left, right);
		} else if(e instanceof NeExpr) {
			newBinopExpr = Jimple.v().newNeExpr(left, right);
		} else if(e instanceof OrExpr) {
			newBinopExpr = Jimple.v().newOrExpr(left, right);
		} else if(e instanceof RemExpr) {
			newBinopExpr = Jimple.v().newRemExpr(left, right);
		} else if(e instanceof ShlExpr) {
			newBinopExpr = Jimple.v().newShlExpr(left, right);
		} else if(e instanceof ShrExpr) {
			newBinopExpr = Jimple.v().newShrExpr(left, right);
		} else if(e instanceof SubExpr) {
			newBinopExpr = Jimple.v().newSubExpr(left, right);
		} else if(e instanceof UshrExpr) {
			newBinopExpr = Jimple.v().newUshrExpr(left, right);
		} else {
			newBinopExpr = Jimple.v().newXorExpr(left, right);
		}
		return newBinopExpr;
	}
	
	/**
	 * Create new IdentityStmt
	 * @param s, the old IdentityStmt
	 * @param b, the Body
	 * @return new IdentityStmt
	 */
	protected static IdentityStmt createIdentity(Stmt s, Body b, int n) {
		IdentityStmt newIdentityStmt;
		IdentityStmt st = (IdentityStmt) s;
		Local leftOp  = (Local) st.getLeftOp();
		Local leftLocal = getLocal(leftOp.getName(),b);
		Value rightOp = (Value) st.getRightOp().clone();
		if(rightOp instanceof ParameterRef) {
			ParameterRef param = (ParameterRef) rightOp;
			ParameterRef newParam = new ParameterRef(param.getType(), n);
			List<Type> params = new ArrayList<Type>();
			params.addAll(b.getMethod().getParameterTypes());
			params.add(param.getType());
			b.getMethod().setParameterTypes(params);
			newIdentityStmt = Jimple.v().newIdentityStmt(leftLocal, newParam);
		} else if(rightOp instanceof ThisRef){
			ThisRef ref = (ThisRef) rightOp;
			ThisRef newRef = Jimple.v().newThisRef((RefType) ref.getType());
			newIdentityStmt = Jimple.v().newIdentityStmt(leftLocal, newRef);
			b.getMethod().setModifiers(Modifier.PUBLIC);
		} else {
			CaughtExceptionRef exceptRef = (CaughtExceptionRef) rightOp.clone();
			newIdentityStmt = Jimple.v().newIdentityStmt(leftLocal, exceptRef);
		}
		return newIdentityStmt;
	}
	
	/**
	 * Create new GotoStmt
	 * @param s, the old GotoStmt
	 * @param b, the body
	 * @return new GotoStmt
	 */
	protected static GotoStmt createGoToStmt(Stmt s, Body b) {
		GotoStmt st = (GotoStmt) s;
		return Jimple.v().newGotoStmt(st.getTarget());
	}
	
	/**
	 * Create new IfStmt
	 * @param s, the old ifStmt
	 * @param b, the Body
	 * @return new IfStmt
	 */
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
	
	
	/**
	 * Create new InvokeStmt
	 * @param s, the old InvokeStmt
	 * @param b, the Body
	 * @return new InvokeStmt
	 */
	protected static InvokeStmt createInvokeStmt(Stmt s, Body b) {
		InvokeStmt newInvokeStmt;
		InvokeStmt st = (InvokeStmt) s;
		InvokeExpr expr = st.getInvokeExpr();
		if(expr instanceof SpecialInvokeExpr) {
			SpecialInvokeExpr specialExpr = createSpecialInvokeExpr(expr, b);
			newInvokeStmt = Jimple.v().newInvokeStmt(specialExpr);
		} else if(expr instanceof InterfaceInvokeExpr) {
			InterfaceInvokeExpr interfaceExpr = createInterfaceInvokeExpr(expr, b);
			newInvokeStmt = Jimple.v().newInvokeStmt(interfaceExpr);
		} else if(expr instanceof VirtualInvokeExpr) {
			VirtualInvokeExpr virtualExpr = createVirtualInvokeExpr(expr, b);
			newInvokeStmt = Jimple.v().newInvokeStmt(virtualExpr);
		} else {
			StaticInvokeExpr staticExpr = createStaticInvokeExpr(expr, b);
			newInvokeStmt = Jimple.v().newInvokeStmt(staticExpr);
		}
		return newInvokeStmt;
	}
	
	/**
	 * Create new SpecialInvokeExpr
	 * @param e, the old SpecialInvokeExpr
	 * @param b, the Body
	 * @return new SpecialInvokeExpr
	 */
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
	
	
	/**
	 * Create new InterfaceInvokeExpr
	 * @param e, the old InterfaceInvokeExpr
	 * @param b, the Body
	 * @return new InterfaceInvokeExpr
	 */
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
	
	/**
	 * Create new VirtualInvokeExpr
	 * @param e, the old VirtualInvokeExpr
	 * @param b, the body
	 * @return new VirtualInvokeExpr
	 */
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
	
	/**
	 * Create new StaticInvokeExpr
	 * @param e, the old StaticInvokeExpr
	 * @param b, the body
	 * @return new StaticInvokeExpr
	 */
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
	
	/**
	 * Create new SwitchStmt
	 * @param s, the old SwitchStmt
	 * @param b, the body
	 * @return new SwitchStmt
	 */
	protected static SwitchStmt createSwitchStmt(Stmt s, Body b) {
		SwitchStmt newSwitchStmt;
		SwitchStmt st = (SwitchStmt) s;
		Value v = st.getKey();
		if(s instanceof TableSwitchStmt) {
			TableSwitchStmt tableStmt = (TableSwitchStmt) s;
			if(v instanceof Local) {
				Local loc = (Local) v;
				v = getLocal(loc.getName(),b);
			}
			newSwitchStmt = Jimple.v().newTableSwitchStmt(v, tableStmt.getLowIndex(), tableStmt.getHighIndex(), tableStmt.getTargets(), tableStmt.getDefaultTarget());
		} else {
			LookupSwitchStmt lookUpStmt = (LookupSwitchStmt) s;
			if(v instanceof Local) {
				Local loc = (Local) v;
				v = getLocal(loc.getName(),b);
			}
			newSwitchStmt = Jimple.v().newLookupSwitchStmt(v, lookUpStmt.getLookupValues(), lookUpStmt.getTargets(), lookUpStmt.getDefaultTarget());
		}
		return newSwitchStmt;
	}
	

	/**
	 * Create new MonitorStmt
	 * @param s, the old MonitorStmt
	 * @param b, the body
	 * @return new MonitorStmt
	 */
	protected static MonitorStmt createMonitorStmt(Stmt s, Body b) {
		MonitorStmt newMonitorStmt;
		MonitorStmt st = (MonitorStmt) s;
		Value v = st.getOp();
		if(s instanceof EnterMonitorStmt) {
			if(v instanceof Local) {
				Local local  = (Local) st.getOp();
				v = getLocal(local.getName(),b);
			}
			newMonitorStmt = Jimple.v().newEnterMonitorStmt(v);
		} else {
			if(v instanceof Local) {
				Local local  = (Local) st.getOp();
				v = getLocal(local.getName(),b);
			}
			newMonitorStmt = Jimple.v().newExitMonitorStmt(v);
		}
		return newMonitorStmt;
	}
	
	/**
	 * Create new ThrowStmt
	 * @param s, the old ThrowStmt
	 * @param b, the body
	 * @return new ThrowStmt
	 */
	protected static ThrowStmt createThrowStmt(Stmt s, Body b) {
		ThrowStmt st = (ThrowStmt) s;
		Value v = st.getOp();
		if(v instanceof Local) {
			Local local  = (Local) st.getOp();
			v = getLocal(local.getName(),b);
		}
		return Jimple.v().newThrowStmt(v);
	}
	
	/**
	 * Create new BreakpointStmt
	 * @return new BreakpointStmt
	 */
	protected static BreakpointStmt createBreakpointStmt() {
		return Jimple.v().newBreakpointStmt();
	}

	/**
	 * Create new NopStmt
	 * @return new NopStmt
	 */
	protected static NopStmt createNopStmt() {
		return Jimple.v().newNopStmt();
	}
	
	/**
	 * Getting local from the body by the name
	 * @param name, String 
	 * @param b, the body
	 * @return the Local
	 */
	private static Local getLocal(String name, Body b) {
		for(Local l : b.getLocals()) {
			if(l.getName().equals(name)) {
				return l;
			}
		}
		System.out.println("ERROR");
		return null;
	}
}
