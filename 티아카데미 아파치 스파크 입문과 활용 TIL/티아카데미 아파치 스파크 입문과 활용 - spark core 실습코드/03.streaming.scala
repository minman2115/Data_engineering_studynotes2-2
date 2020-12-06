%sh
mkdir -p /zeppelin/streaming/
cd /zeppelin/streaming
wget https://raw.githubusercontent.com/databricks/Spark-The-Definitive-Guide/master/data/activity-data/part-00000-tid-730451297822678341-1dda7027-2071-4d73-a0e2-7fb6a91e1d1f-0-c000.json
wget https://raw.githubusercontent.com/databricks/Spark-The-Definitive-Guide/master/data/activity-data/part-00001-tid-730451297822678341-1dda7027-2071-4d73-a0e2-7fb6a91e1d1f-0-c000.json
wget https://raw.githubusercontent.com/databricks/Spark-The-Definitive-Guide/master/data/activity-data/part-00002-tid-730451297822678341-1dda7027-2071-4d73-a0e2-7fb6a91e1d1f-0-c000.json

%spark
//정적인 방식으로 데이터 읽기, 인간행동 이해를 위한 human activity데이터, 스마트폰과 스마트 워치의 가속도계와 자이로스코프에서 지원하는 빈도로 샘플링한 센서 데이터
//사용자는 자전거 타기/앉기/일어서기/걷기등의 활동을 기록한 센서데이터 
//데이터에는 타임스탬프와 모델 정보 사용자 장비정보 등이 있으며 gt는 해당 시점의 사용자의 행동 유형을 나타냄
val static = spark.read.json("/zeppelin/streaming")
val dataSchema = static.schema

static.printSchema
static.show(50)

%spark
//스트리밍 방식으로 읽기, 정적 데이터에서 읽은 스키마를 세팅함, 스키마 추론도 가능하지만..invalid한 상태 발생할수도..
//maxFilesTrigger는 폴더내의 전체 파일을 얼마나 빨리 읽을지 결정 증분 처리를 보여주기 위해 1로 설정..어디까지나 테스트임 
//maxFilesPerTrigger to limit how many files to process every time
val streaming = spark.readStream.schema(dataSchema)
  .option("maxFilesPerTrigger", 1).json("/zeppelin/streaming")


%spark
//gt를 그룹화 하고 건수를 계산
val activityCounts = streaming.groupBy("gt").count()


%spark
spark.conf.set("spark.sql.shuffle.partitions", 5) //join, aggregation suffle partition default-200 


%spark
//action을 memory로 설정, 트리거 수행후 모든키와 데이터 수를 저장하는 complete출력 모드 설정
val activityQuery = activityCounts.writeStream.queryName("activity_counts")
  .format("memory").outputMode("complete")
  .start()


%spark
for( i <- 1 to 5 ) {
    spark.sql("SELECT * FROM activity_counts").show()
    Thread.sleep(1000)//1초마다 출력
}

%spark
import org.apache.spark.sql.functions.expr
//스트림 트랜스포메이션 filter를 적용하여 트랜스포메이션을 얻고 simple_transform으로 append모드로 전달한다. 새로운 연산 결과가 출력 테이블에 추가된다
val simpleTransform = streaming.withColumn("stairs", expr("gt like '%stairs%'"))
  .where("stairs")
  .where("gt is not null")
  .select("gt", "model", "arrival_time", "creation_time")
  .writeStream
  .queryName("simple_transform")
  .format("memory")
  .outputMode("append")
  .start()

%spark
//스마트폰의 모델과 사람의 행동 가속도 센서를 의미하는 x,y,z 평균에 대해 cube를 이용하여 집계를 생성한다.
val deviceModelStats = streaming.cube("gt", "model").avg()
  .drop("avg(Arrival_time)")
  .drop("avg(Creation_Time)")
  .drop("avg(Index)")
  .writeStream.queryName("device_counts").format("memory").outputMode("complete")
  .start()

%sql
SELECT * FROM device_counts

%spark
//정적 데이터프레임과 스트리밍 데이터프레임을 조인한다. 
val historicalAgg = static.groupBy("gt", "model").avg()
val deviceModelStats = streaming.drop("Arrival_Time", "Creation_Time", "Index")
  .cube("gt", "model").avg()
  .join(historicalAgg, Seq("gt", "model"))
  .writeStream.queryName("device_counts_join").format("memory").outputMode("complete")
  .start()

%sql
select * from device_counts_join