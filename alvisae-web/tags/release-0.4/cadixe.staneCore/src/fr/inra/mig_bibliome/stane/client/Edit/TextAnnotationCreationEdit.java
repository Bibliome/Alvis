/*
 *
 *      This software is a result of Quaero project and its use must respect the rules of the Quaero Project Consortium Agreement.
 *
 *      Copyright Institut National de la Recherche Agronomique, 2010.
 *
 */
package fr.inra.mig_bibliome.stane.client.Edit;

import com.google.gwt.core.client.GWT;
import fr.inra.mig_bibliome.stane.client.Events.TextAnnotationSelectionChangedEvent;
import fr.inra.mig_bibliome.stane.client.Events.TextAnnotationSelectionEmptiedEvent;
import fr.inra.mig_bibliome.stane.client.data3.AnnotatedTextHandler;
import fr.inra.mig_bibliome.stane.shared.data3.Annotation;
import fr.inra.mig_bibliome.stane.shared.data3.Fragment;
import java.util.List;

/**
 * Specialized Edit corresponding to the creation of a new Annotation.
 * @author fpapazian
 */
public class TextAnnotationCreationEdit extends AnnotationEdit implements TextAnnotationCoverageEdit {

    private static MessagesToUserConstants messages = (MessagesToUserConstants) GWT.create(MessagesToUserConstants.class);
    private final String annotationId;
    private final String annotationType;
    private final List<Fragment> targets;
    private Annotation newAnnotation;

    public TextAnnotationCreationEdit(AnnotatedTextHandler annotatedDoc, String annotationId, String annotationType, List<Fragment> targets) {
        super(annotatedDoc);
        this.annotationId = annotationId;
        this.annotationType = annotationType;
        this.targets = targets;
    }

    @Override
    protected void undoIt() {
        getMapper().removeAnnotation(newAnnotation);
        getInjector().getMainEventBus().fireEvent(new TextAnnotationSelectionEmptiedEvent(getAnnotatedTextHandler()));

    }

    @Override
    protected boolean doIt() {
        newAnnotation = getMapper().addAnnotation(annotationId, annotationType, targets);
        getInjector().getMainEventBus().fireEvent(new TextAnnotationSelectionChangedEvent(getAnnotatedTextHandler(), newAnnotation));
        return true;
    }

    @Override
    public String getPresentationName() {
        return messages.annotationCreationPresentationName(annotationId);
    }

    public String getUndoPresentationName() {
        return messages.undoAnnotationCreationPresentationName(annotationId, annotationType);
    }

    public String getRedoPresentationName() {
        return messages.redoAnnotationCreationPresentationName(annotationId, annotationType);
    }
}
