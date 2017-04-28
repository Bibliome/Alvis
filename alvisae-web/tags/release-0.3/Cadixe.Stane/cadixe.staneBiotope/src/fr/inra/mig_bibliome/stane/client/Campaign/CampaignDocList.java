/*
 *
 *      This software is a result of Quaero project and its use must respect the rules of the Quaero Project Consortium Agreement.
 *
 *      Copyright Institut National de la Recherche Agronomique, 2010.
 *
 */
package fr.inra.mig_bibliome.stane.client.Campaign;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.shared.EventBus;
import fr.inra.mig_bibliome.stane.client.Config.History.BasicUserCampaignDocParams;
import fr.inra.mig_bibliome.stane.client.Config.StaneClientGinInjector;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PushButton;
import fr.inra.mig_bibliome.stane.client.About.AboutStaneDialog;
import fr.inra.mig_bibliome.stane.client.Annotation.Grid2;
import fr.inra.mig_bibliome.stane.client.Config.History.BasicUserCampaignDocOffsetParams;
import fr.inra.mig_bibliome.stane.client.Start.DefaultPlace;
import fr.inra.mig_bibliome.stane.client.Document.DocEditingPlace;
import fr.inra.mig_bibliome.stane.client.Campaign.DocSelectingView.Presenter;
import fr.inra.mig_bibliome.stane.client.Events.ApplicationStatusChangedEvent;
import fr.inra.mig_bibliome.stane.client.data3.CampaignListImpl;
import fr.inra.mig_bibliome.stane.client.data3.DocumentInfoImpl;
import fr.inra.mig_bibliome.stane.client.data3.DocumentInfoListImpl;

/**
 * Main UI of the Annotation Editor, containing the DocumentView and several auxiliary views
 * @author fpapazian
 */
public class CampaignDocList extends Composite implements DocSelectingView {

    interface CampaignDocListUiBinder extends UiBinder<DockLayoutPanel, CampaignDocList> {
    }
    private static CampaignDocListUiBinder uiBinder = GWT.create(CampaignDocListUiBinder.class);
    private static final StaneClientGinInjector injector = GWT.create(StaneClientGinInjector.class);

    interface Styles extends CssResource {

        String SelectedRow();
    }
    //
    @UiField
    PushButton signingBtn;
    @UiField
    Image aboutImage;
    @UiField
    Grid2 campaignGrid;
    @UiField
    Grid2 docGrid;
    @UiField
    PushButton annotateDocButton;
    @UiField
    PushButton refreshButton;
    //
    @UiField
    Styles style;
    //
    private Presenter presenter;
    private CampaignListImpl campaignList;
    private DocumentInfoListImpl docList;
    private Integer selectedCampaignId = null;
    private Integer selectedDocId = null;
    private Integer selectedUserId = null;
    private SingleSelectionClickHandler selectionHndlr = null;

    static class SingleSelectionClickHandler implements ClickHandler {

        public static interface RowSelectedHandler {

            public void rowSelected(int row);

            public void rowUnSelected(int row);
        }
        private final Grid2 grid;
        private final String styleName;
        private final RowSelectedHandler rowSelectedHandler;

        public SingleSelectionClickHandler(Grid2 grid, String styleName, RowSelectedHandler rowSelectedHandler) {
            this.grid = grid;
            this.styleName = styleName;
            this.rowSelectedHandler = rowSelectedHandler;
        }
        private Integer lastSelected = null;

        @Override
        public void onClick(ClickEvent event) {
            Integer row = grid.getRowForEvent(event.getNativeEvent());
            setSelected(row);
        }

        void reset() {
            lastSelected = null;
        }

        private void setSelected(Integer row) {

            if (lastSelected != null) {
                grid.getRowFormatter().removeStyleName(lastSelected, styleName);
            }
            if (row != null) {
                if (row < grid.getRowCount()) {
                    if (row != lastSelected) {
                        grid.getRowFormatter().addStyleName(row, styleName);
                        lastSelected = row;
                        if (rowSelectedHandler != null) {
                            rowSelectedHandler.rowSelected(row);
                        }
                    } else {
                        lastSelected = null;
                        if (rowSelectedHandler != null) {
                            rowSelectedHandler.rowUnSelected(row);
                        }
                    }
                }
            }
        }

        public Integer getSelectedRow() {
            return lastSelected;
        }
    }

    private void refreshCampaignList() {
        refreshCampaignList(null);
    }

    private void refreshCampaignList(final Command executedOnSuccess) {

        campaignGrid.resize(0, 2);
        docGrid.resize(0, 4);
        selectedDocId = null;
        annotateDocButton.setEnabled(false);
        
        signingBtn.setTitle("authenticated as: " + injector.getCoreDataProvider().getRequestManager().getCurrentUserName() );
        
        selectedUserId = injector.getCoreDataProvider().getRequestManager().getCurrentUserId();
        injector.getCoreDataProvider().getCampaignList(selectedUserId, new AsyncCallback<CampaignListImpl>() {

            @Override
            public void onFailure(Throwable caught) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void onSuccess(CampaignListImpl result) {
                campaignList = result;
                campaignGrid.resize(campaignList.length(), 2);
                for (int row = 0; row < campaignList.length(); row++) {
                    campaignGrid.setText(row, 0, String.valueOf(campaignList.get(row).getId()));
                    campaignGrid.setText(row, 1, campaignList.get(row).getDisplayName());
                }
                if (executedOnSuccess != null) {
                    Scheduler.get().scheduleDeferred(executedOnSuccess);
                }
            }
        });

    }

    private void refreshDocumentList(Integer campaignId) {

        if (campaignId == null) {
            docGrid.resize(0, 4);
        } else {
            injector.getCoreDataProvider().getDocumentInfoList(injector.getCoreDataProvider().getRequestManager().getCurrentUserId(), campaignId, new AsyncCallback<DocumentInfoListImpl>() {

                @Override
                public void onFailure(Throwable caught) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public void onSuccess(DocumentInfoListImpl result) {
                    docList = result;
                    docGrid.resize(result.length(), 4);
                    for (int row = 0; row < result.length(); row++) {
                        DocumentInfoImpl docInfo = result.get(row);
                        docGrid.setText(row, 0, String.valueOf(docInfo.getId()));
                        docGrid.setText(row, 1, docInfo.getDescription());
                        docGrid.setText(row, 2, docInfo.getStartedAt());
                        docGrid.setText(row, 3, docInfo.getFinishedAt());
                    }
                }
            });
        }
    }

    public CampaignDocList() {
        initWidget(uiBinder.createAndBindUi(this));

        refreshCampaignList();

        selectionHndlr = new SingleSelectionClickHandler(campaignGrid, style.SelectedRow(), new SingleSelectionClickHandler.RowSelectedHandler() {

            @Override
            public void rowSelected(int row) {
                selectedCampaignId = campaignList.get(row).getId();
                refreshDocumentList(selectedCampaignId);
            }

            @Override
            public void rowUnSelected(int row) {
                selectedCampaignId = null;
                selectedDocId = null;
                annotateDocButton.setEnabled(false);
                
                refreshDocumentList(selectedCampaignId);
            }
        });
        campaignGrid.addClickHandler(selectionHndlr);

        docGrid.addClickHandler(new SingleSelectionClickHandler(docGrid, style.SelectedRow(), new SingleSelectionClickHandler.RowSelectedHandler() {

            @Override
            public void rowSelected(int row) {
                DocumentInfoImpl docInfo = docList.get(row);
                selectedDocId = docInfo.getId();
                annotateDocButton.setEnabled(true);
            }

            @Override
            public void rowUnSelected(int row) {
                annotateDocButton.setEnabled(false);
            }
        }));

        docGrid.addDoubleClickHandler(new DoubleClickHandler() {

            @Override
            public void onDoubleClick(DoubleClickEvent event) {
                handleGetDocClick(null);
            }
        });


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

    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void setParams(BasicUserCampaignDocParams params) {
        if (selectedCampaignId != null) {
            if (params.getCampaignId() != selectedCampaignId || params.getUserId() != selectedUserId) {
                refreshCampaignList();
            } else {
                refreshDocumentList(selectedCampaignId);
            }
        }
    }

    @Override
    public Integer getNextDocumentId(int documentId) {
        Integer result = null;
        for (int i = 0; i < docList.length() - 1; i++) {
            DocumentInfoImpl docInfo = docList.get(i);
            if (docInfo.getId() == documentId) {
                result = docList.get(i + 1).getId();
                break;
            }
        }
        return result;
    }

    @Override
    public Integer getPrevDocumentId(int documentId) {
        Integer result = null;
        for (int i = 1; i < docList.length(); i++) {
            DocumentInfoImpl docInfo = docList.get(i);
            if (docInfo.getId() == documentId) {
                result = docList.get(i - 1).getId();
                break;
            }
        }
        return result;
    }

    @UiHandler("refreshButton")
    void handleRefreshClick(ClickEvent e) {
        refreshCampaignList(new Command() {

            @Override
            public void execute() {
                Integer campaingIndex = null;
                for (int row = 0; row < campaignList.length(); row++) {
                    if (campaignList.get(row).getId() == selectedCampaignId) {
                        campaingIndex = row;
                        break;
                    }
                }
                refreshDocumentList(selectedCampaignId);
                selectionHndlr.reset();
                selectionHndlr.setSelected(campaingIndex);
            }
        });
    }

    @UiHandler("annotateDocButton")
    void handleGetDocClick(ClickEvent e) {
        if (selectedDocId != null) {
            injector.getMainEventBus().fireEvent(new ApplicationStatusChangedEvent(ApplicationStatusChangedEvent.ApplicationStatusSwitching.Processing, null));
            Scheduler.get().scheduleDeferred(new Command() {

                @Override
                public void execute() {
                    presenter.goTo(new DocEditingPlace(new BasicUserCampaignDocOffsetParams(injector.getCoreDataProvider().getRequestManager().getCurrentUserId(), selectedCampaignId, selectedDocId, 0)));
                    injector.getMainEventBus().fireEvent(new ApplicationStatusChangedEvent(ApplicationStatusChangedEvent.ApplicationStatusSwitching.Idle, null));
                }
            });
        }
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

    @Override
    protected void onAttach() {
        super.onAttach();
        EventBus eventBus = injector.getMainEventBus();
    }

    @Override
    protected void onDetach() {
        super.onDetach();
    }
}
