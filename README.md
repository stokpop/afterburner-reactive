# Afterburner Reactive

Build:

     ./mvnw clean package

Run:

     java -Dserver.port=8080 -Dspring.application.name=AfterburnerReactiveOne -jar target/afterburner-reactive-0.0.1-SNAPSHOT.jar

Example call:

     curl http://localhost:8080/delay?duration=2000
