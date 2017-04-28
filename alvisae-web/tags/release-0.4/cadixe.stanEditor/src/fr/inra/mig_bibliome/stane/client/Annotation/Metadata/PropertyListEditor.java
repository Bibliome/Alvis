/*
 *
 *      This software is a result of Quaero project and its use must respect the rules of the Quaero Project Consortium Agreement.
 *
 *      Copyright Institut National de la Recherche Agronomique, 2010-2011.
 *
 */
package fr.inra.mig_bibliome.stane.client.Annotation.Metadata;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTMLTable.Cell;
import com.google.gwt.user.client.ui.HTMLTable.RowFormatter;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import fr.inra.mig_bibliome.stane.client.Config.GlobalStyles;
import fr.inra.mig_bibliome.stane.client.Config.StaneClientBaseGinInjector;
import fr.inra.mig_bibliome.stane.client.Edit.AnnotationMultipleValuesPropertyEdit;
import fr.inra.mig_bibliome.stane.client.data3.AnnotatedTextHandler;
import fr.inra.mig_bibliome.stane.shared.data3.Annotation;
import fr.inra.mig_bibliome.stane.shared.data3.Properties;
import fr.inra.mig_bibliome.stane.shared.data3.validation.PropertyDefinition;
import fr.inra.mig_bibliome.stane.shared.data3.validation.PropertyType;
import java.util.ArrayList;
import java.util.List;

/**
 * Displays in a modal dialog the list of the values of a specific property and allows to edit them.
 * @author fpapazian
 */
public class PropertyListEditor extends PopupPanel {

    interface PropertyEditorUiBinder extends UiBinder<Widget, PropertyListEditor> {
    }
    private static PropertyEditorUiBinder uiBinder = GWT.create(PropertyEditorUiBinder.class);
    //
    private static final StaneClientBaseGinInjector injector = GWT.create(StaneClientBaseGinInjector.class);
    //
    @UiField
    ScrollPanel scrollPanel;
    @UiField
    Grid propertyGrid;
    @UiField
    PushButton cancelModifBtn;
    @UiField
    PushButton applyModifBtn;
    @UiField
    PushButton delValueBtn;
    @UiField
    PushButton addValueBtn;
    @UiField
    TableCellElement keyName;
    @UiField
    TableCellElement lineCount;
    @UiField
    Styles style;
    //
    private int selectedRow = -1;
    private Annotation annotation;
    private AnnotatedTextHandler document;
    private Properties props;
    private PropertyDefinition propDef;
    private String key;

    static class ValueRef<T> {

        T value;

        private ValueRef(T value) {
            this.value = value;
        }

        public T getValue() {
            return value;
        }

        public void setValue(T value) {
            this.value = value;
        }
    }
    private ArrayList<ValueRef<String>> model;
    private Command command = null;

    public interface Styles extends CssResource {

        String PropertyTextInput();
    }

    public PropertyListEditor() {
        super(false, true);
        setWidget(uiBinder.createAndBindUi(this));
        //scrollPanel.setAlwaysShowScrollBars(true);

        propertyGrid.resize(0, 1);

    }

    @Override
    protected void onPreviewNativeEvent(NativePreviewEvent preview) {
        super.onPreviewNativeEvent(preview);

        NativeEvent evt = preview.getNativeEvent();
        if (evt.getType().equals("keydown")) {
            if (!evt.getAltKey() && !evt.getCtrlKey() && !evt.getShiftKey() && !evt.getMetaKey()) {
                switch (evt.getKeyCode()) {
                    case KeyCodes.KEY_TAB:
                        if (selectedRow == propertyGrid.getRowCount() - 1) {
                            addValue();
                            evt.preventDefault();
                        }
                        break;
                    case KeyCodes.KEY_ENTER:
                        //move focus to button to trigger the onBlur() on the last edited field in order to save the modifications...
                        applyModifBtn.setFocus(true);
                        applyModif();
                        break;
                    case KeyCodes.KEY_ESCAPE:
                        //Cancel modification
                        cancelModif();
                        break;
                }
            }
        }
    }

    /**
     * The Caller must set the command that should be executed when this dialog closes.
     * @param command that will be executed when the change are applied
     */
    public void setRefreshCommand(Command command) {
        this.command = command;
    }

    /**
     * The Caller must set the property that will be editing via this dialog.
     * @param props the Properties to which the edited property belongs
     * @param key the key of the edited property
     */
    public void setProperty(AnnotatedTextHandler document, Annotation annotation, Properties props, PropertyDefinition propDef) {
        this.document = document;
        this.annotation = annotation;
        this.props = props;
        this.propDef = propDef;
        this.key = propDef.getKey();

        selectedRow = -1;
        keyName.setInnerText(key);

        model = new ArrayList<ValueRef<String>>();
        if (props.hasKey(key)) {
            for (String v : props.getValues(key)) {
                model.add(new ValueRef<String>(v));
            }
        }
        applyModifBtn.setFocus(true);
        populateGrid();
    }

    private void populateGrid() {
        int size = model.size();
        propertyGrid.resizeRows(0);
        for (int row = 0; row < size; row++) {
            appendRowtoGrid(row == 0);
        }
        if (size == 0) {
            lineCount.setInnerText(String.valueOf(propertyGrid.getRowCount()));
        }
    }

    private void appendRowtoGrid(boolean focus) {
        final int newIndex = propertyGrid.getRowCount();

        propertyGrid.resizeRows(newIndex + 1);
        RowFormatter formatter = propertyGrid.getRowFormatter();
        formatter.addStyleName(newIndex, style.PropertyTextInput());

        final PropertyInputWidget tb;
        final Widget w;

        //FIXME duplicated code :
        //determine the correct widget for property edition
        PropertyType valType = propDef.getValuesType();
        if ((valType != null) && valType.getAsClosedDomainType()!=null) {
            InputListBox itb = new InputListBox();
            for (String item : valType.getAsClosedDomainType().getDomainValues()) {
                itb.addItem(item);
            }
            tb = itb;
            w = itb;

        } else {
            InputTextBox itb = new InputTextBox();
            tb = itb;
            w = itb;
        }


        final ValueRef<String> value = model.get(newIndex);
        tb.setText(value.getValue());
        tb.addStyleName(style.PropertyTextInput());

        //remove selection decoration from the corresponding row when the TextBox loose the focus
        tb.addBlurHandler(new BlurHandler() {

            @Override
            public void onBlur(BlurEvent event) {
                value.setValue(tb.getText());
                unSelectRow(newIndex);
            }
        });

        //selected the corresponding row when the TextBox get the focus
        tb.addFocusHandler(new FocusHandler() {

            @Override
            public void onFocus(FocusEvent event) {
                Element td = tb.getElement().getParentElement();
                int row = TableRowElement.as(td.getParentElement()).getSectionRowIndex();
                selectRow(row);
            }
        });

        propertyGrid.setWidget(newIndex, 0, w);

        if (focus) {
            unSelectSelectRow(newIndex);
            tb.setFocus(true);
        }

        lineCount.setInnerText(String.valueOf(propertyGrid.getRowCount()));
    }

    @UiHandler("cancelModifBtn")
    void handleCancelClick(ClickEvent e) {
        cancelModif();
    }

    @UiHandler("applyModifBtn")
    void handleApplyClick(ClickEvent e) {
        applyModif();
    }

    void cancelModif() {
        hide();
    }

    void applyModif() {
        hide();
        List<String> oldValues = props.getValues(key);
        List<String> newValues = new ArrayList<String>();
        for (ValueRef<String> value : model) {
            String strVal = value.getValue().trim();
            if (!strVal.isEmpty()) {
                newValues.add(strVal);
            }
        }
        boolean modified = oldValues == null || (newValues.size() != oldValues.size());
        if (!modified) {
            for (int i = 0; i < model.size(); i++) {
                if (!newValues.get(i).equals(oldValues.get(i))) {
                    modified = true;
                    break;
                }
            }
        }

        if (modified) {
            AnnotationMultipleValuesPropertyEdit propedit = new AnnotationMultipleValuesPropertyEdit(document, annotation, key, oldValues, newValues);
            propedit.redo();
        }
        if (command != null) {
            command.execute();
        }
    }

    @UiHandler("propertyGrid")
    void handleGridClick(ClickEvent event) {
        Cell cell = propertyGrid.getCellForEvent(event);
        if (cell != null) {
            unSelectSelectRow(cell.getRowIndex());
        }
    }

    private void selectRow(int rowIndex) {
        selectedRow = rowIndex;
        propertyGrid.getRowFormatter().addStyleName(selectedRow, GlobalStyles.SelectedAnnotation);
    }

    private void unSelectRow(int rowIndex) {
        if (selectedRow > -1) {
            propertyGrid.getRowFormatter().removeStyleName(selectedRow, GlobalStyles.SelectedAnnotation);
        }
    }

    private void unSelectSelectRow(int rowIndex) {
        unSelectRow(rowIndex);
        selectRow(rowIndex);
    }

    @UiHandler("addValueBtn")
    void handleAddValueClick(ClickEvent e) {
        addValue();
    }

    void addValue() {
        model.add(new ValueRef<String>(" "));
        appendRowtoGrid(true);
    }

    @UiHandler("delValueBtn")
    void handleDelValueClick(ClickEvent e) {
        if (selectedRow > -1) {
            propertyGrid.removeRow(selectedRow);
            model.remove(selectedRow);
        }

        int count = propertyGrid.getRowCount();
        lineCount.setInnerText(String.valueOf(count));

        if (count > 0) {
            int newIndex = selectedRow - 1;
            if (newIndex < 0) {
                newIndex = 0;
            }
            selectRow(newIndex);
            Widget tb = propertyGrid.getWidget(selectedRow, 0);
            if (tb instanceof TextBox) {
                ((TextBox) tb).setFocus(true);
            }
        } else {
            selectedRow = -1;
        }
    }
}
