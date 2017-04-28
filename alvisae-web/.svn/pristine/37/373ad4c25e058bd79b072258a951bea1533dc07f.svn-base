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
import fr.inra.mig_bibliome.stane.client.Config.History.BasicUserCampaignDocOffsetParams;
import fr.inra.mig_bibliome.stane.client.Config.StaneClientGinInjector;
import fr.inra.mig_bibliome.stane.client.Events.WorkingDocumentChangedEvent;
import fr.inra.mig_bibliome.stane.client.Events.ApplicationStatusChangedEvent;
import fr.inra.mig_bibliome.stane.client.StaneCoreResources;
import fr.inra.mig_bibliome.stane.client.data3.AnnotatedTextHandler;
import fr.inra.mig_bibliome.stane.client.data3.AnnotatedTextImpl;

/**
 * Activity providing Document Annotation Editing feature
 * @author fpapazian
 */
public class DocEditingActivity extends AbstractActivity implements DocEditingView.Presenter {

    private static final StaneClientGinInjector injector = GWT.create(StaneClientGinInjector.class);
    private final DocEditingPlace place;

    public DocEditingActivity(DocEditingPlace place) {
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
                        final BasicUserCampaignDocOffsetParams params = place.getParams();
                        docEditingView.setParameters(params.getUserId(), params.getCampaignId(), params.getDocumentId(), params.getOffset());
                        docEditingView.setPresenter(DocEditingActivity.this);
                        containerWidget.setWidget(docEditingView.asWidget());

                        if (place != null && place.isComplete()) {
                            injector.getMainEventBus().fireEvent(new WorkingDocumentChangedEvent(null, null, WorkingDocumentChangedEvent.ChangeType.Unloaded));

                            // #########################################################
                            //temporary code
                            if (injector.getCoreDataProvider().getRequestManager().isAutoSignedIn()) {

                                String json = StaneCoreResources.INSTANCE.jsonData3Test().getText();
                                final AnnotatedTextImpl newDoc = AnnotatedTextImpl.createFromJSON(json);
                                //create Modification Handler
                                AnnotatedTextHandler hnd1 = AnnotatedTextHandler.createHandler(0, 0, newDoc);
                                docEditingView.setDocument(hnd1, false);

                                // #########################################################
                            } else {
                                injector.getCoreDataProvider().getAnnotatedDocument(params.getUserId(), params.getCampaignId(), params.getDocumentId(), new AsyncCallback<AnnotatedTextImpl>() {

                                    @Override
                                    public void onFailure(Throwable caught) {
                                        Window.alert("Document " + params.getDocumentId() + " was not found in Campaign " + params.getCampaignId() + " !");
                                        GWT.log(caught.getMessage());
                                        docEditingView.setDocument(null, true);
                                    }

                                    @Override
                                    public void onSuccess(AnnotatedTextImpl result) {
                                        AnnotatedTextHandler hnd2 = AnnotatedTextHandler.createHandler(params.getUserId(), params.getCampaignId(), result);
                                        docEditingView.setDocument(hnd2, false);
                                    }
                                });
                            }

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
        //Alert only if there is a document being edited
        return !injector.getDocEditingView().canCloseView() ? "Do you want to leave this document and loose unsaved modifications?" : null;
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
