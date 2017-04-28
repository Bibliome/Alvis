/*
 *
 *      This software is a result of Quaero project and its use must respect the rules of the Quaero Project Consortium Agreement.
 *
 *      Copyright Institut National de la Recherche Agronomique, 2010-2012.
 *
 */
package fr.inra.mig_bibliome.stane.client.data3;

import com.google.gwt.core.client.GWT;
import fr.inra.mig_bibliome.stane.client.Config.StaneClientBaseGinInjector;
import fr.inra.mig_bibliome.stane.shared.data3.AnnotatedText.AnnotationProcessor;
import fr.inra.mig_bibliome.stane.shared.data3.*;
import fr.inra.mig_bibliome.stane.shared.data3.Properties;
import java.util.Map.Entry;
import java.util.*;

/**
 * Frontal used to access the underlying Data Model. Shared by every views
 * displaying the same AnnotatedText).
 *
 * @author fpapazian
 */
public class AnnotatedTextHandler implements ExtendedAnnotatedText {

    private static final StaneClientBaseGinInjector injector = GWT.create(StaneClientBaseGinInjector.class);

    private static class DocHandlerParams {

        Integer userId;
        Integer campaignId;
        Integer documentId;
        AnnotatedTextHandler handler;

        public DocHandlerParams(Integer userId, Integer campaignId, Integer documentId, AnnotatedTextHandler handler) {
            this.userId = userId;
            this.campaignId = campaignId;
            this.documentId = documentId;
            this.handler = handler;
        }
    }
    private static HashMap<AnnotatedTextImpl, DocHandlerParams> handlerByDocument = new HashMap<AnnotatedTextImpl, DocHandlerParams>();

    /**
     *
     * @throws IllegalArgumentException if the structure of the specified
     * annotatedDoc is invalid
     */
    public static AnnotatedTextHandler createHandler(int userId, int campaignId, AnnotatedTextImpl annotatedDoc) {
        if (annotatedDoc==null) {
             throw new IllegalArgumentException("AnnotatedText should not be null!");
        }
        releaseHandler(annotatedDoc);
        annotatedDoc.checkStructure();
        AnnotatedTextHandler handler = new AnnotatedTextHandler(annotatedDoc);
        handlerByDocument.put(annotatedDoc, new DocHandlerParams(userId, campaignId, annotatedDoc.getDocument().getId(), handler));
        return handler;
    }

    public static AnnotatedTextHandler getHandler(AnnotatedTextImpl annotatedDoc) {
        DocHandlerParams handlerParam = handlerByDocument.get(annotatedDoc);
        return handlerParam.handler;
    }

    public static void releaseHandler(AnnotatedTextImpl annotatedDoc) {
        handlerByDocument.remove(annotatedDoc);
    }
    // -------------------------------------------------------------------------
    private AnnotatedTextImpl annotatedText;
    final HashMap<String, Annotation> annotationById = new HashMap<String, Annotation>();
    AnnotationSetImpl usersAnnotationSet = null;
    AnnotationSetImpl formattingAnnotationSet = null;
    final HashMap<Integer, HashSet<String>> annotationIdByAnnotationSetId = new HashMap<Integer, HashSet<String>>();
    final HashMap<String, Integer> annotationSetIdByAnnotationId = new HashMap<String, Integer>();
    private final HashSet<Integer> loadedAnnotationSets = new HashSet<Integer>();
    private AdditionalAnnotationSetRequestHandler additionalAnnSetRequestHnd = null;

    private AnnotatedTextHandler(AnnotatedTextImpl annotatedDoc) {
        this.annotatedText = annotatedDoc;
        init();
    }

    private void init() {

        usersAnnotationSet = null;
        annotationIdByAnnotationSetId.clear();
        formattingAnnotationSet = null;
        usersAnnotationSet = null;

        AnnotationSetListImpl asl = annotatedText._getAnnotationSetList();
        for (int i = 0; i < asl.length(); i++) {
            AnnotationSetImpl as = asl.get(i);

            loadedAnnotationSets.add(as.getId());

            HashSet<String> annIdSet = new HashSet<String>();
            annotationIdByAnnotationSetId.put(as.getId(), annIdSet);

            if (AnnotationSetType.UserAnnotation.equals(as.getType()) && (as.getOwner() == injector.getCoreDataProvider().getRequestManager().getCurrentUserId())) {
                usersAnnotationSet = as;
            } else if (AnnotationSetType.HtmlAnnotation.equals(as.getType())) {
                formattingAnnotationSet = as;
            }
        }

        if (usersAnnotationSet == null) {
            //FIXME Not I18N
            throw new IllegalArgumentException("Missing User's Annotation Set!");
        }

        annotatedText.scanAnnotations(new AnnotationProcessor() {

            @Override
            public boolean process(AnnotationSet annotationSet, Annotation annotation) {
                annotationIdByAnnotationSetId.get(annotationSet.getId()).add(annotation.getId());
                annotationSetIdByAnnotationId.put(annotation.getId(), annotationSet.getId());
                annotationById.put(annotation.getId(), annotation);
                return true;
            }
        });

    }

    public AnnotatedTextImpl getAnnotatedText() {
        return annotatedText;
    }

    public Collection<Annotation> getAnnotations() {
        return new ArrayList<Annotation>(annotationById.values());
    }

    public List<Annotation> getAnnotationsForAnnSets(Set<Integer> annotationSetIds) {
        ArrayList<Annotation> result = new ArrayList<Annotation>();
        AnnotationSetListImpl asl = annotatedText._getAnnotationSetList();
        for (int i = 0; i < asl.length(); i++) {
            AnnotationSetImpl as = asl.get(i);
            if (annotationSetIds == null || annotationSetIds.contains(as.getId())) {
                result.addAll(as.getTextAnnotations());
                result.addAll(as.getGroups());
                result.addAll(as.getRelations());
            }
        }
        return result;
    }

    @Override
    public Annotation getAnnotation(String annotationId) {
        return annotationById.get(annotationId);
    }

    public Integer getAnnotationSetId(String annotationId) {
        return annotationSetIdByAnnotationId.get(annotationId);
    }

    /**
     *
     * @param annotationId
     * @return true if the specified annotation is a Formatting annotation
     */
    public boolean isFormattingAnnotation(String annotationId) {
        return formattingAnnotationSet != null && annotationIdByAnnotationSetId.get(formattingAnnotationSet.getId()).contains(annotationId);
    }

    public AnnotationSetImpl getFormattingAnnotationSet() {
        return formattingAnnotationSet;
    }

    public AnnotationSetImpl getUsersAnnotationSet() {
        return usersAnnotationSet;
    }

    public Set<Integer> getLoadedAnnotationSets() {
        return loadedAnnotationSets;
    }

    //==========================================================================
    public final String getUsersAnnoationAsCSV() {
        final StringBuilder result = new StringBuilder();

        result.append(AnnotationImpl.getCSV(null));
        getUsersAnnotationSet().scanAnnotations(new AnnotatedTextImpl.AnnotationProcessor() {

            @Override
            public boolean process(AnnotationSetImpl annotationSet, AnnotationImpl annotation) {
                result.append(annotation.getCSV());
                return true;
            }
        });
        return result.toString();
    }

    //==========================================================================
    public static interface AdditionalAnnotationSetRequestHandler {

        public void requestAdditionalAnnotationSet(AnnotatedTextHandler annotatedTextHandler, int annotationSetId);
    }

    public void setAdditionalAnnotationSetRequestHandler(AdditionalAnnotationSetRequestHandler additionalAnnSetRequestHnd) {
        this.additionalAnnSetRequestHnd = additionalAnnSetRequestHnd;
    }

    public void requestAdditionalAnnotationSet(int annotationSetId) {
        if (additionalAnnSetRequestHnd != null) {
            additionalAnnSetRequestHnd.requestAdditionalAnnotationSet(this, annotationSetId);
        }
    }
    //==========================================================================

    public void addAdditionalAnnotationSet(AnnotationSetImpl annotationSet) {
        annotatedText.addAdditionalAnnotationSet(annotationSet);
        init();
    }

    //==========================================================================
    @Override
    public Annotation createTextAnnotation(String id, String type, Collection<Fragment> fragments) {
        if (getAnnotation(id) != null) {
            throw new IllegalArgumentException("this Id is already used for an existing Annotation");
        }
        AnnotationImpl annotation = annotatedText.createLooseTextAnnotation(id, type, fragments);
        usersAnnotationSet.addTextAnnotation(annotation);
        annotationById.put(annotation.getId(), annotation);
        annotationSetIdByAnnotationId.put(annotation.getId(), usersAnnotationSet.getId());
        return annotation;
    }

    @Override
    public Annotation createGroupAnnotation(String id, String type, Collection<Annotation> components) {
        if (getAnnotation(id) != null) {
            throw new IllegalArgumentException("this Id is already used for an existing Annotation");
        }
        AnnotationImpl annotation = annotatedText.createLooseGroupAnnotation(id, type, components);
        usersAnnotationSet.addGroupAnnotation(annotation);
        annotationById.put(annotation.getId(), annotation);
        annotationSetIdByAnnotationId.put(annotation.getId(), usersAnnotationSet.getId());
        return annotation;
    }

    @Override
    public Annotation createRelationAnnotation(String id, String type, Map<String, Annotation> arguments) {
        if (getAnnotation(id) != null) {
            throw new IllegalArgumentException("this Id is already used for an existing Annotation");
        }
        AnnotationImpl annotation = annotatedText.createLooseRelationAnnotation(id, type, arguments);
        usersAnnotationSet.addRelationAnnotation(annotation);
        annotationById.put(annotation.getId(), annotation);
        annotationSetIdByAnnotationId.put(annotation.getId(), usersAnnotationSet.getId());
        return annotation;
    }

    @Override
    public boolean removeAnnotation(String annotationId) {
        if (annotationId == null) {
            throw new NullPointerException("annotationId should not be null");
        }

        if (getAnnotation(annotationId) != null) {
            //checking for references to this Annotation before performing the removal
            if (!getReferencesToAnnotation(annotationId, true).isEmpty()) {
                return false;
            }
            switch (getAnnotation(annotationId).getAnnotationKind()) {
                case TEXT:
                    usersAnnotationSet.removeTextAnnotation(annotationId);
                    break;
                case GROUP:
                    usersAnnotationSet.removeGroupAnnotation(annotationId);
                    break;
                case RELATION:
                    usersAnnotationSet.removeRelationAnnotation(annotationId);
                    break;

            }
            annotationById.remove(annotationId);

            return true;
        } else {
            throw new IllegalArgumentException("the Annotation does not belong to this AnnotatedText");
        }
    }

    @Override
    public void fragmentsAdditionToAnnotation(String annotationId, Collection<Fragment> fragments) {
        if (annotationId == null) {
            throw new NullPointerException("annotationId should not be null");
        } else if (fragments == null) {
            throw new NullPointerException("fragments should not be null");
        }
        Annotation annotation = getAnnotation(annotationId);
        if (annotation != null) {
            if (annotation.getAnnotationKind().equals(AnnotationKind.TEXT)) {
                annotation.getTextBinding().addFragments(fragments);
            } else {
                throw new IllegalArgumentException("Fragment can only be added to TEXT Annotation");
            }
        } else {
            throw new IllegalArgumentException("the Annotation does not belong to this AnnotatedText");
        }
    }

    @Override
    public boolean fragmentsSubstractionToAnnotation(String annotationId, Collection<Fragment> fragments) {
        if (annotationId == null) {
            throw new NullPointerException("annotationId should not be null");
        } else if (fragments == null) {
            throw new NullPointerException("fragments should not be null");
        }
        Annotation annotation = getAnnotation(annotationId);
        if (annotation != null) {
            if (annotation.getAnnotationKind().equals(AnnotationKind.TEXT)) {
                return annotation.getTextBinding().removeFragments(fragments);
            } else {
                throw new IllegalArgumentException("Fragment can only be removed from TEXT Annotation");
            }
        } else {
            throw new IllegalArgumentException("the Annotation does not belong to this AnnotatedText");
        }
    }

    public List<Annotation> getReferencesToAnnotation(String annotationId, boolean fastFail) {
        if (annotationId == null) {
            throw new NullPointerException("annotationId should not be null");
        }
        List<Annotation> result = new ArrayList<Annotation>();

        Annotation annotation = getAnnotation(annotationId);
        if (annotation == null) {
            throw new IllegalArgumentException("the Annotation does not belong to this AnnotatedText");
        }

        MainAnnotationLoop:
        for (Annotation a : getAnnotations()) {
            if (annotation.equals(a)) {
                continue;
            }

            switch (a.getAnnotationKind()) {
                case GROUP:
                    for (AnnotationReference component : a.getAnnotationGroup().getComponentRefs()) {
                        if (annotationId.equals(component.getAnnotationId())) {
                            result.add(a);
                            if (fastFail) {
                                break MainAnnotationLoop;
                            } else {
                                continue MainAnnotationLoop;
                            }
                        }
                    }
                    break;
                case RELATION:
                    for (Entry<String, AnnotationReference> e : a.getRelation().getRolesArguments().entrySet()) {
                        if (annotationId.equals(e.getValue().getAnnotationId())) {
                            result.add(a);
                            if (fastFail) {
                                break MainAnnotationLoop;
                            } else {
                                continue MainAnnotationLoop;
                            }
                        }
                    }
                    break;
            }
        }
        return result;
    }

    @Override
    public List<Annotation> getReferencesToAnnotation(String annotationId) {
        return getReferencesToAnnotation(annotationId, false);
    }

    @Override
    public boolean isEqualToOrReferencedBy(String annotationId, String referentAnnId) {
        boolean isEqualOrReferenced = annotationId.equals(referentAnnId);

        if (!isEqualOrReferenced) {
            Annotation referent = getAnnotation(referentAnnId);
            if (referent == null) {
                throw new IllegalArgumentException("the Annotation does not belong to this AnnotatedText");
            }

            switch (referent.getAnnotationKind()) {
                case TEXT:
                    isEqualOrReferenced = false;
                    break;
                case GROUP:
                    for (AnnotationReference component : referent.getAnnotationGroup().getComponentRefs()) {
                        isEqualOrReferenced = isEqualToOrReferencedBy(annotationId, component.getAnnotationId());
                        if (isEqualOrReferenced) {
                            break;
                        }
                    }
                    break;
                case RELATION:
                    for (AnnotationReference argument : referent.getRelation().getRolesArguments().values()) {
                        isEqualOrReferenced = isEqualToOrReferencedBy(annotationId, argument.getAnnotationId());
                        if (isEqualOrReferenced) {
                            break;
                        }
                    }
                    break;
            }
        }
        return isEqualOrReferenced;
    }

    public Annotation addAnnotation(String annotationId, String annotationType, Collection<Fragment> targets) {
        Annotation annotation = createTextAnnotation(annotationId, annotationType, targets);
        //annotationSaver.addAnnotation(annotation);
        return annotation;
    }

    public boolean removeAnnotation(Annotation annotation) {
        boolean result = removeAnnotation(annotation.getId());
        if (result) {
            //annotationSaver.removeAnnotation(annotation);
        }
        return result;
    }

    public void addAnnotationFragments(String annotationId, List<Fragment> fragments) {
        fragmentsAdditionToAnnotation(annotationId, fragments);
        //annotationSaver.updateAnnotation(getAnnotation(annotationId));
    }

    public boolean removeAnnotationFragment(String annotationId, List<Fragment> fragments) {
        boolean result = fragmentsSubstractionToAnnotation(annotationId, fragments);
        if (result) {
            //annotationSaver.updateAnnotation(getAnnotation(annotationId));
        }
        return result;
    }

    public void setAnnotationFragment(String annotationId, Collection<Fragment> fragments) {
        getAnnotation(annotationId).getTextBinding().setFragments(fragments);
        //annotationSaver.updateAnnotation(getAnnotation(annotationId));
    }

    //==========================================================================
    public Annotation addGroup(String annotationId, String groupType, List<Annotation> components) {
        Annotation annotation = createGroupAnnotation(annotationId, groupType, components);
        //annotationSaver.addAnnotation(annotation);
        return annotation;
    }

    public Annotation addGroup(String groupType, List<Annotation> components) {
        String annotationId = getNewAnnotationId();
        Annotation annotation = addGroup(annotationId, groupType, components);
        //annotationSaver.addAnnotation(annotation);
        return annotation;
    }

    public boolean removeGroup(Annotation annotation) {
        return removeAnnotation(annotation);
    }

    public Annotation modifyGroup(String id, String newType, List<String> newComponents) {
        Annotation annotation = getAnnotation(id);
        for (AnnotationReference c : annotation.getAnnotationGroup().getComponentRefs()) {
            annotation.getAnnotationGroup().removeComponent(c.getAnnotationId());
        }
        for (String cid : newComponents) {
            Annotation c = getAnnotation(cid);
            annotation.getAnnotationGroup().addComponent(c);
        }
        ((AnnotationImpl) annotation).setAnnotationType(newType);
        //annotationSaver.updateAnnotation(annotation);
        return annotation;
    }
    //==========================================================================

    public boolean removeRelation(Annotation annotation) {
        return removeAnnotation(annotation);
    }

    public Annotation addRelation(String annotationId, String relationType, Map<String, Annotation> argumentRoleMap) {
        Annotation annotation = createRelationAnnotation(annotationId, relationType, argumentRoleMap);
        //annotationSaver.addAnnotation(annotation);
        return annotation;
    }

    public Annotation addRelation(String relationType, Map<String, Annotation> argumentRoleMap) {
        String annotationId = getNewAnnotationId();
        Annotation annotation = addRelation(annotationId, relationType, argumentRoleMap);
        //annotationSaver.addAnnotation(annotation);
        return annotation;
    }

    public Annotation modifyRelation(String id, String newRelType, Map<String, Annotation> newArgumentRoleMap) {
        Annotation annotation = getAnnotation(id);
        ((RelationImpl) annotation.getRelation()).setRolesArguments(newArgumentRoleMap);
        ((AnnotationImpl) annotation).setAnnotationType(newRelType);
        //annotationSaver.updateAnnotation(annotation);
        return annotation;
    }

    //==========================================================================
    public boolean replaceProperty(Annotation annotation, String key, List<String> newValues) {
        Properties props = annotation.getProperties();
        if (props.hasKey(key)) {
            props.removeKey(key);
        }
        if (newValues != null && !newValues.isEmpty()) {
            for (String value : newValues) {
                props.addValue(key, value);
            }
        }

        //annotationSaver.updateAnnotation(annotation);
        return true;
    }

    public boolean replaceProperty(Annotation annotation, String key, int propIndex, String oldValue, String newValue) {
        Properties props = annotation.getProperties();
        if (oldValue == null) {
            props.addValue(key, newValue);
        } else if (newValue == null) {
            props.removeValue(key, propIndex);
        } else {
            props.replaceValue(key, propIndex, newValue);
        }

        //annotationSaver.updateAnnotation(annotation);
        return true;
    }
    //==========================================================================

    /**
     * code reused from : http://stackoverflow.com/questions/105034/how-to-create-a-guid-uuid-in-javascript
     * @return an rfc4122 version 4 compliant UUID
     */
    public static native String generateUUID() /*-{ return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {var r = Math.random()*16|0,v=c=='x'?r:r&0x3|0x8;return v.toString(16);}); }-*/;

    @Override
    public final String getNewAnnotationId() {
        // AnnotationId must be:
        //   - unique (to avoid collision if 2 client instances use same authentication)
        //   - locally generated (because the server is not always available, if we allow offline processing)
        return generateUUID();
    }
}
