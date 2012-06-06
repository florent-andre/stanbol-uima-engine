= Build dependencies

* UIMA
 * get uima engines dependencies : 
 * svn co http://svn.apache.org/repos/asf/uima/sandbox/trunk/ sandbox
  * Last Changed Rev tested: 1056535 
 * cd sandbox/aggregate-addons
 * mvn install -Dmaven.test.skip=true
   * skip test because : https://issues.apache.org/jira/browse/UIMA-2003

* Clerezza
 * Last Changed Rev tested: 993306
 * mvn install -Dmaven.test.skip=true

= Add uima modules to the stanbol server

* modifity this file : fise/launchers/full/src/main/bundles/list.xml 
* in <!-- FISE Enhancement Engines -->
	<startLevel level="30">
* add :
<bundle>
      <groupId>org.apache.stanbol</groupId>
      <artifactId>org.apache.stanbol.enhancer.engines.uimaservice</artifactId>
      <version>0.9.0-incubating-SNAPSHOT</version>
    </bundle>
    <bundle>
      <groupId>org.apache.stanbol</groupId>
      <artifactId>org.apache.stanbol.enhancer.engines.uima.gasoil</artifactId>
      <version>0.9.0-incubating-SNAPSHOT</version>
    </bundle>

= Curl test
* cd dev-material
* curl -X POST -H "Accept: text/turtle" -H "Content-type: text/plain" -F "data=@document" http://localhost:8080/engines

= Publish module direclty on running server

* mvn install -o -DskipTests -PinstallBundle -Dsling.url=http://localhost:8080/system/console

