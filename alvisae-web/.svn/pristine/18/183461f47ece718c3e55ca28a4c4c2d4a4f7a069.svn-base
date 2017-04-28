/*
 *
 *      This software is a result of Quaero project and its use must respect the rules of the Quaero Project Consortium Agreement.
 *
 *      Copyright Institut National de la Recherche Agronomique, 2010-2012.
 *
 */
package fr.inra.mig_bibliome.stane.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import fr.inra.mig_bibliome.stane.client.Config.GlobalStyles;
import fr.inra.mig_bibliome.stane.client.Config.StaneClientBaseGinInjector;
import fr.inra.mig_bibliome.stane.client.Document.Blinker;
import fr.inra.mig_bibliome.stane.client.Document.DocumentUi;
import fr.inra.mig_bibliome.stane.client.Document.DocumentView;
import fr.inra.mig_bibliome.stane.client.data.Retrieve.NetworkActivityDisplayer;

/**
 * Main UI of the Annotation Editor, containing the DocumentView and several auxiliary views
 * @author fpapazian
 */
public class Tester extends Composite {
    private static final StaneClientBaseGinInjector injector = GWT.create(StaneClientBaseGinInjector.class);

    interface TesterUiBinder extends UiBinder<DockLayoutPanel, Tester> {
    }
    private static TesterUiBinder uiBinder = GWT.create(TesterUiBinder.class);
    @UiField
    DockLayoutPanel dockPanel;
    @UiField
    PushButton refreshFileListButton;
    @UiField
    ListBox filePathList;
    @UiField
    Image errorImage;
    @UiField
    Label statusLabel;
    @UiField
    DocumentUi documentUI;
    @UiField
    PushButton exportButton;
    @UiField
    PushButton svgButton;
    @UiField
    NetworkActivityDisplayer networkActivityDisplayer;
    
    
    
    interface Styles extends CssResource {
    }

    public Tester() {
        
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

        filePathList.addChangeHandler(new ChangeHandler() {

            @Override
            public void onChange(ChangeEvent event) {
                String f = filePathList.getItemText(filePathList.getSelectedIndex());

                if (f.isEmpty()) {
                    documentUI.setDocument((String)null, new DocumentUi.Options(true, true, true));
                } else {
                    getFile(Tester.this, "LOAD", f);
                }
            }
        });
        filePathList.setEnabled(false);
    }

    @UiHandler("refreshFileListButton")
    void handleRefreshFileListButtonClick(ClickEvent e) {
        getFile(this, "LIST", "data/fileList.txt");
    }

    private static native String getFile(Tester theTester, String method, String url) /*-{
    $wnd.jQuery.ajax({
    dataType: 'text',
    cache: false,         
    success: function(string) {             
    $wnd._Stane_fileLoaded(theTester, method, url, string);
    },
    statusCode: {
    404: function() {
    $wnd._Stane_fileLoaded(theTester, method, url, '');
    },
    403: function() {
    $wnd._Stane_fileLoaded(theTester, method, url, '');
    },
    401: function() {
    $wnd._Stane_fileLoaded(theTester, method, url, '');
    }
    },
    url: url
    });
    }-*/;

    static {
        ExportFunction();
    }

    private static native void ExportFunction() /*-{
    $wnd._Stane_fileLoaded =
    $entry(@fr.inra.mig_bibliome.stane.client.Tester::fileLoaded(Lfr/inra/mig_bibliome/stane/client/Tester;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;));
    
    }-*/;

    public static void fileLoaded(Tester theTester, String method, String url, String content) {
        theTester.fileLoaded(method, url, content);
    }

    private void fileLoaded(String method, String url, String content) {
        if (content == null || content.trim().isEmpty()) {
            errorImage.setVisible(true);
            new Blinker(errorImage, new int[]{120, 40, 100, 40, 100, 1200, 10}).start();
        } else {
            if ("LIST".equals(method)) {
                filePathList.clear();
                filePathList.addItem("");
                for (String line : content.split("\n")) {
                    String f = line.trim();
                    if (!f.isEmpty()) {
                        filePathList.addItem(f);
                    }
                }
                filePathList.setEnabled(true);
            } else if ("LOAD".equals(method)) {
                documentUI.setDocument(content, new DocumentUi.Options(false, false, false));
            }
        }

    }

    public DocumentView getDocumentView() {
        return documentUI;
    }
    
    @UiHandler("exportButton")
    void handleExportAnnotationButtonClick(ClickEvent e) {
        try {
            exportButton.setEnabled(false);
            //String export = injector.getDataProvider().getMapper(documentUI).exportAnchorMarkers();
            String export = documentUI.getDocument().getJSON();
            ExportDialog dlg = new ExportDialog(export);
            dlg.show();
            dlg.center();

        } finally {
            exportButton.setEnabled(true);
        }
    }    
    
    @UiHandler("svgButton")
    void handleSVGButtonClick(ClickEvent e) {
            documentUI.setPrintable(true);
    }    
    
}