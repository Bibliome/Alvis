/*
 *
 *      This software is a result of Quaero project and its use must respect the rules of the Quaero Project Consortium Agreement.
 *
 *      Copyright Institut National de la Recherche Agronomique, 2012.
 *
 */
package bootstrap.liftweb

import _root_.net.liftweb.common._
import _root_.net.liftweb.util._
import _root_.net.liftweb.http._
import _root_.net.liftweb.sitemap._
import _root_.net.liftweb.sitemap.Loc._
import Helpers._
import net.liftweb.http.auth.{AuthRole,HttpBasicAuthentication,userRoles}
import org.squeryl.{Session,SessionFactory}
import org.squeryl.PrimitiveTypeMode._

import fr.inra.mig.tydiws.api._
import fr.inra.mig.tydiws.db._

/**
  * A class that's instantiated early and run.  It allows the application
  * to modify lift's environment
  */
class Boot {
  val logger = Logger(classOf[Boot])

  /* The purpose of this class is to disable the prompt launched by the navigator
   * when authentication fails. This is done by sending a response with an empty
   * header instead of the usual challenge tag
   */
  class CustomUnauthorizedResponse(realm: String) extends UnauthorizedResponse(realm) {  
    override def toResponse = InMemoryResponse(Array(), List(), Nil, 401)  
  }

  class CustomHttpBasicAuthentication(realmName : String)(func : PartialFunction[(String, String, Req), Boolean]) extends HttpBasicAuthentication(realmName)(func) {
    override def unauthorizedResponse = new CustomUnauthorizedResponse(realmName)
  }

    def boot {
    // where to search snippet
    LiftRules.addToPackages("fr.inra.mig.tydiws")

    //Check if configuration is parametrized by a custom resource in JNDI
    val configFile = ConfigResourceLocator.getConfigFile(LiftRules.context.path)
    SessionFactory.concreteFactory = 
      if(configFile != null) 
        //use property file specified in JNDI to retreive DB connection parameters
        Some(() => TydiDB.createSession(configFile))
      else 
        //use embedded property file to retreive DB connection parameters
        Some(() => TydiDB.createSession())
    
    LiftRules.dispatch.prepend(RestAPI.dispatch)
    LiftRules.httpAuthProtectedResource.append(RestAPI.protection)
    
    LiftRules.authentication = new CustomHttpBasicAuthentication("tydiws")({
        case (login, password,_) => {
            transaction {
              TydiDB.check_login(login, password) match {
                case Some(user) => {
                    val admin_role = if(user.appliadmin) { AuthRole("admin") :: Nil } else { Nil }
                    userRoles(AuthRole("logged") :: admin_role)
                    RestAPI.user(Full(user))
                    true
                  } 
                case None => {
                    RestAPI.user(Failure("Authentication failed"))
                    false
                  }
              }
            }
          }
      })

    // Build SiteMap
    val entries = Menu(Loc("Home", List("index"), "Home")) :: Nil
    LiftRules.setSiteMap(SiteMap(entries:_*))
  }
}

