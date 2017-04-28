/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inra.mig_bibliome.stane.client.data3.validation;

import com.google.gwt.core.client.GWT;
import fr.inra.mig_bibliome.stane.shared.data3.AnnotatedText;
import fr.inra.mig_bibliome.stane.shared.data3.Annotation;
import fr.inra.mig_bibliome.stane.shared.data3.AnnotationKind;
import fr.inra.mig_bibliome.stane.shared.data3.Fragment;
import fr.inra.mig_bibliome.stane.shared.data3.validation.AnnotationSchema;
import fr.inra.mig_bibliome.stane.shared.data3.validation.AnnotationTypeDefinition;
import fr.inra.mig_bibliome.stane.shared.data3.validation.FaultListener;
import fr.inra.mig_bibliome.stane.shared.data3.validation.PropertiesDefinition;
import fr.inra.mig_bibliome.stane.shared.data3.validation.PropertyDefinition;
import java.util.List;

/**
 *
 * @author fpapazian
 */
public class BasicAnnotationSchemaValidator extends AnnotationSchema {

    private AnnotatedText annotatedText;

    public void setAnnotatedText(AnnotatedText annotatedText) {
        this.annotatedText = annotatedText;
    }

    @Override
    protected AnnotationTypeDefinition getAnnotationTypeDefinition(String type) {
        return annotatedText.getAnnotationSchema().getAnnotationTypeDefinition(type);
    }

    @Override
    public boolean validateProperty(FaultListener l, PropertyDefinition propDef, Annotation a, boolean fastFail) {
        return super.validateProperty(l, propDef, a, fastFail);
    }

    @Override
    public boolean validateAnnotation(FaultListener l, AnnotationTypeDefinition typeDef, Annotation a, boolean fastFail) {
        return super.validateAnnotation(l, typeDef, a, fastFail);
    }

    public boolean checkBoundaries(FaultListener l, Annotation annotation, boolean fastFail) {
        boolean result = true;
        if (AnnotationKind.TEXT.equals(annotation.getAnnotationKind())) {
            AnnotationTypeDefinition annTypeDef = getAnnotationTypeDefinition(annotation.getAnnotationType());
            boolean crossingAllowed = annTypeDef.getTextBindingDefinition().isCrossingAllowed();
            //FIXME use actual AnnotationSet, once it is implemented....
            String annotationSet = "";

            List<Fragment> refFragments = annotation.getTextBinding().getSortedFragments();

            for (Annotation otherAnnotation : annotatedText.getAnnotations(AnnotationKind.TEXT)) {
                if (otherAnnotation.getId().equals(annotation.getId())) {
                    continue;
                }

                //FIXME use actual AnnotationSet, once it is implemented....
                String otherAnnotationSet = "";

                //check boundary crossing within the same Annotation Set
                if (!annotationSet.equals(otherAnnotationSet)) {
                    continue;
                }

                AnnotationTypeDefinition otherAnnTypeDef = getAnnotationTypeDefinition(otherAnnotation.getAnnotationType());
                //no check necessary if both Annotation Types allow boundary crossing
                if (otherAnnTypeDef==null || (crossingAllowed && otherAnnTypeDef.getTextBindingDefinition().isCrossingAllowed())) {
                    continue;
                }

                for (Fragment refFrag : refFragments) {
                    for (Fragment otherFrag : otherAnnotation.getTextBinding().getSortedFragments()) {

                        if (((otherFrag.getStart() > refFrag.getStart())
                                && (otherFrag.getStart() < refFrag.getEnd())
                                && (otherFrag.getEnd() > refFrag.getEnd()))
                                || ((otherFrag.getStart() < refFrag.getStart())
                                && (otherFrag.getEnd() > refFrag.getStart())
                                && (otherFrag.getEnd() < refFrag.getEnd()))) {
                            l.conflictingTextBinding(otherAnnTypeDef, otherAnnotation);
                            if (fastFail) {
                                result = false;
                                break;
                            }
                        }
                    }
                }
            }
        }
        return result;
    }
}
