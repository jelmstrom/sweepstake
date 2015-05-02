FROM dockerfile/java:oracle-java8
MAINTAINER johan.elmstrom@aptitud.se
ADD target/vmtips-*.war /vmtips.war
EXPOSE 8085
EXPOSE 8086
CMD java -jar /vmtips.war
VOLUME ["/data/neo4j/vmtips"]