/*
 *
 *      This software is a result of Quaero project and its use must respect the rules of the Quaero Project Consortium Agreement.
 *
 *      Copyright Institut National de la Recherche Agronomique, 2010.
 *
 */
package fr.inra.mig_bibliome.stane.client.Annotation;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Widget;
import java.util.ArrayList;

/**
 *
 * @author fpapazian
 */
public class AnnotationPropertiesUi extends Composite  {

    interface AnnotationPropertiesUiBinder extends UiBinder<Widget, AnnotationPropertiesUi> {
    }
    private static AnnotationPropertiesUiBinder uiBinder = GWT.create(AnnotationPropertiesUiBinder.class);

    interface Styles extends CssResource {
        String Outlined();
    }
    @UiField
    Grid metadataGrid;
    @UiField
    Styles style;


    public AnnotationPropertiesUi() {
        initWidget(uiBinder.createAndBindUi(this));

        metadataGrid.setBorderWidth(1);
        metadataGrid.resize(1, 2);
    }

    public void displayAnnotationDetails(ArrayList<String> annotations) {

    }
}
