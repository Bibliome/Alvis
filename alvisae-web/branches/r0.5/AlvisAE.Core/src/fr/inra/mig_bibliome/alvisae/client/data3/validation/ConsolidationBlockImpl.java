/*
 *
 *      This software is a result of Quaero project and its use must respect the rules of the Quaero Project Consortium Agreement.
 *
 *      Copyright Institut National de la Recherche Agronomique, 2012.
 *
 */
package fr.inra.mig_bibliome.alvisae.client.data3.validation;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import fr.inra.mig_bibliome.alvisae.client.data3.AnnotationReferenceImpl;
import fr.inra.mig_bibliome.alvisae.client.data3.JsArrayDecorator;
import fr.inra.mig_bibliome.alvisae.shared.data3.AnnotationReference;
import fr.inra.mig_bibliome.alvisae.shared.data3.validation.ConsolidationBlock;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author fpapazian
 */
public class ConsolidationBlockImpl extends JavaScriptObject implements ConsolidationBlock {

    protected ConsolidationBlockImpl() {
        
    }
    
    @Override
    public final native int getStart() /*-{ return this.start; }-*/;

    @Override
    public final native int getEnd() /*-{ return this.end; }-*/;

    @Override
    public final native boolean isWithoutConflict() /*-{ return this.noconflict; }-*/;

    public final native JsArray<JsArray<AnnotationReferenceImpl>> _getMembers() /*-{ return this.members; }-*/;

    @Override
    public final List<List<? extends AnnotationReference>> getMembers() {
        List<List<? extends AnnotationReference>> result = new ArrayList<List<? extends AnnotationReference>>();
        for (JsArray<AnnotationReferenceImpl> annRefs : new JsArrayDecorator<JsArray<AnnotationReferenceImpl>>(_getMembers())) {
            ArrayList<AnnotationReferenceImpl> lar = new ArrayList<AnnotationReferenceImpl>();
            lar.addAll(new JsArrayDecorator<AnnotationReferenceImpl>(annRefs));
            result.add(lar);
        }
        return result;
    }
}
