/*
 *
 *      This software is a result of Quaero project and its use must respect the rules of the Quaero Project Consortium Agreement.
 *
 *      Copyright Institut National de la Recherche Agronomique, 2010.
 *
 */
package fr.inra.mig_bibliome.stane.client.Events;

import fr.inra.mig_bibliome.stane.client.data3.AnnotatedTextHandler;

/**
 * An event occurring when no more Annotation is selected in the UI
 * @author fpapazian
 */
public class TextAnnotationSelectionEmptiedEvent extends TextAnnotationSelectionChangedEvent {

    public TextAnnotationSelectionEmptiedEvent(AnnotatedTextHandler annotatedDoc) {
        super(annotatedDoc);
    }
}
