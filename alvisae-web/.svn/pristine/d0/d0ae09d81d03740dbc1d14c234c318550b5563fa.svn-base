/*
 *
 *      This software is a result of Quaero project and its use must respect the rules of the Quaero Project Consortium Agreement.
 *
 *      Copyright Institut National de la Recherche Agronomique, 2012.
 *
 */
package fr.inra.mig.cdxws.db

import java.sql.Connection
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Date

import org.squeryl._
import org.squeryl.dsl._
import net.liftweb.json._
import java.util.Properties
import net.liftweb.json.JsonDSL._

import net.liftweb.util.Props
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.adapters.H2Adapter
import org.squeryl.adapters.PostgreSqlAdapter

import AnnotationSetType._

class DbObject extends KeyedEntity[Long] {
  val id : Long = 0
}

object CadixeDB extends org.squeryl.Schema {
  import net.liftweb.json.Serialization.write
  implicit val formats = Serialization.formats(NoTypeHints)

  val users = table[User]("user")
  val campaigns = table[Campaign]("campaign")
  on(campaigns)(t => declare(
      t.schema is(dbType("TEXT"))
    ))
  val documents = table[Document]("document")
  on(documents)(t => declare(
      t.contents is(dbType("TEXT")),
      t.props is(dbType("TEXT"))
    ))
  val campaign_annotators = manyToManyRelation(campaigns,users,"campaignannotator").via[CampaignAnnotator]((c,u,ca) => (ca.campaign_id === c.id, u.id === ca.user_id)) 
  val campaign_documents  = manyToManyRelation(campaigns,documents,"campaigndocument").via[CampaignDocument]((c,d,cd) => (cd.campaign_id === c.id, d.id === cd.doc_id)) 
  on(campaign_documents)(cd => declare(
      cd.alvisnlp_id is(unique,indexed("campaigndocumentIDX1"))
    ))
  
  val document_assignment = table[DocumentAssignment]("documentassignment")

  /**
   * Table of annotation sets. There are implicit invariants:
   * <ul>
   * <li> unique annotationset per user, document, campaign that has head == true and type == UserAnnotation</li>
   * <li> unique annotationset per user, document, campaign (FIXME: qu'est-ce que j'ai voulu dire ?)</li>
   * </ul>
   */
  val annotation_sets = table[AnnotationSet]("annotationset")
  on(annotation_sets)(t => declare(
      t.text_annotations is(dbType("TEXT")),
      t.groups is(dbType("TEXT")),
      t.relations is(dbType("TEXT"))
    ))


  val annotationset_document = oneToManyRelation(documents,annotation_sets).via((d,as) => d.id === as.doc_id)

  def createH2Session(path : String) = {
    Class.forName("org.h2.Driver");
    Session.create(java.sql.DriverManager.getConnection("jdbc:h2:" + path, "foo", "foo"),
                   new H2Adapter)
  }
  
  def createPGSession(server : String, port : Int, 
                      dbname : String, schema : String,
                      user : String, password : String) = {
    Class.forName("org.postgresql.Driver");
    
    val jdbcUrl = "jdbc:postgresql://" + server + ":" + port.toString + "/" + dbname

    Console.println("Creating Connection... " + jdbcUrl)
    val connection = java.sql.DriverManager.getConnection(jdbcUrl, user, password)
    
    val statement = connection.createStatement
    //Console.println("Default schema... " + schema)
    statement.execute("SET SEARCH_PATH TO " + schema)
    val session = Session.create(connection, new PostgreSqlAdapter)
    //session.setLogger(msg => Console.err.println(msg)) 
    
    session
  }

  def createSession() : Session = {
    Props.get("db.type","h2") match {
      case "postgresql" => {
          Props.requireOrDie("db.server", "db.port", "db.dbname", "db.username", "db.password", "db.schema")
          createPGSession(Props.get("db.server", ""),
                          Props.getInt("db.port", 0),
                          Props.get("db.dbname", ""),
                          Props.get("db.schema", ""),
                          Props.get("db.username", ""),
                          Props.get("db.password", ""))
        }
      case _ => createH2Session(Props.get("db.path","/tmp/cadixedb"))
    }
  }

  def createSession(props : Properties) : Session = {
    props.getProperty("db.type","h2") match {
      case "postgresql" => {
          val required = Set("db.server", "db.port", "db.dbname", "db.username", "db.password", "db.schema")
          val missing = required.foldLeft(List[String]()) { 
            (missingSoFar, propName) => props.keySet.contains(propName) match { 
              case false => propName :: missingSoFar
              case _ =>  missingSoFar
            }
          }
          missing.toList match {
            case Nil =>
            case bad => throw new Exception("The following required properties are not defined: "+bad.mkString(","))
          }
          createPGSession(props.getProperty("db.server", ""),
                          props.getProperty("db.port", "0").toInt,
                          props.getProperty("db.dbname", ""),
                          props.getProperty("db.schema", ""),
                          props.getProperty("db.username", ""),
                          props.getProperty("db.password", ""))
        }
      case _ => createH2Session(Props.get("db.path","/tmp/cadixedb"))
    }
  }
  
  /**
   * Returns a timestamp of the current time
   */
  def now()  = new Timestamp((new Date()).getTime)


// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
  def documents_by_user_and_campaign(u : User, c : Campaign) = {
    from(documents,document_assignment)((d,da) => where(u.id === da.user_id and c.id === da.campaign_id and
                                                        d.id === da.doc_id)
                                        select(d,da))
  }

  def createCampaign(name : String, schema : JValue) = {
    import net.liftweb.json._
    val c = Campaign(name,compact(render(schema)))
    campaigns.insert(c)
    c
  }
  
  def createDocument(user : User, contents : String, 
                     props : Map[String,String] = Map(), comment : String = "", 
                     description : String = "") = {
    val d = Document(user.id,write(props),contents,comment,description)
    documents.insert(d)
    d
  }
  
  def addUserAnnotationSet(doc : Document, user : User, campaign : Campaign,
                           `type` : AnnotationSetType.AnnotationSetType,
                           text_annotations : List[TextAnnotation],
                           relations : List[Relation],
                           groups : List[Group],
                           description : String = "") = {
    val nas = annotation_sets.where(as => as.doc_id === doc.id and as.campaign_id === campaign.id and
                                    as.`type` === `type` and as.head === true).headOption match {
      case None => AnnotationSet(doc.id,user.id,campaign.id,
                                 write(text_annotations), write(groups), write(relations), 
                                 true, 0, `type`, description, now())
      case Some(as) => {
          as.head = false
          annotation_sets.update(as)
          AnnotationSet(doc.id,user.id,campaign.id,
                        write(text_annotations), write(groups), write(relations),
                        true, as.revision + 1, `type`,
                        if(description == "") { as.description } else { description },
                        now())
          
        }
    }
    annotation_sets.insert(nas)
    nas
  }
  
  def addUser(login : String, password : String, is_admin : Boolean) = {
    val u = User(login,password,false)
    users.insert(u)
    u
  }
  
  def addUser2Campaign(user : User, campaign : Campaign) = {
    //FIXME : vérifier si l'utilisateur n'est pas déjà dedans
    campaign_annotators.insert(CampaignAnnotator(campaign.id,user.id))
  }
  
  def addDocument2Campaign(doc : Document, campaign : Campaign, alvisnlp_id : Option[String] = None) = {
    //FIXME : vérifier si le doc n'est pas déjà dedans
    campaign_documents.insert(CampaignDocument(campaign.id,doc.id, alvisnlp_id))
  }

  def assignUser2AllDocuments(user : User, campaign : Campaign) = {
    // FIXME: check that user is already registered in the campaign
    campaign_documents.where(cd => cd.campaign_id === campaign.id).foreach(cd =>
      document_assignment.insert(DocumentAssignment(campaign.id,user.id,cd.doc_id,None,None))
    )
  }
  
  /* 
   * Loads the annotation sets referenced in ids as well as the annotation
   * sets they reference
   * FIXME: this is probably not fat enough => fixpoint of reference relation
   */
  def annotationSetLookup(ids : List[Long]) = {
    val ids_star = referenceTransitiveClosure(ids)
    annotation_sets.where(as => as.id in ids_star)
  }
  /*
   * assumes ids does not contain any duplicates
   */
  private def referenceTransitiveClosure(ids : List[Long]) : List[Long] = {
    val ass = ids.map(annotation_sets.lookup(_).get)
    val refs = ass.foldLeft(ids)((accu,as) => as.references ++ accu)
    val union = (ids ++ refs).distinct
    if(union.length == ids.length) { ids } 
    else { referenceTransitiveClosure(union) }
  }
}

// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
case class User(login : String, password : String, is_admin : Boolean) extends DbObject {
  lazy val annotates_in = CadixeDB.campaign_annotators.right(this)
}
case class Campaign(name : String, val schema : String) extends DbObject
case class Document(owner : Long, var props : String, contents : String, comment : String, description : String) extends DbObject 

case class CampaignAnnotator(val campaign_id : Long, val user_id : Long) extends KeyedEntity[CompositeKey2[Long,Long]] {
  def id = compositeKey(campaign_id,user_id)
}

case class CampaignDocument(val campaign_id : Long, val doc_id : Long, val alvisnlp_id : Option[String]) extends KeyedEntity[CompositeKey2[Long,Long]] {
  def id = compositeKey(campaign_id,doc_id)
}

case class DocumentAssignment(val campaign_id : Long, val user_id : Long, val doc_id : Long, var started_at : Option[Timestamp], var finished_at : Option[Timestamp]) extends KeyedEntity[CompositeKey3[Long,Long,Long]] {
  def id = compositeKey(campaign_id,user_id,doc_id)
}

/*
 * A set of annotations with same origin (that is, produced by the same user or agent)
 * 
 */
case class AnnotationSet(val doc_id : Long, val user_id : Long, val campaign_id : Long, 
                         val text_annotations : String, 
                         var groups : String,
                         var relations : String,
                         var head : Boolean, val revision : Int,
                         val `type` : AnnotationSetType.AnnotationSetType,
                         val description : String,
                         val created : Timestamp) extends DbObject {
  implicit val formats = Serialization.formats(NoTypeHints)

  def this() = this(0,0,0,"","","",false,0,UserAnnotation,"",CadixeDB.now())
  
  def get_groups = parse(groups).extract[List[Group]]

  def get_relations = parse(relations).extract[List[Relation]]
  
  def references = {
    val gr_refs = get_groups.foldLeft(Nil : List[Long])((accu,g) => g.references ++ accu)
    get_relations.foldLeft(gr_refs)((accu,r) => r.references ++ accu)
  }
}


case class AnnotationReference(val ann_id : String, var set_id : Option[Long] = None)

case class TextAnnotation(val id : String, val properties : Map[String,List[String]],val text : List[List[Int]], val `type` : String, val kind : Int = 0) {
  def id_substitution(sigma : Substitution[String]) = TextAnnotation(sigma.subst(id),properties,text,`type`)
}
case class Group(val id : String, 
                 val properties : Map[String,List[String]], 
                 val `type` : String, 
                 val group : List[AnnotationReference], 
                 val kind : Int = 1) {
  /*
   * Returns the list of (Long) ids which correspond to annotation sets 
   * referenced by this group
   */
  def references : List[Long] = {
    group.foldLeft(Nil : List[Long]){(accu,ar) => ar.set_id match { 
        case Some(id) => id :: accu
        case _ => accu
      }
    }
  }
}


case class Relation(val id : String, 
                    val properties : Map[String,List[String]], 
                    val `type` : String, 
                    val relation : Map[String,AnnotationReference], 
                    val kind : Int = 2) {
  def id_substitution(sigma : Substitution[String]) = {
    Relation(sigma.subst(id), properties, `type`, relation.mapValues{ 
        case AnnotationReference(uuid,ref) => AnnotationReference(sigma.subst(uuid),ref)
      })
  }
  
  def references : List[Long] = {
    relation.foldLeft(Nil : List[Long])((accu,kv) => kv._2.set_id match {
        case Some(id) => id :: accu
        case _ => accu
      })
  }
}
