/*
 *
 *      This software is a result of Quaero project and its use must respect the rules of the Quaero Project Consortium Agreement.
 *
 *      Copyright Institut National de la Recherche Agronomique, 2010.
 *
 */
package fr.inra.mig_bibliome.stane.client.data3;

import com.google.gwt.core.client.JavaScriptObject;
import fr.inra.mig_bibliome.stane.shared.data3.AnnotationSetCore;
import fr.inra.mig_bibliome.stane.shared.data3.AnnotationSetType;

/**
 *
 * @author fpapazian
 */
class AnnotationSetCoreImpl extends JavaScriptObject implements AnnotationSetCore {

    protected AnnotationSetCoreImpl() {
    }

    @Override
    public final native int getId() /*-{ return this.id; }-*/;

    private final native String _getType() /*-{ return this.type; }-*/;
    
    @Override
    public final AnnotationSetType getType() {
        return AnnotationSetType.valueOf(_getType());
    }

    @Override
    public final native String getTimestamp() /*-{ return this.timestamp; }-*/;

    @Override
    public final native String getDescription() /*-{ return this.description; }-*/;

    @Override
    public final native int getRevision() /*-{ return this.revision; }-*/;

    @Override
    public final native boolean isHead() /*-{ return this.head; }-*/;
    
    @Override
    public final native int getOwner() /*-{ return this.owner; }-*/;
    
}
