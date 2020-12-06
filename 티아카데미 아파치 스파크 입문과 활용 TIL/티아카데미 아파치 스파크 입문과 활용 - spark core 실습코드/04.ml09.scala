cd /spark/bin
wget https://raw.githubusercontent.com/databricks/Spark-The-Definitive-Guide/master/data/sample_movielens_ratings.txt

/spark/bin/spark-shell

import org.apache.spark.ml.recommendation.ALS
val ratings = spark.read.textFile("/spark/bin/sample_movielens_ratings.txt").selectExpr("split(value , '::') as col").selectExpr("cast(col[0] as int) as userId","cast(col[1] as int) as movieId","cast(col[2] as float) as rating","cast(col[3] as long) as timestamp")


val Array(training, test) = ratings.randomSplit(Array(0.8, 0.2))

val als = new ALS().setMaxIter(5).setRegParam(0.01).setUserCol("userId").setItemCol("movieId").setRatingCol("rating")

println(als.explainParams())
val alsModel = als.fit(training)
val predictions = alsModel.transform(test)


alsModel.recommendForAllUsers(10).selectExpr("userId", "explode(recommendations)").show()
alsModel.recommendForAllItems(10).selectExpr("movieId", "explode(recommendations)").show()
