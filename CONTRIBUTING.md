# How to participate in this repository
### Java SDK version
```
openjdk 11.0.1 2018-10-16
OpenJDK Runtime Environment 18.9 (build 11.0.1+13)
OpenJDK 64-Bit Server VM 18.9 (build 11.0.1+13, mixed mode)
```
[JDK 11.0.1 General-Availability Release](https://jdk.java.net/11/)
### Cloning the project
* Start IntelliJ
* On the 'Welcome' screen: Check out from Version Control -> Git
    * URL: `https://github.com/HetBenkt/DQL-Tester-FX.git`
    * Directory: Your own local location
* Hit 'Clone'
* Hit 'Yes' to create an IntelliJ project
* On the 'Import Project' screen: Import project from external model -> Maven
* Click Next (keep the default settings on the 'Root directory' screen)
* Click Next (keep the default settings on the 'Select Maven projects' screen)
* Click Next (keep the default settings on the 'Project name' screen)
* Click Finish -> The project will be created now
### IntelliJ plugins that are used
* Start IntelliJ
* Open 'File' > 'Settings'
* Go to the 'Plugins' section
* Download/install and enable these extra plugins
    * Coverage
    * Emma
    * SonarLint
### Non public dependencies (Documentum Foundation Classes)
Documentum applications are required to use DFC libraries. These can be downloaded from [OpenText My Support](https://mysupport.opentext.com)
The pom.xml refers to these libraries from your local .m2 maven repository. To install external/downloaded jar dependencies to your local maven repository use the commands from the Wiki page [Maven commands](https://github.com/HetBenkt/DQL-Tester-FX/wiki/Maven-commands)
### Run the application
* Add 2 run configurations (Run -> Edit Configurations...)

Run configuration 'Main':
> * Hit the '+' sign to add a new 'Application'
> * Give it a name: Main
> * Give it a main class: nl.bos.Main
> * VM Options: --module-path "c:\javafx-sdk-11.0.1\lib" --add-modules=javafx.controls,javafx.fxml

Run configuration 'Main (dev-mode)':

> * Hit the '+' sign to add a new 'Application'
> * Give it a name: Main (dev-mode)
> * Give it a main class: nl.bos.Main
> * VM Options: --module-path "c:\javafx-sdk-11.0.1\lib" --add-modules=javafx.controls,javafx.fxml
> * Give it the program arguments: REPOSITORY_NAME USERNAME PASSWORD

Create 2 new property files
* A file called `dfc.properties` in the `src\main\resources` directory with this content
    ```
    dfc.docbroker.host[0]=<host; hostname or ip-address>
    dfc.docbroker.port[0]=<port; default 1489>
    dfc.globalregistry.repository=<repository_name>
    dfc.globalregistry.password=<dm_bof_registry (encrypted) password>
    dfc.globalregistry.username=dm_bof_registry
    dfc.session.secure_connect_default=native
    dfc.date_format=dd/mm/yyyy hh:mi:ss
    ```
* A new file called `log4j.properties` with this content in the `src\main\resources` directory
    ```
    log4j.rootCategory=INFO, A1, F1
    log4j.category.MUTE=OFF
    log4j.additivity.tracing=false
    
    #------------------- CONSOLE --------------------------
    log4j.appender.A1=org.apache.log4j.ConsoleAppender
    log4j.appender.A1.threshold=ERROR
    log4j.appender.A1.layout=org.apache.log4j.PatternLayout
    log4j.appender.A1.layout.ConversionPattern=%d{ABSOLUTE} %5p [%t] %c - %m%n
    
    #------------------- FILE --------------------------
    log4j.appender.F1=org.apache.log4j.RollingFileAppender
    log4j.appender.F1.File=C\:/Documentum/logs/log4j.log
    log4j.appender.F1.MaxFileSize=10MB
    log4j.appender.F1.layout=org.apache.log4j.PatternLayout
    log4j.appender.F1.layout.ConversionPattern=%d{ABSOLUTE} %5p [%t] %c - %m%n
    ```
	
Now the application can be run, changed, committed (and pushed) and repackaged with Maven!
### Code coverage
Make sure the 'Coverage' and 'Emmo' plugins are enabled
* Add a run configurations (Run -> Edit Configurations...)

> * Hit the '+' sign to add a new 'JUnit'
> * Give it a name: All in project
> * Leave the Package empty
> * Search for tests: In whole project
### Issues
* Can be raised in [Issues](https://github.com/HetBenkt/DQL-Tester-FX/issues) 
### Features
* Can be logged in the project [User stories](https://github.com/HetBenkt/DQL-Tester-FX/projects/1)
    * Make sure to make a good description so others know what to be done
### Wiki
* [Wiki](https://github.com/HetBenkt/DQL-Tester-FX/wiki) 
### Communication channels
* An e-mail can be sent to [DQLTesterFX@gmail.com](mailto:DQLTesterFX@gmail.com)
