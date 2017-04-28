/*
 *
 *      This software is a result of Quaero project and its use must respect the rules of the Quaero Project Consortium Agreement.
 *
 *      Copyright Institut National de la Recherche Agronomique, 2010.
 *
 */
package fr.inra.mig_bibliome.stane.client.Edit;

import com.google.gwt.core.client.GWT;
import fr.inra.mig_bibliome.stane.client.data3.AnnotatedTextHandler;
import fr.inra.mig_bibliome.stane.shared.data3.Annotation;
import fr.inra.mig_bibliome.stane.shared.data3.AnnotationReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Specialized Edit corresponding to the modification of an Annotation.
 * @author fpapazian
 */
public class AnnotationGroupEditionEdit extends AnnotationEdit implements AnnotationModificationEdit {

    private static MessagesToUserConstants messages = (MessagesToUserConstants) GWT.create(MessagesToUserConstants.class);
    private final String oldType;
    private final String newType;
    private final List<String> oldComponents;
    private final List<String> newComponents;
    private final String oldGroupId;
    private Annotation newGroup;

    public AnnotationGroupEditionEdit(AnnotatedTextHandler annotatedDoc, Annotation group, String newType, List<Annotation> newComponents) {
        super(annotatedDoc);
        //keep a copy of the annotation before modification
        this.newType = newType;
        this.newComponents = new ArrayList<String>();
        for (Annotation c: newComponents) {
            this.newComponents.add(c.getId());
        }
        oldGroupId = group.getId();
        oldType = group.getAnnotationType();
        oldComponents = new ArrayList<String>();
        for (AnnotationReference c: group.getAnnotationGroup().getComponentRefs()) {
            oldComponents.add(c.getAnnotationId());
        }
    }

    @Override
    protected void undoIt() {
        newGroup = getMapper().modifyGroup(newGroup.getId(), oldType, oldComponents);
    }

    @Override
    protected boolean doIt() {
        newGroup = getMapper().modifyGroup(oldGroupId, newType, newComponents);
        return true;
    }

    @Override
    public String getPresentationName() {
        return messages.annotationGroupEditionPresentationName(oldGroupId, oldType);
    }

    @Override
    public String getUndoPresentationName() {
        return messages.undoAnnotationGroupEditionPresentationName(oldGroupId, oldType, getDisplayForm(oldComponents));
    }

    @Override
    public String getRedoPresentationName() {
        return messages.redoAnnotationGroupEditionPresentationName(oldGroupId, newType, getDisplayForm(newComponents));
    }

    private String getDisplayForm(List<String> components) {
        StringBuilder displayForm = new StringBuilder();
        displayForm.append("{ ");
        for (String id : components) {
            displayForm.append(id).append(", ");
        }
        displayForm.append(" }");
        return displayForm.toString();
    }

    @Override
    public Annotation getAnnotation() {
        return newGroup;
    }
}
