import ifIsolator.IfPackage;
import soot.Body;
import soot.Local;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.IdentityStmt;
import soot.jimple.IfStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Stmt;
import soot.util.Chain;
import utils.utils;

public class TestPrint {
	
	public static void test() {
		Chain<SootClass> appClasses = Scene.v().getApplicationClasses();
		for (SootClass c : appClasses) {
			if (!(utils.isSystemClass(c.getName()))) {
				System.out.println();
				System.out.println("*** " + c.getName() + " ***");
				for (SootMethod m : c.getMethods()) {
					if (m.isConcrete()) {
						Body b = m.retrieveActiveBody();
						for (Unit u : b.getUnits()) {
							Stmt s = (Stmt) u;
							if(s.containsFieldRef()) {
								System.out.println(s + " a pour ref : " + s.getFieldRef());
							}
							if(s.containsArrayRef()) {
								//System.out.println(s + "a un array ref : " + s.getArrayRef());
							}
							if(s.containsInvokeExpr()) {
								//System.out.println(s + " a un invoke : " + s.getInvokeExpr());
							}
							//printCible(s);
						}
					}
				}
			}
		}
	}
		
	public static void printCible(Stmt s) {
		if (s instanceof IdentityStmt) {
			//System.out.println("---------");
			//System.out.println(s);
			IdentityStmt st = (IdentityStmt) s;
			Value leftOp  = st.getLeftOp();
			Value rightOp = (Value) st.getRightOp().clone();
			
			//System.out.println("Partie gauche : " + leftOp);
			//System.out.println("Type Gauche : " + leftOp.getClass());
			//System.out.println("Partie droite : " + rightOp);
			//System.out.println("Type Droite : " + rightOp.getClass());
			//System.out.println("FieldRef -> " + st.containsFieldRef());
			//System.out.println("InvokeExpr -> " + st.containsInvokeExpr());
			//System.out.println("ArrayRef -> " + st.containsArrayRef());
			//ThisRef, ParameterRef, CaughExceptionRef
		} else if (s instanceof InvokeStmt) {
			InvokeStmt st = (InvokeStmt) s;
			InvokeExpr expr = st.getInvokeExpr();
			System.out.println("---------");
			System.out.println(s);
			
			System.out.println("Statement : " + st);
			System.out.println("Expression : " + expr);
		}
	}
}
