package lu.uni.trux.IfExtractor.apkGenerator;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.Local;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.UnitPatchingChain;
import soot.Value;
import soot.VoidType;
import soot.jimple.AssignStmt;
import soot.jimple.BreakpointStmt;
import soot.jimple.CaughtExceptionRef;
import soot.jimple.GotoStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.IfStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.LookupSwitchStmt;
import soot.jimple.MonitorStmt;
import soot.jimple.NopStmt;
import soot.jimple.NullConstant;
import soot.jimple.ParameterRef;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.SwitchStmt;
import soot.jimple.TableSwitchStmt;
import soot.jimple.ThrowStmt;

/**
 * Methods used to create new Methods, add it to the ifClass and modify the mainActivityClass
 * @author François JULLION
 */
public class MethodCreator {
	
	/**
	 * Create the init Method and add it to the ifClass
	 * @param ifClass, the SootClass
	 */
	public static void createInitMethod(SootClass ifClass) {
		SootClass activity = Scene.v().loadClassAndSupport("android.app.Activity");
		SootMethod tmp = activity.getMethodByName("<init>");
		SootMethod initMethod = new SootMethod(tmp.getName(), tmp.getParameterTypes(), tmp.getReturnType(), 0);
		ifClass.addMethod(initMethod);
		JimpleBody initBody = Jimple.v().newBody(initMethod);
		initMethod.setActiveBody(initBody);
		UnitPatchingChain units = initBody.getUnits();
		// ifClass r0
		Local r0 = Jimple.v().newLocal("r0", ifClass.getType());
		initBody.getLocals().add(r0);
		// r0 := @this : ifClass
		units.add(Jimple.v().newIdentityStmt(r0, Jimple.v().newThisRef(ifClass.getType())));
		// Specialinvoke r0.<android.app.Activity : void <init()>();
		SpecialInvokeExpr specialExpr = Jimple.v().newSpecialInvokeExpr(r0, tmp.makeRef());
		units.add(Jimple.v().newInvokeStmt(specialExpr));
		// return
		units.add(Jimple.v().newReturnVoidStmt());
		initBody.validate();
	}
	
	/**
	 * Modify the onCreate method of the mainActivityClass
	 * @param mainActivityClass, the SootClass which contains the mainActivity
	 * @param ifClass, the SootClass
	 * @param n, integer which represents the number of ifMethods
	 */
	public static void modifyOnCreateMethod(SootClass mainActivityClass, SootClass ifClass, int n) {
		SootMethod onCreateMethod = mainActivityClass.getMethod("void onCreate(android.os.Bundle)");
		JimpleBody onCreateBody = Jimple.v().newBody();
		onCreateMethod.setActiveBody(onCreateBody);
		UnitPatchingChain units = onCreateBody.getUnits();
		// ifClass r0
		Local r0 = Jimple.v().newLocal("r0", ifClass.getType());
		onCreateBody.getLocals().add(r0);
		// android.os.bundle $r1
		Local $r1 = Jimple.v().newLocal("$r1", onCreateMethod.getParameterType(0));
		onCreateBody.getLocals().add($r1);
		// r0 := @this: ifClass
		units.add(Jimple.v().newIdentityStmt(r0, Jimple.v().newThisRef(ifClass.getType())));
		// $r1 := @parameter0: android.os.Bundle
		units.add(Jimple.v().newIdentityStmt($r1, Jimple.v().newParameterRef(onCreateMethod.getParameterType(0), 0)));
		// invoke r0.ifMethod_n()
		for(int i=1; i<=n; i++) {
			String methodName = "ifMethod" + i;
			SootMethod ifMeth = ifClass.getMethodByName(methodName);
	 		Integer nbParamsIfMethods = ifMeth.getParameterCount();
	 		List<Value> args = new ArrayList<Value>();
			if(nbParamsIfMethods>=1) {
				for(int j=0;j<ifMeth.getParameterCount();j++) {
					NullConstant tmpArg = NullConstant.v();
					args.add(tmpArg);
				}
			}
			// Generate new InvokeStmt
			InvokeExpr expr;
			if(ifMeth.isStatic()) {
				expr = Jimple.v().newStaticInvokeExpr(ifMeth.makeRef());
				if(nbParamsIfMethods>=1) {
					expr = Jimple.v().newStaticInvokeExpr(ifMeth.makeRef(), args);
				}
			} else {
				expr= Jimple.v().newVirtualInvokeExpr($r1, ifMeth.makeRef());
				if(nbParamsIfMethods>=1) {
					expr = Jimple.v().newVirtualInvokeExpr($r1, ifMeth.makeRef(), args);
				}
				
			}
			units.add(Jimple.v().newInvokeStmt(expr));
		}
		// return;
		units.add(Jimple.v().newReturnVoidStmt());
		onCreateBody.validate();
	}
	
	/**
	 * Creating ifMethods and add it to the ifClass
	 * @param ifClass, the SootClass
	 * @param localSet, 
	 * @param stmtList, the ifBlock
	 * @param n, integer which represents the index of the ifMethod
	 */
	public static void createIfMethod(SootClass ifClass, Set<Local> localSet, List<Stmt> stmtList, int n) {
		/* Creating the ifMethod */
		List<Type> typeList = new ArrayList<Type>();
		String methodName = "ifMethod" + n;
		SootMethod ifMethod = new SootMethod(methodName, typeList, VoidType.v(), Modifier.PUBLIC | Modifier.STATIC);
		/* Adding the ifMethod to the ifClass */
		ifClass.addMethod(ifMethod);
		/* Creating new Body */
		JimpleBody ifMethodBody = Jimple.v().newBody(ifMethod);
		ifMethod.setActiveBody(ifMethodBody);
		/* Adding locals to the body */
		addLocals(localSet, ifMethodBody);
		/* Adding Stmt to the body */
		Map<Stmt,Stmt> cloneMap = addStmt(ifMethodBody, stmtList);
		/* Solve targets problems of the body */		
		solveTargets(ifMethodBody, cloneMap);
		ifMethodBody.validate();
	}
	
	/**
	 * Cloning the locals to the new Body
	 * @param s, set of Locals
	 * @param b, new Jimple Body
	 */
	private static void addLocals(Set<Local> s, JimpleBody b) {
		for(Local loc : s) {
			Local tmp = Jimple.v().newLocal(loc.getName(), loc.getType());
			b.getLocals().add(tmp);
		}
	}
	
	/**
	 * Adding Statements to the body
	 * @param b, Jimple Body
	 * @param l, List of Statement
	 * @return Map of Stmt,Stmt
	 */
	private static Map<Stmt,Stmt> addStmt(JimpleBody b, List<Stmt> l) {
		UnitPatchingChain units = b.getUnits();
        List<Unit> generatedUnits = new ArrayList<>();
        Map<Stmt,Stmt> cloneMap = new HashMap<Stmt,Stmt>();
        int counterParameter = 0;
		for(Stmt s : l) {
			/* Assignement */
			if (s instanceof AssignStmt) {
				AssignStmt assign = StmtCreator.createAssignStmt(s, b);
				cloneMap.put(s, assign);
				generatedUnits.add(assign);
			}
			/* Identification */
			else if (s instanceof IdentityStmt) {
				/* Cut the caughtExceptionRef */
				IdentityStmt id = (IdentityStmt) s;
				Value rightOp = id.getRightOp();
				if(rightOp instanceof CaughtExceptionRef == false) {
					IdentityStmt identity = StmtCreator.createIdentity(s, b, counterParameter);
					cloneMap.put(s, identity);
					generatedUnits.add(identity);
					if(rightOp instanceof ParameterRef) {
						counterParameter++;
					}
				}
			}
			/* Go to */
			else if(s instanceof GotoStmt) {
				GotoStmt go = StmtCreator.createGoToStmt(s, b);
				cloneMap.put(s, go);
				generatedUnits.add(go);
			}
			/* if */
			else if (s instanceof IfStmt) {
				IfStmt ifst = StmtCreator.createIfStmt(s, b);
				cloneMap.put(s, ifst);
				generatedUnits.add(ifst);
			}
			/* invoke */
			else if (s instanceof InvokeStmt) {
				InvokeStmt invoke = StmtCreator.createInvokeStmt(s, b); 
				cloneMap.put(s, invoke);
				generatedUnits.add(invoke);
			}
			/* switch */
			else if (s instanceof SwitchStmt) {
				SwitchStmt switchSt = StmtCreator.createSwitchStmt(s, b);
				cloneMap.put(s, switchSt);
				generatedUnits.add(switchSt);
			}
			/* Monitor */
			else if (s instanceof MonitorStmt) {
				MonitorStmt monitor = StmtCreator.createMonitorStmt(s, b);
				cloneMap.put(s, monitor);
				generatedUnits.add(monitor);
			}
			/* return */
			else if (s instanceof ReturnStmt) {
				ReturnVoidStmt ret = Jimple.v().newReturnVoidStmt();
				cloneMap.put(s, ret);
				generatedUnits.add(ret);
			}
			/* throw */
			else if (s instanceof ThrowStmt) {
				ThrowStmt throwSt = StmtCreator.createThrowStmt(s, b);
				cloneMap.put(s, throwSt);
				generatedUnits.add(throwSt);
			}
			/* Breakpoint */
			else if(s instanceof BreakpointStmt) {
				cloneMap.put(s, Jimple.v().newBreakpointStmt());
				generatedUnits.add(Jimple.v().newBreakpointStmt());
			}
			/* Nop */
			else if(s instanceof NopStmt) {
				cloneMap.put(s, Jimple.v().newNopStmt());
				generatedUnits.add(StmtCreator.createNopStmt());
			}
		}
		units.addAll(generatedUnits);
		return cloneMap;
	}
	
	/**
	 * Solve targets
	 * @param b, Jimple Body
	 * @param cloneMap, map of <Stmt,Stmt>
	 */
	private static void solveTargets(JimpleBody b, Map<Stmt,Stmt> cloneMap) {
		ReturnVoidStmt retNull = Jimple.v().newReturnVoidStmt();
		b.getUnits().add(retNull);
		for(Unit u : b.getUnits()) {
			Stmt s = (Stmt) u;
			/* IfStmt */
			if(s instanceof IfStmt) {
				/* Getting the target */
				IfStmt st = (IfStmt) s;
				Stmt target = st.getTarget();
				/* Checking if the target is in the ifBlock Map */
				if(cloneMap.containsKey(target)) {
					st.setTarget(cloneMap.get(target));
				} else {
					st.setTarget(retNull);
				}
			/* TableSwitchStmt */
			} else if(s instanceof TableSwitchStmt) {
				/* Getting the default target */
				TableSwitchStmt st = (TableSwitchStmt) s;
				Stmt defaultTarget = (Stmt) st.getDefaultTarget();
				/* Checking if the default target is in the ifBlock Map */
				if(cloneMap.containsKey(defaultTarget)) {
					st.setDefaultTarget(cloneMap.get(defaultTarget));
				} else {
					st.setDefaultTarget(retNull);
				}
				/* Solving all the targets */
				int min = st.getLowIndex();
				int max = st.getHighIndex();
				for(int i = min; i<=max;i++) {
					Stmt target = (Stmt) st.getTarget(i);
					if(cloneMap.containsKey(target)) {
						st.setTarget(i, cloneMap.get(target));
					} else {
						st.setTarget(i, retNull);
					}
				}
			/* LookUpSwitchStmt */
			} else if(s instanceof LookupSwitchStmt) {
				/* Getting the default target */
				LookupSwitchStmt st = (LookupSwitchStmt) s;
				Stmt defaultTarget = (Stmt) st.getDefaultTarget();
				/* Checking if the default target is in the ifBlock Map */
				if(cloneMap.containsKey(defaultTarget)) {
					st.setDefaultTarget(cloneMap.get(defaultTarget));
				} else {
					st.setDefaultTarget(retNull);
				}
				/* Solving all the targets */
				int n = st.getTargetCount();
				for(int i=0; i <=n;i++) {
					Stmt target = (Stmt) st.getTarget(i);
					if(cloneMap.containsKey(target)) {
						st.setTarget(i, cloneMap.get(target));
					} else {
						st.setTarget(i, retNull);
					}
				}
			/* GotoStmt */
			} else if(s instanceof GotoStmt) {
				/* Getting the target */
				GotoStmt st = (GotoStmt) s;
				Unit target = st.getTarget();
				/* Checking if the target is in the ifBlock Map */
				if(cloneMap.containsKey(target)) {
					st.setTarget(cloneMap.get(target));
				} else {
					st.setTarget(retNull);
				}	
			}
		}
	}
}
