/*
 *
 *      This software is a result of Quaero project and its use must respect the rules of the Quaero Project Consortium Agreement.
 *
 *      Copyright Institut National de la Recherche Agronomique, 2010-2011.
 *
 */
package fr.inra.mig_bibliome.stane.client.Edit;

import com.google.gwt.core.client.GWT;
import fr.inra.mig_bibliome.stane.client.Events.TextAnnotationSelectionChangedEvent;
import fr.inra.mig_bibliome.stane.client.Events.TextAnnotationSelectionEmptiedEvent;
import fr.inra.mig_bibliome.stane.client.data3.AnnotationImpl;
import fr.inra.mig_bibliome.stane.client.data3.AnnotatedTextHandler;
import fr.inra.mig_bibliome.stane.shared.data3.Annotation;
import java.util.List;

/**
 * Specialized Edit corresponding to the removal of an Annotation.
 * @author fpapazian
 */
public class TextAnnotationRemovalEdit extends AnnotationEdit implements PreventableEdit, TextAnnotationCoverageEdit {

    private static MessagesToUserConstants messages = (MessagesToUserConstants) GWT.create(MessagesToUserConstants.class);
    private final String annotationId;
    private Annotation oldAnnotation;

    public TextAnnotationRemovalEdit(AnnotatedTextHandler annotatedDoc, String annotationId) {
        super(annotatedDoc);

        this.annotationId = annotationId;
        Annotation annotation = getMapper().getAnnotation(annotationId);
        oldAnnotation = AnnotationImpl.create((AnnotationImpl) annotation);
    }

    @Override
    protected void undoIt() {
        Annotation newAnnotation = getMapper().addAnnotation(annotationId, oldAnnotation.getAnnotationType(), oldAnnotation.getTextBinding().getFragments());
        newAnnotation.getProperties().replaceAll(oldAnnotation.getProperties());

        Annotation annotation = getMapper().getAnnotation(annotationId);
        getInjector().getMainEventBus().fireEvent(new TextAnnotationSelectionChangedEvent(getAnnotatedTextHandler(), annotation));
    }

    @Override
    protected boolean doIt() {
        if (!getMapper().removeAnnotation(oldAnnotation)) {
            return false;
        } else {
            getInjector().getMainEventBus().fireEvent(new TextAnnotationSelectionEmptiedEvent(getAnnotatedTextHandler()));
        }
        return true;
    }

    @Override
    public String getPresentationName() {
        return messages.annotationRemovalPresentationName(annotationId, oldAnnotation.getAnnotationType());
    }

    @Override
    public String getUndoPresentationName() {
        return messages.undoAnnotationRemovalPresentationName(annotationId, oldAnnotation.getAnnotationType());
    }

    @Override
    public String getRedoPresentationName() {
        return messages.redoAnnotationRemovalPresentationName(annotationId, oldAnnotation.getAnnotationType());
    }

    @Override
    public boolean isPrevented() {
        return !getPreventingCause().isEmpty();
    }

    @Override
    public String getPreventingCause() {

        if (!getAnnotatedTextHandler().getAnnotationSetId(annotationId).equals(getAnnotatedTextHandler().getUsersAnnotationSet().getId())) {
            // FIXME not I18N
            return "The annotation can not be removed because it does not belong to the user's Annoation Set";
        }
        
        List<Annotation> references = getAnnotatedTextHandler().getReferencesToAnnotation(annotationId);
        if (references.isEmpty()) {
            return null;
        } else {
            StringBuilder msg = new StringBuilder();
            // FIXME not I18N
            msg.append("The annotation can not be removed because it is still referenced by the following annotations:");
            String sep = " ";
            for (Annotation a : references) {
                msg.append(sep).append(a.getId());
                sep = ", ";
            }
            return msg.toString();
        }
    }

    @Override
    public boolean isForcible() {
        return false;
    }

    @Override
    public AnnotationCompoundEdit getForcingEdit() {
        throw new UnsupportedOperationException("Forcing this Edit is not allowed.");
    }
}
