/*
 *
 *      This software is a result of Quaero project and its use must respect the rules of the Quaero Project Consortium Agreement.
 *
 *      Copyright Institut National de la Recherche Agronomique, 2010.
 *
 */
package fr.inra.mig_bibliome.stane.client.Edit;

import com.google.gwt.core.client.GWT;
import fr.inra.mig_bibliome.stane.client.Events.GroupSelectionChangedEvent;
import fr.inra.mig_bibliome.stane.client.Events.GroupSelectionEmptiedEvent;
import fr.inra.mig_bibliome.stane.client.data3.AnnotatedTextHandler;
import fr.inra.mig_bibliome.stane.shared.data3.AnnotatedText;
import fr.inra.mig_bibliome.stane.shared.data3.Annotation;
import java.util.List;

/**
 * Specialized Edit corresponding to the creation of a new Annotation.
 * @author fpapazian
 */
public class AnnotationGroupCreationEdit extends AnnotationEdit {

    private static MessagesToUserConstants messages = (MessagesToUserConstants) GWT.create(MessagesToUserConstants.class);
    private final String groupType;
    private final List<Annotation> components;
    private Annotation newAnnotationGroup = null;

    public AnnotationGroupCreationEdit(AnnotatedTextHandler annotatedDoc, String groupType, List<Annotation> components) {
        super(annotatedDoc);
        this.groupType = groupType;
        this.components = components;
    }

    @Override
    protected void undoIt() {
        //FIXME : gather any information useful for later redo : such as metadata, ...
        getMapper().removeGroup(newAnnotationGroup);
        getInjector().getMainEventBus().fireEvent(new GroupSelectionEmptiedEvent(getAnnotatedTextHandler()));
    }

    @Override
    protected boolean doIt() {
        newAnnotationGroup = getMapper().addGroup(groupType, components);
        //FIXME : if necessary also recreate metadata, ....

        getInjector().getMainEventBus().fireEvent(new GroupSelectionChangedEvent(getAnnotatedTextHandler(), newAnnotationGroup));
        return true;
    }

    @Override
    public String getPresentationName() {
        return messages.annotationGroupCreationPresentationName(groupType, getDisplayForm());
    }

    @Override
    public String getUndoPresentationName() {
        return messages.undoAnnotationGroupCreationPresentationName(groupType, getDisplayForm());
    }

    @Override
    public String getRedoPresentationName() {
        return messages.redoAnnotationGroupCreationPresentationName(groupType, getDisplayForm());
    }

    private String getDisplayForm() {
        StringBuilder displayForm = new StringBuilder();
        displayForm.append("{ ");
        for (Annotation a : components) {
            displayForm.append(a.getId()).append(", ");
        }
        displayForm.append(" }");
        return displayForm.toString();
    }
}
