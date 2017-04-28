/*
 *
 *      This software is a result of Quaero project and its use must respect the rules of the Quaero Project Consortium Agreement.
 *
 *      Copyright Institut National de la Recherche Agronomique, 2010-2011.
 *
 */
package fr.inra.mig_bibliome.stane.client.Annotation.Metadata;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.shared.HandlerRegistration;

public interface PropertyInputWidget {

    public boolean isEditable();
    
    public HandlerRegistration addBlurHandler(BlurHandler handler);

    public HandlerRegistration addKeyPressHandler(KeyPressHandler handler);

    public HandlerRegistration addFocusHandler(FocusHandler focusHandler);

    public String getText();

    public void setText(String text);

    public void setVisible(boolean editable);

    public void setFocus(boolean editable);

    public void addStyleName(String PropertyTextInput);

    public Element getElement();

}
