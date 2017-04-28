/*
 *
 *      This software is a result of Quaero project and its use must respect the rules of the Quaero Project Consortium Agreement.
 *
 *      Copyright Institut National de la Recherche Agronomique, 2010-2011.
 *
 */
package fr.inra.mig_bibliome.stane.client.data3;

import com.google.gwt.core.client.JavaScriptObject;
import fr.inra.mig_bibliome.stane.shared.data3.Document;
import fr.inra.mig_bibliome.stane.shared.data3.Properties;


/**
 *
 * @author fpapazian
 */
public class DocumentImpl extends JavaScriptObject implements Document {

    protected DocumentImpl() {
    }

    @Override
    public final native int getId() /*-{ return this.id; }-*/;

    @Override
    public final native String getDescription() /*-{ return this.description; }-*/;

    @Override
    public final native String getContents()  /*-{ return this.contents; }-*/;

    @Override
    public final native void setContents(String contents) /*-{ this.contents=contents; }-*/;

    @Override
    public final native int getOwner() /*-{ return this.owner; }-*/;

    @Override
    public final native Properties getProperties() /*-{ return this.props; }-*/;
    
}
