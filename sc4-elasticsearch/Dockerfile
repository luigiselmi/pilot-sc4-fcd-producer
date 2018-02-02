# from https://github.com/big-data-europe/docker-kafka

# 2017-12-22 15:40:12 this is the current version:
#FROM  docker.elastic.co/elasticsearch/elasticsearch:6.1.0
#FROM  docker.elastic.co/elasticsearch/elasticsearch:5.2.0

FROM elasticsearch:2.3
#FROM elasticsearch:5.6

MAINTAINER Karl-Heinz Sylla <karl-heinz.sylla@iais.fraunhofer.de>

USER root

#RUN chmod +x /wait-for-step.sh
#RUN chmod +x /execute-step.sh
#RUN chmod +x /finish-step.sh

COPY healthcheck /
COPY elasticsearch-schema.json /
COPY elasticsearch-startup.sh /

#RUN chmod +x /healthcheck
#RUN chmod +x /elasticsearch-startup.sh

#RUN sysctl -w vm.max_map_count=262144

EXPOSE 9200
EXPOSE 9300

USER elasticsearch

ENV discovery.type=single-node

CMD [ "/elasticsearch-startup.sh" ]
