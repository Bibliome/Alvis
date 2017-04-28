/*
 *
 *      This software is a result of Quaero project and its use must respect the rules of the Quaero Project Consortium Agreement.
 *
 *      Copyright Institut National de la Recherche Agronomique, 2012.
 *
 */
package fr.inra.mig.cdxws.api

import java.io.File
import java.io.FileInputStream
import java.text.SimpleDateFormat

import net.liftweb.common.{Box,Empty,Failure,Full,Logger,EmptyBox}
import net.liftweb.http._
import net.liftweb.http.auth.{AuthRole,userRoles}
import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import net.liftweb.json.Serialization.write
import net.liftweb.util.BasicTypesHelpers._

import org.squeryl.PrimitiveTypeMode._
import fr.inra.mig.cdxws.db.CadixeDB.UsersAutorizations
import fr.inra.mig.cdxws.db._
import fr.inra.mig.cdxws.db.AnnotationSetType._

object RestAPI {
  implicit val formats = Serialization.formats(NoTypeHints)
  val ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z")

  def user_json(user : User) = { 
    transaction {
      ("id" -> user.id) ~
      ("login" -> user.login) ~
      ("campaigns" -> (user.annotates_in map { c => ("id" -> c.id) ~ ("name" -> c.name) }))
    }
  }

  def user_withauths_json(user : User) = { 
    transaction {
      val userCampaigns = user.annotates_in
      val userCampaignIds = userCampaigns map { c => c.id }
      //return info for only campaigns for which the user has at least one authorization
      val userCampaignAuths = from(CadixeDB.user_campaignauthorizations)((uca) =>
        where(uca.user_id === user.id and uca.campaign_id.in { userCampaignIds })
        select(uca)
        orderBy(uca.campaign_id asc, uca.auth_id asc)).toList
      val userByCampaignAuths = userCampaignAuths groupBy (_.campaign_id) map { case (k,v) => k -> (v map (_.auth_id) ) }
      
      ("id" -> user.id) ~
      ("login" -> user.login) ~
      ("is_admin" -> user.is_admin) ~
      ("campaigns" -> (userCampaigns map { c => ("id" -> c.id) ~ ("name" -> c.name) }))  ~
      ("authorizations" -> 
       ("global" -> (user.has_authorizations map { a => a.id })) ~
       ("bycampaign" -> (userByCampaignAuths map { case (campaign_id, auth_ids) => (campaign_id.toString -> auth_ids) }))
      )
    }
  }
  
  def user_allCampaignAuths_json  (user : User) = { 
    val allCampaignAuths = CadixeDB.getUserAuthsForAllCampaigns(user.id)

    val userByCampaignAuths = allCampaignAuths groupBy (_._1.id) map { case (k,v) => 
        k -> (v  map ( _._2 match { case Some(uca) => Some(uca.auth_id) case None => None }) ).flatten }
      
    ("global" -> (user.has_authorizations map { a => a.id })) ~
    ("bycampaign" -> (userByCampaignAuths map { case (campaign_id, auth_ids) => (campaign_id.toString -> auth_ids) }))
  }

  def user_campaign_json(user : User, campaign : Campaign) = {
    transaction {
      ("documents" -> (CadixeDB.documents_by_user_and_campaign(user,campaign) map { case (d,da) => 
              ("id" -> d.id) ~ 							   
              ("description" -> d.description) ~
              ("started_at" -> (da.started_at map { d => ft.format(d) } getOrElse "")) ~
              ("finished_at" -> (da.finished_at map { d => ft.format(d) } getOrElse "")) }))
    }
  }

  def json_of_annotation_set(as : AnnotationSet) = (
    ("id" -> as.id) ~
    ("owner" -> as.user_id) ~
    ("type" -> as.`type`.toString) ~
    ("timestamp" -> ft.format(as.created)) ~
    ("revision" -> as.revision) ~
    ("description" -> as.description) ~
    ("head" -> as.head) ~
    ("text_annotations" -> parse(as.text_annotations)) ~
    ("groups" -> parse(as.groups)) ~
    ("relations" -> parse(as.relations))
  )

  def json_summary_of_annotation_set(as : AnnotationSet) = (
    ("id" -> as.id) ~
    ("owner" -> as.user_id) ~
    ("type" -> as.`type`.toString) ~
    ("timestamp" -> ft.format(as.created)) ~
    ("revision" -> as.revision) ~
    ("description" -> as.description) ~
    ("head" -> as.head) ~
    ("text_annotations" -> parse(as.text_annotations).extract[List[TextAnnotation]].length) ~
    ("groups" -> parse(as.groups).extract[List[Group]].length) ~
    ("relations" -> parse(as.relations).extract[List[Relation]].length)
  )

  def user_campaign_document_json(user : User, campaign : Campaign, doc : Document, da : DocumentAssignment, forcedAnnotationSetTypes : Set[AnnotationSetType] = Set()) = {
    transaction {
      val as = 
        CadixeDB.annotation_sets.where(as => as.doc_id === doc.id and as.user_id === user.id 
                                       and as.campaign_id === campaign.id and as.head === true and as.`type` === UserAnnotation).headOption match {
          case None => {
              //if no prexisting User's Annotation Set, then create it now
              val as = AnnotationSet(doc.id,user.id,campaign.id,
                                     write(Nil),write(Nil),write(Nil),
                                     true,0,UserAnnotation, user.login + "'s annotation", CadixeDB.now())
              da.started_at = Some(CadixeDB.now())
              CadixeDB.document_assignment.update(da)
              CadixeDB.annotation_sets.insert(as)
            }
          case Some(as) => as
        }
      
      //Formatting AnnotationSet is optional, but if it exists, it must always be sent to client 
      //Force loading of AlvisNLP AnnotationSet if it exists
      val otherAnnotationSetTypes = forcedAnnotationSetTypes.union(Set(HtmlAnnotation, AlvisNLPAnnotation))

      val otherAnnotationSets = otherAnnotationSetTypes.foldLeft(List() : List[AnnotationSet])( (accu, asType) => {
          val otherAnnSet = 
            CadixeDB.annotation_sets.where(as => as.doc_id === doc.id and as.campaign_id === campaign.id 
                                           and as.head === true and as.`type` === asType).toList
          otherAnnSet ++ accu
        })
      
      val completeAnnSets = otherAnnotationSets ++ CadixeDB.annotationSetLookup(as.id :: Nil)
      val jsCompleteAnnSets = completeAnnSets.map(as => json_of_annotation_set(as))
                              
      val camp = CadixeDB.campaigns.lookup(campaign.id).get
      
      ("document" -> (
          ("id" -> doc.id) ~
          ("contents" -> doc.contents) ~
          ("description" -> doc.description) ~
          ("props" -> parse(doc.props)) ~
          ("owner" -> doc.owner))) ~
      ("annotation" -> jsCompleteAnnSets) ~
      ("annotation_sets" -> {
          val asets = CadixeDB.annotation_sets.where(as => as.doc_id === doc.id and 
                                                     as.campaign_id === campaign.id and 
                                                     as.head === true) 
          asets.toList.map { json_summary_of_annotation_set(_) }
        }) ~
      ("schema" -> parse(camp.schema))
    }
  }
  
  def getDocument(user_id : Long, campaign_id : Long, doc_id : Long, forcedAnnotationSetTypes : Set[AnnotationSetType] = Set()) : Option[JObject] = {
    transaction { 
      import CadixeDB._
      // searches for a campaign_id containing doc_id assigned to user_id
      from(campaigns,users,documents,document_assignment)((c,u,d,da) => 
        where(da.user_id === user_id and da.campaign_id === campaign_id and da.doc_id === doc_id and
              u.id === user_id and c.id === campaign_id and d.id === doc_id)
        select(c,u,d,da)).headOption match {
        case Some((campaign,user,doc,doc_assignment)) => 
          Some(user_campaign_document_json(user,campaign,doc,doc_assignment, forcedAnnotationSetTypes))
        case None => 
          None
      }
    }
  }
  // ---------------------------------------------------------------------------

  case class ConflictResponse(message : String) extends LiftResponse with HeaderDefaults {  
    def toResponse = InMemoryResponse(message.getBytes("UTF-8"), "Content-Type" -> "text/plain; charset=utf-8" :: headers, cookies, 409)    
  }    
    
  // ---------------------------------------------------------------------------
  object user extends RequestVar[Option[User]](None)
  
  def dispatch : LiftRules.DispatchPF = {

    //Retrieve AlvisAE webService version
    case Req("api" :: "version" :: Nil,_,GetRequest) => {
        
        () => Full(JsonResponse( "version" -> 1.0) )
      }

      //List Authorizations
    case Req("api" :: "authorizations" :: Nil,_,GetRequest) => {
        user.is match {
          case Some(user) => 
            user.is_admin match {
              //deny listing authorizations to non-admin
              case false =>
                () =>  Full(ResponseWithReason(ForbiddenResponse(), "Only an admin can perform this operation!"))
              case _ =>
                transaction {
                  val auths = from(CadixeDB.authorizations)((a) =>select(a) orderBy(a.id asc))
                  val jsonResponse = auths map { a => ("auth_id" -> a.id) ~ ("description" -> a.description) ~ ("campaignrelated" -> a.campaignrelated)}                   
                  () => Full(JsonResponse(("authorizations" -> jsonResponse)))
                }
            }
          case None => 
            () => Full(BadResponse())
        }
      }

      //List Campaigns
    case Req("api" :: "campaigns" :: Nil,_,GetRequest) => {
        user.is match {
          case Some(user) => 
            user.is_admin match {
              //deny listing authorizations to non-admin
              case false =>
                () =>  Full(ResponseWithReason(ForbiddenResponse(), "Only an admin can perform this operation!"))
              case _ =>
                transaction {
                  val campaigns = from(CadixeDB.campaigns)((c) =>select(c) orderBy(c.id asc))
                  val jsonResponse = campaigns map { c => ("id" -> c.id) ~ ("name" -> c.name) }                   
                  () => Full(JsonResponse(("campaigns" -> jsonResponse)))
                }
            }
          case None => 
            () => Full(BadResponse())
        }
      }

      //List users registered in AlvisAE
    case Req("api" :: "user" :: Nil,_,GetRequest) => {
        user.is match {
          case Some(user) => 
            user.is_admin match {
              //deny listing users to non-admin
              case false =>
                () =>  Full(ResponseWithReason(ForbiddenResponse(), "Only an admin can perform this operation!"))
              case _ =>
                val withAuthorizations = (S.param("wzauths") ?~ "true").map(toBoolean)==true
                transaction {
                  val users = from(CadixeDB.users)((u) =>select(u) orderBy(u.login asc))
                  val jsonResponse = withAuthorizations match {
                    case true =>
                      users map { u => user_withauths_json(u)}                   
                    case _ =>
                      users map { u => user_json(u)}                   
                  }
                  () => Full(JsonResponse(jsonResponse))
                }
            }
          case None => 
            () => Full(BadResponse())
        }
      }
      
      //Retrieve info about current user
    case Req("api" :: "user" :: "me" :: Nil,_,GetRequest) => {
        user.is match {
          case Some(user) => 
            val withAuthorizations = (S.param("wzauths") ?~ "true").map(toBoolean)==true
            withAuthorizations match {
              case true =>
                () => Full(JsonResponse(user_withauths_json(user)))
              case _ =>
                () => Full(JsonResponse(user_json(user)))
            }
          case None => 
            () => Full(BadResponse())
        }
      }

      //Retrieve Authorization info about another user
    case Req("api" :: "user" :: AsLong(user_id) :: "authorizations" :: Nil,_,GetRequest) => {
        user.is match {
          case Some(user) => 
            user.is_admin match {
              //deny retrieving Authorization info about other users to non-admin
              case false =>
                () =>  Full(ResponseWithReason(ForbiddenResponse(), "Only an admin can perform this operation!"))
              case _ =>
                () => transaction { CadixeDB.users.where(u => u.id === user_id).headOption  match {
                    case None =>
                      Full(ResponseWithReason(NotFoundResponse(), "No User identified by id " + user_id+ " could be found"))
                    case Some(user) => 
                      Full(JsonResponse(user_allCampaignAuths_json(user)))
                  }
                }
            }
          case None => 
            () => Full(BadResponse())
        }
      }
      
      //Reset Authorizations info of an user
    case req @ Req("api" :: "user" :: AsLong(user_id) :: "authorizations" :: Nil,_,PutRequest) => {
        user.is match {
          case Some(user) => 
            user.is_admin match {
              //deny updating user's Authorizations to non-admin
              case false =>
                () =>  Full(ResponseWithReason(ForbiddenResponse(), "Only an admin can perform this operation!"))
              case _ =>
                () => transaction { 
                  CadixeDB.users.where(u => u.id === user_id).headOption  match {
                    case None =>
                      Full(ResponseWithReason(NotFoundResponse(), "No User identified by id " + user_id+ " could be found"))
                    case Some(user) => 

                      jsonBody(req) match {
                        case Full(json) => 
                          try {
                            val auths = json.extract[UsersAutorizations]
                            CadixeDB.setUserAuthsForCampaigns(user_id, auths)
                          
                            Full(JsonResponse(user_allCampaignAuths_json(user)))
                          } catch { case ex : MappingException =>
                              Full(ResponseWithReason(BadResponse(), "Invalid user's Authorizations.\n" + ex.getMessage))
                          }
                        case _ => 
                          Full(ResponseWithReason(BadResponse(), "No or Invalid user's Authorizations"))
                      }
                  }
                }
            }
          case None => 
            () => Full(BadResponse())
        }
      }
            
      //create a new user
    case Req("api" :: "user" :: Nil,_,PostRequest) => {
        user.is match {
          case Some(user) => 
            user.is_admin match {
              //deny user creation to non-admin
              case false =>
                () =>  Full(ResponseWithReason(ForbiddenResponse(), "Only an admin can perform this operation!"))
              case _ =>
                () => for(login <- S.param("login").map(_.toString) ?~ "missing login parameter" ~> 400;
                          password <- S.param("passwd").map(_.toString) ?~ "missing passwd parameter" ~> 400;
                          is_admin <- S.param("is_admin").map(_.toBoolean) ?~ "missing is_admin parameter" ~> 400)
                            yield {
                    transaction {
                      CadixeDB.getUserByLogin(login) match {
                        case Some(previousUser) => 
                          ConflictResponse("Can not create new user because the login name '" + login+ "' is already used")
                        case _ => 
                          val newUser = CadixeDB.addUser(login, password, is_admin)
                          JsonResponse(user_withauths_json(newUser))
                      }
                    }
                  }
            }
          case None => 
            () => Full(BadResponse())
        }
      }

      //update an existing user (except password)
    case Req("api" :: "user" :: AsLong(user_id) :: Nil,_,PutRequest) => {
        user.is match {
          case Some(user) => 
            user.is_admin match {
              //deny changing password of other users to non-admin
              case false if user_id != user.id =>
                () =>  Full(ResponseWithReason(ForbiddenResponse(), "Only an admin can perform this operation!"))
              case _ =>
                () => for(login <- S.param("login").map(_.toString) ?~ "missing login parameter" ~> 400;
                          is_admin <- S.param("is_admin").map(_.toBoolean) ?~ "missing is_admin parameter" ~> 400)
                            yield {
                    transaction {
                      CadixeDB.getUserById(user_id) match {
                        case None => 
                          ResponseWithReason(NotFoundResponse(), "No User identified by id " + user_id+ " could be found")
                        case _ => 
                          CadixeDB.getUserByLogin(login) match {
                            case Some(previousUser) if (previousUser.id!=user_id) => 
                              ConflictResponse("Can not apply modification because the login name '" + login+ "' is already used")
                            case _ => 
                              import CadixeDB._
                              CadixeDB.users.update(user =>
                                where(user.id === user_id)
                                set(user.login := login, 
                                    user.is_admin := is_admin)
                              )  
                              val updated = CadixeDB.getUserById(user_id) match { case Some(u) => u ; case _ => null }
                              JsonResponse(user_withauths_json(updated))
                          }
                      }
                    }
                  }
            }
          case None => 
            () => Full(BadResponse())
        }
      }

      //change user password
      //here we are using POST method in with the intention of not to transmit the new password as an URL parameter
    case Req("api" :: "user" :: AsLong(user_id) :: "chpasswd" :: Nil,_,PostRequest) => {
        user.is match {
          case Some(user) => 
            () => for(newPasswd <- S.param("passwd").map(_.toString) ?~ "missing passwd parameter" ~> 400)
              yield {
            
                user.is_admin match {
                  //deny changing password of other users to non-admin
                  case false if user_id != user.id =>
                    ResponseWithReason(ForbiddenResponse(), "Only an admin can perform this operation!")
                  case _ =>
                    newPasswd.isEmpty match {
                      case true =>
                        ResponseWithReason(BadResponse(), "New password must not be empty")
                      case _ =>
                        transaction {
                          val getUserToChange = (user.id == user_id) match {
                            case true => 
                              Some(user)
                            case _ => 
                              import CadixeDB._
                              val otherUser = from(users)((u) => where(u.id === user_id) select(u)).headOption
                              otherUser
                          }
                          getUserToChange match {
                            case Some(userToChange) => 
                              CadixeDB.ChangeUserPassword(userToChange, newPasswd)
                              //Console.err.println("Password successfully changed")
                              JsonResponse(user_json(user))
                            case None =>  
                              ResponseWithReason(BadResponse(), "Specified user not found")
                          }
                        }
                    }
                }
              }
          case None => 
            () => Full(BadResponse())
        }
      }
      
    case Req("api" :: "user" :: AsLong(user_id) :: "campaign" :: AsLong(campaign_id) :: "documents" :: Nil,_,GetRequest) => {
        transaction { 
          import CadixeDB._
          //TODO add "viewDocuments" Authorization check here!
          //
          // is there a campaign_id where user_id is flagged as annotator ?
          from(campaigns,users,campaign_annotators)((c,u,ca) => 
            where(ca.user_id === user_id and ca.campaign_id === campaign_id and
                  u.id === user_id and c.id === campaign_id)
            select(c,u)).headOption match {
            case Some((campaign,user)) => 
              () => Full(JsonResponse(user_campaign_json(user,campaign)))
            case None => 
              () => Full(BadResponse())
          }
        }
      }


    case Req("api" :: "user" :: AsLong(user_id) :: "campaign" :: AsLong(campaign_id) :: "document" :: AsLong(doc_id) :: Nil,_,GetRequest) => {
        //TODO add "viewDocuments" Authorization check here!
        getDocument(user_id, campaign_id, doc_id) match {
          case Some(jsonDoc) => 
            () => Full(JsonResponse(jsonDoc))
          case None => 
            () => Full(BadResponse())        
        }
      }

      
    case req @ Req("api" :: "user" :: AsLong(user_id) :: "campaign" :: AsLong(campaign_id) :: "document" :: AsLong(doc_id) :: Nil,_,PutRequest) => {
        jsonBody(req) match {
          case Full(json) => transaction { 
              import CadixeDB._
              //TODO add "createAnnotations" Authorization check here!

              // searches for a campaign_id containing doc_id assigned to user_id
              from(campaigns,users,documents,document_assignment,annotation_sets)((c,u,d,da,as) => 
                where(da.user_id === user_id and da.campaign_id === campaign_id and da.doc_id === doc_id and
                      u.id === user_id and c.id === campaign_id and d.id === doc_id and
                      as.doc_id === d.id and as.campaign_id === campaign_id and as.user_id === user_id and as.head === true)
                select(c,u,d,as)).headOption match {
                case Some((campaign,user,doc,as)) => {
                    val nas = AnnotationSet(as.doc_id,as.user_id,as.campaign_id,
                                            compact(render(json \\ "text_annotations")),
                                            compact(render(json \\ "groups")),
                                            compact(render(json \\ "relations")),
                                            true, as.revision + 1, as.`type`, as.description,CadixeDB.now())
                    CadixeDB.annotation_sets.insert(nas)
                    as.head = false
                    CadixeDB.annotation_sets.update(as)
                    () => Full(OkResponse())
                  }
                case None => 
                  () => Full(BadResponse())
              }
            }
          case _ => () => Full(BadResponse())
        }
      }

      //retrieve document by its external id (given by AlvisNLP)
    case Req("api" :: "user" :: AsLong(user_id) :: "AlvisNLPID" :: alvisNLP_id :: Nil,_,GetRequest) => {
        transaction { 
          import CadixeDB._
          //TODO add "viewDocuments" Authorization check here!
        
          from(campaign_documents)((cd) => 
            where(cd.alvisnlp_id === Option(alvisNLP_id))
            select(cd)).headOption match {
            case Some((cdoc)) => 
              //Force to send the AlvisNLP AnnotationSet in addition to user's annoation set
              getDocument(user_id, cdoc.campaign_id, cdoc.doc_id, Set(AlvisNLPAnnotation)) match {
                case Some(jsonDoc) => 
                  //return campaign_id and doc_id along with the document itself
                  val resp =  ("doc_id" -> cdoc.doc_id) ~ ("campaign_id" -> cdoc.campaign_id) ~ ("doc" -> jsonDoc)
                  () => Full(JsonResponse(resp))
                case None => 
                  () => Full(ResponseWithReason(BadResponse(), "Document not assigned to user"))
              }
            case None => 
              () => Full(ResponseWithReason(BadResponse(), "Unknown document") )
          }
        }
      }
      
    case Req("api" :: "user" :: AsLong(user_id) :: "campaign" :: AsLong(campaign_id) :: "document" :: AsLong(doc_id) :: "finalize" :: Nil,_,PutRequest) => {
        transaction { 
          import CadixeDB._
          //TODO add "createAnnotations" Authorization check here!
          
          // searches for a campaign_id containing doc_id assigned to user_id
          from(users,document_assignment,annotation_sets)((u,da,as) => 
            where(da.user_id === user_id and da.campaign_id === campaign_id and da.doc_id === doc_id and
                  as.doc_id === doc_id and as.campaign_id === campaign_id and as.user_id === user_id and as.head === true and
                  u.id === user_id)
            select(da)).headOption match {
            case Some(da) => {
                da.finished_at = Some(CadixeDB.now())
                CadixeDB.document_assignment.update(da)
                () => Full(OkResponse())
              }
            case None => 
              () => Full(BadResponse())
          }
        }
      }


    case Req("api" :: "annotation" :: Nil,_,GetRequest) => {
        user.is match {
          case Some(user) => transaction {
              //TODO add "viewOthersAnnotations" Authorization check here!
              
              val ids = S.param("ids").dmap(Array[Long]())(s => s.split(",").map(_.toLong)).toList
              val asets = CadixeDB.annotationSetLookup(ids)
              val res = asets.map(json_of_annotation_set(_))
              () => Full(JsonResponse(res))
            }
          case None => 
            () => Full(BadResponse())
        }
      }
    
      
    case Req("api" :: "user" :: AsLong(user_id) :: "campaign" :: AsLong(campaign_id) :: "annotations" :: "CSV" :: Nil,_,GetRequest) => {
        CadixeDB.createSession().bindToCurrentThread;
        //TODO add "exportAnnotations" Authorization check here!
              
        val tempDir = Utils.createTempDir()
        val workingDirBaseName = "ExportAlvisAE"
        val workingDir = new File(tempDir.getAbsolutePath + "/" + workingDirBaseName + "/")
        workingDir.mkdir()
        Utils.exportCampaignAnnotationAsCSV(workingDir.getAbsolutePath, campaign_id, true)
        val archiveBaseName = "aae_" + campaign_id + ".zip"
        val archiveAbsoluteName = tempDir.getAbsolutePath + "/" + archiveBaseName
        Utils.createZipFromFolder(workingDir.getAbsolutePath, archiveAbsoluteName, true)
        
        val stream = new FileInputStream(archiveAbsoluteName)
        
        () => Full(StreamingResponse(stream, () => stream.close, stream.available, List("Content-Type" -> "application/zip"), Nil, 200))
      }
  }
  
  def jsonBody(req : Req) = 
    req.body.map(bytes => new String(bytes, "UTF-8")) map parse

  def protection : LiftRules.HttpAuthProtectedResourcePF = {
    case Req("api" :: "user" :: _,_,_) => Full(AuthRole("logged"))
    case Req("api" :: "annotation" :: _,_,_) => Full(AuthRole("logged"))
    case Req("api" :: "authorizations" :: _,_,_) => Full(AuthRole("logged"))
    case Req("api" :: "campaigns" :: _,_,_) => Full(AuthRole("logged"))
  }
}
