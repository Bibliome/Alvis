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
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Specialized Edit corresponding to the modification of an Annotation.
 * @author fpapazian
 */
public class AnnotationRelationEditionEdit extends AnnotationEdit implements AnnotationModificationEdit {

    private static MessagesToUserConstants messages = (MessagesToUserConstants) GWT.create(MessagesToUserConstants.class);
    private final String oldRelType;
    private final String newRelType;
    private final Map<String, Annotation> oldArgumentRoleMap;
    private final Map<String, Annotation> newArgumentRoleMap;
    private final String oldRelationId;
    private Annotation newRelation;

    public AnnotationRelationEditionEdit(AnnotatedTextHandler annotatedDoc, Annotation relation, String newRelType, Map<String, Annotation> newArgumentRoleMap) {
        super(annotatedDoc);
        //keep a copy of the annotation before modification
        this.newRelType = newRelType;
        this.newArgumentRoleMap = newArgumentRoleMap;
        oldRelationId = relation.getId();
        oldRelType = relation.getAnnotationType();
        oldArgumentRoleMap = new HashMap<String, Annotation>();

        for (Entry<String, AnnotationReference> e : relation.getRelation().getRolesArguments().entrySet()) {
            Annotation a = annotatedDoc.getAnnotation(e.getValue().getAnnotationId());
            oldArgumentRoleMap.put(e.getKey(), a);
        }
    }

    @Override
    protected void undoIt() {
        newRelation = getMapper().modifyRelation(newRelation.getId(), oldRelType, oldArgumentRoleMap);
    }

    @Override
    protected boolean doIt() {
        newRelation = getMapper().modifyRelation(oldRelationId, newRelType, newArgumentRoleMap);
        return true;
    }

    @Override
    public String getPresentationName() {
        return messages.annotationRelationEditionPresentationName(oldRelationId, oldRelType);
    }

    @Override
    public String getUndoPresentationName() {
        return messages.undoAnnotationRelationEditionPresentationName(oldRelationId, oldRelType, getDisplayForm(oldArgumentRoleMap));
    }

    @Override
    public String getRedoPresentationName() {
        return messages.redoAnnotationRelationEditionPresentationName(oldRelationId, newRelType, getDisplayForm(newArgumentRoleMap));
    }

    private String getDisplayForm(Map<String, Annotation> argumentRoleMap) {
        StringBuilder displayForm = new StringBuilder();
        for (Entry<String, Annotation> e : argumentRoleMap.entrySet()) {
            displayForm.append(e.getKey()).append("(").append(e.getValue().getId()).append(") ");
        }
        return displayForm.toString();
    }

    @Override
    public Annotation getAnnotation() {
        return newRelation;
    }
}
