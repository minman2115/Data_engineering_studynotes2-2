%sh
//test데이터를 다운로드 받습니다
wget https://raw.githubusercontent.com/databricks/Spark-The-Definitive-Guide/master/data/flight-data/json/2015-summary.json


%sh
head /zeppelin/2015-summary.json



%spark
//json format을 바로 읽습니다
val df = spark.read.format("json").load("/zeppelin/2015-summary.json")

//데이터 스키마를 확인합니다
df.printSchema()


import org.apache.spark.sql.types.{StructField, StructType, StringType, LongType}
import org.apache.spark.sql.types.Metadata

//다음과 같이 스키마를 지정하여 필요한 데이터만 데이터 프레임으로 로딩할 수 있습니다
val myManualSchema = StructType(Array(
  StructField("DEST_COUNTRY_NAME", StringType, true),
  StructField("ORIGIN_COUNTRY_NAME", StringType, true),
  StructField("count", LongType, false,
    Metadata.fromJson("{\"hello\":\"world\"}"))
))


//스키마를 통해 필요 데이터만 데이터 프레임에 로딩합니다
val df = spark.read.format("json").schema(myManualSchema)
  .load("/zeppelin/2015-summary.json")


%spark
//데이터 프레임을 확인합니다
df.show()

%spark
//컬럼 정보를 추출합니다
spark.read.format("json").load("/zeppelin/2015-summary.json").columns



%spark
//첫번째 row만 가져옵니다
df.first()



%spark
//임의의 row를 생성하고 확인합니다
import org.apache.spark.sql.Row
val myRow = Row("Hello", null, 1, false)


%spark
//Row의 0번째 값을 가져옵니다
myRow(0)