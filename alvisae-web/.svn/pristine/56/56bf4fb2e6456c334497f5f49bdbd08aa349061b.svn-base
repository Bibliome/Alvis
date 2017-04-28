/*
 *
 *      This software is a result of Quaero project and its use must respect the rules of the Quaero Project Consortium Agreement.
 *
 *      Copyright Institut National de la Recherche Agronomique, 2010.
 *
 */
package fr.inra.mig_bibliome.stane.client.data3.validation;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Messages;
import fr.inra.mig_bibliome.stane.shared.data3.AnnotatedText;
import fr.inra.mig_bibliome.stane.shared.data3.Annotation;
import fr.inra.mig_bibliome.stane.shared.data3.Fragment;
import fr.inra.mig_bibliome.stane.shared.data3.validation.AnnotationGroupDefinition;
import fr.inra.mig_bibliome.stane.shared.data3.validation.AnnotationTypeDefinition;
import fr.inra.mig_bibliome.stane.shared.data3.validation.FaultListener;
import fr.inra.mig_bibliome.stane.shared.data3.validation.PropertiesDefinition;
import fr.inra.mig_bibliome.stane.shared.data3.validation.PropertyDefinition;
import fr.inra.mig_bibliome.stane.shared.data3.validation.RelationDefinition;
import fr.inra.mig_bibliome.stane.shared.data3.validation.TextBindingDefinition;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author fpapazian
 */
public class BasicFaultListener implements FaultListener {

    public interface FaultMessages extends Messages {

        @DefaultMessage("Invalid argument type \"{0}\" for role \"{2}\" in annotation #{1} ")
        public String invalidArgumentType(String annotationType, String id, String role);

        @DefaultMessage("Invalid component type \"{0}\" of Annotation #{1}")
        public String invalidComponentType(String annotationType, String id);

        @DefaultMessage("Invalid fragment boundaries [{0}..{1}] of Annotation #{2} of kind \"{3}\"")
        public String invalidFragmentBoundaries(int start, int end, String id, String annotationType);

        @DefaultMessage("Invalid number of components ({0}) for Annotation {1} (expect [{2}..{3}])")
        public String invalidNumberOfComponents(int size, String id, int minComponents, int maxComponents);

        @DefaultMessage("Invalid number of fragments ({0}) for Annotation {1} (expect [{2}..{3}])")
        public String invalidNumberOfFragments(int size, String id, int minFragments, int maxFragments);

        @DefaultMessage("Invalid number of values for property \"{0}\" of Annotation {1} (expect [{2}..{3}])")
        public String invalidNumberOfPropertyValues(String key, String id, int minValues, int maxValues);

        @DefaultMessage("Invalid value \"{0}\" for property \"{1}\" of Annotation #{2}")
        public String invalidPropertyValue(String value, String key, String id);

        @DefaultMessage("Missing argument \"{0}\" for Annotation #{1}")
        public String missingArgument(String role, String id);

        @DefaultMessage("Missing mandatory property \"{0}\" for Annotation #{1}")
        public String missingMandatoryProperty(String key, String id);

        @DefaultMessage("Unsupported annotation type \"{0}\" for Annotation #{1} of kind \"{2}\"")
        public String unsupportedAnnotationType(String annotationType, String id, String kind);

        @DefaultMessage("Unsupported property key \"{0}\" for Annotation #{1}")
        public String unsupportedPropertyKey(String key, String id);

        @DefaultMessage("Unsupported role \"{0}\" for Annotation #{1}")
        public String unsupportedRole(String role, String id);

        @DefaultMessage("Wrong annotation kind \"{0}\" for Annotation #{1}")
        public String wrongAnnotationKind(String kind, String id);

        @DefaultMessage("conflicting Text binding with Annotation #{1} of type \"{0}\"")
        public String conflictingTextBinding(String type, String id);
    }
    private FaultMessages faultMessages = GWT.create(FaultMessages.class);
    private List<String> msgs = new ArrayList<String>();
    private Map<Integer, Annotation> conflictingAnnotation = new HashMap<Integer, Annotation>();

    public void reset() {
        msgs.clear();
        conflictingAnnotation.clear();
    }

    public List<String> getMessages() {
        return msgs;
    }

    public String getLastMessage() {
        return !msgs.isEmpty() ? msgs.get(msgs.size() - 1) : null;
    }

    public Annotation getConflictingAnnotation(int index) {
        return conflictingAnnotation.get(index);
    }

    @Override
    public void invalidArgumentType(RelationDefinition relDef, Annotation a, String role) {
        AnnotatedText annotatedText = a.getAnnotatedText();
        Annotation annotation = annotatedText.getAnnotation(a.getRelation().getArgumentRef(role).getAnnotationId());
        msgs.add(faultMessages.invalidArgumentType(annotation.getAnnotationType(), a.getId(), role));
    }

    @Override
    public void invalidComponentType(AnnotationGroupDefinition groupDef, Annotation a) {
        msgs.add(faultMessages.invalidComponentType(a.getAnnotationType(), a.getId()));
    }

    @Override
    public void invalidFragmentBoundaries(TextBindingDefinition textDef, Annotation a, Fragment frag) {
        msgs.add(faultMessages.invalidFragmentBoundaries(frag.getStart(), frag.getEnd(), a.getId(), a.getAnnotationType()));
    }

    @Override
    public void invalidNumberOfComponents(AnnotationGroupDefinition groupDef, Annotation a) {
        msgs.add(faultMessages.invalidNumberOfComponents(a.getAnnotationGroup().getComponentRefs().size(), a.getId(), groupDef.getMinComponents(), groupDef.getMaxComponents()));
    }

    @Override
    public void invalidNumberOfFragments(TextBindingDefinition textDef, Annotation a) {
        msgs.add(faultMessages.invalidNumberOfFragments(a.getTextBinding().getFragments().size(), a.getId(), textDef.getMinFragments(), textDef.getMaxFragments()));
    }

    @Override
    public void invalidNumberOfPropertyValues(PropertyDefinition propDef, Annotation a, String key) {
        msgs.add(faultMessages.invalidNumberOfPropertyValues(key, a.getId(), propDef.getMinValues(), propDef.getMaxValues()));
    }

    @Override
    public void invalidPropertyValue(PropertyDefinition propDef, Annotation a, String key, String value) {
        msgs.add(faultMessages.invalidPropertyValue(value, key, a.getId()));
    }

    @Override
    public void missingArgument(RelationDefinition relDef, Annotation a, String role) {
        msgs.add(faultMessages.missingArgument(role, a.getId()));
    }

    @Override
    public void missingMandatoryProperty(PropertyDefinition propDef, Annotation a, String key) {
        msgs.add(faultMessages.missingMandatoryProperty(key, a.getId()));
    }

    @Override
    public void unsupportedAnnotationType(Annotation a) {
        msgs.add(faultMessages.unsupportedAnnotationType(a.getAnnotationType(), a.getId(), a.getAnnotationKind().toString()));
    }

    @Override
    public void unsupportedPropertyKey(PropertiesDefinition propsDef, Annotation a, String key) {
        //msgs.add(faultMessages.unsupportedPropertyKey(key, a.getId()));
    }

    @Override
    public void unsupportedRole(RelationDefinition relDef, Annotation a, String role) {
        msgs.add(faultMessages.unsupportedRole(role, a.getId()));
    }

    @Override
    public void wrongAnnotationKind(AnnotationTypeDefinition typeDef, Annotation a) {
        msgs.add(faultMessages.wrongAnnotationKind(a.getAnnotationKind().toString(), a.getId()));
    }

    @Override
    public void conflictingTextBinding(AnnotationTypeDefinition typeDef, Annotation a) {
        conflictingAnnotation.put(msgs.size(), a);
        msgs.add(faultMessages.conflictingTextBinding(a.getAnnotationType(), a.getId()));
    }
}
