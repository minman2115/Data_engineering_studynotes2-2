%sh
wget https://raw.githubusercontent.com/databricks/Spark-The-Definitive-Guide/master/data/retail-data/by-day/2010-12-01.csv


%spark
val df = spark.read.format("csv")
  .option("header", "true")
  .option("inferSchema", "true")
  .load("/zeppelin/2010-12-01.csv")
df.printSchema()
df.createOrReplaceTempView("dfTable")



%spark
df.na.drop()
//any인 경우 row의 컬럼값 하나라도 null값을 가지면 해당 row를 제거합니다. all인 경우 모든 컬럼의 값이 null이거나 NaN 인 경우에만 row를 제거합니다
df.na.drop("any")
// 특정 컬럼의 null값을 제거하려면 다음과 같이 인자를 넘겨줍니다.
df.na.drop("all", Seq("StockCode","InvoiceNo"))
 
%spark
println(df.count)
println("=======================")
println(df.na.drop("all").count)
println("=======================")
println(df.na.drop().count)
println("=======================")
println(df.na.drop("any").count)
 

 

 

%spark
df.filter("Description is null").show


 
%spark
//fill함수를 이용하여 특정 값으로 채울 수 있습니다. 
val fillColValues = Map("StockCode" -> 5, "Description" -> "No Value")
val fill_df = df.na.fill(fillColValues)

//lit - Creates a Column of literal value.
val df_nvl = df.withColumn("nvl",lit(""))

val df_replace = df_nvl.na.replace("nvl", Map("" -> "UNKNOWN"))

df_replace.filter("nvl == 'UNKNOWN'").show()




//세제곱 연산 UDF만들기
val udfExampleDF = spark.range(5).toDF("num")
//UDF함수 선언
def power3(number:Double):Double = number * number * number
power3(2.0)


//전 노드에서 사용할 수 있도록 udf등록하기
import org.apache.spark.sql.functions.udf
val power3udf = udf(power3(_:Double):Double)


//데이터 프레임에서 사용하기
udfExampleDF.select(power3udf(col("num"))).show()


//spark sql에 사용할 수 있도록 udf등록하기
spark.udf.register("power3", power3(_:Double):Double)
//selectExpr로 사용하기
udfExampleDF.selectExpr("power3(num)").show(2)


udfExampleDF.createOrReplaceTempView("udfExampleTable")

%sql
select num, power3(num) from udfExampleTable