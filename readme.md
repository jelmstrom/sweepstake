Vmtips

Build : mvn clean package

Run : java -jar target/vmtips-{version}.war

URL : 127.0.0.1:8080 (not localhost, bound to IP)

Prerequisites : MongoDB installed locally.


 Build dockerfile
 docker build -t "vmtips:1.2.0" .
 #run export port 8085 as 8080 on docker host
 docker run -p8080:8085 vmtips:1.2.0



