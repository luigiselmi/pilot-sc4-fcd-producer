Pilot SC4 FCD Taxi Historic Data Kafka Producer
===============================================
This component contains an Apache Flink program that reads zipped files of historical traffic data and processes each records
to map match the coordinates pairs to the road segments. The program computes the number of vehicles and the average speed of
each road segment within a time window. The road network is extracted from the Open Street Map database. The result of the 
computation for each road segment, and for all the time interval, is stored in a specific Kafka topic for further processment.

##Requirements

This component depends on PostGis with the road network data pre loaded, R for the map matching algorithm and Rserver for the 
communication. All these modules are included in [pilot-sc4-postgis](https://github.com/big-data-europe/pilot-sc4-postgis). Start
the pilot-sc4-postgis docker container before using this component.

##Build

The software is based on Maven and can be built from the project root folder running the command

    $ mvn install  

##Install and Run
This component can be run as a Java application running the command 

    $ java -jar target/pilot-sc4-fcd-producer-0.1-jar-with-dependencies.jar -input <path_to_the_gzipped_file>

The job can also be started from the Flink JobManager.

##Licence
Apache 2.0
