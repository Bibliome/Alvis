/*
 *
 *      This software is a result of Quaero project and its use must respect the rules of the Quaero Project Consortium Agreement.
 *
 *      Copyright Institut National de la Recherche Agronomique, 2010-2012.
 *
 */
package fr.inra.mig_bibliome.stane.client.Document;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import fr.inra.mig_bibliome.stane.client.About.AboutStaneDialog;
import fr.inra.mig_bibliome.stane.client.Annotation.AnnotationDetailsUi;
import fr.inra.mig_bibliome.stane.client.Campaign.DocSelectingPlace;
import fr.inra.mig_bibliome.stane.client.Config.GlobalStyles;
import fr.inra.mig_bibliome.stane.client.Config.History.BasicUserCampaignDocOffsetParams;
import fr.inra.mig_bibliome.stane.client.Config.History.BasicUserCampaignDocParams;
import fr.inra.mig_bibliome.stane.client.Config.StaneClientGinInjector;
import fr.inra.mig_bibliome.stane.client.Events.Extension.TermAnnotationsExpositionEvent;
import fr.inra.mig_bibliome.stane.client.Events.Extension.TermAnnotationsExpositionEventHandler;
import fr.inra.mig_bibliome.stane.client.Events.*;
import fr.inra.mig_bibliome.stane.client.ExportDialog;
import fr.inra.mig_bibliome.stane.client.SemClass.StructTermUi;
import fr.inra.mig_bibliome.stane.client.StaneResources;
import fr.inra.mig_bibliome.stane.client.Start.DefaultPlace;
import fr.inra.mig_bibliome.stane.client.data.Retrieve.NetworkActivityDisplayer;
import fr.inra.mig_bibliome.stane.client.data.Retrieve.RequestManager;
import fr.inra.mig_bibliome.stane.client.data3.AnnotatedTextHandler;
import fr.inra.mig_bibliome.stane.client.data3.AnnotationSetImpl;
import fr.inra.mig_bibliome.stane.client.data3.AnnotationSetListImpl;
import fr.inra.mig_bibliome.stane.client.data3.JsArrayDecorator;
import java.util.HashSet;
import java.util.logging.Logger;

/**
 * Main UI of the Annotation Editor, it contains - a document editor panel, - a
 * table of all annotations of the current document, - a property grid for the
 * selected annotation - a tool bar allowing to navigate through the documents
 * of the campaign
 *
 * @author fpapazian
 */
public class DocEditingViewImpl extends Composite implements DocEditingView, EditHappenedEventHandler, MaximizingWidgetEventHandler, TermAnnotationsExpositionEventHandler, ApplicationStatusChangedEventHandler {

    interface DocEditingViewImplUiBinder extends UiBinder<DockLayoutPanel, DocEditingViewImpl> {
    }
    private static DocEditingViewImplUiBinder uiBinder = GWT.create(DocEditingViewImplUiBinder.class);
    private static final Logger log = Logger.getLogger(DocEditingViewImpl.class.getName());

    static class InformationPanel {

        private final HTML panel;
        private final Timer timer;

        public InformationPanel(HTML htmlPanel) {
            this.panel = htmlPanel;
            timer = new Timer() {

                @Override
                public void run() {
                    panel.getElement().setAttribute("style", "display:none;");
                }
            };
        }

        protected void cancelEffect() {
            timer.cancel();
        }

        public void slide() {
            panel.getElement().setAttribute("style", "");
        }

        public void slide(int openDuration) {
            slide();
            timer.schedule(openDuration);
        }

        /**
         *
         * @param event the event which must be displayed to the user
         */
        public void setMessage(InformationReleasedEvent event) {
            cancelEffect();
            panel.setHTML(event.getMessage());
        }
    }

    interface Styles extends CssResource {

        String BackGroundPos();

        String ForeGroundPos();
    }
    //
    private static final StaneClientGinInjector injector = GWT.create(StaneClientGinInjector.class);
    //
    // --
    @UiField
    DocEditingViewImpl.Styles style;
    // --
    @UiField
    DockLayoutPanel dockPanel;
    @UiField
    LayoutPanel messageBar;
    @UiField
    Panel glassPanel;
    @UiField
    HTML statusPanel;
    @UiField
    PushButton showMessagesBtn;
    @UiField
    PushButton signingBtn;
    @UiField
    PushButton exportButton;
    @UiField
    PushButton saveAnnotationsButton;
    @UiField
    PushButton nextDocButton;
    @UiField
    PushButton prevDocButton;
    @UiField
    DocumentUi documentUI;
    @UiField
    Image aboutImage;
    @UiField
    SplitLayoutPanel detailsDocSplitPanel;
    @UiField
    LayoutPanel detailsPanel;
    @UiField
    SplitLayoutPanel docTableSplitPanel;
    @UiField
    DockLayoutPanel bottomDockPanel;
    @UiField
    LayoutPanel statusBar;
    @UiField
    NetworkActivityDisplayer networkActivityDisplayer;
    @UiField
    StructTermUi structTermUI;
    @UiField
    AnnotationDetailsUi annotationDetailsUI;
    @UiField
    TabLayoutPanel detailsTabsPanel;
//    
    private Presenter presenter;
    private final InformationReleasedEventHandler informationDisplayer;
    private final InformationPanel messagePanel;
    private Integer userId;
    private Integer campaignId;
    private Integer documentId;
    private Integer offset;
    private boolean modified = false;
    private String waterMark;
    private Integer nextDocId;
    private Integer prevDocId;
    //
    private int prevDetailsPanelSize;
    private int prevBottomDockPanelSize;
    private int prevMessageBarSize;
    private int prevStatusBarSize;

    public DocEditingViewImpl() {

        initWidget(uiBinder.createAndBindUi(this));
        networkActivityDisplayer.setRequestManager(injector.getCoreDataProvider().getRequestManager());

        //Add parametrized global styleSheet
        Element cssElement = GlobalStyles.getInlinedStyleElement();
        cssElement.setId("aae_GlobalDynamicStyles.Block");
        Element oldCssElement = Document.get().getElementById(cssElement.getId());
        if (oldCssElement != null) {
            oldCssElement.removeFromParent();
        }
        RootLayoutPanel.get().getElement().insertFirst(cssElement);

        // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        aboutImage.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                // When the 'About' item is selected, show the AboutDialog.
                // Note that showing a dialog box does not block -- execution continues
                // normally, and the dialog fires an event when it is closed.
                AboutStaneDialog dlg = new AboutStaneDialog();
                dlg.show();
                dlg.center();
            }
        });
        // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

        messagePanel = new InformationPanel(statusPanel);
        informationDisplayer = new InformationReleasedEventHandler() {

            @Override
            public void onInformationReleased(InformationReleasedEvent event) {
                messagePanel.setMessage(event);
                messagePanel.slide(3000);
            }
        };

        showMessagesBtn.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                messagePanel.slide(1500);
            }
        });
        // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        //force the split separator to be always visible, even when the view is zoomed
        docTableSplitPanel.setWidgetMinSize(bottomDockPanel, 2);

        RootLayoutPanel.get().removeStyleName(StaneResources.INSTANCE.style().WaitCursor());
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void setDocument(final AnnotatedTextHandler document, boolean readOnly) {
        modified = false;
        saveAnnotationsButton.setEnabled(modified);
        nextDocButton.setEnabled(false);
        prevDocButton.setEnabled(false);
        getDocumentView().setDocument(document, readOnly);
        if (document != null) {

            signingBtn.setTitle("authenticated as: " + injector.getCoreDataProvider().getRequestManager().getCurrentUserName());

            document.setAdditionalAnnotationSetRequestHandler(new AnnotatedTextHandler.AdditionalAnnotationSetRequestHandler() {

                @Override
                public void requestAdditionalAnnotationSet(final AnnotatedTextHandler annotatedTextHandler, int annotationSetId) {

                    HashSet<Integer> annSetIds = new HashSet<Integer>();
                    annSetIds.add(annotationSetId);
                    AsyncCallback<AnnotationSetListImpl> resultCallback = new AsyncCallback<AnnotationSetListImpl>() {

                        @Override
                        public void onFailure(Throwable caught) {
                            log.severe("Problem while requesting addtionnal AnnotationSet!");
                            injector.getMainEventBus().fireEvent(new InformationReleasedEvent("<span style='color:red;'>Problem while requesting addtionnal AnnotationSet!</span>"));
                        }

                        @Override
                        public void onSuccess(AnnotationSetListImpl result) {
                            for (AnnotationSetImpl as : new JsArrayDecorator<AnnotationSetImpl>(result)) {
                                if (!annotatedTextHandler.getLoadedAnnotationSets().contains(as.getId())) {
                                    annotatedTextHandler.addAdditionalAnnotationSet(as);
                                }
                            }
                            injector.getMainEventBus().fireEvent(new WorkingDocumentChangedEvent(document, documentUI, WorkingDocumentChangedEvent.ChangeType.AdditionalAnnotationSetLoaded));
                        }
                    };

                    injector.getCoreDataProvider().getAdditionalAnnotationSet(document, annSetIds, resultCallback);
                }
            });
        }

        Scheduler.get().scheduleDeferred(new Command() {

            @Override
            public void execute() {
                waterMark = getDocumentView().getUndoManager() != null ? getDocumentView().getUndoManager().getWaterMark() : "";
                int docId;
                if (document != null) {
                    docId = document.getAnnotatedText().getDocument().getId();
                } else {
                    //stay on previously edited document
                    docId = documentId;
                }
                nextDocId = injector.getDocSelectingView().getNextDocumentId(docId);
                prevDocId = injector.getDocSelectingView().getPrevDocumentId(docId);
                nextDocButton.setEnabled(nextDocId != null);
                prevDocButton.setEnabled(prevDocId != null);
            }
        });
    }

    @Override
    public void setParameters(Integer userId, Integer campaignId, Integer documentId, Integer offset) {
        this.userId = userId;
        this.campaignId = campaignId;
        this.documentId = documentId;
        this.offset = offset;
    }

    public DocumentView getDocumentView() {
        return documentUI;
    }

    @Override
    public boolean canCloseView() {
        return !modified;
    }

    @UiHandler("signingBtn")
    void handleSigningClick(ClickEvent e) {
        if (injector.getCoreDataProvider().getRequestManager().isSignedIn()) {
            injector.getCoreDataProvider().getRequestManager().signOut(null);
            signingBtn.setTitle("");
            presenter.goTo(new DefaultPlace(null));
        } else {
            presenter.goTo(new DocSelectingPlace(null));
        }
    }

    @UiHandler("exportButton")
    void handleExportAnnotationButtonClick(ClickEvent e) {
        try {
            exportButton.setEnabled(false);
            //String export = injector.getDataProvider().getMapper(documentUI).exportAnchorMarkers();
            //String export = documentUI.getDocument().getJSON();
            //
            String export = documentUI.getAnnotatedTextHandler().getUsersAnnoationAsCSV();

            ExportDialog dlg = new ExportDialog(export);
            dlg.show();
            dlg.center();

        } finally {
            exportButton.setEnabled(true);
        }
    }

    @UiHandler("gotoDocListButton")
    void handleGotoDocListButtonClick(ClickEvent e) {
        presenter.goTo(new DocSelectingPlace(new BasicUserCampaignDocParams(userId, campaignId, null)));
    }

    @UiHandler("nextDocButton")
    void handleNextDocButtonClick(ClickEvent e) {
        if (nextDocId != null) {
            presenter.goTo(new DocEditingPlace(new BasicUserCampaignDocOffsetParams(userId, campaignId, nextDocId, 0)));
        }
    }

    @UiHandler("prevDocButton")
    void handlePrevDocButtonClick(ClickEvent e) {
        if (prevDocId != null) {
            presenter.goTo(new DocEditingPlace(new BasicUserCampaignDocOffsetParams(userId, campaignId, prevDocId, 0)));
        }
    }

    @UiHandler("saveAnnotationsButton")
    void handleSaveDocButtonClick(ClickEvent e) {
        waterMark = documentUI.getUndoManager().getWaterMark();
        modified = false;
        saveAnnotationsButton.setEnabled(modified);
        saveAnnotationsButton.setVisible(false);
        AnnotatedTextHandler hnd = getDocumentView().getAnnotatedTextHandler();
        AnnotationSetImpl usersAnnotationSet = hnd.getUsersAnnotationSet();

        injector.getCoreDataProvider().saveAnnotationSet(userId, campaignId, documentId, usersAnnotationSet, new AsyncCallback<JavaScriptObject>() {

            @Override
            public void onFailure(Throwable caught) {
                log.severe("Problem while saving AnnotationSet!");
                injector.getMainEventBus().fireEvent(new InformationReleasedEvent("<span style='color:red;'>Problem while saving AnnotationSet!</span>"));
                saveAnnotationsButton.setVisible(true);
            }

            @Override
            public void onSuccess(JavaScriptObject result) {
                log.info("AnnotationSet Saved!");
                injector.getMainEventBus().fireEvent(new InformationReleasedEvent("Save performed!"));
                saveAnnotationsButton.setVisible(true);
            }
        });

    }

    @UiHandler("finalizeDocButton")
    void handleFinalizeButtonClick(ClickEvent e) {
        injector.getCoreDataProvider().finalizeDocument(userId, campaignId, documentId, new AsyncCallback<JavaScriptObject>() {

            @Override
            public void onFailure(Throwable caught) {
                log.severe("Problem while finalizing document!");
                injector.getMainEventBus().fireEvent(new InformationReleasedEvent("<span style='color:red;'>Problem while finalizing document!</span>"));
            }

            @Override
            public void onSuccess(JavaScriptObject result) {
                log.info("Document Finalized!");
                injector.getMainEventBus().fireEvent(new InformationReleasedEvent("Document Finalized!"));
            }
        });
    }

    @Override
    public void onEditHappened(EditHappenedEvent event) {

        if (event.getEdit().getAnnotatedTextHandler().getAnnotatedText().getDocument().getId() == documentId) {
            //deferred to leave time to the Undo manager to handle the current edit 
            Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {

                @Override
                public void execute() {
                    modified = (!waterMark.equals(documentUI.getUndoManager().getWaterMark()));
                    saveAnnotationsButton.setEnabled(modified);
                }
            });
        }
    }

    @Override
    public void onMaximizingWidget(MaximizingWidgetEvent event) {
        if (event.getWidget().equals(documentUI)) {
            if (event.isMaximizing()) {
                //Hide everything except documentUI...

                prevDetailsPanelSize = detailsPanel.getElement().getOffsetWidth();
                prevBottomDockPanelSize = bottomDockPanel.getElement().getOffsetHeight();

                //FIXME : dockPanel unit is EM, but there is no (?) simple way to retrieve size in this unit, 
                //BAD :   so use hard coded values instead (they are not likely to change)....
                prevMessageBarSize = 2;
                prevStatusBarSize = 2;
                detailsDocSplitPanel.setWidgetSize(detailsPanel, 0);
                docTableSplitPanel.setWidgetSize(bottomDockPanel, 0);
                dockPanel.setWidgetSize(messageBar, 0);
                dockPanel.setWidgetSize(statusBar, 0);
            } else {
                detailsDocSplitPanel.setWidgetSize(detailsPanel, prevDetailsPanelSize);
                docTableSplitPanel.setWidgetSize(bottomDockPanel, prevBottomDockPanelSize);
                dockPanel.setWidgetSize(messageBar, prevMessageBarSize);
                dockPanel.setWidgetSize(statusBar, prevStatusBarSize);
            }
        }
    }

    @Override
    public void onTermAnnotationsExpositionChanged(TermAnnotationsExpositionEvent event) {
        if (TermAnnotationsExpositionEvent.ChangeType.Available.equals(event.getChangeType())) {
            //put in foreground the Terminology tree widget if the document can reference external term ressources
            detailsTabsPanel.selectTab(structTermUI);
        } else {
            detailsTabsPanel.selectTab(annotationDetailsUI);
        }
    }

    @Override
    public void onApplicationStatusChanged(final ApplicationStatusChangedEvent event) {
        RequestManager requestManager = injector.getCoreDataProvider().getRequestManager();
        if (requestManager != null && event.getRequestManager() != null && event.getRequestManager().equals(requestManager)) {
            Scheduler.get().scheduleFinally(new Command() {

                @Override
                public void execute() {
                    if (event.getStatus().equals(ApplicationStatusChangedEvent.ApplicationStatusSwitching.Idle)) {
                        glassPanel.addStyleName(style.BackGroundPos());
                        glassPanel.removeStyleName(style.ForeGroundPos());
                    } else if (event.getStatus().equals(ApplicationStatusChangedEvent.ApplicationStatusSwitching.Processing)) {
                        glassPanel.addStyleName(style.ForeGroundPos());
                        glassPanel.removeStyleName(style.BackGroundPos());
                    }
                }
            });
        }
    }

    @Override
    protected void onAttach() {
        super.onAttach();
        EventBus eventBus = injector.getMainEventBus();
        InformationReleasedEvent.register(eventBus, informationDisplayer);
        EditHappenedEvent.register(eventBus, this);
        MaximizingWidgetEvent.register(eventBus, this);
        TermAnnotationsExpositionEvent.register(eventBus, this);
        ApplicationStatusChangedEvent.register(eventBus, this);
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        InformationReleasedEvent.unregister(informationDisplayer);
        EditHappenedEvent.unregister(this);
        MaximizingWidgetEvent.unregister(this);
        TermAnnotationsExpositionEvent.unregister(this);
        ApplicationStatusChangedEvent.unregister(this);
    }
}
