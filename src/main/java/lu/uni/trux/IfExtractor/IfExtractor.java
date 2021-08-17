package lu.uni.trux.IfExtractor;

import java.io.IOException;

import java.util.logging.Logger;

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
import soot.Modifier;
import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.IfStmt;
import soot.jimple.Stmt;
import soot.jimple.infoflow.android.axml.AXmlNode;
import soot.jimple.infoflow.android.manifest.ProcessManifest;

/**
 * the IfExtractor object contains all Logical Bombs in a list and has methods to process the isolation and extraction into apk
 * @author François JULLION
 */
public class IfExtractor {
	
	/* Path of the Apk use as Input */
	private String dirApk;
	
	/* List of IfStmt containing logical Bombs */
	private List<IfStmt> logicBombs;
	
	/*  */
	private Logger log;
	
	/**
	 * Constructor of IfExtractor
	 * @param dirAnd, Path of Android environment
	 * @param dirApk, Path of the APK
	 * @param dirOut, Path of the output folder
	 */
	public IfExtractor(String dirAnd, String dirApk, String dirOut) {
		log = Logger.getLogger(this.getClass().getName());
		log.info("Starting ifExtractor");
		logicBombs = new ArrayList<IfStmt>();
		this.dirApk = dirApk;
		Utils.initSoot(dirAnd, dirApk, dirOut);
	}
	
	/**
	 * Get all logical bombs of the IfExtractor
	 * @return list of IfStmt
	 */
	protected List<IfStmt> getLogicBombs() {
		return this.logicBombs;
	}
	
	/**
	 * Adding a Logical Bomb in the list
	 * @param lb, IFStmt representing a logical Bomb
	 */
	protected void addLogicBomb(IfStmt lb) {
		this.logicBombs.add(lb);
	}
	
	/**
	 * Adding Logical Bombs in the list
	 * @param lbs, List of IfStmt representing Logical Bombs
	 */
	protected void addLogicBombs(List<IfStmt> lbs) {
		for(IfStmt lb : lbs) {
			this.logicBombs.add(lb);
		}
	}
	
	/**
	 * Generate a new apk using the apk put in input and the list of Logical Bomb contained in the IfExtractor Object
	 * @throws XmlPullParserException 
	 * @throws IOException 
	 */
	protected void generateApk() throws IOException, XmlPullParserException {
		/* Creating new Class */
		SootClass ifClass = new SootClass("ifClass", Modifier.PUBLIC);
		SootClass activity = Scene.v().loadClassAndSupport("android.app.Activity");
		ifClass.setSuperclass(activity);
		Scene.v().addClass(ifClass);
		ifClass.setApplicationClass();

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
			// Organize the ifBlock
			List<Stmt> ifStmt = Utils.orderList(oldBody, stmtBlock);
			// Cutting the bottom part of the code
			List<Stmt> blockToAnalyse = Utils.cutBottomBody(oldBody.getUnits(), ifStmt);
			// Solving intra-procedural dependencies
			List<Stmt> newStmtList = DependenciesSolver.getNewStmtBody(m, oldBody, ifStmt, blockToAnalyse, c);
			// Getting Local from Method
			Set<Local> newLocalSet = DependenciesSolver.getLocalIfBlock(newStmtList, oldBody.getLocals());
			// Create the ifMethod_n
			MethodCreator.createIfMethod(ifClass, newLocalSet, newStmtList, n);
		}
		
		/* Manifest editing */
		String mainActivity = getMainActivity();
		
		/* Getting the class of the mainActivity */
		SootClass mainActivityClass = null;
		for(SootClass cl : Scene.v().getClasses()) {
			if(cl.getName().toString().equals(mainActivity)) {
				mainActivityClass = cl;
			}
		}
		
		/* modify onCreate() */
		MethodCreator.modifyOnCreateMethod(mainActivityClass,ifClass,n);
		
		/* Write the apk */
		PackManager.v().writeOutput();
	}
	
	/**
	 * Modify the Android Manifest, delete the launch activity and add a new launch activity as the onCreate of the ifClass
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	private String getMainActivity() throws IOException, XmlPullParserException {
		ProcessManifest obj = new ProcessManifest(this.dirApk);
		String res = "";
		for(AXmlNode act : obj.getActivities()) {
			for(AXmlNode filters : act.getChildren()) {
				List<AXmlNode> action = filters.getChildrenWithTag("action");
				List<AXmlNode> category = filters.getChildrenWithTag("category");
				if(action.size() == 1 && category.size()==1) {
					AXmlNode actionNode = action.get(0);
					AXmlNode categoryNode = category.get(0);
					String val1 = actionNode.getAttribute("name").toString();
					String val2 = categoryNode.getAttribute("name").toString();
					if(val1.equals("name=\"android.intent.action.MAIN\"") && val2.equals("name=\"android.intent.category.LAUNCHER\"")) {
						res = act.getAttribute("name").getValue().toString();
					}
				}
			}
		}
		obj.close();
		return res;
	}

}
