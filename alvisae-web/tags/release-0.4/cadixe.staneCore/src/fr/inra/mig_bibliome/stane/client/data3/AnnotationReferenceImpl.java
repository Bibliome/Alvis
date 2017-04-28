/*
 *
 *      This software is a result of Quaero project and its use must respect the rules of the Quaero Project Consortium Agreement.
 *
 *      Copyright Institut National de la Recherche Agronomique, 2010-2012.
 *
 */
package fr.inra.mig_bibliome.stane.client.data3;

import com.google.gwt.core.client.JavaScriptObject;
import fr.inra.mig_bibliome.stane.shared.data3.AnnotationReference;

/**
 * 
 * @author fpapazian
 */
public class AnnotationReferenceImpl extends JavaScriptObject implements AnnotationReference {

    public static final native AnnotationReferenceImpl create(String annotationId) /*-{
    aRef = {};
    aRef.ann_id=annotationId;
    return aRef;
    }-*/;

    public static final AnnotationReferenceImpl create(String annotationId, int annotationSetId) {
        AnnotationReferenceImpl aRef = create(annotationId);
        aRef.setAnnotationSetId(annotationSetId);
        return aRef;
    }

    protected AnnotationReferenceImpl() {
    }

    @Override
    public final native String getAnnotationId() /*-{ return this.ann_id; }-*/;

    @Override
    public final native Integer getAnnotationSetId() /*-{ if (this.hasOwnProperty('set_id')) { return this.set_id; } else { return null; } }-*/;

    private final native void setAnnotationSetId(int annotationSetId) /*-{ this.set_id = annotationSetId; }-*/;

    private final static String INVALIDFIELD_PREFIX = "Invalid field ";

    /**
     * Check that the AnnotationReference parsed from a JSON string conforms to the expected
     * structure
     *
     * @throws IllegalArgumentException
     */
    public final void checkStructure() {
        String annId = null;
        try {
            annId = getAnnotationId();
        } catch (Exception ex) {
            throw new IllegalArgumentException(INVALIDFIELD_PREFIX + "AnnotationRefId", ex);
        }
        if (annId == null || annId.trim().isEmpty()) {
            throw new IllegalArgumentException(INVALIDFIELD_PREFIX + "-> AnnotationRefId should not be null");
        }
        try {
            Integer annSetId = getAnnotationSetId();
        } catch (Exception ex) {
            throw new IllegalArgumentException(INVALIDFIELD_PREFIX + "AnnotationSetRefId for AnnotationRefId=" + annId, ex);
        }
    }
}
