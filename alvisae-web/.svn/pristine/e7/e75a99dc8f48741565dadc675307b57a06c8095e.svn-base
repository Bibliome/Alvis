/*
 *
 *      This software is a result of Quaero project and its use must respect the rules of the Quaero Project Consortium Agreement.
 *
 *      Copyright Institut National de la Recherche Agronomique, 2010-2012.
 *
 */
package fr.inra.mig_bibliome.stane.client.Annotation;

import fr.inra.mig_bibliome.stane.shared.data3.AnnotatedText;
import fr.inra.mig_bibliome.stane.shared.data3.Annotation;
import fr.inra.mig_bibliome.stane.shared.data3.AnnotationGroup;
import fr.inra.mig_bibliome.stane.shared.data3.AnnotationKind;
import fr.inra.mig_bibliome.stane.shared.data3.Extension.TermAnnotation;
import fr.inra.mig_bibliome.stane.shared.data3.Properties;
import fr.inra.mig_bibliome.stane.shared.data3.Relation;
import fr.inra.mig_bibliome.stane.shared.data3.TextBinding;
import java.util.List;

/**
 *
 * @author fpapazian
 */
public class TermAnnotationBox implements Annotation, TermAnnotation {

    private final Annotation annotation;
    private final String tyDITermRefPropName;
    private final String tyDIClassRefPropName;

    public TermAnnotationBox(Annotation annotation, String tyDITermRefPropName, String tyDIClassRefPropName) {
        this.annotation = annotation;
        this.tyDITermRefPropName = tyDITermRefPropName;
        this.tyDIClassRefPropName = tyDIClassRefPropName;
    }

    @Override
    public String getSurfaceForm() {
        return getAnnotationText("");
    }

    @Override
    public String getLemma() {
        //FIXME lemma may be computed from included Lemma preannotations, or from the value of a specific this annotation property
        return getAnnotationText("");
    }

    @Override
    public String getTermExternalId() {
        //shortcut to the property that contains the external Id of the Term corresponding to this annotation
        if (tyDITermRefPropName != null) {
            List<String> vals = annotation.getProperties().getValues(tyDITermRefPropName);
            return vals != null && !vals.isEmpty() ? vals.get(0) : "";
        } else {
            return null;
        }
    }

    @Override
    public void setTermExternalId(String termExternalId) {
        if (tyDITermRefPropName != null) {
            annotation.getProperties().removeKey(tyDITermRefPropName);
            if (termExternalId != null && !termExternalId.trim().isEmpty()) {
                annotation.getProperties().addValue(tyDITermRefPropName, termExternalId.trim());
            }
        }
    }

    @Override
    public String getSemClassExternalId() {
        //shortcut to the property that contains the external Id of the Semantic class corresponding to this annotation
        if (tyDIClassRefPropName != null) {
            List<String> vals = annotation.getProperties().getValues(tyDIClassRefPropName);
            return vals != null && !vals.isEmpty() ? vals.get(0) : "";
        } else {
            return null;
        }
    }

    @Override
    public void setSemClassExternalId(String semClassExternalId) {
        if (tyDIClassRefPropName != null) {
            annotation.getProperties().removeKey(tyDIClassRefPropName);
            if (semClassExternalId != null && !semClassExternalId.trim().isEmpty()) {
                annotation.getProperties().addValue(tyDIClassRefPropName, semClassExternalId.trim());
            }
        }
    }

    @Override
    public String getId() {
        return annotation.getId();
    }

    @Override
    public AnnotatedText getAnnotatedText() {
        return annotation.getAnnotatedText();
    }

    @Override
    public AnnotationKind getAnnotationKind() {
        return annotation.getAnnotationKind();
    }

    @Override
    public String getAnnotationType() {
        return annotation.getAnnotationType();
    }

    @Override
    public TextBinding getTextBinding() {
        return annotation.getTextBinding();
    }

    @Override
    public AnnotationGroup getAnnotationGroup() {
        return annotation.getAnnotationGroup();
    }

    @Override
    public Relation getRelation() {
        return annotation.getRelation();
    }

    @Override
    public Properties getProperties() {
        return annotation.getProperties();
    }

    @Override
    public String getAnnotationText(String fragmentSeparator) {
        return annotation.getAnnotationText(fragmentSeparator);
    }
}
