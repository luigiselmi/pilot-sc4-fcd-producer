# Dockerfile for the SC4 Pilot Floating Car Data producer 
#
# 1) Build an image using this docker file. Run the following docker command
#
#    $ docker build -t bde2020/pilot-sc4-fcd-producer:v0.0.1 .
#
# 2) Start a container 
#
#    $ docker run -d --network pilot-sc4 --name fcd-producer -v /home/luigi/projects/fraunhofer/bde/certh-data/taxi_gps_10000000.gz:/data/fcd-dataset1.gz  bde2020/pilot-sc4-fcd-producer:v0.0.1 

FROM bde2020/flink-maven-template:latest

MAINTAINER Luigi Selmi <luigiselmi@gmail.com>

ENV FLINK_APPLICATION_JAR_NAME pilot-sc4-fcd-applications-0.1-jar-with-dependencies.jar
ENV FLINK_APPLICATION_MAIN_CLASS eu.bde.pilot.sc4.fcd.FlinkFcdProducer
ENV FLINK_APPLICATION_ARGS "--path /data/fcd-dataset1.gz --topic taxi"
