/*
 *
 *      This software is a result of Quaero project and its use must respect the rules of the Quaero Project Consortium Agreement.
 *
 *      Copyright Institut National de la Recherche Agronomique, 2010-2011.
 *
 */
package fr.inra.mig_bibliome.stane.client.Config.History;

/**
 *
 * @author fpapazian
 */
public class UserCampaignDocPlaceUtils {

    /**
     * Parameters for Places
     */
    public static interface UserCampaignDocParams {

        public Integer getCampaignId();

        public Integer getDocumentId();

        public Integer getUserId();

        public void setCampaignId(Integer campaignId);

        public void setDocumentId(Integer documentId);

        public void setUserId(Integer userId);
    }

    public static interface UserCampaignDocOffsetParams extends UserCampaignDocParams {

        public Integer getOffset();

        public void setOffset(Integer offset);
    }

    /**
     * Simple methods to parse Place parameters encoded within URL fragment
     */
    public static interface ParamsProcessor {

        public static String PARAM_SEP = "&";
        public static String KEYVAL_SEP = "=";

        void processParam(String key, String value);
    }

    public static void parseToken(String token, ParamsProcessor paramsProc) {
        for (String param : token.split(ParamsProcessor.PARAM_SEP)) {
            String[] kv = param.split(ParamsProcessor.KEYVAL_SEP, 2);
            if (kv.length == 2) {
                String key = kv[0];
                String val = kv[1];
                paramsProc.processParam(key, val);
            }
        }
    }
}