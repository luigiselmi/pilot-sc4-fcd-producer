FROM tenforce/virtuoso:1.0.0-virtuoso7.2.2

MAINTAINER Karl-Heinz Sylla <karl-heinz.sylla@iais.fraunhofer.de>

COPY data/database/toLoad/pipeline.ttl /data/toLoad/

