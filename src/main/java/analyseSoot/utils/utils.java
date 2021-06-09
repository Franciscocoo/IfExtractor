package analyseSoot.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import soot.SootClass;
import soot.SootMethod;
import soot.Body;

public class utils {
	
	private static String directory = System.getProperty("user.dir");
	private static String repOutput = directory + "/output";
	
	public static void stringToJimple(SootClass cls) throws IOException{
		String filename = repOutput + "/" + cls.getName() + ".jmpl";
		FileWriter filewrite = new FileWriter(filename);
		PrintWriter printwrite = new PrintWriter(filewrite);
		for(SootMethod meth: cls.getMethods()) {
			Body b = meth.retrieveActiveBody();
			printwrite.write(b.toString());
		}
		printwrite.close();
	}
}
