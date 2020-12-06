%sh
//zeppelin로그를 확인합니다.
ls -al /logs


%spark
//특정 파일을 읽어 10개의 라인을 가져옵니다.
val logs = sc.textFile("/logs/zeppelin--4c4bf3f64278.log")
logs.take(10)



%spark
//전체 문자길이와 개수 및 평균등을 구해봅니다.
val lengths = logs.map(str => str.length )
val totalLength = lengths.reduce( (acc, newValue) => acc + newValue )
val count = lengths.count()
val average = totalLength.toDouble / count

%spark
//각 line의 사이즈를 구한다
val log = logs.map((log) => log.size )
log.collect().foreach(println)


%spark
//각 라인의 길이가 200이상인 것만 필터링
val filter = logs.filter( (log) => log.size > 200)


%spark
//출력하여 다음과 같이 확인한다
filter.collect().foreach(println)