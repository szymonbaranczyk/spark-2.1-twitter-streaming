package main



import com.typesafe.config.ConfigFactory
import org.apache.spark._
import org.apache.spark.sql.{DataFrame, SQLContext, SaveMode, SparkSession}
import org.apache.spark.streaming._
import org.apache.spark.streaming.twitter._

/**
  * Created by Szymon Baranczyk on 2017-01-17.
  */

object Main extends App {

  lazy val conf = ConfigFactory.load()
  lazy val spark = SparkSession.builder()
    .appName(conf.getString("spark.appname"))
    .master("spark://" + conf.getString("spark.domain") + ":" + conf.getInt("spark.port"))
    .config("spark.cores.max", "2")
    .getOrCreate()

  def creatingFunc(): StreamingContext = {
    val ssc = new StreamingContext(spark.sparkContext, Seconds(5))
    ssc.checkpoint(conf.getString("spark.streaming.checkpoint"))
    val stream = TwitterUtils.createStream(ssc, None)
    stream
      .filter(s => s.getLang == "en")
      .foreachRDD(
      (rdd) => {
        val spark = SparkSession.builder.config(rdd.sparkContext.getConf).getOrCreate()

        import spark.implicits._

        val df: DataFrame = rdd.map(s => TwitterStatus(new java.sql.Date(s.getCreatedAt.getTime), s.getText, s.isRetweet, s.getHashtagEntities.map(h => h.getText))).toDF("time",  "text", "isRetweet", "hashtags")
        df.write.mode(SaveMode.Append).saveAsTable("TwitterCount")
      }
    )
    ssc
  }

  val s = StreamingContext.getActiveOrCreate(creatingFunc)
  s.start()
  scala.io.StdIn.readLine()
  s.stop(stopSparkContext = true, stopGracefully = true)
}

case class TwitterStatus(time:java.sql.Date, text: String, isRetweet: Boolean, hashtags: Array[String])