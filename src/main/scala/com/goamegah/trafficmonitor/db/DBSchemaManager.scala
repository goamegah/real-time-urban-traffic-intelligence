package com.goamegah.trafficmonitor.db

import java.nio.file.{Files, Paths}
import java.sql.{Connection, DriverManager}
import scala.io.Source
import java.io.File
import com.goamegah.trafficmonitor.config.AppConfig
import com.goamegah.trafficmonitor.load.PostgresLoader.getClass
import org.slf4j.LoggerFactory

object DBSchemaManager {
  private val logger = LoggerFactory.getLogger(getClass)

  private def getConnection(): Connection = {
    DriverManager.getConnection(
      AppConfig.Postgres.jdbcUrl,
      AppConfig.Postgres.user,
      AppConfig.Postgres.password
    )
  }

  private def executeSqlFile(path: String)(implicit conn: Connection): Unit = {
    val file = new File(path)
    if (!file.exists()) {
      logger.warn(s"[WARN] Le fichier $path n’existe pas. Ignoré.")
      return
    }

    val sql = Source.fromFile(file).getLines().mkString("\n")
    try {
      val stmt = conn.createStatement()
      stmt.execute(sql)
      logger.info(s"[OK] Script exécuté avec succès : ${file.getName}")
    } catch {
      case ex: Exception =>
        logger.warn(s"[/!\\] Erreur lors de l'exécution du script ${file.getName} : ${ex.getMessage}")
    }
  }

  def init(): Unit = {
    logger.info("[...] Initialisation du schéma PostgreSQL...")

    implicit val conn: Connection = getConnection()

    try {
      // Exécution des tables
      executeSqlFile("ddl/tables/create_road_traffic_stats_minute.sql")
      executeSqlFile("ddl/tables/create_road_traffic_stats_hour.sql")
      executeSqlFile("ddl/tables/create_road_traffic_stats_sliding_window.sql")

      // Exécution des vues
      val viewDir = new File("ddl/views/")
      if (viewDir.exists && viewDir.isDirectory) {
        val viewFiles = viewDir.listFiles().filter(_.getName.endsWith(".sql")).sorted
        viewFiles.foreach(view => executeSqlFile(view.getPath))
      } else {
        logger.warn("[WARN] Le dossier 'ddl/views/' est introuvable.")
      }

      logger.warn("[#] Schéma PostgreSQL initialisé.")
    } finally {
      conn.close()
    }
  }

  def cleanup(): Unit = {
    println("[...] Nettoyage du schéma PostgreSQL...")
    implicit val conn: Connection = getConnection()
    try {
      executeSqlFile("ddl/cleanup/drop_all.sql")
      logger.info("[OK] Schéma PostgreSQL nettoyé.")
    } finally {
      conn.close()
    }
  }
}
