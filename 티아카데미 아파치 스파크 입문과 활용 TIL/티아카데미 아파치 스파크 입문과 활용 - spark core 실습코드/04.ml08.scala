%sh
wget -O regression.gz.parquet https://raw.githubusercontent.com/databricks/Spark-The-Definitive-Guide/master/data/regression/part-r-00002-8c34224c-f0ce-46e1-a271-791df66b7ae9.gz.parquet

%spark

val df = spark.read.load("/zeppelin/regression.gz.parquet")


import org.apache.spark.ml.regression.LinearRegression
val lr = new LinearRegression().setMaxIter(10).setRegParam(0.3).setElasticNetParam(0.8)
println(lr.explainParams())
val lrModel = lr.fit(df)


val summary = lrModel.summary
summary.residuals.show()
println(summary.objectiveHistory.toSeq.toDF.show())
println(summary.rootMeanSquaredError)
println(summary.r2)