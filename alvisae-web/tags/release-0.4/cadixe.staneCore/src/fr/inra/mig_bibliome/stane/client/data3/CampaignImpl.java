/*
 *
 *      This software is a result of Quaero project and its use must respect the rules of the Quaero Project Consortium Agreement.
 *
 *      Copyright Institut National de la Recherche Agronomique, 2010-2011.
 *
 */
package fr.inra.mig_bibliome.stane.client.data3;

import com.google.gwt.core.client.JavaScriptObject;
import fr.inra.mig_bibliome.stane.shared.data3.Campaign;
import java.util.Date;

/**
 *
 * @author fpapazian
 */
public class CampaignImpl extends JavaScriptObject implements Campaign {

    protected CampaignImpl() {
    }
    
    @Override
    public final native int getId() /*-{ return this.id; }-*/;

    @Override
    public final native String getDisplayName() /*-{ return this.name; }-*/;

    @Override
    public final String getDescription() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public final boolean isOpen() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public final Date getStartingDate() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public final Date getPlannedEndDate() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public final String getAdminUserId() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
