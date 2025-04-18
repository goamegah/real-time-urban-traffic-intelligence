package com.goamegah.trafficmonitor.testing

import com.goamegah.trafficmonitor.db.DBSchemaManager.getClass
import com.goamegah.trafficmonitor.streaming.TrafficTransformer
import com.goamegah.trafficmonitor.processing.TrafficStatsAggregator
import org.apache.spark.sql.SparkSession
import org.slf4j.LoggerFactory

object TrafficSlidingWindowBatchTest {
  private val logger = LoggerFactory.getLogger(getClass)
  def main(args: Array[String]): Unit = {
    implicit val spark: SparkSession = SparkSession.builder()
      .appName("TrafficSlidingWindowBatchTest")
      .master("local[*]")
      .getOrCreate()

    import spark.implicits._

    // Charger un fichier JSON en mode batch
    val inputPath = "services/orchestrator/data/raw/20250409215402119045.json"
    val rawDF = spark.read
      .option("multiLine", true)
      .json(inputPath)

    println("[✅] Fichier JSON chargé en batch.")
    rawDF.printSchema()
    rawDF.show(2, truncate = false)

    // Appliquer la transformation
    val transformedDF = TrafficTransformer.transform(rawDF)(spark)
    logger.info("[OK] Résultat final après transformation:")
    transformedDF.printSchema()
    transformedDF.show(10, truncate = false)

    // Appeler l'agrégation par sliding window (l'implicit spark est désormais disponible)
    val slidingDF = TrafficStatsAggregator.aggregateBySlidingWindow(transformedDF)
    logger.info("[⏱] Résultat de l'agrégation par sliding window:")
    slidingDF.printSchema()
    slidingDF.show(10, truncate = false)

    spark.stop()
  }
}
