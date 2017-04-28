/*
 *
 *      This software is a result of Quaero project and its use must respect the rules of the Quaero Project Consortium Agreement.
 *
 *      Copyright Institut National de la Recherche Agronomique, 2012.
 *
 */
package fr.inra.mig.cdxws.api

import java.text.SimpleDateFormat

import net.liftweb.common.{Box,Empty,Failure,Full,Logger,EmptyBox}
import net.liftweb.http._
import net.liftweb.http.auth.{AuthRole,userRoles}
import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import net.liftweb.json.Serialization.write
import net.liftweb.util.BasicTypesHelpers._

import org.squeryl.PrimitiveTypeMode._
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
      val otherAnnotationSetTypes = forcedAnnotationSetTypes.union(Set(HtmlAnnotation))

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
  

  object user extends RequestVar[Option[User]](None)

  def dispatch : LiftRules.DispatchPF = {

    case Req("api" :: "user" :: "me" :: Nil,_,GetRequest) => {
        user.is match {
          case Some(user) => 
            () => Full(JsonResponse(user_json(user)))
          case None => 
            () => Full(BadResponse())
        }
      }

    case Req("api" :: "user" :: AsLong(user_id) :: Nil,_,GetRequest) => {
        transaction { CadixeDB.users.where(u => u.id === user_id).headOption } match {
          case Some(user) => 
            () => Full(JsonResponse(user_json(user)))
          case None => 
            () => Full(BadResponse())
        }
      }

    case Req("api" :: "user" :: AsLong(user_id) :: "campaign" :: AsLong(campaign_id) :: "documents" :: Nil,_,GetRequest) => {
        transaction { 
          import CadixeDB._
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
              val ids = S.param("ids").dmap(Array[Long]())(s => s.split(",").map(_.toLong)).toList
              val asets = CadixeDB.annotationSetLookup(ids)
              val res = asets.map(json_of_annotation_set(_))
              () => Full(JsonResponse(res))
            }
          case None => 
            () => Full(BadResponse())
        }
      }
    


  }
  
  def jsonBody(req : Req) = 
    req.body.map(bytes => new String(bytes, "UTF-8")) map parse

  def protection : LiftRules.HttpAuthProtectedResourcePF = {
    case Req("api" :: "user" :: "me" :: Nil,_,_) => Full(AuthRole("logged"))
    case Req("api" :: "user" :: user_id :: _,_,_) => Full(AuthRole("user:" + user_id))
    case Req("api" :: "annotation" :: _,_,_) => Full(AuthRole("logged"))
  }
}
