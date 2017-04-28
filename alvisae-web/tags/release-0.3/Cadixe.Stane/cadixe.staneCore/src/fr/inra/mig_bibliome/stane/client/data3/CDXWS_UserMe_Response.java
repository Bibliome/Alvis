/*
 *
 *      This software is a result of Quaero project and its use must respect the rules of the Quaero Project Consortium Agreement.
 *
 *      Copyright Institut National de la Recherche Agronomique, 2010-2011.
 *
 */
package fr.inra.mig_bibliome.stane.client.data3;

/**
 *
 * @author fpapazian
 */
public class CDXWS_UserMe_Response extends Basic_UserMe_Response {

    public final native String getLoginName() /*-{ return this.name; }-*/;

    public final native CampaignListImpl getCampaignList() /*-{ return this.campaigns; }-*/;

    protected CDXWS_UserMe_Response() {
    }
}
