/*
 *
 *      This software is a result of Quaero project and its use must respect the rules of the Quaero Project Consortium Agreement.
 *
 *      Copyright Institut National de la Recherche Agronomique, 2010-2011.
 *
 */
package fr.inra.mig_bibliome.stane.client.Config.History;

import fr.inra.mig_bibliome.stane.client.Campaign.DocSelectingPlace;
import fr.inra.mig_bibliome.stane.client.SignIn.SignInActivity;
import com.google.gwt.activity.shared.Activity;
import com.google.gwt.core.client.GWT;
import com.google.gwt.place.shared.Place;
import fr.inra.mig_bibliome.stane.client.Start.DefaultViewActivity;
import fr.inra.mig_bibliome.stane.client.Document.DocEditingActivity;
import fr.inra.mig_bibliome.stane.client.Start.DefaultPlace;
import fr.inra.mig_bibliome.stane.client.Document.DocEditingPlace;
import fr.inra.mig_bibliome.stane.client.SignIn.SignInPlace;
import fr.inra.mig_bibliome.stane.client.Config.StaneClientGinInjector;
import fr.inra.mig_bibliome.stane.client.Campaign.DocSelectingActivity;
import fr.inra.mig_bibliome.stane.client.Document.DocDisplayActivity;
import fr.inra.mig_bibliome.stane.client.Document.DocDisplayPlace;

/**
 * Provides the Activity corresponding to a specific Place, while checking that Authentication is performed whenever it is necessary
 * @author fpapazian
 */
public class StaneActivityMapperImpl implements StaneActivityMapper {

    private static final StaneClientGinInjector injector = GWT.create(StaneClientGinInjector.class);
    private Activity currentActivity = null;
    private Place currentPlace = null;
    private Place intendedPlace = null;

    @Override
    public Activity getActivity(Place place) {
        Activity nextActivity = null;

        //Jump to SignIn whenever the activity requires authentication while the user has not yet beeing authenticated
        if (place instanceof AuthenticationRequiredPlace && !injector.getCoreDataProvider().getRequestManager().isSignedIn()) {
            if (place instanceof CloneablePlace) {
                //need to clone the intended place otherwise the PlaceController will not forward to it after signIn
                intendedPlace = ((CloneablePlace) place).clone();
            } else {
                intendedPlace = null;
            }
            boolean trySilentSignIn = !(place instanceof ExplicitAuthenticationRequiredPlace) && (injector.getCoreDataProvider().getRequestManager().canPerformReSignIn());
            nextActivity = new SignInActivity(new SignInPlace(trySilentSignIn));

        } else {
            intendedPlace = null;

            if (place instanceof SignInPlace) {
                nextActivity = new SignInActivity(new SignInPlace());
            } else {
                if (place instanceof DefaultPlace) {

                    nextActivity = new DefaultViewActivity((DefaultPlace) place);

                } else if (place instanceof DocEditingPlace) {
                    nextActivity = new DocEditingActivity((DocEditingPlace) place);
                } else if (place instanceof DocDisplayPlace) {
                    nextActivity = new DocDisplayActivity((DocDisplayPlace) place);
                } else if (place instanceof DocSelectingPlace) {
                    nextActivity = new DocSelectingActivity((DocSelectingPlace) place);
                }
            }
        }

        if (nextActivity != null) {
            currentActivity = nextActivity;
        }

        currentPlace = place;
        return nextActivity;
    }

    /**
     *
     * @return the last Place that was intended to be reached, but was not actually reached due to lack of authentication
     */
    public Place getIntendedPlace() {
        return intendedPlace != null ? intendedPlace : new DefaultPlace("Start");
    }
}
