package apkGenerator;

import java.util.ArrayList;
import java.util.List;

import soot.SootClass;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.FieldRef;
import soot.jimple.Stmt;

public class interDependencies {
	
	public static List<FieldRef> getRefs(List<Stmt> l, SootClass c) {
		List<FieldRef> res = new ArrayList<FieldRef>();
		for (Stmt s : l) {
			if (s instanceof AssignStmt) {
				Value v1 = ((AssignStmt) s).getLeftOp();
				Value v2 = ((AssignStmt) s).getRightOp();
				if (v1 instanceof FieldRef) {
					FieldRef rv1 = (FieldRef) v1;
					res.add(rv1);
				}
				if (v2 instanceof FieldRef) {
					FieldRef rv2 = (FieldRef) v2;
					res.add(rv2);
				}
			}
		}
		return res;
	}
}
