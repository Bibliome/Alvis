/*
 *
 *      This software is a result of Quaero project and its use must respect the rules of the Quaero Project Consortium Agreement.
 *
 *      Copyright Institut National de la Recherche Agronomique, 2010-2011.
 *
 */
package fr.inra.mig_bibliome.stane.client.data3;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.json.client.JSONObject;

/**
 *
 * @author fpapazian
 */
public class CDXWS_UserAlvisNLPDocId_Response extends JavaScriptObject {

    /**
     * @throws IllegalArgumentException if invalid or not expected JSON 
     */
    public final static CDXWS_UserAlvisNLPDocId_Response createFromJSON(String jsonStr) {
        CDXWS_UserAlvisNLPDocId_Response result = JsonUtils.safeEval(jsonStr).cast();
        return result;
    }

    public final native int getDocumentId() /*-{ return this.doc_id; }-*/;

    public final native int getCampaignId() /*-{ return this.campaign_id; }-*/;

    private final native JavaScriptObject _getAnnotatedText() /*-{ return this.doc; }-*/;

    public final AnnotatedTextImpl getAnnotatedText() {
        //Warning : simple JSON deserialization does not produce a complete AnnotatedTextImpl
        //          use adhoc method instead
        String jsonStr = new JSONObject(_getAnnotatedText()).toString();
        return AnnotatedTextImpl.createFromJSON(jsonStr);
    }
    
    protected CDXWS_UserAlvisNLPDocId_Response() {
    }
}
