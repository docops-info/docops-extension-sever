rm -R application
java -Djarmode=tools -jar target/docops-extension-server-2024.00.jar extract --destination application

java -XX:ArchiveClassesAtExit=application.jsa -Dspring.context.exit=onRefresh -jar application/docops-extension-server-2024.00.jar

java -XX:SharedArchiveFile=application.jsa -jar application/docops-extension-server-2024.00.jar