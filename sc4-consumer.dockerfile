# Dockerfile for the SC4 Pilot Floating Car Data consumer
#
# 1) Build an image using this docker file. Run the following docker command
#
#    $ docker build -f consumer.dockerfile -t bde2020/pilot-sc4-fcd-consumer:v0.10.0 .
#
# 2) Start a container
#
#    $ docker run -d --network pilot-sc4 --name fcd-consumer bde2020/pilot-sc4-fcd-consumer:v0.10.0

FROM bde2020/flink-maven-template:1.4.0-hadoop2.7

MAINTAINER Luigi Selmi <luigiselmi@gmail.com>, Karl-Heinz Sylla <karl-heinz.sylla@iais.fraunhofer.de>

ENV FLINK_APPLICATION_JAR_NAME pilot-sc4-fcd-applications-0.10.0-jar-with-dependencies
ENV FLINK_APPLICATION_MAIN_CLASS eu.bde.pilot.sc4.fcd.FlinkFcdConsumerElasticsearch
ENV FLINK_APPLICATION_ARGS "--topic taxi --window 2 --sink hdfs://namenode:8020/user/hue/taxi_aggregates.txt"
