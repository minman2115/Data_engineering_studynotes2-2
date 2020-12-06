
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


//두개의 컬럼을 선택하여 complex타입으로 생성할 수 있다.
//complex타입에는 struct, array, map이 있다.
val sample = df.selectExpr("(Description, InvoiceNo) as complex", "*")
sample.printSchema
sample.show(false)


import org.apache.spark.sql.functions.struct
val complexDF = df.select(struct("Description", "InvoiceNo").alias("complex"))
complexDF.createOrReplaceTempView("complexDF")



//complex field에서 Description을 가져옵니다.

complexDF.select("complex.Description").show()

//complex field에서 Description을 가져옵니다.

complexDF.select(col("complex").getField("Description"))

complexDF.select("complex").show(false)

//complex type모두를 조회합니다

complexDF.select("complex.*")


//description을 space로 split하여 배열로 변환합니다.
df.select(split(col("Description"), " ")).show(2,false)


//array column에서 0번째 값만 추출한다
df.select(split(col("Description"), " ").alias("array_col"))
.selectExpr("array_col[0]").show(2)

//배열에 크기를 확인합니다.
df.select($"Description",size(split(col("Description"), " "))).show(2,false)


//array_contains 함수를 사용하여 배열에 특정 값이 존재하는지 확인 할 수 있습니다
import org.apache.spark.sql.functions.array_contains

df.select($"Description",array_contains(split(col("Description"), " "),"WHITE")).show(5,false)


//explode함수는 배열 타입의 컬럼을 받아서 모든 값을 row로 변환합니다. 나머지 컬럼값은 중복으로 표시됩니다
import org.apache.spark.sql.functions.{split, explode}


df.withColumn("splitted", split(col("Description"), " "))
  .withColumn("exploded", explode(col("splitted")))
  .select("StockCode","Description", "InvoiceNo", "exploded").show(false)
  
//SELECT Description, InvoiceNo, exploded
//FROM (SELECT *, split(Description, " ") as splitted FROM dfTable)
//LATERAL VIEW explode(splitted) as exploded

//맵은 함수와 컬럼의 키-값 쌍을 이용해 생성합니다. 그리고 array와 동일하게 값을 선택할 수 있습니다
import org.apache.spark.sql.functions.map
df.select(map(col("Description"), col("InvoiceNo")).alias("complex_map")).show(5,false)

//해당 키와 매핑되는 값이 없는 경우 null을 리턴함 
df.select(map(col("Description"), col("InvoiceNo")).alias("complex_map"))
.selectExpr("complex_map['WHITE HANGING HEART T-LIGHT HOLDER']").show(5)

//맵타입을 explode하여 컬럼으로 변환함
df.select(map(col("Description"), col("InvoiceNo")).alias("complex_map"))
.selectExpr("explode(complex_map)").show(2,false)
