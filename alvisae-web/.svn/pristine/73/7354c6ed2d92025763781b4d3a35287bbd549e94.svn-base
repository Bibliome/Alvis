/*
 *
 *      This software is a result of Quaero project and its use must respect the rules of the Quaero Project Consortium Agreement.
 *
 *      Copyright Institut National de la Recherche Agronomique, 2010-2011.
 *
 */
package fr.inra.mig_bibliome.stane.client.Document;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;
import com.google.gwt.user.client.Window;
import fr.inra.mig_bibliome.stane.client.Config.History.AuthenticationRequiredPlace;
import fr.inra.mig_bibliome.stane.client.Config.History.CloneablePlace;
import fr.inra.mig_bibliome.stane.client.Config.History.UserCampaignDocPlaceUtils;
import fr.inra.mig_bibliome.stane.client.Config.History.UserCampaignDocPlaceUtils.ParamsProcessor;

/**
 * Place Corresponding to a specific document display activity
 * @author fpapazian
 */
public class DocDisplayPlace extends Place implements CloneablePlace, AuthenticationRequiredPlace {

    public static class BasicDocExtIdParams {
        private static String DOCEXTERNALID_PARAMNAME = "AlvisNLPID";

        public static BasicDocExtIdParams createFromToken(String token) {
            return new BasicDocExtIdParams(token);
        }
        
        private String docExternalId;

        public BasicDocExtIdParams(String token) {
            docExternalId = null;
            parseToken(token);
        }

        public String getDocExternalId() {
            return docExternalId;
        }

        public void setDocExternalId(String docExternalId) {
            this.docExternalId = docExternalId;
        }

        public String createToken() {
            return DOCEXTERNALID_PARAMNAME + ParamsProcessor.KEYVAL_SEP + getDocExternalId();
        }

        public class BasicDocExtIdParamsProcessor implements ParamsProcessor {

            @Override
            public void processParam(String key, String value) {
                if (key.toLowerCase().equals(DOCEXTERNALID_PARAMNAME.toLowerCase()) && docExternalId == null) {
                    docExternalId = value;
                }
            }
        }

        protected void parseToken(String token) {
            UserCampaignDocPlaceUtils.parseToken(token, new BasicDocExtIdParamsProcessor());
        }
    }
    
    private BasicDocExtIdParams params;

    public DocDisplayPlace(BasicDocExtIdParams params) {
        this.params = params;
    }

    public BasicDocExtIdParams getParams() {
        return params;
    }

    @Override
    public DocDisplayPlace clone() {
        return tokenizer.getPlace(tokenizer.getToken(this));
    }
    public static final Tokenizer tokenizer = new Tokenizer();

    @Prefix("docDisp")
    public static class Tokenizer implements PlaceTokenizer<DocDisplayPlace> {

        @Override
        public String getToken(DocDisplayPlace place) {
            return place.getParams().createToken();
        }

        @Override
        public DocDisplayPlace getPlace(String token) {
            return new DocDisplayPlace(new BasicDocExtIdParams(token));
        }
    }
}
