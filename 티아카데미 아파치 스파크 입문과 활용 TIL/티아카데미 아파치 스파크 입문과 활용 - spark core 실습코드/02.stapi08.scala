%sh
wget https://raw.githubusercontent.com/databricks/Spark-The-Definitive-Guide/master/data/flight-data/json/2015-summary.json


spark.read.json("/zeppelin/2015-summary.json")
  .createOrReplaceTempView("some_sql_view") // DF => SQL

spark.sql("""
SELECT DEST_COUNTRY_NAME, sum(count)
FROM some_sql_view GROUP BY DEST_COUNTRY_NAME
""")
  .where("DEST_COUNTRY_NAME like 'S%'").where("`sum(count)` > 10")
  .count() // SQL => DF
  
%sh
wget https://raw.githubusercontent.com/databricks/Spark-The-Definitive-Guide/master/data/retail-data/by-day/2010-12-01.csv
head /zeppelin/2010-12-01.csv


%spark
  // in Scala
val df = spark.read.format("csv")
    .option("header", "true")
    .option("inferSchema", "true")
    .load("/zeppelin/2010-12-01.csv")

	
//csv file을 읽어서 json으로 write하기
df.write.format("json").mode("overwrite").save("csv_to_json.json")	


%sh
//head csv_to_json.json/*

%spark
//parquet format으로 저장하기. 컬럼 기반의 데이터 저장방식으로 저장소 공간을 절약할 수 있고 전체 파일을 읽는 대신에 개별 컬럼을 읽을수 있고 컬럼기반 압축을 제공한다. spark의 기본 파일 포맷이기도 하다. 
//읽기 연산시 text나 json보다 훨씬 효율적으로 동적하고 complex type을 제공하므로 데이터를 구조화 할 수 있다. 
df.write.format("parquet").mode("overwrite").save("csv_to_parquet.parquet")	


%spark
val new_df = df.withColumn("partitioned_date", to_date('InvoiceDate))


%spark
//파티션으로 저장하기 
new_df.show()  
new_df.write.format("csv")
  .option("mode", "OVERWRITE")
  .partitionBy("partitioned_date")
  .option("path", "csv_partitioned")
  .save()
  

%sh
ls -al csv_partitioned/partitioned_date=2010-12-01

%spark
//DB읽기 
val pgDF = spark.read
  .format("jdbc")
  .option("driver", "org.postgresql.Driver")
  .option("url", "jdbc:postgresql://database_server")
  .option("dbtable", "schema.tablename")
  .option("user", "username").option("password","my-secret-password").load()
 


%spark
spark.read.format("parquet").load("csv_to_parquet.parquet").show()	

	
import org.apache.spark.sql.functions.{col, to_date}
val dfWithDate = df.withColumn("date", to_date(col("InvoiceDate"),
  "MM/d/yyyy H:mm"))
dfWithDate.createOrReplaceTempView("dfWithDate")  

//drop()  => Returns a new DataFrame that drops rows containing any null or NaN values.
val dfNoNull = dfWithDate.drop()
dfNoNull.createOrReplaceTempView("dfNoNull")

%sql
select * from dfNoNull


%spark
//date와 country컬럼 기준 롤업 생성, 모든 날짜의 총합, 날짜별 총합, 날짜별 국가별 총합을 포함함 
val rolledUpDF = dfNoNull.rollup("Date", "Country").agg(sum("Quantity"))
  .selectExpr("Date", "Country", "`sum(Quantity)` as total_quantity")
  .orderBy("Date")
rolledUpDF.show()


%spark
//전체 국가 합계
rolledUpDF.where("Country IS NULL").show()

%spark
//전체 날짜 합계 
rolledUpDF.where("Date IS NULL").show()

//cube를 모든차원에 대해 동일한 작업을 수행하여 이용하여 전체 기간에 대해 날짜와 국가별 결과를 얻을 수 있다. 
// 전체 날짜와 모든 국가에 대한 합계를 생성/모든 국가의 날짜별 합계/날짜별 국가별 합계/전체 날짜의 국가별 합계를 구할 수 있다. 
dfNoNull.cube("Date", "Country").agg(sum(col("Quantity")))
  .select("Date", "Country", "sum(Quantity)").orderBy("Date","Country").show()



import org.apache.spark.sql.functions.{grouping_id, sum, expr}
//grouping_id 를 사용하여 결과 데이터셋의 집계 수준을 명시하는 컬럼을 제공한다.
// 3 - 총수량, 2 - stockCode 코드의 집계결과, 1 -customerId 기반의 총 수량, 0 - customerId,  stockCode 조합에 따라 총 수량 
dfNoNull.cube("customerId", "stockCode").agg(grouping_id(), sum("Quantity"))
.orderBy(expr("grouping_id()").desc)
//.filter("customerId is not null and stockCode is not null")
.show()


%spark
//pivot을 통해 row를 column으로 변경함 
val pivoted = dfWithDate.groupBy("date").pivot("Country").sum()

pivoted.show()

%spark
pivoted.select("date","`Australia_sum(Quantity)`").show()



%sql
show tables
describe table some_sql_view
explain select * from some_sql_view

create table flights(
DEST_COUNTRY_NAME string, ORIGIN_COUNTRY_NAME string, count long)
using json options(path '/zeppelin/2015-summary.json')

show tables

select * from flights

create table flights_from_select using parquet as select * from flights

select * from flights_from_select

describe table flights_from_select

cache table flights

uncache table flights

create view just_usa_view as 
select * from flights where dest_country_name='United States'

select * from just_usa_view

show tables

 
  