package lu.uni.trux.IfExtractor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.xmlpull.v1.XmlPullParserException;

import lu.uni.trux.IfExtractor.apkGenerator.DependenciesSolver;
import lu.uni.trux.IfExtractor.apkGenerator.MethodCreator;
import lu.uni.trux.IfExtractor.ifIsolator.IfManager;
import lu.uni.trux.IfExtractor.ifIsolator.IfPackage;
import lu.uni.trux.IfExtractor.utils.Utils;
import soot.Body;
import soot.Local;
import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.IfStmt;
import soot.jimple.Stmt;
import soot.jimple.infoflow.android.axml.AXmlNode;
import soot.jimple.infoflow.android.manifest.ProcessManifest;

public class IfExtractor {
	
	private String dirApk;
	
	/* */
	private List<IfStmt> logicBombs;
	
	public IfExtractor(String dirAnd, String dirApk, String dirOut) {
		/* CHECK si les path sont bon */
		logicBombs = new ArrayList<IfStmt>();
		this.dirApk = dirApk;
		Utils.initSoot(dirAnd, dirApk, dirOut);
	}
	
	protected List<IfStmt> getLogicBombs() {
		return this.logicBombs;
	}
	
	/**
	 * 
	 * @param lb
	 */
	protected void addLogicBomb(IfStmt lb) {
		this.logicBombs.add(lb);
	}
	
	/**
	 * 
	 * @param lbs
	 */
	protected void addLogicBombs(List<IfStmt> lbs) {
		for(IfStmt lb : lbs) {
			this.logicBombs.add(lb);
		}
	}
	
	/**
	 * @throws XmlPullParserException 
	 * @throws IOException 
	 * 
	 */
	protected void generateApk() throws IOException, XmlPullParserException {
		/* Creating new Class */
		SootClass ifClass = new SootClass("ifClass");
		Scene.v().addClass(ifClass);
		SootClass activity = Scene.v().loadClassAndSupport("android.app.Activity");
		ifClass.setSuperclass(activity);
		
		/* Creating init<>() */
		MethodCreator.createInitMethod(ifClass);
		
		/* Creating all IfMethods */
		// Get all ifPackage
		List<IfPackage> ifPackageList = new ArrayList<IfPackage>();
		for(IfStmt lb : this.logicBombs) {
			ifPackageList.add(IfManager.getIf(lb));
		}
		// Create ifMethod one by one based on the ifPackage
		int n = 0;
		Body oldBody;
		List<Stmt> stmtBlock;
		SootMethod m;
		SootClass c;
		for(IfPackage ifp : ifPackageList) {
			oldBody = ifp.getBody();
			stmtBlock = ifp.getBlock();
			c = ifp.getClasse();
			m = ifp.getMethod();
			n++;
			// Organisation du if block
			List<Stmt> ifStmt = Utils.orderList(oldBody, stmtBlock);
			// Coupage du code après le if (Partie Haute)
			List<Stmt> blockToAnalyse = Utils.cutBottomBody(oldBody.getUnits(), ifStmt);
			// Solving intra-procedural dependencies
			List<Stmt> newStmtList = DependenciesSolver.getNewStmtBody(m, oldBody, ifStmt, blockToAnalyse, c);
			// Getting Local from Method
			Set<Local> newLocalSet = DependenciesSolver.getLocalIfBlock(newStmtList, oldBody.getLocals());
			// Create the ifMethod_n
			MethodCreator.createIfMethod(ifClass, newLocalSet, newStmtList, n);
		}
		
		/* Creating onCreate() */
		MethodCreator.createOnCreateMethod(ifClass,n);
		
		modifyManifest();
		
		/* Write the apk */
		//PackManager.v().writeOutput();
	}
	
	private void modifyManifest() throws IOException, XmlPullParserException {
		ProcessManifest androidManifest = new ProcessManifest(this.dirApk);
		System.out.println("***********************");
		for(AXmlNode act : androidManifest.getActivities()) {
			//Parcourir les attributes
			
			//Chercher le intentfilter MAIN
			//L'enlever
			//Rajouter une activité et mettre l'intentfilterMAIN
			System.out.println(act.getTag());
			System.out.println(act.getNamespace());
			System.out.println(act.getParent());
			System.out.println(act.getAttributes());
		}
		//androidManifest.addActivity(node);
		System.out.println("***********************");
	}

}
