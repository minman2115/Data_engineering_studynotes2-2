%sh
wget https://raw.githubusercontent.com/databricks/Spark-The-Definitive-Guide/master/data/retail-data/by-day/2010-12-01.csv


val sales = spark.read.format("csv")
  .option("header", "true")
  .option("inferSchema", "true")
  .load("/zeppelin/2010-12-01.csv")
  .where("Description IS NOT NULL")
  
  
//개별 단어로 분리하기
import org.apache.spark.ml.feature.Tokenizer
val tkn = new Tokenizer().setInputCol("Description").setOutputCol("DescOut")
val tokenized = tkn.transform(sales.select("Description"))
tokenized.show(false)

//정규 표현식을 통해 Tokernizer를 만들수도 있음
import org.apache.spark.ml.feature.RegexTokenizer
val rt = new RegexTokenizer()
  .setInputCol("Description")
  .setOutputCol("DescOut")
  .setPattern(" ") // simplest expression
  .setToLowercase(true)
rt.transform(sales.select("Description")).show(false)






// in Scala
import org.apache.spark.ml.feature.RegexTokenizer
val rt = new RegexTokenizer()
  .setInputCol("Description")
  .setOutputCol("DescOut")
  .setPattern(" ")
  .setGaps(false)
  .setToLowercase(true)
rt.transform(sales.select("Description")).show(false)


import org.apache.spark.ml.feature.RegexTokenizer
val rt = new RegexTokenizer()
  .setInputCol("Description")
  .setOutputCol("DescOut")
  .setPattern(" ")
  .setGaps(false) //개별 단어에 대한 포착
  .setToLowercase(true)
rt.transform(sales.select("Description")).show(false)



//일반적인 단어 제거하기
import org.apache.spark.ml.feature.StopWordsRemover
val englishStopWords = StopWordsRemover.loadDefaultStopWords("english")
val stops = new StopWordsRemover()
  .setStopWords(englishStopWords)
  .setInputCol("DescOut")
stops.transform(tokenized).show(false)


englishStopWords //확인 

//단어 조합 만들기
//단어의 조합은 길이가 n개인 시퀀스 n-gram을 말하고 길이가 1인것은 unigram 길이가 2인것은 bigram, 길이가 3인것은trigram 이다. 4-gram, 5-gram등도 가능
//n-gram을 사용하면 공통적으로 발생하는 단어 시퀀스를 파악할 수 있고 이것을 통해 머신러닝의 알고리즘 입력으로 활용 될 수 있다. 
import org.apache.spark.ml.feature.NGram
val unigram = new NGram().setInputCol("DescOut").setN(1)
unigram.transform(tokenized.select("DescOut")).show(false)


val bigram = new NGram().setInputCol("DescOut").setN(2)
bigram.transform(tokenized.select("DescOut")).show(false)



//단어를 숫자로 변환한다. 단어 특징을 생성하고 모델에서 사용하기 위해서는 단어와 단어 조합 수를 세어야 한다. 
//CountVectorizer를 이용하면 단어를 셀 수 있으며 TF-IDF 변환을 통해 모든 문서에서 주어진 단어의 발생 빈도에 따라 단어의 가중치를 재측정 할 수 있다. 
//모든 row를 문서로 취급하고 모든 단어를 term으로 취급하여 모든 용어의 집합을 voca로 인식한다
//countVec 컬럼은 총 어휘 크기, 어휘에 포함된 단어 색인, 특정 단어의 출현빈도를 나타냄 
import org.apache.spark.ml.feature.CountVectorizer
val cv = new CountVectorizer()
  .setInputCol("DescOut")
  .setOutputCol("countVec")
  .setVocabSize(500) //최대 voca 크기
  .setMinTF(1) //최소 용어 빈도
  .setMinDF(2) //최소 문서 빈도
val fittedCV = cv.fit(tokenized)
fittedCV.transform(tokenized).show(false)



//텍스트를 숫자로 변환하기 위해 얼마나 많은 문서가 그 용어를 포함하고 있는지에 따라 가중치를 부여하면서 특정 용어가 각 문서에서 얼마나 자주 나타내는지 측정
// 이때 여러 문서에서 발생하는 용어보다 적은 문서에서 출현하는 용어에 더 많은 가중치가 부여됨, 비슷한 주제를 가진 문서를 찾는데도 사용가능 
//아래의 예제에서는 red 가 포함된 문서를 찾아본다
val tfIdfIn = tokenized
  .where("array_contains(DescOut, 'red')")
  .select("DescOut")
  .limit(10)
tfIdfIn.show(false)


//TF-IDF에 값을 넣으려면 각 단어를 해싱하여 수치형으로 변환한 다음 역문서 빈도에 따라 어휘에 포함된 각 단어에 가중치를 부여한다.
//해싱은 CountVertorizer와 비슷한 과정이지만 결과를 되돌릴수 없다. 

import org.apache.spark.ml.feature.{HashingTF, IDF}
val tf = new HashingTF()
  .setInputCol("DescOut")
  .setOutputCol("TFOut")
  .setNumFeatures(10000)
val idf = new IDF()
  .setInputCol("TFOut")
  .setOutputCol("IDFOut")
  .setMinDocFreq(2)



//red에 특정 값이 할당되고 벡터에는 총 어휘크기 문서에 나타내는 모든 단어의 해시, 각 용어의 가중치 등이 표현된다. 
idf.fit(tf.transform(tfIdfIn)).transform(tf.transform(tfIdfIn)).show(false)



//Word2Vec은 단어 집합의 벡터 표현을 계산하는 기법으로 비슷한 단어를 벡터 공간에 서로 가깝게 배치하여 단어를 일반화한다. 각 단어간의 관계를 파악하는데 유용하다.
import org.apache.spark.ml.feature.Word2Vec
import org.apache.spark.ml.linalg.Vector
import org.apache.spark.sql.Row
// Input data: Each row is a bag of words from a sentence or document.
//입력데이터는 각 row의 bag of words이다. 
val documentDF = spark.createDataFrame(Seq(
  "Hi I heard about Spark".split(" "),
  "I wish Java could use case classes".split(" "),
  "Logistic regression models are neat".split(" ")
).map(Tuple1.apply)).toDF("text")


// Learn a mapping from words to Vectors.
//단어를 벡터에 매핑한다
val word2Vec = new Word2Vec()
  .setInputCol("text")
  .setOutputCol("result")
  .setVectorSize(3)
  .setMinCount(0)
  
val model = word2Vec.fit(documentDF)
val result = model.transform(documentDF)
result.collect().foreach { case Row(text: Seq[_], features: Vector) =>
  println(s"Text: [${text.mkString(", ")}] => \nVector: $features\n")
}

result.show(false)

model.explainParams


model.findSynonyms("Spark", 3).show()  //3- number of synonyms to find, find cosineSimilarity

//UnaryTransformer 는 사용자 정의 Transformer입니다. ML파이프라인에서 구동될 수 있도록 아래와 같이 토크나이저를 구현합니다
import org.apache.spark.ml.UnaryTransformer
import org.apache.spark.ml.util.{DefaultParamsReadable, DefaultParamsWritable,
  Identifiable}
import org.apache.spark.sql.types.{ArrayType, StringType, DataType}
import org.apache.spark.ml.param.{IntParam, ParamValidators}
//Custom Tokenizer를 만들수 있습니다
class MyTokenizer(override val uid: String) extends UnaryTransformer[String, Seq[String], MyTokenizer] with DefaultParamsWritable {

  def this() = this(Identifiable.randomUID("myTokenizer"))

  val maxWords: IntParam = new IntParam(this, "maxWords",
    "The max number of words to return.",
  ParamValidators.gtEq(0))

  def setMaxWords(value: Int): this.type = set(maxWords, value)

  def getMaxWords: Integer = $(maxWords)

  override protected def createTransformFunc: String => Seq[String] = (
    inputString: String) => {
      inputString.split(" ").take($(maxWords))
  }

  override protected def validateInputType(inputType: DataType): Unit = {
    require(
      inputType == StringType, s"Bad input type: $inputType. Requires String.")
  }

  override protected def outputDataType: DataType = new ArrayType(StringType,
    true)
}


// this will allow you to read it back in by using this object.
object MyTokenizer extends DefaultParamsReadable[MyTokenizer]



//Cusotom tokernizer를 호출하고 기존 데이터프레임을 transform합니다
val myT = new MyTokenizer().setInputCol("someCol").setOutputCol("someOut").setMaxWords(2)
myT.transform(Seq("hello world. This text won't show.").toDF("someCol")).show(false)

