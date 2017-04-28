/*
 *
 *      This software is a result of Quaero project and its use must respect the rules of the Quaero Project Consortium Agreement.
 *
 *      Copyright Institut National de la Recherche Agronomique, 2010.
 *
 */
package fr.inra.mig_bibliome.stane.client.Annotation;

import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import fr.inra.mig_bibliome.stane.client.Document.AnnotationDocumentViewMapper;
import fr.inra.mig_bibliome.stane.client.Events.Selection.GenericAnnotationSelection;
import fr.inra.mig_bibliome.stane.client.Events.TextAnnotationSelectionChangedEvent;
import fr.inra.mig_bibliome.stane.client.Events.AnnotationSelectionChangedEventHandler;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.dom.client.HandlesAllMouseEvents;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLTable.Cell;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import fr.inra.mig_bibliome.stane.client.Config.GlobalStyles;
import fr.inra.mig_bibliome.stane.client.Config.ShortCutToActionTypeMapper;
import fr.inra.mig_bibliome.stane.client.Config.StaneClientBaseGinInjector;
import fr.inra.mig_bibliome.stane.client.Document.AnnotationMarkerManager;
import fr.inra.mig_bibliome.stane.client.Document.DocumentView;
import fr.inra.mig_bibliome.stane.client.Events.GenericAnnotationSelectionChangedEvent;
import fr.inra.mig_bibliome.stane.client.Events.WorkingDocumentChangedEvent;
import fr.inra.mig_bibliome.stane.client.Events.WorkingDocumentChangedEventHandler;
import fr.inra.mig_bibliome.stane.client.Edit.AnnotationEdit;
import fr.inra.mig_bibliome.stane.client.Events.EditHappenedEvent;
import fr.inra.mig_bibliome.stane.client.Events.EditHappenedEventHandler;
import fr.inra.mig_bibliome.stane.client.Events.GroupSelectionChangedEvent;
import fr.inra.mig_bibliome.stane.client.Events.GroupSelectionEmptiedEvent;
import fr.inra.mig_bibliome.stane.client.Events.RelationSelectionChangedEvent;
import fr.inra.mig_bibliome.stane.client.Events.RelationSelectionEmptiedEvent;
import fr.inra.mig_bibliome.stane.client.Events.TextAnnotationSelectionEmptiedEvent;
import fr.inra.mig_bibliome.stane.client.Events.Selection.AnnotationSelections;
import fr.inra.mig_bibliome.stane.client.Events.Selection.GroupAnnotationSelection;
import fr.inra.mig_bibliome.stane.client.Events.Selection.RelationAnnotationSelection;
import fr.inra.mig_bibliome.stane.client.Events.Selection.TextAnnotationSelection;
import fr.inra.mig_bibliome.stane.client.data3.AnnotatedTextHandler;
import fr.inra.mig_bibliome.stane.client.data3.AnnotatedTextProcessor;
import fr.inra.mig_bibliome.stane.shared.data3.AnnotatedText;
import fr.inra.mig_bibliome.stane.shared.data3.Annotation;
import fr.inra.mig_bibliome.stane.shared.data3.AnnotationKind;
import fr.inra.mig_bibliome.stane.shared.data3.AnnotationReference;
import fr.inra.mig_bibliome.stane.shared.data3.Fragment;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

/**
 * Table displaying the currently selected document annotations
 * @author fpapazian
 */
@Deprecated
public class AnnotationListUi extends Composite implements WorkingDocumentChangedEventHandler, EditHappenedEventHandler, AnnotationSelectionChangedEventHandler {

    interface AnnotationListUiBinder extends UiBinder<Widget, AnnotationListUi> {
    }
    private static AnnotationListUiBinder uiBinder = GWT.create(AnnotationListUiBinder.class);
    //
    private static final StaneClientBaseGinInjector injector = GWT.create(StaneClientBaseGinInjector.class);
    //    

    interface Styles extends CssResource {
    }
    @UiField
    FocusPanel focusPanel;
    @UiField
    Grid2 annotationsGrid;
    @UiField
    ScrollPanel scrollPanel;
    //
    @UiField
    Styles style;
    private AnnotatedTextHandler lastWorkingDocument = null;
    private ArrayList<String> annotationIds = new ArrayList<String>();
    private AnnotationSelections selectedTextAnnotations = new AnnotationSelections();
    private AnnotationSelections selectedGroupAnnotations = new AnnotationSelections();
    private AnnotationSelections selectedRelationAnnotations = new AnnotationSelections();

    public AnnotationListUi() {
        initWidget(uiBinder.createAndBindUi(this));

        annotationsGrid.resize(1, 5);
        annotationsGrid.setText(0, 0, "Id");
        annotationsGrid.setText(0, 1, "Kind");
        annotationsGrid.setText(0, 2, "Type");
        annotationsGrid.setText(0, 3, "Details");
        annotationsGrid.setText(0, 4, "Visible");
        //Selection handler
        HandlesAllMouseEvents mousehandler = new HandlesAllMouseEvents() {

            private boolean selecting;
            private Integer startAnnotationIndex;

            @Override
            public void onMouseDown(MouseDownEvent event) {
                if (lastWorkingDocument != null) {
                    if (event.getNativeButton() == NativeEvent.BUTTON_LEFT) {
                        Integer column = annotationsGrid.getColumnForEvent(event.getNativeEvent());
                        if (column != null && column != 4) {
                            Integer row = annotationsGrid.getRowForEvent(event.getNativeEvent());
                            if (row != null) {
                                boolean multiSelectKeyDown = (!ShortCutToActionTypeMapper.isMacOs() && event.isControlKeyDown()) || (ShortCutToActionTypeMapper.isMacOs() && event.isMetaKeyDown());
                                startAnnotationIndex = row - 1;
                                updateSelection(row - 1, startAnnotationIndex, selecting, multiSelectKeyDown);
                                selecting = true;
                            }
                        }
                    }
                }
            }

            @Override
            public void onMouseUp(MouseUpEvent event) {
                if (selecting) {
                    startAnnotationIndex = null;
                    selecting = false;
                }
            }

            @Override
            public void onMouseMove(MouseMoveEvent event) {
                if (selecting) {
                    if (lastWorkingDocument != null) {
                        Integer column = annotationsGrid.getColumnForEvent(event.getNativeEvent());
                        if (column != null && column != 4) {
                            Integer row = annotationsGrid.getRowForEvent(event.getNativeEvent());
                            if (row != null) {
                                boolean multiSelectKeyDown = (!ShortCutToActionTypeMapper.isMacOs() && event.isControlKeyDown()) || (ShortCutToActionTypeMapper.isMacOs() && event.isMetaKeyDown());
                                updateSelection(row - 1, startAnnotationIndex, selecting, multiSelectKeyDown);
                            }
                            AnnotationMarkerManager.clearNativeSelection();
                        }
                    }
                }
                if (event.getNativeButton() != NativeEvent.BUTTON_LEFT) {
                    selecting = false;
                }
            }

            @Override
            public void onMouseOut(MouseOutEvent event) {
                startAnnotationIndex = null;
                selecting = false;
            }

            @Override
            public void onMouseOver(MouseOverEvent event) {
            }

            @Override
            public void onMouseWheel(MouseWheelEvent event) {
            }

            private void updateSelection(int annotationIndex, int startAnnotationIndex, boolean continueSelection, boolean multiSelectKeyDown) {
                AnnotationDocumentViewMapper mapper = AnnotationDocumentViewMapper.getMapper(lastWorkingDocument.getAnnotatedText());
                // check that selected row is not the header one
                if (annotationIndex >= 0) {

                    boolean someTextAnnSelected = !selectedTextAnnotations.isEmpty();
                    boolean someGroupSelected = !selectedGroupAnnotations.isEmpty();
                    boolean someRelationSelected = !selectedRelationAnnotations.isEmpty();

                    EventBus eventBus = injector.getMainEventBus();
                    ArrayList<GenericAnnotationSelectionChangedEvent> events = new ArrayList<GenericAnnotationSelectionChangedEvent>();

                    //check that the user is not dragging the selection
                    if (!continueSelection) {
                        Annotation annotation = lastWorkingDocument.getAnnotation(annotationIds.get(annotationIndex));

                        //clear current selection only if the multiselection key is not pressed
                        if (!multiSelectKeyDown) {
                            selectedTextAnnotations.clear();
                            selectedGroupAnnotations.clear();
                            selectedRelationAnnotations.clear();
                        }

                        //if the multiselection key is pressed, then the selection is in a toggle mode
                        if (multiSelectKeyDown) {
                            if (selectedTextAnnotations.isAnnotationSelected(annotation.getId())) {
                                selectedTextAnnotations.removeAnnotationFromSelection(annotation.getId());
                            } else if (selectedGroupAnnotations.isAnnotationSelected(annotation.getId())) {
                                selectedGroupAnnotations.removeAnnotationFromSelection(annotation.getId());
                            } else if (selectedRelationAnnotations.isAnnotationSelected(annotation.getId())) {
                                selectedRelationAnnotations.removeAnnotationFromSelection(annotation.getId());
                            }
                        }
                        GenericAnnotationSelection newSelection = null;
                        switch (annotation.getAnnotationKind()) {
                            case TEXT:
                                newSelection = new TextAnnotationSelection(annotation, mapper.getMarkerIdsFromAnnotationId(annotation.getId()));
                                selectedTextAnnotations.getSelections().add(newSelection);
                                break;
                            case GROUP:
                                newSelection = new GroupAnnotationSelection(annotation);
                                selectedGroupAnnotations.getSelections().add(newSelection);
                                break;
                            case RELATION:
                                newSelection = new RelationAnnotationSelection(annotation);
                                selectedRelationAnnotations.getSelections().add(newSelection);
                                break;
                        }
                    } else {
                        //dragged selection : select every rows from startRow
                        int from, to;
                        if (startAnnotationIndex > annotationIndex) {
                            from = annotationIndex;
                            to = startAnnotationIndex;
                        } else {
                            from = startAnnotationIndex;
                            to = annotationIndex;
                        }
                        //GWT.log("from:"+ from + ", to:"+ to);
                        selectedTextAnnotations.clear();
                        selectedGroupAnnotations.clear();
                        selectedRelationAnnotations.clear();

                        for (; from <= to; from++) {
                            Annotation annotation = lastWorkingDocument.getAnnotation(annotationIds.get(from));
                            GenericAnnotationSelection newSelection = null;
                            switch (annotation.getAnnotationKind()) {
                                case TEXT:
                                    newSelection = new TextAnnotationSelection(annotation, mapper.getMarkerIdsFromAnnotationId(annotation.getId()));
                                    selectedTextAnnotations.getSelections().add(newSelection);
                                    break;
                                case GROUP:
                                    newSelection = new GroupAnnotationSelection(annotation);
                                    selectedGroupAnnotations.getSelections().add(newSelection);
                                    break;
                                case RELATION:
                                    newSelection = new RelationAnnotationSelection(annotation);
                                    selectedRelationAnnotations.getSelections().add(newSelection);
                                    break;
                            }

                        }

                    }

                    if (selectedTextAnnotations.isEmpty()) {
                        if (someTextAnnSelected) {
                            eventBus.fireEvent(new TextAnnotationSelectionEmptiedEvent(lastWorkingDocument));
                        }
                    } else {
                        TextAnnotationSelectionChangedEvent selectionChangedEvent = new TextAnnotationSelectionChangedEvent(lastWorkingDocument, selectedTextAnnotations);
                        events.add(selectionChangedEvent);
                    }
                    if (selectedGroupAnnotations.isEmpty()) {
                        if (someGroupSelected) {
                            eventBus.fireEvent(new GroupSelectionEmptiedEvent(lastWorkingDocument));
                        }
                    } else {
                        GroupSelectionChangedEvent selectionChangedEvent = new GroupSelectionChangedEvent(lastWorkingDocument, selectedGroupAnnotations);
                        events.add(selectionChangedEvent);
                    }
                    if (selectedRelationAnnotations.isEmpty()) {
                        if (someRelationSelected) {
                            eventBus.fireEvent(new RelationSelectionEmptiedEvent(lastWorkingDocument));
                        }
                    } else {
                        RelationSelectionChangedEvent selectionChangedEvent = new RelationSelectionChangedEvent(lastWorkingDocument, selectedRelationAnnotations);
                        events.add(selectionChangedEvent);
                    }

                    //selection events must be fired after clearing selection events
                    for (GenericAnnotationSelectionChangedEvent e : events) {
                        eventBus.fireEvent(e);
                    }

                }
            }
        };

        focusPanel.addMouseDownHandler(mousehandler);
        focusPanel.addMouseMoveHandler(mousehandler);
        focusPanel.addMouseUpHandler(mousehandler);
        focusPanel.addMouseOutHandler(mousehandler);


        //visibility toggle handler
        annotationsGrid.addClickHandler(
                new ClickHandler() {

                    @Override
                    public void onClick(ClickEvent event) {
                        AnnotationDocumentViewMapper mapper = AnnotationDocumentViewMapper.getMapper(lastWorkingDocument.getAnnotatedText());
                        Cell cell = annotationsGrid.getCellForEvent(event);
                        if (cell != null) {
                            if (cell.getCellIndex() == 4) {
                                int annOrdNum = cell.getRowIndex() - 1;
                                if (annOrdNum >= 0) {
                                    Annotation annotation = lastWorkingDocument.getAnnotation(annotationIds.get(annOrdNum));
                                    boolean veiledStatus = mapper.toggleVeiledStatus(annotation.getId());
                                    annotationsGrid.setText(cell.getRowIndex(), 4, veiledStatus ? "✕" : "✔");
                                }
                            }
                        }
                    }
                });
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    //FIXME not clean : should not be creating  html tags in java code -> UIBinder/CellList
    public static String getAnnotationHtmlRepresentation(Annotation annotation) {
        StringBuilder html = new StringBuilder();
        boolean once = false;

        switch (annotation.getAnnotationKind()) {
            case TEXT:
                AnnotatedText annotatedText = annotation.getAnnotatedText();
                String leTexte = annotatedText.getDocument().getContents();
                for (Fragment f : annotation.getTextBinding().getSortedFragments()) {
                    if (once) {
                        html.append("<span style='background-color:silver;'>&nbsp;</span>");
                    } else {
                        once = true;
                    }
                    html.append("<b>").append(leTexte.substring(f.getStart(), f.getEnd())).append("</b>");
                }
                break;
            case GROUP:
                html.append("<span style='color:grey;'>{</span> ");
                for (AnnotationReference a : annotation.getAnnotationGroup().getComponentRefs()) {
                    if (once) {
                        html.append("<span style='color:grey;'>,</span>");
                    } else {
                        once = true;
                    }
                    String id = a.getAnnotationId();
                    html.append("<span ann:id='").append(id).append("' style='background-color:silver;'>").append(AnnotatedTextProcessor.getBriefId(id)).append("</span>");
                }
                html.append(" <span style='color:grey;'>}</span>");
                break;
            case RELATION:
                for (String role : annotation.getRelation().getRoles()) {
                    if (once) {
                        html.append("<span style='color:grey;'>+</span>");
                    } else {
                        once = true;
                    }
                    String id = annotation.getRelation().getArgumentRef(role).getAnnotationId();
                    html.append("<i>").append(role).append("</i> <span style='color:grey;'>(</span>").append("<span ann:id='").append(id).append("' 'style='background-color:silver;'>").append(AnnotatedTextProcessor.getBriefId(id)).append("</span>").append(" <span style='color:grey;'>)</span>");
                }
                break;
        }
        return html.toString();
    }
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    private void displayAnnotationList(AnnotatedTextHandler workingDocument, DocumentView docView) {
        annotationIds.clear();
        lastWorkingDocument = workingDocument;
        displayAnnotationList();
    }

    private void displayAnnotationList() {

        if (lastWorkingDocument == null) {
            annotationsGrid.resize(1, 5);
        } else {
            annotationIds.clear();

            AnnotationDocumentViewMapper mapper = AnnotationDocumentViewMapper.getMapper(lastWorkingDocument.getAnnotatedText());

            Collection<Annotation> annotations = lastWorkingDocument.getAnnotations();
            annotationsGrid.resize(1 + annotations.size(), 5);
            int row = 0;
            for (Annotation annotation : annotations) {
                if (annotation != null) {
                    row++;
                    annotationIds.add(annotation.getId());
                    String annotationId = annotation.getId();
                    annotationsGrid.setText(row, 4, mapper.isVeiled(annotationId) ? "✕" : "✔");

                    annotationsGrid.setText(row, 0, AnnotatedTextProcessor.getBriefId(annotationId));
                    annotationsGrid.setText(row, 1, annotation.getAnnotationKind().toString());
                    annotationsGrid.setText(row, 2, annotation.getAnnotationType());
                    //PROTOBREAK should be styleName instead of TypeName
                    annotationsGrid.getCellFormatter().setStyleName(row, 2, annotation.getAnnotationType());
                    String html = getAnnotationHtmlRepresentation(annotation);
                    HTML p = new HTML(html);
                    p.addDoubleClickHandler(new DoubleClickHandler() {

                        @Override
                        public void onDoubleClick(DoubleClickEvent event) {
                            NativeEvent ntvEvent = event.getNativeEvent();
                            EventTarget evtTarget = ntvEvent.getEventTarget();
                            Element targetElement = evtTarget.cast();

                            String annotationId = targetElement.getAttribute("ann:id");
                            final GenericAnnotationSelectionChangedEvent selEvent;
                            if (annotationId != null && annotationIds.indexOf(annotationId) >= 0) {
                                Annotation annotation = lastWorkingDocument.getAnnotation(annotationId);

                                if (annotation.getAnnotationKind().equals(AnnotationKind.TEXT)) {
                                    AnnotationDocumentViewMapper mapper = AnnotationDocumentViewMapper.getMapper(lastWorkingDocument.getAnnotatedText());
                                    ArrayList<String> markers = mapper.getMarkerIdsFromAnnotationId(annotation.getId());
                                    if (markers != null && !markers.isEmpty()) {
                                        selEvent = new TextAnnotationSelectionChangedEvent(lastWorkingDocument, annotation, markers, markers.get(0));
                                    } else {
                                        selEvent = null;
                                    }
                                } else if (annotation.getAnnotationKind().equals(AnnotationKind.GROUP)) {
                                    selEvent = new GroupSelectionChangedEvent(lastWorkingDocument, annotation);
                                } else if (annotation.getAnnotationKind().equals(AnnotationKind.RELATION)) {
                                    selEvent = new RelationSelectionChangedEvent(lastWorkingDocument, annotation);
                                } else {
                                    selEvent = null;
                                }

                                if (selEvent != null) {
                                    EventBus eventBus = injector.getMainEventBus();
                                    eventBus.fireEvent(new TextAnnotationSelectionEmptiedEvent(lastWorkingDocument));
                                    eventBus.fireEvent(new GroupSelectionEmptiedEvent(lastWorkingDocument));
                                    eventBus.fireEvent(new RelationSelectionEmptiedEvent(lastWorkingDocument));
                                    eventBus.fireEvent(selEvent);
                                }
                            }
                        }
                    });

                    annotationsGrid.setWidget(row, 3, p);
                }
            }
        }
    }

    private void displaySelectedAnnotation() {
        Scheduler.get().scheduleFinally(new Command() {

            @Override
            public void execute() {

                HashSet<String> selectedIds = new HashSet<String>();
                HashSet<String> mainSelectedId = new HashSet<String>();
                if (!selectedTextAnnotations.isEmpty()) {
                    selectedIds.addAll(selectedTextAnnotations.getSeletedAnnotationIds());
                    mainSelectedId.add(selectedTextAnnotations.getMainSelectedAnnotation().getId());
                }
                if (!selectedGroupAnnotations.isEmpty()) {
                    selectedIds.addAll(selectedGroupAnnotations.getSeletedAnnotationIds());
                    mainSelectedId.add(selectedGroupAnnotations.getMainSelectedAnnotation().getId());
                }
                if (!selectedRelationAnnotations.isEmpty()) {
                    selectedIds.addAll(selectedRelationAnnotations.getSeletedAnnotationIds());
                    mainSelectedId.add(selectedRelationAnnotations.getMainSelectedAnnotation().getId());
                }
                int row = 0;
                for (String annotationId : annotationIds) {
                    row++;
                    if (selectedIds.contains(annotationId)) {
                        if (mainSelectedId.contains(annotationId)) {
                            annotationsGrid.getRowFormatter().removeStyleName(row, GlobalStyles.SelectedRow);
                            annotationsGrid.getRowFormatter().addStyleName(row, GlobalStyles.MainSelectedRow);

                            //Perform scrolling only if element is not showing on the screen.
                            int vTop = scrollPanel.getAbsoluteTop();
                            int vBottom = scrollPanel.getAbsoluteTop() + scrollPanel.getVerticalScrollPosition();
                            Widget w = annotationsGrid.getWidget(row, 3);
                            int wTop = w.getAbsoluteTop();
                            int wBottom = w.getAbsoluteTop() + w.getOffsetHeight();
                            if ((wBottom > vBottom) || (wTop < vTop)) {
                                scrollPanel.ensureVisible(w);
                            }

                        } else {
                            annotationsGrid.getRowFormatter().removeStyleName(row, GlobalStyles.MainSelectedRow);
                            annotationsGrid.getRowFormatter().addStyleName(row, GlobalStyles.SelectedRow);
                        }
                    } else {
                        annotationsGrid.getRowFormatter().removeStyleName(row, GlobalStyles.MainSelectedRow);
                        annotationsGrid.getRowFormatter().removeStyleName(row, GlobalStyles.SelectedRow);
                    }
                }
            }
        });
    }

    @Override
    public void onWorkingDocumentChanged(WorkingDocumentChangedEvent event) {
        displayAnnotationList(event.getWorkingDocument(), event.getDocView());
    }

    @Override
    public void onAnnotationSelectionChanged(GenericAnnotationSelectionChangedEvent event) {

        if (event instanceof TextAnnotationSelectionChangedEvent) {
            if (event instanceof TextAnnotationSelectionEmptiedEvent) {
                selectedTextAnnotations.clear();
            } else {
                selectedTextAnnotations.replaceSelection(event.getAnnotationSelection());
            }
        } else if (event instanceof GroupSelectionChangedEvent) {
            if (event instanceof GroupSelectionEmptiedEvent) {
                selectedGroupAnnotations.clear();
            } else {
                selectedGroupAnnotations.replaceSelection(event.getAnnotationSelection());
            }
        } else if (event instanceof RelationSelectionChangedEvent) {
            if (event instanceof RelationSelectionEmptiedEvent) {
                selectedRelationAnnotations.clear();
            } else {
                selectedRelationAnnotations.replaceSelection(event.getAnnotationSelection());
            }
        }
        displaySelectedAnnotation();
    }

    @Override
    public void onEditHappened(EditHappenedEvent event) {
        if (event.getEdit() instanceof AnnotationEdit) {
            if (((AnnotationEdit) event.getEdit()).getAnnotatedTextHandler().getAnnotatedText().equals(lastWorkingDocument.getAnnotatedText())) {
                displayAnnotationList();
            }
        }
    }

    @Override
    protected void onAttach() {
        super.onAttach();
        EventBus eventBus = injector.getMainEventBus();
        WorkingDocumentChangedEvent.register(eventBus, this);
        EditHappenedEvent.register(eventBus, this);
        TextAnnotationSelectionChangedEvent.register(eventBus, this);
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        WorkingDocumentChangedEvent.unregister(this);
        TextAnnotationSelectionChangedEvent.unregister(this);
        EditHappenedEvent.unregister(this);
    }
}
