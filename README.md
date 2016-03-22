advertising-lambda-app
======================

A Lambda Architecture sample to be used on Azure HDInsight (or the Hortonworks sandbox). 

The main components are:

* **HBase** database for realtime & batch writes 
* **Apache Storm** Trident for realtime computation
* **Apache Pig** for batch computation
* **NodeJs** for dashboards
* **Apache Kafka** for ingestion (optional)

### Create HBase table
```
hbase shell
create 'advertising', 'bulk', 'realtime'
```

### Copy the HBase config file
```
scp USERNAME@CLUSTERNAME-ssh.azurehdinsight.net:/etc/hbase/conf/hbase-site.xml ./conf/hbase-site.xml
```


### Compile & Submit the Apache Storm topology
```
mvn clean package
scp target/advertising-lambda-app-1.0-SNAPSHOT-jar-with-dependencies.jar USERNAME@CLUSTERNAME-ssh.azurehdinsight.net:advertising-lambda.jar
storm jar advertising-lambda.jar net.serrate.advertising.storm.AdvertisingTridentTopology config.properties
```

### Execute the pig script
```
scp hadoop/advertising-script.pig USERNAME@CLUSTERNAME-ssh.azurehdinsight.net:advertising-script.pig
pig advertising-script.pig
```

### Credits
* The dashboard has been based on the work of: https://github.com/Blackmist/hdinsight-eventhub-example
* The advertising example has been based on the great book _Storm Blueprints: Patterns for Distributed Real-time Computation_ by P. Taylor Goetz and Brian O'Neill
