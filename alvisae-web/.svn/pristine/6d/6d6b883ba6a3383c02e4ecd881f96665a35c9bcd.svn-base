/*
 *
 *      This software is a result of Quaero project and its use must respect the rules of the Quaero Project Consortium Agreement.
 *
 *      Copyright Institut National de la Recherche Agronomique, 2010-2012.
 *
 */
package fr.inra.mig_bibliome.stane.client.Annotation.Metadata;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import fr.inra.mig_bibliome.stane.client.Edit.AnnotationSingleValuePropertyEdit;
import fr.inra.mig_bibliome.stane.client.data3.AnnotatedTextHandler;
import fr.inra.mig_bibliome.stane.client.data3.validation.BasicAnnotationSchemaValidator;
import fr.inra.mig_bibliome.stane.client.data3.validation.BasicFaultListener;
import fr.inra.mig_bibliome.stane.shared.data3.Annotation;
import fr.inra.mig_bibliome.stane.shared.data3.Properties;
import fr.inra.mig_bibliome.stane.shared.data3.validation.PropertyDefinition;
import fr.inra.mig_bibliome.stane.shared.data3.validation.PropertyType;
import java.util.List;

/**
 * Widget that display properties value(s) within a single field, and allows editing.<br/>
 * If the property have only one value, the editing can be performed inplace, within the filed itself.<br/>
 * Otherwise, the editing can be performed in a List style widget (opened by a expand button)
 * @author fpapazian
 */
public class PropertyDisplayer extends Composite {

    private static BasicAnnotationSchemaValidator validator = new BasicAnnotationSchemaValidator();
    private static BasicFaultListener faultLstnr = new BasicFaultListener();
    private Properties props;
    private String key;
    private boolean inplaceEditing = false;
    private PropertyDefinition propDef;
    private final Annotation annotation;
    private final AnnotatedTextHandler document;

    interface PropertyDisplayerUiBinder extends UiBinder<Widget, PropertyDisplayer> {
    }
    private static PropertyDisplayerUiBinder uiBinder = GWT.create(PropertyDisplayerUiBinder.class);
    @UiField
    TableCellElement inputCell;
    @UiField
    HTML textLabel;
    @UiField
    InputTextBox textBox;
    @UiField
    InputListBox listBox;
    @UiField
    FocusPanel expandBtn;
    @UiField
    Styles style;
    PropertyInputWidget inputWidget;

    public interface Styles extends CssResource {

        String PropertyTextNoValue();

        String PropertyInputInvalid();

        String DetailGridFirstRow();
    }

    public Styles getStyle() {
        return style;
    }

    public PropertyDisplayer(AnnotatedTextHandler document, Annotation annotation) {
        this.document = document;
        this.annotation = annotation;
        initWidget(uiBinder.createAndBindUi(this));
        //switchInlineEditingMode(false);
    }

    private void updatePropertyFromUI() {
        if (inplaceEditing) {
            List<String> values = props.getValues(key);
            int size = props.hasKey(key) ? values.size() : 0;
            boolean modified = true;
            String prevValue = null;
            String strValue = inputWidget.getText().trim();
            if (size == 0) {
                if (!strValue.isEmpty()) {
                    //new value entered
                    prevValue = null;
                } else {
                    //no value before, and no value after edition
                    modified = false;
                }
            } else if (size > 0) {
                // direct edit of the first value only
                if (strValue.isEmpty()) {
                    prevValue = values.get(0);
                    strValue = null;
                } else {
                    if (!values.get(0).equals(strValue)) {
                        prevValue = values.get(0);
                    } else {
                        modified = false;
                    }
                }
            }
            if (modified) {
                AnnotationSingleValuePropertyEdit propedit = new AnnotationSingleValuePropertyEdit(document, annotation, key, 0, prevValue, strValue);
                propedit.redo();
            }
        }
    }

    private void switchInlineEditingMode(boolean editable) {
        int size = props.hasKey(key) ? props.getValues(key).size() : 0;
        if (editable) {
            if (size == 0) {
                inputWidget.setText("");
            } else if (size == 1) {
                String value = props.getValues(key).get(0);
                inputWidget.setText(value);
                textLabel.setText(value);
            } else {
                editable = false;
                inputWidget.setText("");
            }
        } else {

            //renders text depending on the number of values
            if (size == 0) {
                //no value for this property
                //FIXME not I18N
                textLabel.setHTML("(no&nbsp;value)");
                textLabel.addStyleName(style.PropertyTextNoValue());
            } else if (size == 1) {
                //one value
                inputWidget.setText(props.getValues(key).get(0));
                textLabel.setText(props.getValues(key).get(0));
                textLabel.removeStyleName(style.PropertyTextNoValue());
            } else {
                //several values
                textLabel.removeStyleName(style.PropertyTextNoValue());
                StringBuilder s = new StringBuilder();
                s.append("<span class=\"").append(style.PropertyTextNoValue()).append("\">(").append(size).append(")</span>&nbsp;");
                for (String val : props.getValues(key)) {
                    s.append(" ").append(val).append(" <span class=\"").append(style.PropertyTextNoValue()).append("\">|</span>");
                }
                textLabel.setHTML(s.toString());
            }


            //Value validity check
            validator.setAnnotatedText(annotation.getAnnotatedText());
            faultLstnr.reset();
            validator.validateProperty(faultLstnr, propDef, annotation, true);
            if (faultLstnr.getMessages().size() > 0) {
                inputCell.addClassName(style.PropertyInputInvalid());
                inputCell.setTitle(faultLstnr.getMessages().get(0).toString());
            } else {
                inputCell.removeClassName(style.PropertyInputInvalid());
                inputCell.setTitle("");
            }
        }
        textLabel.setVisible(!editable);
        inputWidget.setVisible(editable);
        inputWidget.setFocus(editable);
        inplaceEditing = editable;
    }

    /**
     * The Caller must set the property that will be edited via this widget.
     * @param props the Properties to which the edited property belongs
     * @param key the key of the edited property
     */
    public void setProperty(Properties props, PropertyDefinition propDef) {
        this.props = props;
        this.propDef = propDef;
        key = propDef.getKey();
        PropertyType valType = propDef.getValuesType();
        //deny manual modification of the reference to a TyDI resource
        boolean readOnly = (valType != null && (valType.getAsTyDISemClassRefType()!=null || valType.getAsTyDITermRefType()!=null));
        
        if ((valType != null) && valType.getAsClosedDomainType()!=null) {
            inputWidget = listBox;
            for (String item : valType.getAsClosedDomainType().getDomainValues()) {
                listBox.addItem(item);
            }
        } else {
            inputWidget = textBox;
            textBox.setReadOnly(readOnly);
        }
        inputWidget.addBlurHandler(new BlurHandler() {

            @Override
            public void onBlur(BlurEvent event) {
                updatePropertyFromUI();
                switchInlineEditingMode(false);
            }
        });

        expandBtn.setVisible(!readOnly);
        
        inputWidget.addKeyPressHandler(new KeyPressHandler() {

            @Override
            public void onKeyPress(KeyPressEvent event) {
                NativeEvent evt = event.getNativeEvent();
                if (!evt.getAltKey() && !evt.getCtrlKey() && !evt.getShiftKey() && !evt.getMetaKey()) {
                    switch (evt.getKeyCode()) {
                        case KeyCodes.KEY_ENTER:
                            //Apply changes and leave the field
                            updatePropertyFromUI();
                            switchInlineEditingMode(false);
                            break;
                        case KeyCodes.KEY_ESCAPE:
                            //Cancel the changes and leave the field
                            switchInlineEditingMode(false);
                            break;
                    }
                }
            }
        });

        switchInlineEditingMode(false);
    }

    @UiHandler("textLabel")
    void handleLabelClick(ClickEvent e) {
        //do not switch to editing mode if there is more than one value
        if (!props.hasKey(key) || props.getValues(key).size() <= 1) {
            switchInlineEditingMode(true);
        }
    }

    @UiHandler("expandBtn")
    void handleExpandClick(ClickEvent e) {
        updatePropertyFromUI();
        final PropertyListEditor popup = new PropertyListEditor();
        popup.setProperty(document, annotation, props, propDef);
        popup.setRefreshCommand(new Command() {

            @Override
            public void execute() {
                switchInlineEditingMode(false);
            }
        });
        popup.setPopupPositionAndShow(new PopupPanel.PositionCallback() {

            @Override
            public void setPosition(int offsetWidth, int offsetHeight) {
                popup.setPopupPosition(expandBtn.getAbsoluteLeft(), expandBtn.getAbsoluteTop() + expandBtn.getOffsetHeight());
                popup.setSize("15em", "8.6em");
            }
        });
    }
}
