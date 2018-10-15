Nexus Maven Repo import/export Spring Boot APP
==============================================

Actions:
import - import Maven Artifacts into Nexus
export - export Maven Artifacts from Nexus


Parameters: 

maven.directory  
nexus.url  
nexus.repository  
nexus.username  
nexus.password  
debug:false  


Examples:

java -jar nexus-upload-app/build/libs/nexus-upload-app.jar import --maven.directory=~/.m2/repositor --nexus.url=http://my-nexus/service/rest --nexus.repository=maven-public --nexus.username=admin --nexus.password=admin123

java -jar nexus-upload-app/build/libs/nexus-upload-app.jar export --maven.directory=~/m2-repository --nexus.url=http://my-nexus/service/rest --nexus.repository=maven-public --nexus.username=admin --nexus.password=admin123