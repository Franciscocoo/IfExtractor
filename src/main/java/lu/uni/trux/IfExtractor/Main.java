package lu.uni.trux.IfExtractor;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import lu.uni.trux.IfExtractor.utils.*;
import soot.Body;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.IfStmt;
import soot.jimple.Stmt;
import soot.util.Chain;

/**
 * Test class to find Ifstmt and execute the ifExtractor
 * @author François JULLION
 */
public class Main {

	public static void main(String[] args) throws IOException, XmlPullParserException {

		String dirAndroid = "/home/student/Android-platforms/jars/stubs";

		String directory = System.getProperty("user.dir");
		String apkName = "instagram";
		String dirApk = directory + "/apk/" + apkName + ".apk";

		String dirOutput = directory + "/output";

		/* Cleaning Output Folder */
		final File[] files = (new File(dirOutput)).listFiles();
		if(files != null && files.length>0) {
			Arrays.asList(files).forEach(File::delete);
		}
		
		IfExtractor test = new IfExtractor(dirAndroid, dirApk, dirOutput);
		Chain<SootClass> appClasses = Scene.v().getApplicationClasses();
		List<IfStmt> l = new ArrayList<IfStmt>();
		for (SootClass c : appClasses) {
			if(c.getName().equals("com.instagram.ui.widget.emitter.PulseEmitter")) {
				if (!(Utils.isSystemClass(c.getName()))) {
					for (SootMethod m : c.getMethods()) {
						if (m.isConcrete()) {
							Body b = m.retrieveActiveBody();
							for (Unit u : b.getUnits()) {
								Stmt s = (Stmt) u;
								IfStmt is = null;
								if (s instanceof IfStmt) {
									is = (IfStmt)s;
									l.add(is);
									break;
								}
							}
						}
					}
				}
			}
		}		
		test.addLogicBombs(l);
		test.generateApk();
	}
}
