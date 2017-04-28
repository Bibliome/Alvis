/*
 *
 *      This software is a result of Quaero project and its use must respect the rules of the Quaero Project Consortium Agreement.
 *
 *      Copyright Institut National de la Recherche Agronomique, 2010.
 *
 */
package fr.inra.mig_bibliome.stane.client.Edit;

import com.google.gwt.core.client.GWT;
import fr.inra.mig_bibliome.stane.client.Events.RelationSelectionChangedEvent;
import fr.inra.mig_bibliome.stane.client.Events.RelationSelectionEmptiedEvent;
import fr.inra.mig_bibliome.stane.client.data3.AnnotatedTextHandler;
import fr.inra.mig_bibliome.stane.shared.data3.Annotation;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Specialized Edit corresponding to the creation of a new Annotation.
 * @author fpapazian
 */
public class AnnotationRelationCreationEdit extends AnnotationEdit {

    private static MessagesToUserConstants messages = (MessagesToUserConstants) GWT.create(MessagesToUserConstants.class);
    private final String relationType;
    private final Map<String, Annotation> argumentRoleMap;
    private Annotation newAnnotationRelation = null;

    public AnnotationRelationCreationEdit(AnnotatedTextHandler annotatedDoc, String relationType, Map<String, Annotation> argumentRoleMap) {
        super(annotatedDoc);
        this.relationType = relationType;
        this.argumentRoleMap = argumentRoleMap;
    }

    @Override
    protected void undoIt() {
        //FIXME : gather any information useful for later redo : such as metadata, ...
        getMapper().removeRelation(newAnnotationRelation);
        getInjector().getMainEventBus().fireEvent(new RelationSelectionEmptiedEvent(getAnnotatedTextHandler()));
    }

    @Override
    protected boolean doIt() {
        newAnnotationRelation = getMapper().addRelation(relationType, argumentRoleMap);
        getInjector().getMainEventBus().fireEvent(new RelationSelectionChangedEvent(getAnnotatedTextHandler(), newAnnotationRelation));

        //FIXME : if necessary also recreate metadata, ....
        return true;
    }

    @Override
    public String getPresentationName() {
        return messages.annotationRelationCreationPresentationName(relationType, getDisplayForm());
    }

    @Override
    public String getUndoPresentationName() {
        return messages.undoAnnotationRelationCreationPresentationName(relationType, getDisplayForm());
    }

    @Override
    public String getRedoPresentationName() {
        return messages.redoAnnotationRelationCreationPresentationName(relationType, getDisplayForm());
    }

    private String getDisplayForm() {
        StringBuilder displayForm = new StringBuilder();
        for (Entry<String, Annotation> e : argumentRoleMap.entrySet()) {
            displayForm.append(e.getKey()).append("(").append(e.getValue().getId()).append(") ");
        }
        return displayForm.toString();
    }
}
