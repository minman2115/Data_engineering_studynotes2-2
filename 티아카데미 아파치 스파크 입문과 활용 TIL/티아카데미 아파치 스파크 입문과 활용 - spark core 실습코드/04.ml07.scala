%sh
wget -O binary-classification.gz.parquet https://raw.githubusercontent.com/databricks/Spark-The-Definitive-Guide/master/data/binary-classification/part-r-00007-e02e56d5-d522-4b93-a7f2-f2dc1b2fdba9.gz.parquet


%spark
val bInput = spark.read.format("parquet").load("/zeppelin/binary-classification.gz.parquet")
  .selectExpr("features", "cast(label as double) as label")


import org.apache.spark.ml.classification.LogisticRegression
val lr = new LogisticRegression()
println(lr.explainParams()) // see all parameters
val lrModel = lr.fit(bInput)


println(lrModel.coefficients)
println(lrModel.intercept)


import org.apache.spark.ml.classification.BinaryLogisticRegressionSummary
val summary = lrModel.summary
val bSummary = summary.asInstanceOf[BinaryLogisticRegressionSummary]
println(bSummary.areaUnderROC)
bSummary.roc.show()
bSummary.pr.show()

