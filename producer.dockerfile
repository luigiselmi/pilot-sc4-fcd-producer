# Dockerfile for the SC4 Pilot Floating Car Data producer 
#
# 1) Build an image using this docker file. Run the following docker command
#
#    $ docker build -f producer.dockerfile -t bde2020/pilot-sc4-fcd-producer:v0.0.1 .
#
# 2) Start a container 
#
#    $ docker run -d --network pilot-sc4 --name fcd-producer -v /datasets/fcd-sample-data.csv.gz:/data/fcd-sample-data.csv.gz:ro  bde2020/pilot-sc4-fcd-producer:v0.0.1 

FROM bde2020/flink-maven-template:latest

MAINTAINER Luigi Selmi <luigiselmi@gmail.com>

ENV FLINK_APPLICATION_JAR_NAME pilot-sc4-fcd-applications-0.1-jar-with-dependencies.jar
ENV FLINK_APPLICATION_MAIN_CLASS eu.bde.pilot.sc4.fcd.FlinkFcdProducer
ENV FLINK_APPLICATION_ARGS "--path /datasets/fcd-sample-data.csv.gz --topic taxi"
