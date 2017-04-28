/*
 *
 *      This software is a result of Quaero project and its use must respect the rules of the Quaero Project Consortium Agreement.
 *
 *      Copyright Institut National de la Recherche Agronomique, 2012.
 *
 */
package fr.inra.mig.tydiws.db

import fr.inra.mig.tydiws.api.RestAPI.UnprocessableException
import java.sql.Connection
import java.sql.Timestamp
import java.util.Date

import org.squeryl._
import org.squeryl.dsl._
import net.liftweb.json._
import java.util.Properties
import net.liftweb.common.Logger
import net.liftweb.json.JsonDSL._
import scala.util.parsing.json.JSON._

import net.liftweb.util.Props
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.adapters.H2Adapter
import org.squeryl.adapters.PostgreSqlAdapter

import org.jgrapht._
import org.jgrapht.graph._
import org.jgrapht.alg._
import org.mindrot.djm.BCrypt

object TydiDB extends Schema {

  val termproject   = table[termproject]
  val candidatecomp = table[candidatecomp]
  on(candidatecomp)(t=> declare(t.id is(primaryKey, autoIncremented("hibernate_sequence"))))
  val candgroup = table[candgroup]
  on(candgroup)(t=> declare(t.id is(primaryKey, autoIncremented("hibernate_sequence"))))
  val termproducer = table[termproducer]
  val featuretype = table[featuretype]
  val feature = table[feature]
  val validationstatuscategory = table[validationstatuscategory]
  val linkcategory = table[linkcategory]
  val candgrouplink = table[candgrouplink]
  val groupgrouplink = table[groupgrouplink]
  val termproducerauthorisation = table[termproducerauthorisation]
  val processing = table[processing]
  val linkfeature = table[linkfeature]
  val candidatelink = table[candidatelink]
  val projectfeature = table[projectfeature]
  val registereduser = table[registereduser]

  lazy val canonical_representative_link_category = transaction {
    linkcategory.where(lc => lc.name === "CANONIC").head.id
  }

  lazy val synonymy_link_category = transaction {
    linkcategory.where(lc => lc.name === "SYNONYM").head.id
  }

  lazy val quasisynonymy_link_category = transaction {
    linkcategory.where(lc => lc.name === "QUASISYN").head.id
  }

  lazy val hyponym_of_link_category = transaction {
    linkcategory.where(lc => lc.name === "HYPONYM").head.id
  }

  lazy val antonym_of_link_category = transaction {
    linkcategory.where(lc => lc.name === "ANTONYM").head.id
  }

  lazy val typovariant_link_category = transaction {
    linkcategory.where(lc => lc.name === "TYPOVAR").head.id
  }

  lazy val acronym_link_category = transaction {
    linkcategory.where(lc => lc.name === "ACRONYM").head.id
  }

  lazy val variant_link_category = transaction {
    linkcategory.where(lc => lc.name === "VARIANT").head.id
  }

  lazy val translation_link_category = transaction {
    linkcategory.where(lc => lc.name === "TRANSL").head.id
  }

  lazy val lemma_feature_id = transaction {
    featuretype.where(ft => ft.name === "LEMMA").head.id
  }

  val relevant_candgroup_relations = Set(canonical_representative_link_category,
                                         synonymy_link_category,
                                         quasisynonymy_link_category)

  val relevant_candcand_relations = Set(typovariant_link_category, acronym_link_category,
                                        variant_link_category, translation_link_category)

  def group(group_id : Long) = {
    val g = candgroup.where(g => g.id === group_id).head
    val canonic =
      from(candidatecomp,candgrouplink)((cc,cgl) =>
        where(cgl.group_id === g.id and cgl.candidate_id === cc.id and
              cgl.linkcategory_id === canonical_representative_link_category)
        select(cc)).head
    (g,canonic)
  }

  def group_neighbourhood(group_id : Long) = {
    val (g,canonic) = group(group_id)
    val hyperonyms =
      from(groupgrouplink)(ggl =>
        where(ggl.headgroup_id === g.id and
              ggl.linkcategory_id === hyponym_of_link_category)
        select(ggl.tailgroup_id)).toList
    val hyponyms =
      from(groupgrouplink)(ggl =>
        where(ggl.tailgroup_id === g.id and
              ggl.linkcategory_id === hyponym_of_link_category).
        select(ggl.headgroup_id)).toList
    (g,canonic,hyponyms,hyperonyms)
  }

  def tree_group(group_id : Long) = {
    val g = group_neighbourhood(group_id)
    val hyponyms = g._3.map(id => group_neighbourhood(id))
    (g, hyponyms)
  }

  def groups_linked_to_root(project_id : Long) = {
    val children =
      from(TydiDB.groupgrouplink)(ggl =>
        where(ggl.project_id === project_id and ggl.linkcategory_id === TydiDB.hyponym_of_link_category)
        select(ggl.headgroup_id)
      )
    from(TydiDB.candgroup)(cg =>
      where(cg.project_id === project_id and (cg.id notIn children))
      select(cg.id)).toList
  }

  def group_related_terms(group_id : Long) = {
    val terms =
      from(candgrouplink,candidatecomp)((cgl,cc) =>
        where(cgl.group_id === group_id and
              cgl.candidate_id === cc.id and
              (cgl.linkcategory_id in relevant_candgroup_relations))
        select(cc.id,cgl.linkcategory_id,cc.compvalue)
      ).toList
    val head_linked_terms = terms.map( t =>
      from(candidatelink,candidatecomp)((cl,cc) =>
        where(cl.headcandidate_id === t._1 and cc.id === cl.tailcandidate_id and
              (cl.linkcategory_id in relevant_candcand_relations))
        select(cl.linkcategory_id,cc.id,cc.compvalue)
      ).toList)
    val tail_linked_terms = terms.map( t =>
      from(candidatelink,candidatecomp)((cl,cc) =>
        where(cl.tailcandidate_id === t._1 and cc.id === cl.headcandidate_id and
              (cl.linkcategory_id in relevant_candcand_relations))
        select(cl.linkcategory_id,cc.id,cc.compvalue)
      ).toList)
    val englobing_groups = terms.map( t =>
      from(candgrouplink)(cgl =>
        where(cgl.candidate_id === t._1 and (cgl.linkcategory_id in relevant_candgroup_relations))
        select(cgl.group_id)
      ).toList)
    terms zip ((head_linked_terms zip tail_linked_terms) zip englobing_groups)
  }


  case class UnknownGroup(id : Long) extends Exception

  def hyperonymy_graph(project_id : Long) = {
    val relations =
      from(groupgrouplink)(ggl =>
        where(ggl.project_id === project_id and ggl.linkcategory_id === hyponym_of_link_category)
        select((ggl.headgroup_id, ggl.tailgroup_id))
      ).toList
    val roots = groups_linked_to_root(project_id)
    val g : DefaultDirectedGraph[Long,DefaultEdge] = new DefaultDirectedGraph(classOf[DefaultEdge])
    relations.foreach(e => {
        g.addVertex(e._1)
        g.addVertex(e._2)
        g.addEdge(e._2, e._1) // inversion parce que la base stocke la relation "est hyponyme de"
      })
    roots.foreach(v => {
        g.addVertex(v)
        g.addVertex(0)
        g.addEdge(0,v)
      })
    g
  }

  def hyperonymy_paths(g : DefaultDirectedGraph[Long,DefaultEdge], start : Long, end : Long) : java.util.List[GraphPath[Long,DefaultEdge]] = {
    if(!g.containsVertex(start)) throw UnknownGroup(start)
    if(!g.containsVertex(end)) throw UnknownGroup(end)
    val kshortest = new KShortestPaths(g,start,100)
    kshortest.getPaths(end)
  }

  def hyperonymy_paths(project_id : Long, start : Long, end : Long) : java.util.List[GraphPath[Long,DefaultEdge]] = {
    val g = hyperonymy_graph(project_id)
    hyperonymy_paths(g, start, end)
  }

  /**
   * Return the preexisting term corresponding to the specified surface form and lemma
   */
  def exists_term(project_id : Long, surfaceForm : String, lemma : Option[String]) : Option[candidatecomp] = {

    //if no lemmatized form provided, use the surface form instead
    val lemmaValue = lemma match {
      case Some(l)  => l
      case None  => surfaceForm
    }

    //1rst search term by their lemmatized form
    val sameLemma =
      from(candidatecomp,feature)((cc,f) =>
        where(cc.project_id === project_id and cc.superseded===false and f.value === lemmaValue and
              f.candidate_id === cc.id and f.featuretype_id === lemma_feature_id)
        select(cc)).headOption

    sameLemma  match
    {
      case Some(term) => sameLemma
      case None  => {
          // if no prexisting term with same lemma, search for term with same surface form....
          val sameSurface =
            from(candidatecomp)(cc =>
              where(cc.project_id === project_id and cc.superseded===false and cc.compvalue === surfaceForm)
              select(cc)).headOption

          //FIXME maybe there is several terms with same form
          sameSurface match {
            case Some(term) => {
                from(feature)(f =>
                  where(f.candidate_id === term.id and f.featuretype_id === lemma_feature_id)
                  select(f)).headOption match {
                  case Some(feat) =>
                    // the 2 terms are different because their lemmatized forms are
                    None
                  case None =>
                    //the prexisiting term is considered equal if it has no lemma
                    Some(term)
                }
              }
            case None  => None
          }
        }
    }
  }

  /**
   * Return the list preexisting group(s) of which the specified term is member
   */
  def groupsOfTerm(project_id : Long, term_id : Long): (Boolean, Option[Long], JObject) = {
    //groups in which the term appears
    val groupLinksOfTerm =
      from(candgrouplink)(cgl =>
        where(cgl.project_id === project_id
              and cgl.candidate_id === term_id
              and (cgl.linkcategory_id in relevant_candgroup_relations))
        select(cgl))

    //retrieve the canonic representative of the above groups
    val groupsOfTerm =
      from(groupLinksOfTerm, candgroup, candgrouplink, candidatecomp) ((got, cg, cgl, cc) =>
        where( cgl.project_id === got.project_id
              and cgl.group_id === got.group_id
              and cg.id  === got.group_id
              and cgl.linkcategory_id === canonical_representative_link_category
              and cc.id === cgl.candidate_id)
        select(got, cg, cgl, cc)
        orderBy(got.group_id, got.linkcategory_id)
      )

    var nbOfGroups = 0
    var representativeOf : Option[Long] = None
    val groups = ("inGroups" -> groupsOfTerm.map { a=> {

          nbOfGroups += 1

          //is this term already a Canonical representative of a Semantic Class
          if (canonical_representative_link_category.equals(a._1.linkcategory_id)) {
            representativeOf = Some(a._1.group_id);
          }

          ("groupId" -> a._1.group_id) ~
          ("canonicId" -> a._3.candidate_id) ~
          ("canonicLabel" -> a._4.compvalue) ~
          ("version" -> a._2.version) ~
          ("memberType" -> a._1.linkcategory_id)
        }
      })

    val isAlreadyInGroups = nbOfGroups > 0
    (isAlreadyInGroups, representativeOf, groups)
  }

  def getSemClassDetails(project_id : Long, semClassId : Long) = {
    val semClass = candgroup.where(cg => cg.id === semClassId).head

    val members = from(candgrouplink)(cgl =>
      where(cgl.project_id === project_id
            and cgl.group_id ===semClassId)
      select(cgl)).toList

    val hyperonymIds =
      from(groupgrouplink)(ggl =>
        where(ggl.headgroup_id === semClassId)
        select(ggl.tailgroup_id)).toSet

    val hyponymIds =
      from(groupgrouplink)(ggl =>
        where(ggl.tailgroup_id === semClassId)
        select(ggl.headgroup_id)).toSet

    (semClass, members, hyperonymIds, hyponymIds)
  }

  //Merged 2 classes by disolving the second one within the first one (adding non-redondant members and hyper/hypo links)
  def mergeClasses(project_id : Long, producer_id : Long, semClassId1 : Long, semClassId2 : Long) = {
    val (semClass1, members1, hyperonymIds1, hyponymIds1) = getSemClassDetails(project_id, semClassId1)
    val (semClass2, members2, hyperonymIds2, hyponymIds2) = getSemClassDetails(project_id, semClassId2)

    val memberTypeByCandidate1 = members1.map( cgl => (cgl.candidate_id -> cgl.linkcategory_id) ).toMap

    members2.foreach { cgl =>
      {
        val memberId = cgl.candidate_id
        val memberType2 = cgl.linkcategory_id

        memberTypeByCandidate1.get(memberId) match {
          //the term is member of both classes, so the member does not need to be copied, but member types consistency check must be performed
          case Some(memberType1) =>
            memberType2 match {

              //canonical representative is also a synonym
              case canonical_representative_link_category if (memberType1 != synonymy_link_category) =>
                throw new UnprocessableException("Can not merge class #" + semClassId2 + " within #" + semClassId1 + " because types of member #" + memberId + " are not consistent")
              case synonymy_link_category if (memberType1 != canonical_representative_link_category || memberType1 != synonymy_link_category) =>
                throw new UnprocessableException("Can not merge class #" + semClassId2 + " within #" + semClassId1 + " because types of member #" + memberId + " are not consistent")

                //same member type on both classes
              case _ =>
            }

            //the term of second class was not present in first class, so it will be copied
          case None => {
              //canonical representative of second class become a simple synonym
              val newMemberType = (memberType2==canonical_representative_link_category) match { 
                case true => synonymy_link_category 
                case false =>  memberType2 
              }
              addTermtoGroup(project_id, memberId, producer_id, semClassId1, newMemberType)
            }
        }

        //delete links to the term members of the second class
        candgrouplink.deleteWhere( cgl2 => cgl2.group_id === semClassId2 and cgl2.candidate_id===memberId)
      }
    }

    hyperonymIds2.foreach { hyperId2 =>
      
      //attach hyperonyms of class2 to the merged class
      if (! hyperonymIds1.contains(hyperId2)) {
        addHyponymyLink(project_id, producer_id, semClassId1, hyperId2)
      }
      //delete link to the hyperonym of the second class
      groupgrouplink.deleteWhere( ggl => ggl.headgroup_id === semClassId2 and ggl.tailgroup_id === hyperId2)

      //update old hyperonym version level
      incCandGroupVersion(hyperId2, producer_id)

    }

    hyponymIds2.foreach { hypoId2 =>
      
      //attach hyponyms of class2 to the merged class
      if (! hyponymIds1.contains(hypoId2)) {
        addHyponymyLink(project_id, producer_id, hypoId2, semClassId1)
      }

      //delete link to the hyponym of the second class
      groupgrouplink.deleteWhere( ggl => ggl.headgroup_id === hypoId2 and ggl.tailgroup_id === semClassId2)

      //update old hyponym version level
      incCandGroupVersion(hypoId2, producer_id)
      
    }
    
    //delete the second class itself
    candgroup.deleteWhere( cg => cg.id === semClass2.id)
    
    //
    semClassId1
  }


  def semClassInfoCanonicLabel(candgroup_id : Long) = {
    candgroup.where(cg => cg.id === candgroup_id).headOption match {
      case Some(group) => {
          val canonic = from(candidatecomp,candgrouplink)((cc,cgl) =>
            where(cgl.group_id === candgroup_id and cgl.candidate_id === cc.id and
                  cgl.linkcategory_id === canonical_representative_link_category)
            select(cc)).head

          Some(canonic.compvalue)
        }
      case None => None
    }
  }

  def now() : Timestamp = new Timestamp(new Date().getTime())
  
  def incCandGroupVersion(candgroup_id : Long, user_id : Long)  {
    val updated = update(candgroup)(group =>
      where(group.id === candgroup_id)
      set(
        group.version := (group.version .~ + 1),
        group.lastmodifiedat := now(),
        group.lastmodifiedby := user_id)
    )
  }
  
  //Enforce Optimistic locking by increasing the version number (Test-and-set) of the specified CandidateGroup before it is modified
  def testAndSetCandGroupVersion(candgroup_id : Long, currentVersionNumber : Long, user_id : Long) {
    //perform update only if version number is same as specified in parameter
    val updated = update(candgroup)(group =>
      where(group.id === candgroup_id
            and group.version===currentVersionNumber)
      set(
        group.version := (group.version .~ + 1),
        group.lastmodifiedat := now(),
        group.lastmodifiedby := user_id)
    )
    if (updated!=1) {
      //if the version number could not be increased, it means that a concurrent modification has taken place since the CandidateGroup was last retrieved by the client: then No Modification will e allowed
      val message = semClassInfoCanonicLabel(candgroup_id) match {
        case Some(canonicLabel) =>
          "Semantic Class '" + canonicLabel + "' (" + candgroup_id + "@" + currentVersionNumber+ ") has been modified since you retrieved it."
        case None => "Semantic Class " + candgroup_id + " does not exist."
      }
      throw new StaleUpdateException(message)
    }
  }

  def addTermtoGroup(project_id : Long, term_id : Long, producer_id : Long,
                     semClass_id : Long, link_category : Long) = {
    candgrouplink.insert(
      new candgrouplink("",
                        now(),
                        producer_id,
                        1,
                        semClass_id,
                        term_id,
                        link_category,
                        producer_id,
                        project_id)
    )
  }

  def createGroup(project_id : Long, producer_id : Long) = {
    val group = new candgroup(now(),producer_id,1,producer_id,project_id)
    candgroup.insert(group)
    group
  }

  def createCandidate(project_id : Long, producer_id : Long,
                      form : String, lemma : Option[String]) = {

    val cc = new candidatecomp(form,false,1,10,producer_id,project_id)
    candidatecomp.insert(cc)
    lemma.foreach { lemma => feature.insert(new feature(0,lemma,cc.id,lemma_feature_id,project_id)) } // FIXME !!! num pour feature
    cc
  }

  def addTermAsSynonym(project_id : Long, producer_id : Long,
                       candidate : candidatecomp,
                       semClass : Option[Long], classVersion : Long) = {
    semClass.foreach { class_id => testAndSetCandGroupVersion(class_id, classVersion, producer_id) }
    semClass.foreach { class_id => addTermtoGroup(project_id, candidate.id, producer_id, class_id, synonymy_link_category) }
    candidate
  }

  def createTermAndAddAsSynonym(project_id : Long, producer_id : Long,
                                form : String, lemma : Option[String],
                                semClass : Option[Long], classVersion : Long) = {
    val candidate = createCandidate(project_id, producer_id, form, lemma)
    addTermAsSynonym(project_id, producer_id, candidate, semClass, classVersion)
  }

  def createGroupAndAddTermRepresentative(project_id : Long, producer_id : Long,
                                          candidate : candidatecomp,
                                          hyperId : Option[Long], hyperVersion : Long) = {
    hyperId.foreach { class_id => testAndSetCandGroupVersion(class_id, hyperVersion, producer_id) }

    val cg = createGroup(project_id, producer_id)
    addTermtoGroup(project_id, candidate.id, producer_id, cg.id, canonical_representative_link_category)

    hyperId.foreach { class_id =>  groupgrouplink.insert(
        new groupgrouplink("",
                           now(),
                           producer_id,
                           1,
                           hyponym_of_link_category,
                           cg.id,
                           class_id,
                           producer_id,
                           project_id))
    }
    (cg,candidate)
  }

  def createGroupAndNewTermRepresentative(project_id : Long, producer_id : Long,
                                          form : String, lemma : Option[String],
                                          hyperId : Option[Long], hyperVersion : Long) = {

    val candidate = createCandidate(project_id, producer_id, form, lemma)
    createGroupAndAddTermRepresentative(project_id, producer_id, candidate, hyperId, hyperVersion)
  }

  def remove_hyponymy(project_id : Long, producer_id : Long, hyponym : Long, hyperonym : Long) = {
    val changed = groupgrouplink.deleteWhere(ggl => ggl.project_id === project_id and
                                             ggl.linkcategory_id === TydiDB.hyponym_of_link_category and
                                             ggl.headgroup_id === hyponym and
                                             ggl.tailgroup_id === hyperonym)
  }

  def addHyponymyLink(project_id : Long, producer_id : Long, hyponym : Long, hyperonym : Long) = {
    groupgrouplink.insert(
      new groupgrouplink("",now(),producer_id,1,
                         hyponym_of_link_category,
                         hyponym, hyperonym,
                         producer_id, project_id)
    )
  }

  def create_hyponymy(project_id : Long, producer_id : Long, hyponym : Long, hyperonym : Long) = {
    val query = groupgrouplink.where(ggl => ggl.project_id === project_id and
                                     ggl.headgroup_id === hyponym and
                                     ggl.tailgroup_id === hyperonym)
    if(query.headOption == None) {
      addHyponymyLink(project_id, producer_id, hyponym, hyperonym)
    }
  }


  def projects_of_user_id(user_id : Long) = {
    from(termproducerauthorisation, termproject)((auth,proj) =>
      where(auth.termproject_id === proj.id and
            auth.termproducer_id === user_id and
            auth.readingalloved === true)
      select(proj)
      orderBy(proj.id)
    ).toList
  }

  def check_login(user_id : String, password : String) = {
    registereduser.where(u => u.name === user_id).headOption match {
      case Some(user) if BCrypt.checkpw(new String(password), user.hpassword) => Some(user)
      case _ => None
    }
  }

  def createPGSession(server : String, port : Int, dbname : String, schema : String,
                      username : String, password : String) = {
    Class.forName("org.postgresql.Driver")

    val jdbcUrl = "jdbc:postgresql://" + server + ":" + port + "/" + dbname

    //Console.out.println("Creating Connection... " + jdbcUrl)
    val connection = java.sql.DriverManager.getConnection(jdbcUrl, username,password)

    val statement = connection.createStatement
    statement.execute("SET SEARCH_PATH TO " + schema)
    //Console.out.println("Default schema... " + schema)

    val session = Session.create(connection, new PostgreSqlAdapter)
    //session.setLogger(msg => Console.err.println(msg))
    session
  }

  def createSession() : Session = {
    Props.requireOrDie("tydidb.server", "tydidb.port", "tydidb.dbname",
                       "tydidb.username", "tydidb.password", "tydidb.schema")
    createPGSession(Props.get("tydidb.server",""), Props.getInt("tydidb.port",0),
                    Props.get("tydidb.dbname",""), Props.get("tydidb.schema",""),
                    Props.get("tydidb.username",""), Props.get("tydidb.password",""))
  }

  def createSession(props : Properties) : Session = {
    val required = Set("db.server", "db.port", "db.dbname", "db.username", "db.password", "db.schema")
    val missing = required.foldLeft(List[String]()) {
      (missingSoFar, propName) => props.keySet.contains(propName) match {
        case false => propName :: missingSoFar
        case _ =>  missingSoFar
      }
    }
    if(missing != Nil) {
      throw new Exception("The following required properties are not defined: " + missing.mkString(","))
    }
    createPGSession(props.getProperty("db.server", ""),
                    props.getProperty("db.port", "0").toInt,
                    props.getProperty("db.dbname", ""),
                    props.getProperty("db.schema", ""),
                    props.getProperty("db.username", ""),
                    props.getProperty("db.password", ""))
  }
}

class dbobject extends KeyedEntity[Long] {
  val id: Long = 0
}

class candgrouplink(val comment : String,
                    val lastmodifiedat : Timestamp,
                    val lastmodifiedby : Long,
                    val version : Long,
                    val group_id : Long,
                    val candidate_id : Long,
                    val linkcategory_id : Long,
                    val termproducer_id : Long,
                    val project_id : Long)

class candgroup(var lastmodifiedat : Timestamp,
                var lastmodifiedby : Long,
                var version : Long,
                val producer_id : Long,
                val project_id : Long)
extends dbobject

class candidatecomp(val compvalue : String,
                    val superseded : Boolean,
                    val version : Long,
                    val comptype_id : Long,
                    val producer_id : Long,
                    val project_id : Long)
extends dbobject

class candidatelink(val lastmodifiedat : Timestamp,
                    val lastmodifiedby : Long,
                    val headcandidate_id : Long,
                    val tailcandidate_id : Long,
                    val linkcategory_id : Int,
                    val termproducer_id : Long,
                    val comment : String,
                    val status : Int,
                    val version : Long,
                    val project_id : Long)

class feature(val num : Int,
              val value : String,
              val candidate_id : Long,
              val featuretype_id : Long,
              val project_id : Long)

class featuretype(val description : String,
                  val name : String)
extends dbobject

class groupgrouplink(val comment : String,
                     val lastmodifiedat : Timestamp,
                     val lastmodifiedby : Long,
                     val version : Long,
                     val linkcategory_id : Long,
                     val headgroup_id : Long,
                     val tailgroup_id : Long,
                     val termproducer_id : Long,
                     val project_id : Long)

class linkcategory(val description : String,
                   val directed : Boolean,
                   val name : String)
extends dbobject

class linkfeature(val num : Int,
                  val value : String,
                  val candidatelink_headcandidate_id : Long,
                  val candidatelink_linkcategory_id : Int,
                  val candidatelink_tailcandidate_id : Long,
                  val featuretype_id : Int,
                  val project_id : Long,
                  val candidatelink_termproducer_id : Long)

class processing(val processigparameters : String,
                 val processingcategory : String,
                 val id : Long,
                 val termproject_id : Long,
                 val name : String)

class projectfeature(val featuretype_id : Int,
                     val termproject_id : Long)


class termproducerauthorisation(val creatingalloved : Boolean,
                                val lastmodifiedat : Timestamp,
                                val lastmodifiedby : Long,
                                val readingalloved : Boolean,
                                val validatingalloved : Boolean,
                                val validationmode : String,
                                val version : Long,
                                val termproject_id : Long,
                                val termproducer_id : Long)


class termproducer(val active : Boolean,
                   val lastmodifiedat : Timestamp,
                   val lastmodifiedby : Long,
                   val version : Long)
extends dbobject

class termproject(val closed : Boolean,
                  val datalang : String,
                  val defaultvalidationmode : String,
                  val description : String,
                  val enddate : Option[Timestamp],
                  val lastmodifiedat : Timestamp,
                  val lastmodifiedby : Long,
                  val metadatalang : String,
                  val name : String,
                  val startdate : Timestamp,
                  val subjectfield : String,
                  val version : Long)
extends dbobject
{
  def this() = this(false,"","","",Some (TydiDB.now()),TydiDB.now(),0L,"","",TydiDB.now(),"",0L)
}


class validationstatuscategory(val statusval : Int,
                               val description : String,
                               val name : String,
                               val termproject_id : Long,
                               val color : Int)

class registereduser(val aliasname : String,
                     val appliadmin : Boolean,
                     val hpassword : String,
                     val name : String,
                     val id : Long)
