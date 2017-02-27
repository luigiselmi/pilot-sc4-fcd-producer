# Dockerfile for the SC4 Pilot Floating Car Data consumer 
#
# 1) Build an image using this docker file. Run the following docker command
#
#    $ docker build -f consumer.dockerfile -t bde2020/pilot-sc4-fcd-consumer:v0.0.1 .
#
# 2) Start a container 
#
#    $ docker run -d --network pilot-sc4 --name fcd-consumer bde2020/pilot-sc4-fcd-consumer:v0.0.1 

FROM bde2020/flink-maven-template:latest

MAINTAINER Luigi Selmi <luigiselmi@gmail.com>

ENV FLINK_APPLICATION_JAR_NAME pilot-sc4-fcd-applications-0.1-jar-with-dependencies.jar
ENV FLINK_APPLICATION_MAIN_CLASS eu.bde.pilot.sc4.fcd.FlinkFcdConsumer
ENV FLINK_APPLICATION_ARGS "--topic taxi --window 5"
