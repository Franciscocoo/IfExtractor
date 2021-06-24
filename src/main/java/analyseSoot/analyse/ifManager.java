package analyseSoot.analyse;

import analyseSoot.utils.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import soot.Body;
import soot.PackManager;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.Unit;
import soot.jimple.IfStmt;
import soot.jimple.Stmt;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.SimpleDominatorsFinder;
import soot.toolkits.graph.UnitGraph;
import soot.util.Chain;

public class ifManager {

	/* */
	static SootMethod methodToIsolate;
	static List<Stmt> codeToIsolate;
	
	
	public static void getIf() {
		PackManager.v().getPack("wjtp").add(new Transform("wjtp.myTransform", new SceneTransformer() {
			@Override
			protected void internalTransform(String phaseName, Map<String, String> options) {
				Chain<SootClass> appClasses = Scene.v().getApplicationClasses();
				for(SootClass c : appClasses) {
					if(!(utils.isSystemClass(c.getName()))) {
						//System.out.println(c.getName());
						for(SootMethod m : c.getMethods()) {
							if(m.isConcrete()) {
								Body b = m.retrieveActiveBody();
								//System.out.println(b);
								for(Unit u : b.getUnits()) {
									Stmt s = (Stmt) u;
									if(s instanceof IfStmt) {
										//System.out.println(c);
										//System.out.println(m.getSignature());
										methodToIsolate = m;
										codeToIsolate = isolate(m, (IfStmt)s);
										return;
									}
								}
							}
						}
					}
				}
			}
		}));
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static List<Stmt> isolate(SootMethod m, IfStmt stmt) {
		/* Getting Data and graph from Method m*/
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
		/*System.out.println("Branch left");
		System.out.println(branchLeft);
		System.out.println("Branch Right");
		System.out.println(branchRight);*/
		List<Stmt> union = new ArrayList<Stmt>(branchLeft);
		union.addAll(branchRight);
		List<Stmt> intersection = new ArrayList<Stmt>(branchLeft);
		intersection.retainAll(branchRight);
		List<Stmt> res = new ArrayList<Stmt>(union);
		res.removeAll(intersection);
		//System.out.println("IF");
		//System.out.println(res);
		return res;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static List<Stmt> getBranch(DirectedGraph dg, List<Stmt> l, Stmt s) {
		l.add(s);
		for(int i=0;i < dg.getSuccsOf(s).size();i++) {
			Stmt succ = (Stmt) dg.getSuccsOf(s).get(i);
			if(!l.contains(succ)) {
				getBranch(dg, l, succ);
			}
		}
		return l;
	}
}
