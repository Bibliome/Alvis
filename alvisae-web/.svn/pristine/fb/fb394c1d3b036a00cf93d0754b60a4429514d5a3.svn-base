/*
 *
 *      This software is a result of Quaero project and its use must respect the rules of the Quaero Project Consortium Agreement.
 *
 *      Copyright Institut National de la Recherche Agronomique, 2010-2011.
 *
 */
package fr.inra.mig_bibliome.stane.shared.data3;

import java.util.List;

/**
 *
 * @author fpapazian
 */
public interface AnnotationSet  extends AnnotationSetCore {
    
    List<? extends Annotation> getTextAnnotations();

    List<? extends Annotation> getGroups();

    List<? extends Annotation> getRelations();
    
    /**
     *
     * @return the JSON string representation of this AnnotationSet
     */
    String getJSON();
    
    /**
     *
     * @return the CSV string representation of this AnnotationSet
     */
    String getCSV();
    
}
