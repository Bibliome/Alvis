/*
 *
 *      This software is a result of Quaero project and its use must respect the rules of the Quaero Project Consortium Agreement.
 *
 *      Copyright Institut National de la Recherche Agronomique, 2010-2011.
 *
 */
package fr.inra.mig_bibliome.stane.client.Document;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import fr.inra.mig_bibliome.stane.client.Config.StaneClientGinInjector;
import fr.inra.mig_bibliome.stane.client.Document.DocDisplayPlace.BasicDocExtIdParams;
import fr.inra.mig_bibliome.stane.client.Events.WorkingDocumentChangedEvent;
import fr.inra.mig_bibliome.stane.client.Events.ApplicationStatusChangedEvent;
import fr.inra.mig_bibliome.stane.client.data3.AnnotatedTextHandler;
import fr.inra.mig_bibliome.stane.client.data3.CDXWS_UserAlvisNLPDocId_Response;

/**
 * Activity providing Document Annotation Editing feature
 * @author fpapazian
 */
public class DocDisplayActivity extends AbstractActivity implements DocEditingView.Presenter {

    private static final StaneClientGinInjector injector = GWT.create(StaneClientGinInjector.class);
    private final DocDisplayPlace place;

    public DocDisplayActivity(DocDisplayPlace place) {
        this.place = place;
    }

    /**
     * Invoked by the ActivityManager to start a new Activity
     */
    @Override
    public void start(final AcceptsOneWidget containerWidget, final EventBus eventBus) {

        //TODO since the Asynch loading can take some time, a wait dialog should appear on screen to make user more comfortable
        eventBus.fireEvent(new ApplicationStatusChangedEvent(ApplicationStatusChangedEvent.ApplicationStatusSwitching.Processing, null));

        //Async loading to enable fast startup of the default activity
        GWT.runAsync(new RunAsyncCallback() {

            @Override
            public void onFailure(Throwable reason) {
                eventBus.fireEvent(new ApplicationStatusChangedEvent(ApplicationStatusChangedEvent.ApplicationStatusSwitching.Idle, null));
                throw new UnsupportedOperationException("Unable to load DocEditing view.");
            }

            @Override
            public void onSuccess() {
                Scheduler.get().scheduleDeferred(new Command() {

                    @Override
                    public void execute() {
                        final DocEditingView docEditingView = injector.getDocEditingView();
                        final BasicDocExtIdParams params = place.getParams();
                        docEditingView.setPresenter(DocDisplayActivity.this);
                        containerWidget.setWidget(docEditingView.asWidget());

                        if (place != null) {
                            injector.getMainEventBus().fireEvent(new WorkingDocumentChangedEvent(null, null, WorkingDocumentChangedEvent.ChangeType.Unloaded));
                            final int userId = injector.getCoreDataProvider().getRequestManager().getCurrentUserId();

                            injector.getCoreDataProvider().getAnnotatedDocument(userId, params.getDocExternalId(), new AsyncCallback<CDXWS_UserAlvisNLPDocId_Response>() {

                                @Override
                                public void onFailure(Throwable caught) {
                                    Window.alert("Document " + params.getDocExternalId() + " is unknown!");
                                    GWT.log(caught.getMessage());
                                    docEditingView.setDocument(null, true);
                                }

                                @Override
                                public void onSuccess(CDXWS_UserAlvisNLPDocId_Response result) {

                                    int campaignId = result.getCampaignId();
                                    docEditingView.setParameters(userId, campaignId, result.getDocumentId(), 0);
                                    AnnotatedTextHandler hnd2 = AnnotatedTextHandler.createHandler(userId, campaignId, result.getAnnotatedText());
                                    //document is displayed in readonlyMode
                                    docEditingView.setDocument(hnd2, true);
                                }
                            });
                        } else {
                            //empty the view
                            docEditingView.setDocument(null, true);
                        }
                        eventBus.fireEvent(new ApplicationStatusChangedEvent(ApplicationStatusChangedEvent.ApplicationStatusSwitching.Idle, null));
                    }
                });

            }
        });
    }

    /**
     * Ask user before stopping this activity
     */
    @Override
    public String mayStop() {
        //Since no modification is allowed, the view can be closed anytime
        return null;
    }

    @Override
    public void onStop() {
        super.onStop();
        //empty the view
        injector.getDocEditingView().setDocument(null, true);
        injector.getMainEventBus().fireEvent(new WorkingDocumentChangedEvent(null, null, WorkingDocumentChangedEvent.ChangeType.Unloaded));
    }

    /**
     * Navigate to a new Place in the browser
     */
    @Override
    public void goTo(Place place) {
        injector.getPlaceController().goTo(place);
    }
}
