//json데이터를 읽고 SQL테스트를 위해 tempview를 생성합니다. tempview는 sparkcontext에서만 유지됩니다
val df = spark.read.format("json").load("/zeppelin/2015-summary.json")
df.createOrReplaceTempView("dfTable")



import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{StructField, StructType, StringType, LongType}
//스키마를 지정하고 Row를 생성한후 데이터 프레임을 생성합니다
val myManualSchema = new StructType(Array(
  new StructField("some", StringType, true),
  new StructField("col", StringType, true),
  new StructField("names", LongType, false)))
val myRows = Seq(Row("Hello", null, 1L))
val myRDD = spark.sparkContext.parallelize(myRows)
val myDf = spark.createDataFrame(myRDD, myManualSchema)
myDf.show()



//임의의 값을 생성한 후 Dataframe으로 생성해 봅니다. toDF의 파라미터는 컬럼 이름입니다
val myDF = Seq(("Hello", 2, 1L)).toDF("col1", "col2", "col3")



myDf.show()


//DEST_COUNTRY_NAME 컬럼을 선택하여 2개의 값을 확인합니다.
df.select("DEST_COUNTRY_NAME").show(2)
//SELECT DEST_COUNTRY_NAME FROM dfTable LIMIT 2

df.select("DEST_COUNTRY_NAME", "ORIGIN_COUNTRY_NAME").show(2)
//SELECT DEST_COUNTRY_NAME, ORIGIN_COUNTRY_NAME FROM dfTable LIMIT 2



//컬럼을 선택할때는 다음과 같이 다양한 방법으로 지정할 수 있습니다
import org.apache.spark.sql.functions.{expr, col, column}
df.select(
    df.col("DEST_COUNTRY_NAME"),
    col("DEST_COUNTRY_NAME"),
    column("DEST_COUNTRY_NAME"),
    'DEST_COUNTRY_NAME,
    $"DEST_COUNTRY_NAME",
    expr("DEST_COUNTRY_NAME"))
  .show(2)
  
//컬럼 이름에 대해 alias를 지정할때는 AS구문을 사용합니다. 아래 두 표현식은 동일합니다.
//   ds.selectExpr("colA", "colB as newName", "abs(colC)")
//   ds.select(expr("colA"), expr("colB as newName"), expr("abs(colC)"))
df.select(expr("DEST_COUNTRY_NAME AS destination")).show(2)
//SELECT DEST_COUNTRY_NAME as destination FROM dfTable LIMIT 2


df.selectExpr("DEST_COUNTRY_NAME as newColumnName", "DEST_COUNTRY_NAME").show(2)

df.selectExpr(
    "*", // include all original columns
    "(DEST_COUNTRY_NAME = ORIGIN_COUNTRY_NAME) as withinCountry").show(5)
 
//SELECT *, (DEST_COUNTRY_NAME = ORIGIN_COUNTRY_NAME) as withinCountry FROM dfTable LIMIT 2


df.selectExpr("avg(count)", "count(distinct(DEST_COUNTRY_NAME))").show(2)
//SELECT avg(count), count(distinct(DEST_COUNTRY_NAME)) FROM dfTable LIMIT 2


df.rdd.getNumPartitions // 1


df.repartition(5)


//컬럼 기준으로 리파티션 할 수 있습니다. 
df.repartition(col("DEST_COUNTRY_NAME"))


df.repartition(5, col("DEST_COUNTRY_NAME")).coalesce(2)


res30.rdd.getNumPartitions


