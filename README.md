# ifExtractor
ifExtractor is a Open-Source librairy, made in Java.  
The goal of ifExtractor is to isolate and extract logical bomb in Android Malware.

## Getting Started

### Download
You can download the release [here](https://google.com).

### Install
* If you are using a IDE, you just have to had the file to the library in .
Exemple : 

* If you are using a CLI, add this option when you build your project :
<pre>
-classpath path/to/ifExtractor.jar
</pre>

## How it works
ifExtractor take in input the Soot Stmt as IfStmt.
In output, ifExtractor will give an apk containing the "ifClass", containing itself n ifMethod as n Logical Bomb put in input.

## Built With

* [Maven](https://maven.apache.org/) - Dependency Management

## Authors

* **[François JULLION](https://github.com/Franciscocoo)**
