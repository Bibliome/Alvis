package fr.inra.mig_bibliome.stane.shared.data3.validation;

import fr.inra.mig_bibliome.stane.shared.data3.AnnotationKind;

public interface AnnotationTypeDefinition {
	/**
	 * Type this definition specifies.
	 * Will not return null.
	 * @return
	 */
	String getType();
	
	/**
	 * Kind of annotations for this type.
	 * Will not return null.
	 * @return
	 */
	AnnotationKind getAnnotationKind();

	/**
	 * Color used to this type of annotation.
	 * (CSS syntax)
	 * @return
	 */
	String getColor();

	/**
	 * Return the properties definition for annotations of this type.
	 * Will not return null.
	 * @return
	 */
	PropertiesDefinition getPropertiesDefinition();
	
	/**
	 * Return a text binding definition for annotations of this type.
	 * Return null if this object does not define a text annotation type.
	 * @return
	 */
	TextBindingDefinition getTextBindingDefinition();
	
	/**
	 * Downcast this definition to a group annotation type definition.
	 * Return null if this object does not define a group annotation type.
	 * @return
	 */
	AnnotationGroupDefinition getAnnotationGroupDefinition();
	
	/**
	 * Downcast this definition to a relation annotation type definition.
	 * Return null if this object does not define a relation annotation type.
	 * @return
	 */
	RelationDefinition getRelationDefinition();
}
