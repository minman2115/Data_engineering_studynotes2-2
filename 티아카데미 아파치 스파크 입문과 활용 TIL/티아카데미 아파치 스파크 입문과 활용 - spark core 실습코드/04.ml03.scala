%sh 
wget https://raw.githubusercontent.com/databricks/Spark-The-Definitive-Guide/master/data/retail-data/by-day/2010-12-01.csv

wget -O simple-ml-integers.gz.parquet https://raw.githubusercontent.com/databricks/Spark-The-Definitive-Guide/master/data/simple-ml-integers/part-00000-ce2a44c8-feb4-4369-a2c3-4bf2f0e63b07-c000.gz.parquet
wget -O model.json https://raw.githubusercontent.com/databricks/Spark-The-Definitive-Guide/master/data/simple-ml/part-r-00000-f5c243b9-a015-4a3b-a4a8-eca00f80f04c.json
wget -O simple-ml-scaling.gz.parquet https://raw.githubusercontent.com/databricks/Spark-The-Definitive-Guide/master/data/simple-ml-scaling/part-00000-cd03406a-cc9b-42b0-9299-1e259fdd9382-c000.gz.parquet


%spark
val sales = spark.read.format("csv")
  .option("header", "true")
  .option("inferSchema", "true")
  .load("/zeppelin/2010-12-01.csv")
  .coalesce(5)
  .where("Description IS NOT NULL")
val fakeIntDF = spark.read.parquet("/zeppelin/simple-ml-integers.gz.parquet")
var simpleDF = spark.read.json("/zeppelin/model.json")
val scaleDF = spark.read.parquet("/zeppelin/simple-ml-scaling.gz.parquet")


sales.cache()
sales.show()

import org.apache.spark.ml.feature.Tokenizer
val tkn = new Tokenizer().setInputCol("Description")
tkn.transform(sales.select("Description")).show(false)



import org.apache.spark.ml.feature.StandardScaler
val ss = new StandardScaler().setInputCol("features")
ss.fit(scaleDF).transform(scaleDF).show(false)


