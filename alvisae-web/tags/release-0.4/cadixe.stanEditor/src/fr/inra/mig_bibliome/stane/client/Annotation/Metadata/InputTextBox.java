/*
 *
 *      This software is a result of Quaero project and its use must respect the rules of the Quaero Project Consortium Agreement.
 *
 *      Copyright Institut National de la Recherche Agronomique, 2010-2011.
 *
 */
package fr.inra.mig_bibliome.stane.client.Annotation.Metadata;

import com.google.gwt.user.client.ui.TextBox;

/**
 *
 * @author fpapazian
 */
public class InputTextBox extends TextBox implements PropertyInputWidget{

    @Override
    public boolean isEditable() {
        return true;
    }


}
