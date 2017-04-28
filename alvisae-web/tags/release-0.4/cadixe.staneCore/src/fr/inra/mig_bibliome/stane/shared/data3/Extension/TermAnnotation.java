/*
 *
 *      This software is a result of Quaero project and its use must respect the rules of the Quaero Project Consortium Agreement.
 *
 *      Copyright Institut National de la Recherche Agronomique, 2010-2011.
 *
 */
package fr.inra.mig_bibliome.stane.shared.data3.Extension;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

public interface TermAnnotation {
   
   public final static String DragNDropScope = "TermAnnotationDNDScope";
   
   public static final class ResourceLocator {

       private static RegExp regex =  RegExp.compile("^(https?:/(?:/[^/]+)+/)(?:projects/+){1}(\\d+)/?$");
       private final String resourceUrl;
       private final int projectId;

        public ResourceLocator(String baseResourceUrl) {
            MatchResult result = regex.exec(baseResourceUrl);
            if (result.getGroupCount()!=3) {
                throw new IllegalArgumentException("Not a valid TyDI resource url! " + baseResourceUrl);
            } 
            this.resourceUrl = result.getGroup(1);
            this.projectId = Integer.valueOf(result.getGroup(2));
        }
       
        public ResourceLocator(String resourceUrl, int projectId) {
            this.resourceUrl = resourceUrl;
            this.projectId = projectId;
        }

        public int getProjectId() {
            return projectId;
        }

        public String getResourceUrl() {
            return resourceUrl;
        }
   }
   
	/**
	 * @return the surface form of the term.
	 */
	String getSurfaceForm();

	/**
	 * @return the lemma (or null if there is no lemma for this TermAnnotation)
	 */
	String getLemma();

	/**
	 * @return the external id of the corresponding Term from TyDI (or null if this TermAnnotation is not associated to a Term of TyDI)
   * (this Id is typically the URL of the Rest web service that return the term resource itself)
	 */
	String getTermExternalId();
  
  void setTermExternalId(String termExternalId);
  
	/**
	 * @return the external id of the corresponding Semantic Class from TyDI (or null if this TermAnnotation is not associated to a SemanticClass of TyDI)
   * (this Id is typically the URL of the Rest web service that return the Semantic Class resource itself)
	 */
	String getSemClassExternalId();

  void setSemClassExternalId(String semClassExternalId);
  
}
