/*
 *
 *      This software is a result of Quaero project and its use must respect the rules of the Quaero Project Consortium Agreement.
 *
 *      Copyright Institut National de la Recherche Agronomique, 2010-2011.
 *
 */
package fr.inra.mig_bibliome.stane.client.data3;

import com.google.gwt.core.client.JavaScriptObject;
import fr.inra.mig_bibliome.stane.shared.data3.DocumentInfo;

/**
 *
 * @author fpapazian
 */
public class DocumentInfoImpl extends JavaScriptObject implements DocumentInfo {

    protected DocumentInfoImpl() {
    }

    @Override
    public final native int getId() /*-{ return this.id; }-*/;

    @Override
    public final native String getDescription() /*-{ return this.description; }-*/;

    @Override
    public final native String getStartedAt() /*-{ return this.started_at; }-*/;

    @Override
    public final native String getFinishedAt() /*-{ return this.finished_at; }-*/;
}
