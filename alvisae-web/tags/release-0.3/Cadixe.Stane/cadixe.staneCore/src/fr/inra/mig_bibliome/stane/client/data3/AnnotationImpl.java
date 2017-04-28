/*
 *
 *      This software is a result of Quaero project and its use must respect the rules of the Quaero Project Consortium Agreement.
 *
 *      Copyright Institut National de la Recherche Agronomique, 2010.
 *
 */
package fr.inra.mig_bibliome.stane.client.data3;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.json.client.JSONObject;
import fr.inra.mig_bibliome.stane.shared.data3.AnnotationGroup;
import fr.inra.mig_bibliome.stane.shared.data3.AnnotationKind;
import fr.inra.mig_bibliome.stane.shared.data3.Properties;
import fr.inra.mig_bibliome.stane.shared.data3.Annotation;
import fr.inra.mig_bibliome.stane.shared.data3.AnnotationReference;
import fr.inra.mig_bibliome.stane.shared.data3.Fragment;
import fr.inra.mig_bibliome.stane.shared.data3.Relation;
import fr.inra.mig_bibliome.stane.shared.data3.TextBinding;
import fr.inra.mig_bibliome.stane.shared.data3.validation.AnnotationTypeDefinition;

/**
 *
 * @author fpapazian
 */
public class AnnotationImpl extends JavaScriptObject implements Annotation {

    private static final native AnnotationImpl create(String id, int kind, String type, String owner, PropertiesImpl props) /*-{
    a = {};
    a.id=id;
    a.kind=kind;
    a.type=type;
    a.properties=props;
    return a;
    }-*/;

    public static final AnnotationImpl create(String id, AnnotationKind annotationKind, String type) {
        AnnotationImpl a = create(id, annotationKind.ordinal(), type, "?", PropertiesImpl.create());
        switch (annotationKind) {
            case TEXT:
                a.initKind("text", TextBindingImpl.create());
                break;
            case GROUP:
                a.initKind("group", AnnotationGroupImpl.create());
                break;
            case RELATION:
                a.initKind("relation", RelationImpl.create());
                break;
        }
        return a;
    }

    /**
     * Create a loose copy of an annotation (i.e. an Annotation not associated to an AnnotatedText)
     * @param annotation source annotation to be copied
     * @return a new instance of Annotation
     */
    public static final AnnotationImpl create(AnnotationImpl annotation) {
        AnnotatedTextImpl annotatedText = annotation.getAnnotatedText();
        AnnotationImpl result = null;
        try {
            annotation.setAnnotatedText(null);
            result = JsonUtils.safeEval(new JSONObject(annotation).toString());
        } finally {
            annotation.setAnnotatedText(annotatedText);
        }
        return result;
    }

    //--------------------------------------------------------------------------
    protected AnnotationImpl() {
    }

    private final native void initKind(String kindKey, JavaScriptObject kindObject) /*-{ this[kindKey] = kindObject; }-*/;

    @Override
    public final native String getId() /*-{ return this.id; }-*/;

    @Override
    public final AnnotatedTextImpl getAnnotatedText() {
        return _getAnnotatedText();
    }

    public final native AnnotatedTextImpl _getAnnotatedText() /*-{ return this.annotatedText; }-*/;

    public final native void _setAnnotatedText(AnnotatedTextImpl annotatedText) /*-{ this.annotatedText = annotatedText; }-*/;

    public final native void _removeAnnotatedTextRef() /*-{ if (this.hasOwnProperty('annotatedText')) { delete this['annotatedText']; } }-*/;

    final void setAnnotatedText(AnnotatedTextImpl annotatedText) {
        if (annotatedText != null) {
            _setAnnotatedText(annotatedText);
        } else {
            _removeAnnotatedTextRef();
        }
    }

    @Override
    public final AnnotationKind getAnnotationKind() {
        return AnnotationKind.values()[_getKind()];
    }

    private final native int _getKind() /*-{ return this.kind; }-*/;

    @Override
    public final native String getAnnotationType() /*-{ return this.type; }-*/;

    public final native void setAnnotationType(String type) /*-{ this.type=type; }-*/;

    @Override
    public final TextBinding getTextBinding() {
        if (getAnnotationKind().equals(AnnotationKind.TEXT)) {
            return _getTextBinding();
        } else {
            return null;
        }
    }

    private final native TextBindingImpl _getTextBinding() /*-{ return this.text; }-*/;

    @Override
    public final AnnotationGroup getAnnotationGroup() {
        if (getAnnotationKind().equals(AnnotationKind.GROUP)) {
            return _getAnnotationGroup();
        } else {
            return null;
        }
    }

    private final native AnnotationGroupImpl _getAnnotationGroup() /*-{ return this.group; }-*/;

    @Override
    public final Relation getRelation() {
        if (getAnnotationKind().equals(AnnotationKind.RELATION)) {
            return _getRelation();
        } else {
            return null;
        }
    }

    private final native RelationImpl _getRelation() /*-{ return this.relation; }-*/;

    @Override
    public final native Properties getProperties() /*-{ return this.properties; }-*/;

    public final String getJSON() {
        String jsonStr = null;
        AnnotatedTextImpl annotatedText = getAnnotatedText();
        try {
            setAnnotatedText(null);
            jsonStr = new JSONObject(this).toString();
        } finally {
            setAnnotatedText(annotatedText);
        }
        return jsonStr;
    }

    @Override
    public final String getAnnotationText(String fragmentSeparator) {
        StringBuilder annotationText = new StringBuilder();
        getAnnotationText(annotationText, fragmentSeparator);
        return annotationText.toString();
    }

    private final void getAnnotationText(StringBuilder annotationText, String fragmentSeparator) {
        if (AnnotationKind.TEXT.equals(this.getAnnotationKind())) {
            boolean once = false;
            String leTexte = this.getAnnotatedText().getDocument().getContents();
            for (Fragment f : this.getTextBinding().getSortedFragments()) {
                if (once) {
                    if (fragmentSeparator != null) {
                        annotationText.append(fragmentSeparator);
                    }
                } else {
                    once = true;
                }
                annotationText.append(leTexte.substring(f.getStart(), f.getEnd()));
            }
        }
    }

    public final void getAnnotationFragmentCoords(StringBuilder annotationText) {
        if (AnnotationKind.TEXT.equals(this.getAnnotationKind())) {
            boolean once = false;
            for (Fragment f : this.getTextBinding().getSortedFragments()) {
                if (once) {
                    annotationText.append(", ");
                } else {
                    once = true;
                }
                annotationText.append("[").append(f.getStart()).append(", ").append(f.getEnd()).append("]");
            }
        }
    }

    private final String getDetail() {
        boolean once = false;
        StringBuilder sb = new StringBuilder();
        AnnotatedTextImpl annotatedText = this.getAnnotatedText();

        switch (this.getAnnotationKind()) {
            case TEXT:
                this.getAnnotationFragmentCoords(sb);
                break;
            case GROUP:
                sb.append("{ ");
                for (AnnotationReference a : this.getAnnotationGroup().getComponentRefs()) {
                    if (once) {
                        sb.append(", ");
                    } else {
                        once = true;
                    }
                    String id = a.getAnnotationId();

                    Annotation refAnn = annotatedText.getAnnotation(id);
                    sb.append("[").append(refAnn.getAnnotationKind().name()).append(":");
                    sb.append(refAnn.getAnnotationType()).append("]").append(" ");
                    
                    if (AnnotationKind.TEXT.equals(refAnn.getAnnotationKind())) {
                        sb.append(refAnn.getAnnotationText(null));
                    } else {
                        sb.append(AnnotatedTextProcessor.getBriefId(id));
                    }
                }
                sb.append(" }");
                break;
            case RELATION:
                String annType = this.getAnnotationType();
                AnnotationTypeDefinition annTypeDef = annotatedText.getAnnotationSchema().getAnnotationTypeDefinition(annType);
                for (String role : annTypeDef.getRelationDefinition().getSupportedRoles()) {
                    if (once) {
                        sb.append(" + ");
                    } else {
                        once = true;
                    }
                    String id = this.getRelation().getArgumentRef(role).getAnnotationId();
                    sb.append(role).append("( ");

                    Annotation refAnn = annotatedText.getAnnotation(id);
                    sb.append("[").append(refAnn.getAnnotationKind().name()).append(":");
                    sb.append(refAnn.getAnnotationType()).append("]").append(" ");

                    if (AnnotationKind.TEXT.equals(refAnn.getAnnotationKind())) {
                        sb.append(refAnn.getAnnotationText(null));
                    } else {
                        sb.append(AnnotatedTextProcessor.getBriefId(id));
                    }
                    sb.append(" )");
                }
                break;
        }
        return sb.toString();
    }
    private static final String FIELD_SEPARATOR = ",";
    private static final String FIELD_DELIMITER = "\"";
    private static final String FIELD_TERMINATOR = "\n";

    public static final String getCSV(AnnotationImpl annotation) {
        StringBuilder result = new StringBuilder();
        if (annotation == null) {
            result.append(FIELD_DELIMITER).append("ID").append(FIELD_DELIMITER).append(FIELD_SEPARATOR);
            result.append(FIELD_DELIMITER).append("KIND").append(FIELD_DELIMITER).append(FIELD_SEPARATOR);
            result.append(FIELD_DELIMITER).append("TYPE").append(FIELD_DELIMITER).append(FIELD_SEPARATOR);
            result.append(FIELD_DELIMITER).append("TEXT").append(FIELD_DELIMITER).append(FIELD_SEPARATOR);
            result.append(FIELD_DELIMITER).append("DETAILS").append(FIELD_DELIMITER).append(FIELD_SEPARATOR);

        } else {
            result.append(FIELD_DELIMITER).append(annotation.getId()).append(FIELD_DELIMITER).append(FIELD_SEPARATOR);
            result.append(FIELD_DELIMITER).append(annotation.getAnnotationKind().name()).append(FIELD_DELIMITER).append(FIELD_SEPARATOR);
            result.append(FIELD_DELIMITER).append(annotation.getAnnotationType()).append(FIELD_DELIMITER).append(FIELD_SEPARATOR);
            String txt = annotation.getAnnotationText(null);
            if (!txt.isEmpty()) {
                result.append(FIELD_DELIMITER).append(txt).append(FIELD_DELIMITER);
            }
            result.append(FIELD_SEPARATOR);
            result.append(FIELD_DELIMITER).append(annotation.getDetail()).append(FIELD_DELIMITER).append(FIELD_SEPARATOR);

        }
        result.append(FIELD_TERMINATOR);
        return result.toString();
    }

    public final String getCSV() {
        return getCSV(this);
    }
}
