/*
 *
 *      This software is a result of Quaero project and its use must respect the rules of the Quaero Project Consortium Agreement.
 *
 *      Copyright Institut National de la Recherche Agronomique, 2010-2012.
 *
 */
package fr.inra.mig_bibliome.stane.client.Document;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.RootPanel;
import fr.inra.mig_bibliome.stane.client.Config.GlobalStyles;
import fr.inra.mig_bibliome.stane.client.Document.DocumentView.Options;
import fr.inra.mig_bibliome.stane.client.StaneCoreResources;
import fr.inra.mig_bibliome.stane.client.data3.AnnotatedTextHandler;
import fr.inra.mig_bibliome.stane.client.data3.AnnotatedTextImpl;
import fr.inra.mig_bibliome.stane.shared.data3.AnnotatedText;

/**
 *
 * @author fpapazian
 */
public class DocumentUIWrapper {

    //export la methode static dans une variable globale accessible depuis Javascript
    public static native void exportMethods() /*-{
    $wnd._Stane_createDocumentUI =
    $entry(@fr.inra.mig_bibliome.stane.client.Document.DocumentUIWrapper::createDocumentUI(Ljava/lang/String;));
    $wnd._Stane_loadDocument =
    $entry(@fr.inra.mig_bibliome.stane.client.Document.DocumentUIWrapper::loadDocument(Lfr/inra/mig_bibliome/stane/client/Document/DocumentUi;));
    $wnd._Stane_setDocument =
    $entry(@fr.inra.mig_bibliome.stane.client.Document.DocumentUIWrapper::setDocument(Lfr/inra/mig_bibliome/stane/client/Document/DocumentUi;Ljava/lang/String;Ljava/lang/String;));
    $wnd._Stane_getDocument =
    $entry(@fr.inra.mig_bibliome.stane.client.Document.DocumentUIWrapper::getDocument(Lfr/inra/mig_bibliome/stane/client/Document/DocumentUi;));
    }-*/;

    private static void prepareDocumentUI() {
        Element e = Document.get().getBody();
        Element cssElement = GlobalStyles.getInlinedStyleElement();
        cssElement.setId("aae_GlobalDynamicStyles.Block");
        Element oldCssElement = Document.get().getElementById(cssElement.getId());
        if (oldCssElement != null) {
            oldCssElement.removeFromParent();
        }
        e.insertFirst(cssElement);
    }

    //
    public static DocumentUi createDocumentUI(String parentNodeId) {
        Element e = Document.get().getElementById(parentNodeId);
        if (e != null && e.hasTagName("DIV")) {
            prepareDocumentUI();

            DocumentUi docUI = new DocumentUi();
            RootPanel.get(parentNodeId).add(docUI);
            return docUI;
        } else {
            throw new IllegalArgumentException("Unable to instanciate DocumentUI: DIV '" + parentNodeId + "' not found!");
        }
    }

    @Deprecated
    public static void loadDocument(DocumentUi docUI) {
        String json = StaneCoreResources.INSTANCE.jsonData3Test().getText();
        AnnotatedTextImpl newDoc = AnnotatedTextImpl.createFromJSON(json);
        //create Modification Handler
        AnnotatedTextHandler hnd = AnnotatedTextHandler.createHandler(0, 0, newDoc);
        docUI.setDocument(hnd, false);
    }

    public static boolean setDocument(DocumentUi docUI, String jsonStr, String optionStr) {
        boolean result = false;
        try {
            Options options = new DocumentUi.Options(optionStr);
            docUI.setDocument(jsonStr, options);
            result = true;
        } catch (Exception e) {
            //error while parsing options
        }
        return result;
    }

    public static String getDocument(DocumentUi docUI) {
        AnnotatedText doc = docUI.getDocument();
        return doc.getJSON();
    }
}
