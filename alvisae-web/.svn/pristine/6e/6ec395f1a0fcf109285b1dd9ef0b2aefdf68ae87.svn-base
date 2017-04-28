/*
 *
 *      This software is a result of Quaero project and its use must respect the rules of the Quaero Project Consortium Agreement.
 *
 *      Copyright Institut National de la Recherche Agronomique, 2010-2011.
 *
 */
package fr.inra.mig_bibliome.stane.client.Annotation.Metadata;

import com.google.gwt.user.client.ui.ListBox;

/**
 *
 * @author fpapazian
 */
public class InputListBox extends ListBox implements PropertyInputWidget {

    @Override
    public String getText() {
        int i = getSelectedIndex();
        return getValue(i);
    }

    @Override
    public boolean isEditable() {
        return false;
    }

    @Override
    public void setText(String text) {
        for (int i = 0; i < getItemCount(); i++) {
            if (getItemText(i).equals(text)) {
                setSelectedIndex(i);
                break;
            }
        }
    }
}
