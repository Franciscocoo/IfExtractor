package lu.uni.trux.IfExtractor.ifIsolator;

import java.util.List;

import soot.Body;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.Stmt;

public class IfPackage {
	
	protected SootMethod method;
	protected Body body;
	protected List<Stmt> block;
	protected SootClass classe;
	
	public IfPackage(SootMethod m, Body b, List<Stmt> bl, SootClass c) {
		this.method = m;
		this.body = b;
		this.block = bl;
		this.classe = c;
	}

	public SootMethod getMethod() {
		return method;
	}

	public void setMethod(SootMethod method) {
		this.method = method;
	}

	public Body getBody() {
		return body;
	}

	public void setBody(Body body) {
		this.body = body;
	}

	public List<Stmt> getBlock() {
		return block;
	}

	public void setBlock(List<Stmt> block) {
		this.block = block;
	}

	public SootClass getClasse() {
		return classe;
	}

	public void setClasse(SootClass classe) {
		this.classe = classe;
	}
	
	
}
