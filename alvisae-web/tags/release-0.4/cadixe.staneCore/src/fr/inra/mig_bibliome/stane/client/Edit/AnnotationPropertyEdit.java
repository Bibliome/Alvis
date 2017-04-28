/*
 *
 *      This software is a result of Quaero project and its use must respect the rules of the Quaero Project Consortium Agreement.
 *
 *      Copyright Institut National de la Recherche Agronomique, 2010.
 *
 */
package fr.inra.mig_bibliome.stane.client.Edit;

import fr.inra.mig_bibliome.stane.client.data3.AnnotatedTextHandler;
import fr.inra.mig_bibliome.stane.shared.data3.Annotation;

/**
 * Abstract Edit corresponding to the modification of the properties of an Annotation.
 * @author fpapazian
 */
public abstract class AnnotationPropertyEdit extends AnnotationEdit implements AnnotationModificationEdit {

    private final Annotation annotation;
    private final String key;

    public AnnotationPropertyEdit(AnnotatedTextHandler document, Annotation annotation, String key) {
        super(document);
        this.annotation = annotation;
        this.key = key;
    }

    @Override
    public Annotation getAnnotation() {
        return annotation;
    }

    public String getKey() {
        return key;
    }
}
