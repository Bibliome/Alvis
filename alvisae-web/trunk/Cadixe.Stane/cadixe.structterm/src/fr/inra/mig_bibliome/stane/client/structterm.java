/*
 *
 *      This software is a result of Quaero project and its use must respect the rules of the Quaero Project Consortium Agreement.
 *
 *      Copyright Institut National de la Recherche Agronomique, 2010-2012.
 *
 */
package fr.inra.mig_bibliome.stane.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import fr.inra.mig_bibliome.stane.client.Config.StaneClientBaseGinInjector;
import fr.inra.mig_bibliome.stane.client.Events.Extension.TermAnnotationsExpositionEvent;
import fr.inra.mig_bibliome.stane.shared.data3.Extension.TermAnnotation;
import fr.inra.mig_bibliome.stane.shared.data3.Extension.TermAnnotation.ResourceLocator;

/**
 * Entry point classes define
 * <code>onModuleLoad()</code>.
 */
public class structterm implements EntryPoint {

    //
    private static final StaneClientBaseGinInjector injector = GWT.create(StaneClientBaseGinInjector.class);

    /**
     * This is the entry point method.
     */
    @Override
    public void onModuleLoad() {

        Element testContainer = Document.get().getElementById("TerminologyShapingWidgetTester");
        //testContainer is a placeholder for 
        if (testContainer != null) {

            GWT.log("Creating widget");
            TesterStructTerm appWidget = new TesterStructTerm();
            RootLayoutPanel.get().add(appWidget);

            //
            GWT.log("Preparing locator");
            ResourceLocator locator = new TermAnnotation.ResourceLocator("http://127.0.0.1:8080/projects/10360305/");
            //ResourceLocator locator = new TermAnnotation.ResourceLocator("http://bibliome.jouy.inra.fr/test/tydiws/rest/pharmaco/projects/6/");
            //ResourceLocator locator = new TermAnnotation.ResourceLocator("http://cl30.dbcls.jp:9080/tydiws/rest/training/projects/10360305/");
            //ResourceLocator locator = new TermAnnotation.ResourceLocator("http://bibliome.jouy.inra.fr/test/tydiws/rest/ontobiodemo/projects/8725573/");
            //ResourceLocator locator = new TermAnnotation.ResourceLocator("http://bibliome.jouy.inra.fr/test/tydiws/rest/training/projects/10360305/");
            GWT.log("locator " + locator.getProjectId() + " " + locator.getResourceUrl());

            injector.getMainEventBus().fireEvent(new TermAnnotationsExpositionEvent(TermAnnotationsExpositionEvent.ChangeType.Available, locator));
        }
    }
}
