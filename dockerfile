FROM dockerfile/java:oracle-java8
MAINTAINER johan.elmstrom@aptitud.se
ADD target/vmtips-1.2.0.war /vmtips.war
EXPOSE 8085
CMD java -jar /vmtips.war