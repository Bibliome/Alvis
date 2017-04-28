/*
 *
 *      This software is a result of Quaero project and its use must respect the rules of the Quaero Project Consortium Agreement.
 *
 *      Copyright Institut National de la Recherche Agronomique, 2010-2011.
 *
 */
package fr.inra.mig_bibliome.stane.shared.data3;

public interface Annotation {
	/**
	 * Return the identifier of this annotation.
	 * Will not return null.
	 * @return
	 */
	String getId();
	
	/**
	 * Return the annotated text to which this annotation belongs.
	 * Will not return null.
	 * @return
	 */
	AnnotatedText getAnnotatedText();
	
	/**
	 * Return the kind of this annotation.
	 * Will not return null.
	 * @return
	 */
	AnnotationKind getAnnotationKind();
	
	/**
	 * Return the type of this annotation.
	 * Will not return null.
	 * @return
	 */
	String getAnnotationType();
	
	/**
	 * Return the text binding of this annotation.
	 * Return null if this annotation is not a text annotation.
	 * @return
	 */
	TextBinding getTextBinding();
	
	/**
	 * Return an annotation group corresponding to this annotation.
	 * Return null if this annotation is not a group annotation.
	 * @return
	 */
	AnnotationGroup getAnnotationGroup();
	
	/**
	 * Return the relation corresponding to this annotation.
	 * Return null if this annotation is not a relation annotation.
	 * @return
	 */
	Relation getRelation();
	
	/**
	 * Return the properties of this annotation.
	 * Will not return null.
	 * @return
	 */
	Properties getProperties();
    
    /**
     * Return the string concatenation of every text fragment of the Annotation (null if not a Text Annotation)
     * @param fragmentSeparator
     * @return 
     */
    String getAnnotationText(String fragmentSeparator);

	
}
