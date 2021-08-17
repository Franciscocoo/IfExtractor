package lu.uni.trux.IfExtractor.ifIsolator;

import java.util.ArrayList;
import java.util.List;

import lu.uni.trux.IfExtractor.utils.Utils;
import soot.Body;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.IfStmt;
import soot.jimple.Stmt;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.SimpleDominatorsFinder;
import soot.toolkits.graph.UnitGraph;
import soot.util.Chain;

/**
 * Provides methods to retrieve the IfStmt in the application and isolate the block of Stmt include in the block
 * Call as the ifBLock
 * @author François JULLION
 */
public class IfManager {

	/**
	 * Find the ifStmt into the Classes and return all the information of the IfBlock
	 * @return a IfPackage, containing all necessary informations about the IfStmt
	 */
	public static IfPackage getIf(IfStmt is) {
		Chain<SootClass> appClasses = Scene.v().getApplicationClasses();
		for (SootClass c : appClasses) {
			if (!(Utils.isSystemClass(c.getName()))) {
				for (SootMethod m : c.getMethods()) {
					if (m.isConcrete()) {
						Body b = m.retrieveActiveBody();
						for (Unit u : b.getUnits()) {
							Stmt s = (Stmt) u;
							if (s.equals(is)) {
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
	
	/**
	 * Isolate all the Stmt contains into the IfBlock from the rest of the Body
	 * @param m, SootMethod which contains the IfStmt
	 * @param stmt, the IfStmt
	 * @return a list of Stmt in the IfBlock
	 */
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
		// Left Union Right
		List<Stmt> union = new ArrayList<Stmt>(branchLeft);
		union.addAll(branchRight);
		// Left Inter Right
		List<Stmt> intersection = new ArrayList<Stmt>(branchLeft);
		// (Left Union Right) exclude (Left Inter Right)
		intersection.retainAll(branchRight);
		List<Stmt> res = new ArrayList<Stmt>(union);
		res.removeAll(intersection);
		return res;
	}

	/**
	 * Execute a DFS on a branch of the CFG
	 * @param dg, the DirectedGraph of Stmt
	 * @param l, the list of Stmt of the branch
	 * @param s, the actual stmt of the branch
	 * @return list of Stmt containing in the Branch
	 */
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
