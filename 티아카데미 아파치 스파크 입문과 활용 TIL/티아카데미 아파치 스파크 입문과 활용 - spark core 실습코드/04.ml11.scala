cd /spark/bin
wget -O 201508_station_data.csv https://raw.githubusercontent.com/databricks/Spark-The-Definitive-Guide/master/data/bike-data/201508_station_data.csv

wget -O 201508_trip_data.csv https://raw.githubusercontent.com/databricks/Spark-The-Definitive-Guide/master/data/bike-data/201508_trip_data.csv


/spark/bin/spark-shell --packages graphframes:graphframes:0.5.0-spark2.1-s_2.11

val bikeStations = spark.read.option("header","true").csv("/spark/bin/201508_station_data.csv")
val tripData = spark.read.option("header","true").csv("/spark/bin/201508_trip_data.csv")


bikeStations.show
tripData.show 
val stationVertices = bikeStations.withColumnRenamed("name", "id").distinct()
val tripEdges = tripData.withColumnRenamed("Start Station", "src").withColumnRenamed("End Station", "dst")
  
  
import org.graphframes.GraphFrame
val stationGraph = GraphFrame(stationVertices, tripEdges)
stationGraph.cache()


println(s"Total Number of Stations: ${stationGraph.vertices.count()}")
println(s"Total Number of Trips in Graph: ${stationGraph.edges.count()}")
println(s"Total Number of Trips in Original Data: ${tripData.count()}")


import org.apache.spark.sql.functions.desc
val ranks = stationGraph.pageRank.resetProbability(0.15).maxIter(10).run()
ranks.vertices.orderBy(desc("pagerank")).select("id", "pagerank").show(10)


val inDeg = stationGraph.inDegrees
inDeg.orderBy(desc("inDegree")).show(5, false)


val outDeg = stationGraph.outDegrees
outDeg.orderBy(desc("outDegree")).show(5, false)


val degreeRatio = inDeg.join(outDeg, Seq("id")).selectExpr("id", "double(inDegree)/double(outDegree) as degreeRatio")
degreeRatio.orderBy(desc("degreeRatio")).show(10, false)
degreeRatio.orderBy("degreeRatio").show(10, false)


  
  
  