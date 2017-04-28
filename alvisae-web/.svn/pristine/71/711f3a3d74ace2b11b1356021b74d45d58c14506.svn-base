/*
 *
 *      This software is a result of Quaero project and its use must respect the rules of the Quaero Project Consortium Agreement.
 *
 *      Copyright Institut National de la Recherche Agronomique, 2010.
 *
 */
package fr.inra.mig_bibliome.stane.client.Campaign;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.IsWidget;
import fr.inra.mig_bibliome.stane.client.Config.History.BasicUserCampaignDocParams;

/**
 *
 * @author fpapazian
 */
public interface DocSelectingView extends IsWidget {

    void setParams(BasicUserCampaignDocParams params);

    void setPresenter(Presenter presenter);

    public interface Presenter {

        void goTo(Place place);
    }
    
    Integer getNextDocumentId(int documentId);
    
    Integer getPrevDocumentId(int documentId);
}
