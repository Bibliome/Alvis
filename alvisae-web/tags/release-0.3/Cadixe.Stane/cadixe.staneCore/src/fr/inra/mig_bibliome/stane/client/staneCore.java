package fr.inra.mig_bibliome.stane.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import fr.inra.mig_bibliome.stane.client.Config.StaneClientBaseGinInjector;
import fr.inra.mig_bibliome.stane.client.data.ResultMessageDialog;
import fr.inra.mig_bibliome.stane.client.data3.CampaignListImpl;
import fr.inra.mig_bibliome.stane.shared.data3.Campaign;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class staneCore implements EntryPoint {

    //
    public static native void speedTracerlog(String msg) /*-{
    var logger = $wnd.console;
    if(logger && logger.markTimeline) {
    logger.markTimeline(msg); 
    }
    }-*/;
    
    
    private static final StaneClientBaseGinInjector injector = GWT.create(StaneClientBaseGinInjector.class);

    /**
     * This is the entry point method.
     */
    @Override
    public void onModuleLoad() {
        Element testContainer = Document.get().getElementById("StaneCoreTester");
        if (testContainer != null) {

            GWT.log(StaneCoreResources.INSTANCE.jsonData3Test().getText());
            final Label label = new Label("Hello, GWT!!!");
            final Button button = new Button("nothing");
            RootPanel.get("StaneCoreTester").add(button);
            RootPanel.get("StaneCoreTester").add(label);

            button.addClickHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {

                    injector.getCoreDataProvider().testRequest(new AsyncCallback<CampaignListImpl>() {

                        @Override
                        public void onFailure(Throwable caught) {

                            ResultMessageDialog resultMessageDialog = new ResultMessageDialog(1, "Exception", caught.getLocalizedMessage());
                            resultMessageDialog.center();
                            resultMessageDialog.show();
                        }

                        @Override
                        public void onSuccess(CampaignListImpl result) {

                            String msg = "";
                            for (int i = 0, n = result.length(); i < n; ++i) {
                                Campaign c = result.get(i);
                                msg += c.getId() + " - " + c.getDisplayName() + "\n";

                            }

                            ResultMessageDialog resultMessageDialog = new ResultMessageDialog(1, "Nb campaign " + result.length(), msg);
                            resultMessageDialog.center();
                            resultMessageDialog.show();
                        }
                    });
                }
            });

        }
    }
}
