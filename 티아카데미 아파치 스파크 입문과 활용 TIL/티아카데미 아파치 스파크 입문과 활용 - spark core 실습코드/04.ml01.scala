import org.apache.spark.ml.linalg.Vectors
val denseVec = Vectors.dense(1.0, 2.0, 3.0)
val size = 3
val idx = Array(1,2) // locations of non-zero elements in vector
val values = Array(2.0,3.0)
val sparseVec = Vectors.sparse(size, idx, values)
sparseVec.toDense
denseVec.toSparse


%sh
wget -O model.json  https://raw.githubusercontent.com/databricks/Spark-The-Definitive-Guide/master/data/simple-ml/part-r-00000-f5c243b9-a015-4a3b-a4a8-eca00f80f04c.json


%spark
var df = spark.read.json("/zeppelin/model.json")
df.orderBy("value2").show()


import org.apache.spark.ml.feature.RFormula
val supervised = new RFormula()
  .setFormula("lab ~ . + color:value1 + color:value2")
 
 
val fittedRF = supervised.fit(df)
val preparedDF = fittedRF.transform(df)
preparedDF.show()


val Array(train, test) = preparedDF.randomSplit(Array(0.7, 0.3))


import org.apache.spark.ml.classification.LogisticRegression
val lr = new LogisticRegression().setLabelCol("label").setFeaturesCol("features")


println(lr.explainParams())


val fittedLR = lr.fit(train)


fittedLR.transform(train).select("label", "prediction").show()
