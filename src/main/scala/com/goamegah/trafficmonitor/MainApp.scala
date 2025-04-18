package com.goamegah.trafficmonitor

import com.goamegah.trafficmonitor.db.DBSchemaManager
import com.goamegah.trafficmonitor.streaming.TrafficStreamProcessor

object MainApp {
  def main(args: Array[String]): Unit = {

    // Initialisation du schéma PostgreSQL
    //DBSchemaManager.init()

    // Démarrage du streaming
    //TrafficStreamProcessor.start()

    // Arrêt du streaming après 5 minutes
    // Thread.sleep(5 * 60 * 1000) // 5 minutes

    // Arrêt du streaming
    // TrafficStreamProcessor.stop()

    // Nettoyage du schéma PostgreSQL
    // DBSchemaManager.cleanup()

    // Arrêt de l'application
    // spark.stop()

  }
}
