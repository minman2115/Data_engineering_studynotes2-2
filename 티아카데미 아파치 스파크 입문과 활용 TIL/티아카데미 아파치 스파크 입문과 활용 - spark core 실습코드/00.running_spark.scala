%spark
//sparksession driver process,어플리케이션 기준 
spark


%spark
//한개의 컬럼과 1000개의 row(0~999)를 생성한 데이터 프레임을 만든다
val myRange = spark.range(1000).toDF("number")


%spark
//transformation - narrow dependency
val divisBy2 = myRange.where("number % 2 = 0")

%spark
//action
divisBy2.count()


%sh
wget https://raw.githubusercontent.com/databricks/Spark-The-Definitive-Guide/master/data/flight-data/csv/2015-summary.csv
//데이터 다운로드

%sh
head /zeppelin/2015-summary.csv
//데이터 확인


%spark
val flightData2015 = spark.read.option("inferSchema", "true").option("header", "true").csv("/zeppelin/2015-summary.csv")
//CSV데이터 데이터 프레임으로 바로 읽기, visit to http://localhost:4040


%spark
flightData2015.take(3)
//3건만 가져오기

%spark
flightData2015.sort("count").explain()
//실행계획 확인하기

%spark
flightData2015.sort("count").take(2)
//count필드로 정렬해서 2개 가져오기


%spark
flightData2015.createOrReplaceTempView("flight_data_2015")
//SQL을 사용하기 위해 임시 view만들기

%spark
val sqlWay = spark.sql("""
SELECT DEST_COUNTRY_NAME, count(1)
FROM flight_data_2015
GROUP BY DEST_COUNTRY_NAME
""")
//SQL로DEST_COUNTRY_NAME 기준으로 몇건인지 확인하는 spark sql실행하기

%spark
sqlWay.describe().show()
//데이터 프레임 통계 보기


%spark
val dataFrameWay = flightData2015
  .groupBy('DEST_COUNTRY_NAME)
  .count.explain
//데이터 프레임으로 실행계획 확인하기

%spark
sqlWay.explain
//sql과 dataframe실행계획 비교하기

%spark
spark.sql("SELECT max(count) from flight_data_2015").take(1)
//sql질의로 max값 가져와서 take로 한건만 확인하기

%spark
import org.apache.spark.sql.functions.max

flightData2015.select(max("count")).take(1)
//데이터 프레임에서 max함수 이용하여 count의 최대값 가져오기

%spark
val maxSql = spark.sql("""
SELECT DEST_COUNTRY_NAME, sum(count) as destination_total
FROM flight_data_2015
GROUP BY DEST_COUNTRY_NAME
ORDER BY sum(count) DESC
LIMIT 5
""")

//spark sql로 DEST_COUNTRY_NAME 을 집계연산을 수행하여 합계를 구하여 큰 순서대로 5건 확인하기

%spark
maxSql.explain
maxSql.show()
//sql결과 확인하기


%spark
//동일한 쿼리를 dataframe으로 구현해 봅니다.  End-to-End example
flightData2015
.groupBy("DEST_COUNTRY_NAME")
.sum("count")
.withColumnRenamed("sum(count)", "destination_total")
.sort(desc("destination_total"))
.limit(5)
.explain


%spark
flightData2015
.groupBy("DEST_COUNTRY_NAME")
.sum("count")
.withColumnRenamed("sum(count)", "destination_total")
.sort(desc("destination_total"))
.limit(5)
.show()


%spark
//임의의 값을 가지고 데이터 프레임 생성하기
val data = Seq(("Banana",1000,"USA"), ("Carrots",1500,"USA"), ("Beans",1600,"USA"),
      ("Orange",2000,"USA"),("Orange",2000,"USA"),("Banana",400,"China"),
      ("Carrots",1200,"China"),("Beans",1500,"China"),("Orange",4000,"China"),
      ("Banana",2000,"Canada"),("Carrots",2000,"Canada"),("Beans",2000,"Mexico"))

import spark.sqlContext.implicits._
val df = data.toDF("Product","Amount","Country")
df.show()

//각 제품의 각 국가로 수출 된 총 금액을 얻으려면 제품 별 그룹화, 국가 별 피봇 팅 및 금액 합계를 얻어온다
val pivotDF = df.groupBy("Product").pivot("Country").sum("Amount")
pivotDF.show()

//spark2.0에서 성능향상을 위해 pivot대상열을 선언함 
val countries = Seq("USA","China","Canada","Mexico")
val pivotDF = df.groupBy("Product").pivot("Country", countries).sum("Amount")
pivotDF.show()

//성능 향상을 위해 두단계 집계를 사용함 
val pivotDF = df.groupBy("Product","Country")
      .sum("Amount")
      .groupBy("Product")
      .pivot("Country")
      .sum("sum(Amount)")
pivotDF.show()


//stack기능을 이용하여 주요 국가에 대해 unpivot을 수행
val unPivotDF = pivotDF.select($"Product",
expr("stack(4, 'Canada', Canada, 'China', China, 'Mexico', Mexico, 'USA', USA) as (Country,Total)"))
.where("Total is not null")
unPivotDF.show()
