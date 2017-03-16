# from https://github.com/big-data-europe/docker-kafka

FROM bde2020/kafka

MAINTAINER Karl-Heinz Sylla <karl-heinz.sylla@iais.fraunhofer.de>

RUN chmod +x /wait-for-step.sh
RUN chmod +x /execute-step.sh
RUN chmod +x /finish-step.sh

COPY healthcheck /
COPY kafka-startup.sh /

EXPOSE 9092

CMD [ "./kafka-startup.sh" ]
