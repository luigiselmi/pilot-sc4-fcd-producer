Pilot SC4 Floating Car Data Applications
===============================================
This repository contains [Apache Flink](http://flink.apache.org/) programs that produce, consume and process traffic data.
The producer (FlinkFcdProducer.java) reads the records from a gzipped file in the file system and write them in a [Kafka](http://kafka.apache.org/)
topic in binary format ([Avro](http://avro.apache.org/)).
The consumer (FlinkFcdConsumer.java) reads the records from the Kafka topic, separates them into cells within a bounding box, 
computes the number of records within each cell in time windows and prints them to stdout
The MapMatch program (FlinkMapMatch.java, work in progress) reads the records from a Kafka topic, separates the records 
computing their geohash and then matches the coordinates pairs to the road segments. Finally program computes the number 
of vehicles and the average speed in each road segment within a time window. The result of the computation for each road segment, 
and for all the time interval, is sent to a sink (TBD, Elasticsearch or Cassandra).

##Requirements

The MapMatch program depends on PostGis with the road network data pre loaded, R for the map matching algorithm and Rserver for the 
communication. All these modules are included in [pilot-sc4-postgis](https://github.com/big-data-europe/pilot-sc4-postgis). Start
the pilot-sc4-postgis docker container before running the MapMatch program.

##Build

The software is based on Maven and can be built from the project root folder running the command

    $ mvn install  

##Install and Run
This component can be run as a Java application passing some arguments to select the Flink application and further parameters.

### Floating Car Data Producer 
In order to start a producer run the following command

    $ java -jar target/pilot-sc4-fcd-applications-0.1-jar-with-dependencies.jar producer -path <path_to_the_gzipped_file> -topic <a kafka topic>

The job can also be started from the Flink JobManager, see the [Flink JobManager Quick Star Setup](https://ci.apache.org/projects/flink/flink-docs-release-1.2/quickstart/setup_quickstart.html#start-a-local-flink-cluster) 
to learn how to do it. Once Flink is started you can submit a job uploading the project jar file and setting the following parameters

    Entry Class: eu.bde.pilot.sc4.fcd.FlinkFcdProducer
    Program Arguments: --path <path_to_the_gzipped_file> --topic <a kafka topic>

    
### Floating Car Data Consumer
In order to start a consumer run the following command

    $ java -jar target/pilot-sc4-fcd-applications-0.1-jar-with-dependencies.jar consumer -topic <a kafka topic> -window <minutess>

This job can also be started from the Flink JobManager using the same jar file (you don't have to upload it again) and setting the 
following parameters  

    Entry Class: eu.bde.pilot.sc4.fcd.FlinkFcdConsumer
    Program Arguments: --topic <a kafka topic> --window <minutes>
    
### Floating Car Data Map-Match
In order to start the map-match run the following command

    $ java -jar target/pilot-sc4-fcd-applications-0.1-jar-with-dependencies.jar mapmatch -topic <a kafka topic> -window <minutes>

You can submit the MapMatch job to the Flink Job manager setting the following parameters  

    Entry Class: eu.bde.pilot.sc4.fcd.FlinkMapMatch
    Program Arguments: mapmatch --topic <a kafka topic> --window <minutes>

## Troubleshooting
### Flink Task Manager
Before submitting a 2nd or more jobs to the Task Manager, be sure to set the number of task slots equal to the number of jobs you want 
the Task Manager to run. From your Flink installation root folder open conf/flink-conf.yaml and set 

    taskmanager.numberOfTaskSlots: 2
    
to run the producer and the consumer in local.
### Nginx Configuration
The Hue service currently doesn't allow to copy a file bigger than 1MB in HDFS. In order to overcome this problem after running the pilot with the docker-compose-initdaemon.yml file you have to enter the nginx-proxy-with-css (csswrapper) container 

    $ docker exec -it csswrapper /bin/bash
    
The file /etc/nginx/nginx.conf must be updated in order to set the client_max_body_size parameter within the http section of the file to the size of the file that must be copied in HDFS. Since there's no editor in the container you must install one

    # apt-get update
    # apt-get install vim
    
Open the nginx.conf file, add the client_max_body_size parameter and set its value (e.g. 200 MB)

    http {
        .....
        client_max_body_size 200M;
    }
 
Leave the container with Ctrl-P Ctrl-Q and restart it

    $ docker stop csswrapper
    $ docker start csswrapper
    
Now Hue should accept files whose size is below the value set in nginx.conf  

##Licence
Apache 2.0
