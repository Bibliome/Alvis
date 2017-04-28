/*
 *
 *      This software is a result of Quaero project and its use must respect the rules of the Quaero Project Consortium Agreement.
 *
 *      Copyright Institut National de la Recherche Agronomique, 2012.
 *
 */
package fr.inra.mig.tydiws.api

import org.squeryl.StaleUpdateException
import scala.collection.JavaConversions._

import net.liftweb.common.{Box,Full,Empty,Failure,ParamFailure,Logger}
import net.liftweb.http._
import net.liftweb.json._
import net.liftweb.http.auth.AuthRole
import net.liftweb.json.JsonDSL._
import net.liftweb.util.BasicTypesHelpers._

import org.jgrapht.Graphs
import org.jgrapht.graph.DefaultEdge
import org.squeryl.PrimitiveTypeMode._

import org.jgrapht.GraphPath

import fr.inra.mig.tydiws.db.TydiDB.UnknownGroup
import fr.inra.mig.tydiws.db._

object RestAPI {
  implicit val json_formats = Serialization.formats(NoTypeHints)

  def user_json(user : registereduser) = { 
    transaction {
      ("id" -> user.id) ~
      ("login" -> user.aliasname) ~
      ("projects" -> 
       TydiDB.projects_of_user_id(user.id).map { proj => 
          ("id" -> proj.id) ~ ("name" -> proj.name)
        })
    }
  }
  
  def user_project_json(user : registereduser) = {
    transaction {
      TydiDB.projects_of_user_id(user.id).map { proj => 
        ("id" -> proj.id) ~ ("name" -> proj.name)
      }
    }
  }

  type details = Tuple4[candgroup, candidatecomp, List[Long], List[Long]]
  
  def base_group_json(group_neighbourhood : details) = {
    val (group, canonic, hyponyms, hyperonyms) = group_neighbourhood
    ("groupId" -> group.id) ~
    ("canonicId" -> canonic.id) ~
    ("canonicLabel" -> canonic.compvalue) ~
    ("version" -> group.version) ~
    ("hyperGroupIds" -> { if(hyperonyms.isEmpty) List[Long](0) else hyperonyms}) ~
    ("hypoGroupIds" -> hyponyms)
  }
  
  def details_json(hypodetails : List[details]) = {
    hypodetails.map(x => (x._1.id.toString -> base_group_json(x))).toMap
  }
  
  def group_json(group_id : Long) = {
    val (group_neighbourhood,hypodetails) = TydiDB.tree_group(group_id)
    base_group_json(group_neighbourhood) ~ 
    ("hypoGroupsDetails" -> details_json(hypodetails))
  }

  def group_addedTerm_json(group_id : Long, addedTerm_id : Long) = {
    val base = group_json(group_id)
    base ~ ("addedTermId" -> addedTerm_id)
  }
  
  def root_group_json(roots : List[Long], withDetails : Boolean = true) = {
    val hypodetails = roots.map { id => TydiDB.group_neighbourhood(id) }
    val base = 
      ("groupId" -> 0) ~
    ("canonicId" -> 0) ~
    ("canonicLabel" -> "") ~
    ("version" -> 1) ~
    ("hyperGroupIds" -> (Nil : List[JValue])) ~
    ("hypoGroupIds" -> hypodetails.map(h => h._1.id))
    if(withDetails) { 
      base ~ ("hypoGroupsDetails" -> details_json(hypodetails)) }
    else base
  }

  def groupnterms_json(project_id : Long,group_id : Long) = {
    val (group_neighbourhood,hypodetails) = TydiDB.tree_group(group_id)
    val related_terms = TydiDB.group_related_terms(group_id)
    base_group_json(group_neighbourhood) ~
    ("termMembers" -> related_terms.map { 
        case ((tid,mt,form),((hlt,tlt),eg)) => 
          ("memberType" -> mt) ~
          ("termId" -> tid) ~
          ("form" -> form) ~
          ("linkedTerms" -> 
           (hlt.map({
                  case (cat,id,form) => 
                    ("linkType" -> cat) ~
                    ("termId" -> id) ~
                    ("form" -> form) ~
                    ("linkHead" -> true)
                }) ++ tlt.map({
                  case (cat,id,form) => 
                    ("linkType" -> cat) ~
                    ("termId" -> id) ~
                    ("form" -> form) ~
                    ("linkHead" -> false)
                })
            )) ~
          ("englobingGroups" -> eg)
      }) ~
    ("relatedGroupsDetails" -> (group_neighbourhood._3 ++ group_neighbourhood._4).map { id =>
        path_group_json(project_id,id)
      }
    )
        
  }
  
  
  def path_group_json(project_id : Long, group_id : Long) = {
    if(group_id == 0) root_group_json(TydiDB.groups_linked_to_root(project_id),withDetails = false)
    else base_group_json(TydiDB.group_neighbourhood(group_id))
  }
  
  def path_json(project_id : Long, p : GraphPath[Long,DefaultEdge]) = {
    val vertices = asScalaBuffer(Graphs.getPathVertexList(p)).toList
    vertices match {
      case subpath @ _ :: _ :: _ => {
          val inner_vertices = subpath.slice(1, subpath.length - 1)
          inner_vertices.map(path_group_json(project_id,_))
        }
      case _ => Nil
    }
  }
  
  def paths_json(project_id : Long, start : Long, end : Long, paths : List[GraphPath[Long,DefaultEdge]]) = {
    ("fromGroup" -> path_group_json(project_id,start)) ~
    ("toGroup" -> path_group_json(project_id,end)) ~
    ("paths" -> paths.map(p => "levels" -> path_json(project_id, p)))
  }

  object user extends RequestVar[Box[registereduser]](Empty)
  object logger extends Logger
  
  case class ConflictResponse(message : String) extends LiftResponse with HeaderDefaults {  
    def toResponse = InMemoryResponse(message.getBytes("UTF-8"), "Content-Type" -> "text/plain; charset=utf-8" :: headers, cookies, 409)    
  }    
  
  case class UnprocessableResponse(message : String) extends LiftResponse with HeaderDefaults {  
    def toResponse = InMemoryResponse(message.getBytes("UTF-8"), "Content-Type" -> "text/plain; charset=utf-8" :: headers, cookies, 422)    
  }    

  case class PreExistingTermResponse(jsonMessage : JObject) extends LiftResponse with HeaderDefaults {  
    val asString = compact(render(jsonMessage))
    def toResponse = InMemoryResponse(asString.getBytes("UTF-8"), "Content-Type" -> "application/json; charset=utf-8" :: headers, cookies, 422)    
  }    
  
  case class UnprocessableException(message : String) extends IllegalArgumentException(message : String)
  

  def project_guard(project_id : Long, writeaccess : Boolean = false)(f : registereduser => Box[LiftResponse]) = () => {
    user.flatMap(u => 
      try {
        transaction {
          val auth = TydiDB.termproducerauthorisation.where(tpa => tpa.termproject_id === project_id and 
                                                            tpa.termproducer_id === u.id and tpa.readingalloved === true and
                                                            ((writeaccess === false) or (tpa.creatingalloved === true)))
          if(auth == None) { Full(ForbiddenResponse()) }
          else f(u) match {
            case ParamFailure(msg, _, _, code: Int) =>
              Full(InMemoryResponse(msg.getBytes("UTF-8"),
                                    ("Content-Type" ->
                                     "text/plain; charset=utf-8") :: Nil, 
                                    Nil, code))
            case x => x
          }
        }
      } catch {
        //intercept the Optimistic Locking Exception that prevented the modification, and send back an explicit message
        case sue:StaleUpdateException => Full(ConflictResponse(sue.getMessage))        
          //intercept the Unprocessable Entity Exception that prevented the modification, and send back an explicit message
        case ue:UnprocessableException => Full(UnprocessableResponse(ue.getMessage))        
      }
    )
  }
  
  def semClassId(id : Long) = {
    if(id == 0) None else Some(id)
  }

  def termExistsJsonResponse(term : candidatecomp, representativeOf : Option[Long]) = {
    val commonPart = 
      ("msgNum" -> 1) ~ 
    ("message" -> "The term already exists") ~ 
    ("termId" -> term.id) ~ 
    ("form" -> term.compvalue)
    
    val jsonMsg =
      representativeOf match { case Some(groupId) => 
          commonPart ~ 
          ("isClassRepresentative" -> true) ~
          ("representativeOf" -> groupId)
                    
        case None =>  
          commonPart ~ 
          ("isClassRepresentative" -> false) 
      }
  
    jsonMsg
  }
  def dispatch : LiftRules.DispatchPF = {

    case Req("user" :: "me" :: Nil,_,GetRequest) => () => {
        user.map(u => JsonResponse(user_json(u)))
        
      }
    case Req("users" :: AsLong(user_id) :: "projects" :: Nil,_,GetRequest) => () => {
        user.is match {
          case Full(u) if u.id == user_id || u.appliadmin => Full(JsonResponse(user_project_json(u)))
          case _ => Full(ForbiddenResponse())
        }
      }

    case Req("projects" :: AsLong(project_id) :: "semClass" :: AsLong(semclass_id) :: Nil,_,GetRequest) =>
      project_guard(project_id) { u =>
        if(semclass_id == 0) {
          Full(JsonResponse(root_group_json(TydiDB.groups_linked_to_root(project_id))))
        }
        else {
          TydiDB.candgroup.where(cg => cg.id === semclass_id and cg.project_id === project_id).headOption match {
            case Some(cg)  => Full(JsonResponse(group_json(cg.id)))
            case None => Full(BadResponse())
          }
        }
      }

    case Req("projects" :: AsLong(project_id) :: "semClassNTerms" :: AsLong(semclass_id) :: Nil,_,GetRequest) => {
        project_guard(project_id) { u =>
          if(semclass_id == 0) {
            Full(JsonResponse(root_group_json(TydiDB.groups_linked_to_root(project_id))))
          }
          else {
            TydiDB.candgroup.where(cg => cg.id === semclass_id and cg.project_id === project_id).headOption match {
              case Some(cg)  => Full(JsonResponse(groupnterms_json(project_id,cg.id)))
              case None => Full(BadResponse())
            }
          }
        }
      }
      
    case Req("projects" :: AsLong(project_id) :: "semClasses" :: Nil,_,GetRequest) => {
        project_guard(project_id) { u =>
          val pat = S.param("pattern").getOrElse("*")
          val exactMatch = (S.param("exactMatch") ?~ "true").map(toBoolean)
          val semClass_ids = 
            from(TydiDB.candgroup,TydiDB.candidatecomp,TydiDB.candgrouplink)((cg,cc,cgl) =>
              where(cc.project_id === project_id and 
                    (cc.compvalue like pat) and cg.project_id === project_id and 
                    cgl.group_id === cg.id and cgl.candidate_id === cc.id)
              select(cg.id)).distinct.toList
          Full(JsonResponse(semClass_ids.map(group_json(_))))
        }
      }


      //Create a new Term and add it as a synonym to an existing Semantic Class
    case Req("projects" :: AsLong(project_id) :: "term" :: Nil,_,PostRequest) => {
        project_guard(project_id,writeaccess = true) { u =>
          for(classId <- S.param("classId").map(_.toLong) ?~ "missing classID parameter" ~> 400;
              //TODO Check that classID is not 0
              classVersion <- S.param("classVersion").map(_.toLong) ?~ "missing classVersion parameter" ~> 400;
              surfaceForm <- S.param("surfaceForm") ?~ "missing surfaceForm parameter" ~> 400)
                yield {
              val lemma = S.param("lemmatizedForm").toOption
              if(TydiDB.candgroup.where(cg => cg.id === classId).headOption == None) { BadResponse() }
              
              val prexistingTerm = TydiDB.exists_term(project_id,surfaceForm,lemma)
              
              val (performCreation, jsonMsg) = prexistingTerm match {
                case Some(term) => 
                  //there is a preexisting term: need more detailled info to allow creation
                  val (isAlreadyInGroups, representativeOf, inGroups) = TydiDB.groupsOfTerm(project_id, term.id)
                  
                  val isAlreadyRepresentative = representativeOf match { 
                    case Some(groupId) => true 
                    case _ => false 
                  }

                  //Always deny term creation if the term is a representative of another class, 
                  // or if it is present in another class
                  if (isAlreadyRepresentative || isAlreadyInGroups) {
                  
                    val jsonMsg =  termExistsJsonResponse(term, representativeOf) ~ inGroups
                    (false, jsonMsg)
                    
                  } else {
                    //send a message to inform that the term already exists, hence not attached to any Class
                    val jsonMsg =  termExistsJsonResponse(term, representativeOf) ~ inGroups
                    (true, jsonMsg)
                  }
                case _ => 
                  //No preexisting term: creation is always allowed
                  (true, null)
              }
              
              if (performCreation) {
                val addedTerm = TydiDB.createTermAndAddAsSynonym(project_id, u.id, surfaceForm, lemma = lemma, semClass = semClassId(classId), classVersion)
                JsonResponse(group_addedTerm_json(classId, addedTerm.id))
              } else {
                PreExistingTermResponse(jsonMsg) 
              }
            }
        }
      }
      
      
      //Add an existing term as a synonym to an existing Semantic Class
    case Req("projects" :: AsLong(project_id) :: "semClassNTerms" :: AsLong(semclass_id) :: Nil,_,PostRequest) => {
        project_guard(project_id,writeaccess = true) { u =>
          for(
            termId <- S.param("termId").map(_.toLong) ?~ "missing termId parameter" ~> 400;
            semClassVersion <- S.param("classVersion").map(_.toLong) ?~ "missing classVersion parameter" ~> 400
          )
            yield {
              if(semclass_id == 0) {
                BadResponse()
              } else {
                //check that Semantic class exists
                TydiDB.candgroup.where(cg => cg.id === semclass_id and cg.project_id === project_id).headOption match {
                  case Some(cg)  => {
                      TydiDB.testAndSetCandGroupVersion(semclass_id, semClassVersion, u.id)
                      TydiDB.candgrouplink.where(cgl => cgl.group_id === semclass_id and cgl.project_id === project_id and cgl.candidate_id===termId).headOption match {
                        case Some(cgl)  => {
                            UnprocessableResponse("Term #" + termId + " is already member of group #"+cgl.group_id)
                          }
                        
                        case None  => {
                            TydiDB.addTermtoGroup(project_id, termId, u.id, semclass_id, TydiDB.synonymy_link_category)
                            JsonResponse(groupnterms_json(project_id, semclass_id))
                          }
                      }
                    }
                  case None => BadResponse()
                }             
              }
            }
        }
      }
      
      //Create a new Term and add it as the Canonic representative of a new Semantic Class
    case Req("projects" :: AsLong(project_id) :: "semClasses" :: Nil,_,PostRequest) => {
        project_guard(project_id,writeaccess = true) { u =>
          (S.param("surfaceForm").toOption, S.param("hyperId").map(_.toLong).toOption, S.param("lemmatizedForm").toOption) match {
            case (None,_,_) => Full(BadResponse())
            case (_,Some(hyperId),_) if hyperId != 0 && TydiDB.candgroup.where(cg => cg.id === hyperId).headOption == None => Full(BadResponse())
            case (Some(form),hyperId,lemma) => {
                
                val prexistingTerm = TydiDB.exists_term(project_id,form,lemma)
                val (performCreation, jsonMsg) = prexistingTerm match {
                  case Some(term) => 
                    //there is a preexisting term: need more detailled info to allow creation
                    val (isAlreadyInGroup, representativeOf, inGroups) = TydiDB.groupsOfTerm(project_id, term.id)
                  
                    val forceCreation = (S.param("force") ?~ "true").map(toBoolean)==true
                    
                    val isOtherClassRepresentative = representativeOf match { 
                      case Some(groupId) if (!groupId.equals(hyperId)) => true 
                      case _ => false 
                    }
                    
                    //Always deny term creation if the term is a representative of another class, 
                    // or if it is present in another class and the force parameter is not set
                    if (isOtherClassRepresentative || (isAlreadyInGroup && forceCreation==false) ) {
                  
                      val jsonMsg =  termExistsJsonResponse(term, representativeOf) ~ inGroups
                      (false, jsonMsg)

                    } else {
                      //creation allowed despite that the term already exists
                      forceCreation match { 
                        case true => (true, null)
                        case _ => {
                            //send a message to inform that the term already exists, hence not attached to any Class
                            val jsonMsg =  termExistsJsonResponse(term, representativeOf) ~ inGroups
                            (true, jsonMsg)
                          } 
                      }
                    }
                    
                  case _ => 
                    //No preexisting term: creation is always allowed
                    (true, null)
                }
                
                if (performCreation) {
                  for ( hyperVersion <- S.param("hyperVersion").map(_.toLong ) ?~ "missing hyperVersion parameter" ~> 400 )
                    yield {
                      val (newgroup, addedTerm) = prexistingTerm match {
                        case Some(term) => 
                          TydiDB.createGroupAndAddTermRepresentative(project_id,u.id,term,hyperId.flatMap(semClassId(_)), hyperVersion)
                        case _ => 
                          TydiDB.createGroupAndNewTermRepresentative(project_id,u.id,form,lemma,hyperId.flatMap(semClassId(_)), hyperVersion)
                      }
                      JsonResponse(group_addedTerm_json(newgroup.id, addedTerm.id))
                    }
                } else {
                  Full(PreExistingTermResponse(jsonMsg))
                }
                
              }
          }
        }
      }
      
      
    case Req("projects" :: AsLong(project_id) :: "semClass" :: AsLong(semclass_id) :: Nil,_,PutRequest) => {
        project_guard(project_id,writeaccess = true) { u =>
          for(semClassVersion <- S.param("semClassVersion").map(_.toLong) ?~ "Missing semClassVersion parameter" ~> 400 ;
              prevHyperSemClassId <- S.param("prevHyperSemClassId").map(_.toLong) ?~ "Missing prevHyperSemClassId parameter" ~> 400 ;
              prevHyperSemClassVersion <- S.param("prevHyperSemClassVersion").map(_.toLong ) ?~ "missing prevHyperSemClassVersion parameter" ~> 400;
              newHyperSemClassId <- S.param("newHyperSemClassId").map(_.toLong) ?~ "Missing newHyperSemClassId parameter" ~> 400;
              newHyperSemClassVersion <- S.param("newHyperSemClassVersion").map(_.toLong) ?~ "Missing newHyperSemClassVersion parameter" ~> 400)
                yield { 
              TydiDB.testAndSetCandGroupVersion(semclass_id, semClassVersion, u.id)
                
              if(prevHyperSemClassId != 0) {
                TydiDB.testAndSetCandGroupVersion(prevHyperSemClassId, prevHyperSemClassVersion, u.id)
                TydiDB.remove_hyponymy(project_id, u.id, semclass_id, prevHyperSemClassId)
              }
              if(newHyperSemClassId != 0) {
                //Cycles may appear when creating a new hyponymy link : check that it won't, or report the error 

                //Do NOT link a class to itself!
                if (newHyperSemClassId==semclass_id) {
                  val message = (TydiDB.semClassInfoCanonicLabel(semclass_id) match {
                      case Some(canonicLabel) => 
                        "Cannot link a class to itself!  '" + canonicLabel + "' "
                      case None => ""
                    }) +  "(" + semclass_id + "@" + semClassVersion+ ")"

                  throw new UnprocessableException(message)
                }

                //if the specified new direct Hyperonym has already the specified semantic class for hyperonym (direct or not) then adding this link will create a cycle in the graph
                val hyperpath = TydiDB.hyperonymy_paths(project_id, semclass_id, newHyperSemClassId)
                if (hyperpath!=null) {
                  val msg1 = (TydiDB.semClassInfoCanonicLabel(semclass_id) match {
                      case Some(canonicLabel) => 
                        "'" + canonicLabel + "' "
                      case None =>  "" 
                    }) + "(" + semclass_id + "@" + semClassVersion+ ")"
                
                  val msg2 = (TydiDB.semClassInfoCanonicLabel(newHyperSemClassId) match {
                      case Some(canonicLabel) => 
                        "'" + canonicLabel + "' "
                      case None =>  ""
                    }) + "(" + newHyperSemClassId + "@" + newHyperSemClassId+ ")"
                
                  throw new UnprocessableException("Linking these classes would create a cycle: " + msg1 + " ->  " + msg2)
                }
                TydiDB.testAndSetCandGroupVersion(newHyperSemClassId, newHyperSemClassVersion, u.id);
                TydiDB.create_hyponymy(project_id, u.id, semclass_id, newHyperSemClassId)
              }
              JsonResponse(group_json(semclass_id))
            }
        }        
      }
      
    case Req("projects" :: AsLong(project_id) :: "branches" :: "fromSemClass" :: AsLong(start) :: "toSemClass" :: AsLong(end) :: Nil,_,GetRequest) => {
        project_guard(project_id) { u =>
          try { 
            val paths = asScalaBuffer(TydiDB.hyperonymy_paths(project_id, start, end)).toList
            if(paths.isEmpty) { Full(NotFoundResponse()) }
            else {
              Full(JsonResponse(paths_json(project_id,start,end,paths)))
            }
          }
          catch { case e : UnknownGroup => Full(NotFoundResponse()) }
        }
      }
      
      //Merging of two Semantic Classes
    case Req("projects" :: AsLong(project_id) :: "semClasses" :: "merge" :: Nil,_,PostRequest) => {
        project_guard(project_id,writeaccess = true) { u =>
          
          for(semClassId1 <- S.param("semClassId1").map(_.toLong) ?~ "Missing semClassId1 parameter" ~> 400 ;
              semClassVersion1 <- S.param("semClassVersion1").map(_.toLong) ?~ "Missing semClassVersion1 parameter" ~> 400 ;
              semClassId2 <- S.param("semClassId2").map(_.toLong ) ?~ "missing semClassId2 parameter" ~> 400;
              semClassVersion2 <- S.param("semClassVersion2").map(_.toLong) ?~ "Missing semClassVersion2 parameter" ~> 400)
                yield { 
              
              val resp = mergeClasses(project_id, u.id, semClassId1, semClassVersion1, semClassId2, semClassVersion2)
              Console.println(resp.toString)
              resp
            }
        }
      }
  }

  def getHyperPathMessage(semClassId1 : Long, semClassVersion1 : Long, semClassId2 : Long, semClassVersion2 : Long) : String = {
    val msg1 = (TydiDB.semClassInfoCanonicLabel(semClassId1) match {
        case Some(canonicLabel) => 
          "'" + canonicLabel + "' "
        case None =>  "" 
      }) + "(" + semClassId1 + "@" + semClassVersion1+ ")"
                
    val msg2 = (TydiDB.semClassInfoCanonicLabel(semClassId2) match {
        case Some(canonicLabel) => 
          "'" + canonicLabel + "' "
        case None =>  ""
      }) + "(" + semClassId2 + "@" + semClassVersion2+ ")"
                
    msg1 + " ->  " + msg2
  }
  
  def mergeClasses(project_id : Long,producer_id : Long, semClassId1 : Long, semClassVersion1 : Long, semClassId2 : Long, semClassVersion2 : Long) : LiftResponse  = {
    //the two classes must exist
    TydiDB.candgroup.where(cg => cg.id === semClassId1).headOption match { 
      case None => return ResponseWithReason(BadResponse(), "The class " +semClassId1 + " was not found")
      case _ =>
    }
    TydiDB.candgroup.where(cg => cg.id === semClassId2).headOption match { 
      case None => return ResponseWithReason(BadResponse(), "The class " +semClassId2 + " was not found") 
      case _ =>
    }

    //the two classes must be distinct 
    if (semClassId1==semClassId2) { 
      return ResponseWithReason(BadResponse(), "The two classes must be distinct to be merged")
    } else {
              
      //Test-and-Set version levels of the 2 classes to be merged
      TydiDB.testAndSetCandGroupVersion(semClassId1, semClassVersion1, producer_id)
      TydiDB.testAndSetCandGroupVersion(semClassId2, semClassVersion2, producer_id)
              
      //if both the classes are already linked by an hyperonym bond (direct or not) then merging them will create a cycle in the graph
      val graph = TydiDB.hyperonymy_graph(project_id)
      val hyperpath = TydiDB.hyperonymy_paths(graph, semClassId1, semClassId2)
      if (hyperpath==null) {
        val hypopath = TydiDB.hyperonymy_paths(graph, semClassId2, semClassId1)
        if (hypopath==null) {
          
          val mergedId = TydiDB.mergeClasses(project_id, producer_id, semClassId1, semClassId2)
          JsonResponse(group_json(mergedId))
          
        } else  {
          throw new UnprocessableException("Merging these classes may create a cycle: " +getHyperPathMessage(semClassId2, semClassVersion2, semClassId1, semClassVersion1))
        }
            
      } else  {
        throw new UnprocessableException("Merging these classes may create a cycle: " +getHyperPathMessage(semClassId1, semClassVersion1, semClassId2, semClassVersion2))
      }
    }
  }
  
  def protection : LiftRules.HttpAuthProtectedResourcePF = {
    case Req("user" :: "me" :: Nil,_,_) => Full(AuthRole("logged"))
    case Req("users" :: AsLong(_) :: "projects" :: Nil,_,_) => Full(AuthRole("logged"))
//    case Req("projects" :: AsLong(_) :: "semClass" :: AsLong(_) :: Nil,_,_) => Full(AuthRole("logged"))
    case _ => Full(AuthRole("logged"))
  }  
}
