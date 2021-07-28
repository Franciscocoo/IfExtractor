package ifIsolator;

import java.util.ArrayList;
import java.util.List;

import soot.Body;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.IdentityStmt;
import soot.jimple.IfStmt;
import soot.jimple.Stmt;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.SimpleDominatorsFinder;
import soot.toolkits.graph.UnitGraph;
import soot.util.Chain;
import utils.utils;

public class ifManager {

	/*  */
	private static String directory = System.getProperty("user.dir");
	private static String dirOutput = directory + "/output";

	public static IfPackage getIf() {
		Chain<SootClass> appClasses = Scene.v().getApplicationClasses();
		utils.saveJimple(appClasses, dirOutput);
		for (SootClass c : appClasses) {
			if (!(utils.isSystemClass(c.getName()))) {
				for (SootMethod m : c.getMethods()) {
					if (m.isConcrete()) {
						Body b = m.retrieveActiveBody();
						for (Unit u : b.getUnits()) {
							Stmt s = (Stmt) u;
							if (s instanceof IfStmt) {
								IfPackage res = new IfPackage(m, b, isolate(m, (IfStmt) s), c);
								return res;
							}
						}
					}
				}
			}
		}
		return null;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static List<Stmt> isolate(SootMethod m, IfStmt stmt) {
		/* Getting Data and graph from Method m */
		Body b = m.retrieveActiveBody();
		UnitGraph ug = new ExceptionalUnitGraph(b);
		SimpleDominatorsFinder sdf = new SimpleDominatorsFinder(ug);
		DirectedGraph dg = sdf.getGraph();
		/* Getting the statement of right and left branch */
		Stmt left = (Stmt) dg.getSuccsOf(stmt).get(0);
		Stmt right = (Stmt) dg.getSuccsOf(stmt).get(1);
		List<Stmt> branchLeft = new ArrayList<Stmt>();
		branchLeft = getBranch(dg, branchLeft, left);
		List<Stmt> branchRight = new ArrayList<Stmt>();
		branchRight = getBranch(dg, branchRight, right);
		List<Stmt> union = new ArrayList<Stmt>(branchLeft);
		union.addAll(branchRight);
		List<Stmt> intersection = new ArrayList<Stmt>(branchLeft);
		intersection.retainAll(branchRight);
		List<Stmt> res = new ArrayList<Stmt>(union);
		res.removeAll(intersection);
		return res;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static List<Stmt> getBranch(DirectedGraph dg, List<Stmt> l, Stmt s) {
		l.add(s);
		for (int i = 0; i < dg.getSuccsOf(s).size(); i++) {
			Stmt succ = (Stmt) dg.getSuccsOf(s).get(i);
			if (!l.contains(succ)) {
				getBranch(dg, l, succ);
			}
		}
		return l;
	}
}
