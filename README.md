# ifExtractor
ifExtractor is a extractor of Logical Bomb located inside Andoird Application.
This extraction is made with static analysis of Jimple Bytecode and instrumentation of this same Jimple Bytecode.
Written in Java and using [FlowDroid](https://github.com/secure-software-engineering/FlowDroid) , this library is Open-Source.

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
The extractor is represented by a Java Object.  
This object contains a list of If Statement and some methods to add the statement to the list and generate the new APK.  
The generation method takes in input : If Statements in Jimple  
It produce in output a new APK.  
  
Every input statement will generate a ifMethod containing the ifBlock Statements and the dependencies.  
All of the ifMethods are contains inside the ifClass.  
Model of IfClass in Jimple syntax:
<pre>
public void ifClass extends MainActivity {  
  
  void < init >()
    {
        ifClass r0;

        r0 := @this: ifClass;

        specialinvoke r0.< android.app.Activity: void < init >() >();

        return;
    }
    
  public ifMethod1(...) {  
    /* Contains if Block of the LogicBomb_1 */  
  } 
  
  ...  
    
  public ifMethodN(...) {  
    /* Contains if Block of the LogicBomb_n */  
  }  
    
}  
</pre>  
  
Once all of the methods are created, ifExtractor is parsing the AndroidManifest.xml and looking for the MainActivity. Then, the onCreate method of the MainActivity is delete and replace a new one containing calls of the ifMethods.  
Model of the OnCreate in Jimple syntax:  
<pre>
public class MainActivity extends ... {  
  
  ...
  
  public final void onCreate(android.os.Bundle)
    {
        android.os.Bundle $r1;
        ifClass r0;

        r0 := @this: ifClass;

        $r1 := @parameter0: android.os.Bundle;

        virtualinvoke $r1.< ifClass: void ifMethod1() >();

        ...

        virtualinvoke $r1.< ifClass: void ifMethodN() >();

        return;
    }
  
  }
</pre>
Finally, the apk is generated. When it's going to be executed, all the LogicalBomb are executed in priority to observe the behaviour.  
  
## How to use it
Let's see the methods of the ifExtractor object.  
<pre>
// Constructor with the paths of Android Platforms, apk and output folder
IfExtractor example = new IfExtractor(androidPath, apkPath, outputPath);
// Add the IfStmt to the ifExtractor
example.addLogicBomb(ifStmt);
example.addLogicBombs(listOfIfStmt);
// Call the method to generate the apk
example.generateApk();
</pre>

## Built With

* [Maven](https://maven.apache.org/) - Dependency Management

## Authors

* **[François JULLION](https://github.com/Franciscocoo)**
