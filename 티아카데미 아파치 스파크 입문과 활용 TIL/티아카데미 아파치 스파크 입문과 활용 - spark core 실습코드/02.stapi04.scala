val df = spark.read.format("json").load("/zeppelin/2015-summary.json")
df.printSchema


import org.apache.spark.sql.Row
val schema = df.schema
//위에 선언된 스키마에 row를 추가합니다.

val newRows = Seq(
  Row("New Country", "Other Country", 5L),
  Row("New Country 2", "Other Country 3", 1L)
)


val parallelizedRows = spark.sparkContext.parallelize(newRows)
val newDF = spark.createDataFrame(parallelizedRows, schema)

  



df.union(newDF)
  .where("count = 1")
  .where($"ORIGIN_COUNTRY_NAME" =!= "United States")
  .show() // get all of them and we'll see our new rows at the end
  
  

  
df.sort(desc("count")).show(5)
df.sort("count").show(5)

df.orderBy("count", "DEST_COUNTRY_NAME").show(5)
df.orderBy(col("count"), col("DEST_COUNTRY_NAME")).show(5)
df.orderBy(asc("count"), desc("DEST_COUNTRY_NAME")).show(5)


df.limit(5).show()
//SELECT * FROM dfTable LIMIT 5

df.orderBy(expr("count desc")).limit(5).show()
//SELECT * FROM dfTable ORDER BY count desc LIMIT 6


val collectDF = df.limit(10)
collectDF.take(5) // take works with an Integer count
collectDF.show() // this prints it out nicely
collectDF.show(5, false)
collectDF.collect()


df.createOrReplaceTempView("dfTable")

%sql
select * from dfTable

%spark
// join연산을 위해 아래와 같이 세개의 데이터 프레임을 생성합니다
val person = Seq(
    (0, "Bill Chambers", 0, Seq(100)),
    (1, "Matei Zaharia", 1, Seq(500, 250, 100)),
    (2, "Michael Armbrust", 1, Seq(250, 100)))
  .toDF("id", "name", "graduate_program", "spark_status")
val graduateProgram = Seq(
    (0, "Masters", "School of Information", "UC Berkeley"),
    (2, "Masters", "EECS", "UC Berkeley"),
    (1, "Ph.D.", "EECS", "UC Berkeley"))
  .toDF("id", "degree", "department", "school")
val sparkStatus = Seq(
    (500, "Vice President"),
    (250, "PMC Member"),
    (100, "Contributor"))
  .toDF("id", "status")




%spark
person.printSchema


%spark
person.createOrReplaceTempView("person")
graduateProgram.createOrReplaceTempView("graduateProgram")
sparkStatus.createOrReplaceTempView("sparkStatus")


%spark
// join연산을 위해 컬럼을 대입합니다. 
val joinExpression = person.col("graduate_program") === graduateProgram.col("id")



%spark
//person과 gradeateProgram에 대한 join, 기본이 inner 조인이다
person.join(graduateProgram, joinExpression).show()


var joinType = "inner"

//jointype을 지정할 수 있다

person.join(graduateProgram, joinExpression, joinType).show()


joinType = "outer"
//왼쪽 데이터프레임이나 오른쪽 데이터 프레임에 키가 일치하는 row가 없다면 해당 위치에 null을 삽입한다

person.join(graduateProgram, joinExpression, joinType).show()



joinType = "left_outer"

//왼쪽 데이터 프레임의 키 기준으로 오른쪽 데이터 프레임에 일치하는 row가 없다면 스파크는 해당 위치에 null을 삽입한다.
graduateProgram.join(person, joinExpression, joinType).show()



joinType = "right_outer"

//오른쪽 데이터 프레임기준으로 왼쪽 데이터 프레임에 일치하는 키가 없다면 해당 위치에 null을 삽입한다.


person.join(graduateProgram, joinExpression, joinType).show()



joinType = "left_semi"
//오른쪽 데이터프레임의 어떤 값도 포함하지 않으며, 두번째 데이터 프레임에 값이 존재하는지 확인하기 위해 값만 비교하는 용도로 사용한다.
//만약 값이 존재한다면 왼쪽 데이터 프레임에 중복키가 존재하더라도 해당 로우는 결과에 포함된다. left_semi조인은 기존 조인과 달리 데이터 프레임의 필터로 볼 수 있다.
graduateProgram.show()
person.show()
graduateProgram.join(person, joinExpression, joinType).show()



// 기존 graduateProgram 데이터 프레임에 값을 추가
val gradProgram2 = graduateProgram.union(Seq(
    (0, "Masters", "Duplicated Row", "Duplicated School")).toDF())

gradProgram2.createOrReplaceTempView("gradProgram2")




//새로 row가 추가되어 조건식에 의해 left_semi에 row를 반환함을 알 수 있다. 
println(joinType)
gradProgram2.join(person, joinExpression, joinType).show()




joinType = "left_anti"
//left_semi 조인의 반대개념으로 두번째 데이터프레임에 관련된 키를 찾을수 없는 경우에만 row를 추출한다. SQL에서 NOT IN과 같은 필터로 볼수 있다.
//graduateProgram 에 속하지 않는 row만 추출
graduateProgram.join(person, joinExpression, joinType).show()



joinType = "cross"
graduateProgram.join(person, joinExpression, joinType).show()
//SQL
// SELECT * FROM graduateProgram CROSS JOIN person
// ON graduateProgram.id = person.graduate_program


//명시적으로 교차조인을 선언한 경우 AXB - 왠만하면 하지 마세요 OTL
person.crossJoin(graduateProgram).show()


import org.apache.spark.sql.functions.expr
//복합 데이터 타입의 조인을 수행합니다. 여기서 spark_status 에 id를 추출하여 조인합니다
person.withColumnRenamed("id", "personId")
  .join(sparkStatus, expr("array_contains(spark_status, id)")).show()


val gradProgramDupe = graduateProgram.withColumnRenamed("id", "graduate_program")
//컬럼을 중복으로 만들어본다


//두개의 데이터 프레임에 조건식은 다음과 같다
val joinExpr = gradProgramDupe.col("graduate_program") === person.col(
  "graduate_program")

person.join(gradProgramDupe, joinExpr).show()

person.join(gradProgramDupe, joinExpr).select("graduate_program").show()
//person.join(gradProgramDupe, joinExpr).show() --데이터를 확인해보자

//Reference 'graduate_program' is ambiguous 에러발생

//해결방법1 : 동일한 이름의 두개의 키를 사용한다면 boolean방식의 조건 표현식을 문자열이나 sequence형태로 변경한다. 
person.join(gradProgramDupe, "graduate_program").select("graduate_program").show()


//해결방법2: 조인후에 문제되는 컬럼 제거한다
person.join(gradProgramDupe, joinExpr).drop(person.col("graduate_program"))
  .select("graduate_program").show()

//해결방법3: 조인전에 컬럼명을 변경한다.
val gradProgram3 = graduateProgram.withColumnRenamed("id", "grad_id")
val joinExpr = person.col("graduate_program") === gradProgram3.col("grad_id")
person.join(gradProgram3, joinExpr).show()


//spark 조인방식 비교
val joinExpr = person.col("graduate_program") === graduateProgram.col("id")

person.join(graduateProgram, joinExpr).explain()
//spark이 자동적으로 BroadcastHashJoin으로 전환한다
//큰 테이블과 작은 테이블 조인시에 작은 테이블인 경우 브로드 캐스트 조인을 통해 쿼리를 최적화 한다. 이 방법은 작은 데이터프레임을 클러스터 전체 워커 노드에 복제하는것이다. 


//아래와 같이 brodcast함수를 사용하여 브로드캐스트 조인을 강제적으로 실행할 수 있도록 한다. 
import org.apache.spark.sql.functions.broadcast
val joinExpr = person.col("graduate_program") === graduateProgram.col("id")
person.join(broadcast(graduateProgram), joinExpr).explain()


%sql
SELECT /*+MAPJOIN(graduateProgram) */ * FROM person JOIN graduateProgram 
ON person.graduate_program = graduateProgram.id

%sql
--다음과 같이 힌트절을 이용하여 spark sql에서도 브로드캐스트 조인을 실행할 수 있으나, 옵티마이저가 이를 무시할 수도 있다. 
explain 
SELECT /*+ MAPJOIN(graduateProgram) */ * FROM person JOIN graduateProgram 
ON person.graduate_program = graduateProgram.id

   
   
  

  
  
  
  