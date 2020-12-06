val jsonDF = spark.range(1).selectExpr("""
  '{"myJSONKey" : {"myJSONValue" : [1, 2, 3]}}' as jsonString""")

//json값에서 array의 1번째 값을 추출하여 column으로 정의하고 중첩이 없는 경우라면 json tuple을 생성하여 json 컬럼으로 정의 
import org.apache.spark.sql.functions.{get_json_object, json_tuple}
jsonDF.select(
    get_json_object(col("jsonString"), "$.myJSONKey.myJSONValue[1]") as "column",
    json_tuple(col("jsonString"), "myJSONKey") as "json").show(false)

//struct type을 json format으로 변환
import org.apache.spark.sql.functions.to_json
df.selectExpr("(InvoiceNo, Description) as myStruct")
 .select(to_json(col("myStruct"))).show(false)


//struct type을 만들고 json으로 변경할 수 있고 json에서 다시 객체로 from_json을 통해 변환할 수 있다. 이때, 스키마 선언이 필요하다
import org.apache.spark.sql.functions.from_json
import org.apache.spark.sql.types._
val parseSchema = new StructType(Array(
  new StructField("InvoiceNo",StringType,true),
  new StructField("Description",StringType,true)))
df.selectExpr("(InvoiceNo, Description) as myStruct")
  .select(to_json(col("myStruct")).alias("to_json"))
  .select(from_json(col("to_json"), parseSchema).alias("parsed_json"), col("to_json")).show(2,false)

//json data에서 특정 컬럼을 가져올때, get_json_object 를 사용한다
df.selectExpr("(InvoiceNo, Description) as myStruct")
 .select(to_json(col("myStruct")).alias("struct_to_json"))
 .withColumn("get_invoice_nox`",get_json_object($"struct_to_json", "$.InvoiceNo")).show(false)  
