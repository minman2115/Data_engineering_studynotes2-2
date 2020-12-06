val df = spark.read.format("json").load("/zeppelin/2015-summary.json")

df.printSchema
//ORIGIN_COUNTRY_NAME 컬럼을 제거합니다
df.drop("ORIGIN_COUNTRY_NAME").columns




df.withColumn("count2", col("count").cast("long"))
//SELECT *, cast(count as long) AS count2 FROM dfTable

df.withColumn("count2", col("count").cast("long")).show

df.filter(col("count") < 2).show(2)
df.where("count < 2").show(2)
//SELECT * FROM dfTable WHERE count < 2 LIMIT 2



df.where(col("count") < 2).where(col("ORIGIN_COUNTRY_NAME") =!= "Croatia")
  .show(2)
//SELECT * FROM dfTable WHERE count < 2 AND ORIGIN_COUNTRY_NAME != "Croatia" LIMIT 2

df.select("ORIGIN_COUNTRY_NAME", "DEST_COUNTRY_NAME").distinct().count()
//SELECT COUNT(DISTINCT(ORIGIN_COUNTRY_NAME, DEST_COUNTRY_NAME)) FROM dfTable



df.select("ORIGIN_COUNTRY_NAME").distinct().count()
//SELECT COUNT(DISTINCT ORIGIN_COUNTRY_NAME) FROM dfTable


%sh
wget https://raw.githubusercontent.com/databricks/Spark-The-Definitive-Guide/master/data/retail-data/by-day/2010-12-01.csv

head /zeppelin/2010-12-01.csv


%spark
// in Scala
val df = spark.read.format("csv")
  .option("header", "true")
  .option("inferSchema", "true")
  .load("/zeppelin/2010-12-01.csv")
df.printSchema()
df.createOrReplaceTempView("dfTable")
df.show


import org.apache.spark.sql.functions.col
df.where(col("InvoiceNo").equalTo(536365))
  .select("InvoiceNo", "Description")
  .show(5, false)



import org.apache.spark.sql.functions.col
df.where(col("InvoiceNo") === 536365)
  .select("InvoiceNo", "Description")
  .show(5, false)



df.show()
val priceFilter = col("UnitPrice") >3.0
val descripFilter = col("Description").contains("HAND")
df.where(col("StockCode").isin("85123A")).where(priceFilter.or(descripFilter))
  .show(false)


val DOTCodeFilter = col("StockCode") === "DOT"
val priceFilter = col("UnitPrice") > 600
val descripFilter = col("Description").contains("POSTAGE")
df.withColumn("isExpensive", DOTCodeFilter.and(priceFilter.or(descripFilter)))
  .where("isExpensive")
  .select("unitPrice", "isExpensive").show(5)

import org.apache.spark.sql.functions.{expr, not, col}
df.withColumn("isExpensive", not(col("UnitPrice").leq(250)))
  .filter("isExpensive")
  .select("Description", "UnitPrice").show(5)
df.withColumn("isExpensive", expr("NOT UnitPrice <= 250"))
  .filter("isExpensive")
  .select("Description", "UnitPrice").show(5)


import org.apache.spark.sql.functions.{expr, pow}
val fabricatedQuantity = pow(col("Quantity") * col("UnitPrice"), 2) + 5
df.select(expr("CustomerId"), fabricatedQuantity.alias("realQuantity")).show(2)

df.selectExpr(
  "CustomerId",
  "(POWER((Quantity * UnitPrice), 2.0) + 5) as realQuantity").show(2)


import org.apache.spark.sql.functions.{round, bround}
df.select(round(col("UnitPrice"), 1).alias("rounded"), col("UnitPrice")).show(5)

import org.apache.spark.sql.functions.{corr}
//두 데이터 간의 시리즈에 대한 연관관계 통계를 보여줍니다
df.stat.corr("Quantity", "UnitPrice")
df.select(corr("Quantity", "UnitPrice")).show()


// 데이터 프레임워크 내의 컬럼에 대한 통계를 보여줍니다
df.describe().show()

import org.apache.spark.sql.functions.{count, mean, stddev_pop, min, max}

// quartiles 을 구할 수 있습니다. 분위의 값을 넣고 오류 비율을 지정합니다
val colName = "UnitPrice"
val quantileProbs = Array(0.5)
val relError = 0.05
df.stat.approxQuantile("UnitPrice", quantileProbs, relError) // 2.51


//crosstab을 이용하여 변수 셋에 대한 도수분표표를 구합니다.
df.filter("StockCode in ('85123A','22745')").stat.crosstab("StockCode", "Quantity").show()


//빈번하게 발생되는 item을 선발합니다. 10%의 빈번한 아이템만 선출합니다.
df.stat.freqItems(Seq("StockCode", "Country"), 0.1).show(false)


import org.apache.spark.sql.functions.monotonically_increasing_id
//하나씩 채번되는 64bit integer입니다.
df.select(monotonically_increasing_id()).show(2)


import org.apache.spark.sql.functions.{initcap}
//단어의 첫 글자만 대문자로 변환합니다
df.select(col("Description"),initcap(col("Description"))).show(2, false)


import org.apache.spark.sql.functions.{lower, upper}
//단어를 소문자 또는 대문자로 변경할 수 있습니다
df.select(col("Description"),
  lower(col("Description")),
  upper(lower(col("Description")))).show(2)


import org.apache.spark.sql.functions.regexp_replace
//기존 단어를 다른 단어로 치환할 수 있습니다. 아래 color가 매핑되면 COLOR로 치환하는 예제입니다.
val simpleColors = Seq("black", "white", "red", "green", "blue")
val regexString = simpleColors.map(_.toUpperCase).mkString("|")
// the | signifies `OR` in regular expression syntax
df.select(
  regexp_replace(col("Description"), regexString, "COLOR").alias("color_clean"),
  col("Description")).show(2,false)


import org.apache.spark.sql.functions.translate
//LEET 단어를 만나면 숫자로 치환하는 예제입니다
df.select(translate(col("Description"), "LEET", "1337"), col("Description"))
  .show(2,false)

import org.apache.spark.sql.functions.regexp_extract
//지정된 문자열에서 정규식과 매칭되는 그룹을 추출합니다. 아래 예제에서는 단어만 추출하는 예제입니다
val regexString = simpleColors.map(_.toUpperCase).mkString("(", "|", ")")
// the | signifies OR in regular expression syntax
df.select(
     regexp_extract(col("Description"), regexString, 1).alias("color_clean"),
     col("Description")).show(2,false)
	 
	 
// BLACK과 WHITE만 포함된 열만 추출하는 예제입니다
val containsBlack = col("Description").contains("BLACK")
val containsWhite = col("DESCRIPTION").contains("WHITE")
df.withColumn("hasSimpleColor", containsBlack.or(containsWhite))
  .where("hasSimpleColor")
  .select("Description").show(3, false)

// 날짜 함수를 사용하는 예제입니다. 
import org.apache.spark.sql.functions.{current_date, current_timestamp}
val dateDF = spark.range(10)
  .withColumn("today", current_date())
  .withColumn("now", current_timestamp())
dateDF.createOrReplaceTempView("dateTable")

dateDF.printSchema()

//date_add 함수를 통해 기존 컬럼에서 특정 수만큼 이후 일자를 구할 수 있습니다
import org.apache.spark.sql.functions.{date_add, date_sub}
dateDF.select(date_sub(col("today"), 5), date_add(col("today"), 5)).show(1)


// 5개의 row를 생성하여 date필드에 문자열을 추가하고 date타입으로 변경합니다
import org.apache.spark.sql.functions.{to_date, lit}
spark.range(5).withColumn("date", lit("2017-01-01"))
  .select(to_date(col("date"))).show()
  
// 아래와 같이 날짜 포맷을 지정할 수 있습니다
import org.apache.spark.sql.functions.to_date
val dateFormat = "yyyyMMdd"
val cleanDateDF = spark.range(1).select(
    to_date(lit("20171211"), dateFormat).alias("date"),
    to_date(lit("20170112"), dateFormat).alias("date2"))
cleanDateDF.show()


cleanDateDF.createOrReplaceTempView("dateTable2")


%sql
select * from dateTable2

// 해당 컬럼을 날짜 포맷을 주고 timestamp로 변경해 봅니다
import org.apache.spark.sql.functions.to_timestamp
cleanDateDF.select(to_timestamp(col("date"), dateFormat)).show()

import org.apache.spark.sql.functions.coalesce
//coalesce는 null가 아닌 최초의 열을 돌려줍니다. 모든 입력이 null의 경우는 null를 돌려줍니다.
df.select(coalesce(col("Description"), col("Country"))).show(false)


