/*
 *
 *      This software is a result of Quaero project and its use must respect the rules of the Quaero Project Consortium Agreement.
 *
 *      Copyright Institut National de la Recherche Agronomique, 2012.
 *
 */
package fr.inra.mig.tydiws.db

import org.squeryl._
import org.squeryl.dsl._
import net.liftweb.json._
import net.liftweb.common.Logger
import net.liftweb.json.JsonDSL._
import scala.util.parsing.json.JSON._

import net.liftweb.util.Props
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.adapters.H2Adapter
import org.squeryl.adapters.PostgreSqlAdapter

object TydiSession {
  def create(schema : String) = {
    Props.requireOrDie("tydidb.server", "tydidb.port", "tydidb.dbname", "tydidb.username", "tydidb.password")
    Class.forName("org.postgresql.Driver");
    
    val jdbcUrl = "jdbc:postgresql://" + Props.get("tydidb.server", "") + ":" + Props.getInt("tydidb.port", 0) + "/" + Props.get("tydidb.dbname", "")

    val connection = java.sql.DriverManager.getConnection(jdbcUrl, Props.get("tydidb.username", ""), Props.get("tydidb.password", ""))
    
    val statement = connection.createStatement
    statement.execute("SET SEARCH_PATH TO " + schema)
    Session.create(connection, new PostgreSqlAdapter)
  }
}
