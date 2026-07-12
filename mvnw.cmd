@ECHO OFF
SETLOCAL
SET WRAPPER_JAR=%~dp0.mvn\wrapper\maven-wrapper.jar
IF NOT EXIST "%WRAPPER_JAR%" (
  powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -UseBasicParsing 'https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar' -OutFile '%WRAPPER_JAR%'"
)
"%JAVA_HOME%\bin\java.exe" -classpath "%WRAPPER_JAR%" -Dmaven.multiModuleProjectDirectory="%~dp0" org.apache.maven.wrapper.MavenWrapperMain %*
