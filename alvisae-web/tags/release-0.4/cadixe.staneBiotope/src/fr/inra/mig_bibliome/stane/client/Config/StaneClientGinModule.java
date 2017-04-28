/*
 *
 *      This software is a result of Quaero project and its use must respect the rules of the Quaero Project Consortium Agreement.
 *
 *      Copyright Institut National de la Recherche Agronomique, 2010-2012.
 *
 */
package fr.inra.mig_bibliome.stane.client.Config;

import com.google.inject.Provides;
import fr.inra.mig_bibliome.stane.client.Campaign.CampaignDocList;
import fr.inra.mig_bibliome.stane.client.Campaign.DocSelectingView;
import fr.inra.mig_bibliome.stane.client.Config.History.StaneActivityMapper;
import fr.inra.mig_bibliome.stane.client.Config.History.StaneActivityMapperImpl;
import fr.inra.mig_bibliome.stane.client.Document.DocEditingView;
import fr.inra.mig_bibliome.stane.client.Document.DocEditingViewImpl;
import fr.inra.mig_bibliome.stane.client.SignIn.SignInDialog;
import fr.inra.mig_bibliome.stane.client.SignIn.SignInView;
import fr.inra.mig_bibliome.stane.client.User.UserManager;
import fr.inra.mig_bibliome.stane.client.User.UserManagingView;

/**
 * Binding Gin module - provides implementations for several global interface instances
 * @author fpapazian
 */
public class StaneClientGinModule extends StaneClientBaseGinModule {

    private static SignInView loginView = null;
    private static StaneActivityMapperImpl activityMapper = null;
    private static DocEditingView docEditingView = null;
    private static DocSelectingView docSelectingView = null;
    private static UserManagingView userManagingView = null;

    @Override
    protected void configure() {
    }

    @Provides
    public SignInView getLoginView() {
        if (loginView == null) {
            loginView = new SignInDialog();
        }
        return loginView;
    }

   @Provides
    public DocEditingView getDocEditingView() {
        if (docEditingView == null) {
            docEditingView = new DocEditingViewImpl();
        }
        return docEditingView;
    }

    @Provides
    public StaneActivityMapper getStaneActivityMapper() {
        if (activityMapper == null) {
            activityMapper = new StaneActivityMapperImpl();
        }
        return activityMapper;
    }

   @Provides
    public DocSelectingView getDocSelectingView() {
        if (docSelectingView == null) {
            docSelectingView = new CampaignDocList();
        }
        return docSelectingView;
    }

   @Provides
    public UserManagingView getUserManagingView() {
        if (userManagingView == null) {
            userManagingView = new UserManager();
        }
        return userManagingView;
    }
}
