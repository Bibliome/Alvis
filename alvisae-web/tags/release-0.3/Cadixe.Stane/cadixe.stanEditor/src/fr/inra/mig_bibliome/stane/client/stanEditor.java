package fr.inra.mig_bibliome.stane.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import fr.inra.mig_bibliome.stane.client.Config.StaneClientBaseGinInjector;
import fr.inra.mig_bibliome.stane.client.Document.DocumentUIWrapper;
import fr.inra.mig_bibliome.stane.client.data3.AnnotatedTextHandler;
import fr.inra.mig_bibliome.stane.client.data3.AnnotatedTextImpl;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class stanEditor implements EntryPoint {

    /**
     * This is the entry point method.
     */
    @Override
    public void onModuleLoad() {
        DocumentUIWrapper.exportMethods();

        Element testContainer = Document.get().getElementById("StanEditorTester");
        if (testContainer != null) {
            StaneClientBaseGinInjector injector = GWT.create(StaneClientBaseGinInjector.class);
            injector.getCoreDataProvider().getRequestManager().setAutoSignedIn();
            Tester testerview = new Tester();
            
            //
            String json = StaneCoreResources.INSTANCE.jsonData3Test().getText();
            AnnotatedTextImpl newDoc = AnnotatedTextImpl.createFromJSON(json);
            
            //create Modification Handler
            AnnotatedTextHandler handler = null;
            handler = AnnotatedTextHandler.createHandler(0, 0, newDoc);
            
            testerview.getDocumentView().setDocument(handler, false);

            //RootPanel.get("StanEditorTester").add(testerview);
            RootLayoutPanel.get().add(testerview);

        }
    }
}
