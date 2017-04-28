/*
 *
 *      This software is a result of Quaero project and its use must respect the rules of the Quaero Project Consortium Agreement.
 *
 *      Copyright Institut National de la Recherche Agronomique, 2010.
 *
 */
package fr.inra.mig_bibliome.stane.client.Campaign;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;
import fr.inra.mig_bibliome.stane.client.Config.History.BasicUserCampaignDocParams;
import fr.inra.mig_bibliome.stane.client.Config.History.CloneablePlace;
import fr.inra.mig_bibliome.stane.client.Config.History.ExplicitAuthenticationRequiredPlace;

/**
 *
 * @author fpapazian
 */
public class DocSelectingPlace extends Place implements CloneablePlace, ExplicitAuthenticationRequiredPlace {

    private BasicUserCampaignDocParams params;

    public DocSelectingPlace(BasicUserCampaignDocParams params) {
        if (params==null) {
            params = new BasicUserCampaignDocParams("");
        }
        this.params = params;
    }

    public BasicUserCampaignDocParams getParams() {
        return params;
    }

    @Override
    public DocSelectingPlace clone() {
        return tokenizer.getPlace(tokenizer.getToken(this));
    }
    public static final Tokenizer tokenizer = new Tokenizer();

    @Prefix("docSelect")
    public static class Tokenizer implements PlaceTokenizer<DocSelectingPlace> {

        @Override
        public String getToken(DocSelectingPlace place) {
            return place.getParams().createToken();
        }

        @Override
        public DocSelectingPlace getPlace(String token) {
            return new DocSelectingPlace(new BasicUserCampaignDocParams(token));
        }
    }
}
