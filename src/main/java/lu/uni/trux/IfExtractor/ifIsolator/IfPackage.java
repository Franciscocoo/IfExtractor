package lu.uni.trux.IfExtractor.ifIsolator;

import java.util.List;

import soot.Body;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.Stmt;

/**
 * Object which contains all importants informations for the ifBlock
 * @author François JULLION
 */
public class IfPackage {
	
	/*
	 * SootMethod which contains the ifBlock
	 */
	private SootMethod method;
	
	/*
	 * Body which contains the ifBlock
	 */
	private Body body;
	
	/*
	 * List<Stmt> which represents the IfBlock
	 */
	private List<Stmt> block;
	
	/*
	 * SootClass which contains the SootMethod of the ifBlock
	 */
	private SootClass classe;
	
	public IfPackage(SootMethod m, Body b, List<Stmt> bl, SootClass c) {
		this.method = m;
		this.body = b;
		this.block = bl;
		this.classe = c;
	}

	/**
	 * Get the Method
	 * @return SootMethod
	 */
	public SootMethod getMethod() {
		return method;
	}

	/**
	 * Set the Method
	 * @param method, SootMethod
	 */
	public void setMethod(SootMethod method) {
		this.method = method;
	}

	/**
	 * Get the Body
	 * @return Body
	 */
	public Body getBody() {
		return body;
	}

	/**
	 * Set the Body
	 * @param body, Body
	 */
	public void setBody(Body body) {
		this.body = body;
	}

	/**
	 * Get ifBlock
	 * @return List<Stmt>
	 */
	public List<Stmt> getBlock() {
		return block;
	}

	/**
	 * Set ifBlock
	 * @param block, List<Stmt>
	 */
	public void setBlock(List<Stmt> block) {
		this.block = block;
	}

	/**
	 * Get Classe
	 * @return SootClass
	 */
	public SootClass getClasse() {
		return classe;
	}

	/**
	 * Set Classe
	 * @param classe, SootClass
	 */
	public void setClasse(SootClass classe) {
		this.classe = classe;
	}
	
	
}
