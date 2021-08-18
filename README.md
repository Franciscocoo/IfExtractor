# ifExtractor
ifExtractor is a Open-Source librairy, made in Java.  
The goal of ifExtractor is to isolate and extract logical bomb in Android Malware.

## Getting Started

### Download
You can download the release [here](https://google.com).

### Install
* If you are using a IDE, you just have to had the library to the Java Build Path. 

* If you are using a CLI, add this option when you build your project :
<pre>
-classpath path/to/ifExtractor.jar
</pre>

## How it works
ifExtractor take in input the Soot Stmt as IfStmt and produce in output a new apk.
Here a example of code :  
<pre>
/* Create the ifExtractor with the path of the android platforms, the path of the apk to analyse and the path of the output folder */
IfExtractor example = new IfExtractor(androidPath, apkPath, outputPath);
// INSERT TRAITEMENT OF APk
/* Add the IfStmt to the ifExtractor */
example.addLogicBomb(ifStmt);
example.addLogicBombs(listOfIfStmt);
/* Call the method to generate the apk */
example.generateApk();
</pre>

## Built With

* [Maven](https://maven.apache.org/) - Dependency Management

## Authors

* **[François JULLION](https://github.com/Franciscocoo)**
