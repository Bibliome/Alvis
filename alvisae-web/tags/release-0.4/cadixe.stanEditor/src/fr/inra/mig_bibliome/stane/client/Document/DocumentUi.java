/*
 *
 *      This software is a result of Quaero project and its use must respect the rules of the Quaero Project Consortium Agreement.
 *
 *      Copyright Institut National de la Recherche Agronomique, 2010-2012.
 *
 */
package fr.inra.mig_bibliome.stane.client.Document;

import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.PopupPanel.PositionCallback;
import com.google.gwt.user.client.ui.*;
import fr.inra.mig_bibliome.stane.client.Annotation.EditGroupDialog;
import fr.inra.mig_bibliome.stane.client.Annotation.EditRelationDialog;
import fr.inra.mig_bibliome.stane.client.Annotation.ExplainSchemaPanel;
import fr.inra.mig_bibliome.stane.client.Config.GlobalStyles;
import fr.inra.mig_bibliome.stane.client.Config.ShortCutToActionTypeMapper;
import fr.inra.mig_bibliome.stane.client.Config.ShortCutToActionTypeMapper.ShortCutTriggeredActionType;
import fr.inra.mig_bibliome.stane.client.Config.StaneClientBaseGinInjector;
import fr.inra.mig_bibliome.stane.client.Document.AnnotationReificator.DroppingAnnotationCallback;
import fr.inra.mig_bibliome.stane.client.Document.CombinedAnnotationDisplayer.CombinedAnnotationWidget;
import fr.inra.mig_bibliome.stane.client.Document.DocumentUi.Styles;
import fr.inra.mig_bibliome.stane.client.Edit.*;
import fr.inra.mig_bibliome.stane.client.Edit.undo.CannotRedoException;
import fr.inra.mig_bibliome.stane.client.Edit.undo.CannotUndoException;
import fr.inra.mig_bibliome.stane.client.Edit.undo.UndoManager;
import fr.inra.mig_bibliome.stane.client.Events.Selection.AnnotationSelections;
import fr.inra.mig_bibliome.stane.client.Events.Selection.GenericAnnotationSelection;
import fr.inra.mig_bibliome.stane.client.Events.Selection.TextAnnotationSelection;
import fr.inra.mig_bibliome.stane.client.Events.*;
import fr.inra.mig_bibliome.stane.client.ExportDialog;
import fr.inra.mig_bibliome.stane.client.StanEditorResources;
import fr.inra.mig_bibliome.stane.client.data.ResultMessageDialog;
import fr.inra.mig_bibliome.stane.client.data3.AnnotatedTextHandler;
import fr.inra.mig_bibliome.stane.client.data3.AnnotatedTextImpl;
import fr.inra.mig_bibliome.stane.client.data3.AnnotatedTextProcessor;
import fr.inra.mig_bibliome.stane.client.data3.DOMRange;
import fr.inra.mig_bibliome.stane.client.data3.validation.BasicAnnotationSchemaValidator;
import fr.inra.mig_bibliome.stane.client.data3.validation.BasicFaultListener;
import fr.inra.mig_bibliome.stane.client.staneCore;
import fr.inra.mig_bibliome.stane.shared.data3.*;
import fr.inra.mig_bibliome.stane.shared.data3.Properties;
import java.util.*;
import java.util.logging.Logger;
import org.vaadin.gwtgraphics.client.DrawingArea;
import org.vaadin.gwtgraphics.client.Group;
import org.vaadin.gwtgraphics.client.shape.Path;

/**
 * UI that displays a formatted text with its associated Annotations
 *
 * @author fpapazian
 */
public class DocumentUi extends ResizeComposite implements DocumentView, AnnotationSelectionChangedEventHandler, EditHappenedEventHandler, AnnotationStatusChangedEventHandler, AnnotationFocusChangedEventHandler, WorkingDocumentChangedEventHandler, MaximizingWidgetEventHandler {

    interface DocumentUiBinder extends UiBinder<Widget, DocumentUi> {
    }
    private static DocumentUiBinder uiBinder = GWT.create(DocumentUiBinder.class);
    // -------------------------------------------------------------------------
    private static MessagesToUserConstants messages = (MessagesToUserConstants) GWT.create(MessagesToUserConstants.class);
    private static final StaneClientBaseGinInjector injector = GWT.create(StaneClientBaseGinInjector.class);
    private static final Logger log = Logger.getLogger(DocumentUi.class.getName());

    //
    // -------------------------------------------------------------------------
    public interface Styles extends CssResource {

        String SelectableText();

        String DropTargetAnnot();

        String Processing();

        String BackGroundPos();

        String ForeGroundPos();

        String SelectionMarker();
    }
    // -------------------------------------------------------------------------
    @UiField
    LayoutPanel mainLayoutPanel;
    @UiField
    FocusPanel titleBar;
    @UiField
    Label titleLabel;
    @UiField
    Anchor titleHLink;
    @UiField
    LayoutPanel layoutPanel;
    @UiField
    FlowPanel toolBar;
    @UiField
    LayoutPanel docPanel;
    @UiField
    LayoutPanel docTextPanel;
    @UiField
    Image expandCollapseImg;
    @UiField
    ScrollPanel scrollPanel;
    @UiField
    AbsolutePanel absolutePanel;
    @UiField
    HTML contentHTML;
    @UiField
    RequiresResizeSpy resizeSpy;
    @UiField(provided = true)
    final DrawingArea canvas;
    @UiField
    AbsolutePanel occurencePanel;
    @UiField(provided = true)
    final DrawingArea occurenceBar;
    @UiField
    PushButton incLineSize;
    @UiField
    PushButton decLineSize;
    @UiField
    PushButton undoBtn;
    @UiField
    PushButton redoBtn;
    @UiField
    ToggleButton selectionModeBtn;
    @UiField
    FocusPanel focusPanel;
    @UiField
    SimplePanel sp;
    @UiField
    ListBox annTypeList;
    @UiField
    PushButton addAnnotButton;
    @UiField
    PushButton deleteAnnButton;
    /*
    @UiField
    PushButton editAnnButton;
     */
    @UiField
    PushButton addSelectionButton;
    @UiField
    PushButton delSelectionButton;
    @UiField
    PushButton addGroupButton;
    @UiField
    PushButton deleteGroupButton;
    @UiField
    PushButton editGroupButton;
    @UiField
    PushButton addRelButton;
    @UiField
    PushButton deleteRelButton;
    @UiField
    PushButton editRelButton;
    @UiField
    PushButton explainSchemaBtn;
    @UiField
    PushButton validateButton;
    @UiField
    MenuBar annSetsSelectionMenu;
    @UiField
    MenuItem annSetMenuItem;
    @UiField
    Image errorImage;
    /*
     * @UiField AnnotationTypeDropDownList annTypeList2;
     *
     */
    @UiField
    Styles style;
    //
    private EventBus eventBus;
    //
    private UndoManager undoManager = null;
    private PickupDragController dragController;
    private HandlerRegistration textAnnotationSelectionChangedRegistration;
    private AnnotationSelections selectedTextAnnotations = new AnnotationSelections();
    private AnnotationSelections selectedGroups = new AnnotationSelections();
    private AnnotationSelections selectedRelations = new AnnotationSelections();
    private final GroupDisplayer groupDisplayer;
    private final RelationDisplayer relationDisplayer;
    private final ExtraFeatureDisplayer extraDisplayer;
    private final AnnotationMarkerManager annMarkerMgr;
    private AnnotationDocumentViewMapper mapper;
    private final ButtonEnablementManager buttonManager;
    private int interlineSizeIndex;
    private Options options = new Options();
    private AnnotationReificator annotationReificator = null;
    private final MouseHandler mouseHandler;
    private final ToolBarExpandCollapseHandler toolBarExpandCollapseHandler;
    private final OccurenceDisplayer occurenceDisplayer;
    private boolean maxmimized = false;
    private HandlerRegistration annTypeListRegistration = null;
    private SelectAnnotationPopup selectAnnotationPopup = null;

    /**
     *
     */
    class MouseHandler implements ClickHandler, MouseMoveHandler, MouseUpHandler {

        private Element previoustargetElement = null;
        private Boolean previousShiftKeyStatus = null;
        private ArrayList<String> temporarilyUnveiledRelationIds = new ArrayList<String>();

        //Show relations associated to the specified AnnotationId (e.g. when the text annotation is hovered)
        private void temporarilyUnveilRelation(String annotationId) {
            List<Annotation> references = mapper.getReferencesToAnnotation(annotationId);
            for (Annotation refRelation : references) {
                if (AnnotationKind.RELATION.equals(refRelation.getAnnotationKind())) {
                    String refRelationId = refRelation.getId();
                    if (mapper.isVeiled(refRelationId)) {
                        temporarilyUnveiledRelationIds.add(refRelationId);

                        mapper.setUnveiled(refRelationId);
                        relationDisplayer.refreshVeiledStatus(refRelationId);
                    }
                }
            }
        }

        //Hide back the relations which were temporarily displayed 
        private void veilTemporarilyUnveiledRelation() {
            Iterator<String> iterator = temporarilyUnveiledRelationIds.iterator();
            while (iterator.hasNext()) {
                String refRelationId = iterator.next();
                iterator.remove();
                mapper.setVeiled(refRelationId);
                relationDisplayer.refreshVeiledStatus(refRelationId);
            }
        }

        private void hoveringAnnotation(Element hoveredTargetElement) {
            String markerId = hoveredTargetElement.getId();
            String annotationId = mapper.getAnnotationIdFromMakerId(markerId);

            if (annotationId != null && !mapper.isFormattingAnnotation(annotationId)) {

                //expose relations whose argument is the hovered element
                temporarilyUnveilRelation(annotationId);

                //fire event to inform that annotation selection has changed
                if (selectedTextAnnotations.isEmpty()) {
                    eventBus.fireEvent(new TextAnnotationSelectionEmptiedEvent(getAnnotatedTextHandler()));
                } else {
                    eventBus.fireEvent(new TextAnnotationSelectionChangedEvent(getAnnotatedTextHandler(), selectedTextAnnotations));
                }
            }
        }

        @Override
        public void onClick(ClickEvent event) {
            NativeEvent ntvEvent = event.getNativeEvent();
            EventTarget evtTarget = ntvEvent.getEventTarget();
            Element targetElement = evtTarget.cast();

            String annotationId = null;
            //find the main selected range mark
            targetElement = AnnotatedTextProcessor.getFirstEnclosingMarkerElement(targetElement, annMarkerMgr.getTextContainerId());
            if (targetElement != null) {
                String markerId = targetElement.getId();
                annotationId = getMapper().getAnnotationIdFromMakerId(markerId);

                boolean multiSelectKeyDown = (!ShortCutToActionTypeMapper.isMacOs() && event.isControlKeyDown()) || (ShortCutToActionTypeMapper.isMacOs() && event.isMetaKeyDown());
                boolean removeIfPreviouslySelected = event.isControlKeyDown();

                updateMarkerSelection(targetElement.getId(), annotationId, multiSelectKeyDown, removeIfPreviouslySelected);
            }
        }

        @Override
        public void onMouseUp(MouseUpEvent event) {
            //FIXME : implement "snap to boundary" constraint
            textSelectionChanged(event.getClientX(), event.getClientY());
        }

        @Override
        public void onMouseMove(MouseMoveEvent event) {

            NativeEvent ntvEvent = event.getNativeEvent();
            EventTarget evtTarget = ntvEvent.getEventTarget();
            Element targetElement = evtTarget.cast();
            //to avoid unecessary processing, first check that hovered annotatino is diffrent or shift key status has changed 
            if (!targetElement.equals(previoustargetElement) || previousShiftKeyStatus != ntvEvent.getShiftKey()) {
                previoustargetElement = targetElement;
                previousShiftKeyStatus = ntvEvent.getShiftKey();

                //hide back the relation which were exposed when one of its argument was hovered
                veilTemporarilyUnveiledRelation();

                //find the main selected range mark
                List<Element> ascendantMarkerElts = AnnotatedTextProcessor.getAscendantMarkerElements(targetElement, annMarkerMgr.getTextContainerId());
                int nbAscendantMarker = ascendantMarkerElts.size();

                if (nbAscendantMarker == 1) {
                    //only one single annotation under the mouse pointer
                    Element selectedTargetElement = ascendantMarkerElts.get(0);
                    hoveringAnnotation(selectedTargetElement);


                } else if (nbAscendantMarker > 1 && previousShiftKeyStatus) {
                    //several annotations under the mouse pointer : if Shift key is pressed, then display popup with exposed annotations to allow precise selection

                    if (selectAnnotationPopup == null) {
                        selectAnnotationPopup = new SelectAnnotationPopup(annMarkerMgr.getTextContainerId(), new SelectAnnotationPopup.SelectingAnnotationCallback() {

                            @Override
                            public void annotationSelected(String markerId, String annotationId) {
                                updateMarkerSelection(markerId, annotationId, false, false);
                            }

                            @Override
                            public void annotationSelectionAborted() {
                                if (annotationReificator != null) {
                                    annotationReificator.resetWidget();
                                }
                            }
                        });
                    }

                    final Element selectedTargetElement = ascendantMarkerElts.get(0);
                    selectAnnotationPopup.setSelectableAnnotation(ascendantMarkerElts);
                    if (annotationReificator != null) {
                        annotationReificator.hideWidget();
                    }
                    selectAnnotationPopup.setPopupPositionAndShow(new PositionCallback() {

                        @Override
                        public void setPosition(int offsetWidth, int offsetHeight) {
                            selectAnnotationPopup.setPopupPosition(selectedTargetElement.getAbsoluteLeft() + selectedTargetElement.getOffsetWidth() - offsetWidth / 2, selectedTargetElement.getAbsoluteTop() + selectedTargetElement.getOffsetHeight() - offsetHeight / 2);
                        }
                    });
                }
            }
        }
    }

    public DocumentUi() {
        canvas = new DrawingArea(10, 10);
        occurenceBar = new DrawingArea(10, 10);

        initWidget(uiBinder.createAndBindUi(this));
        canvas.setWidth(absolutePanel.getOffsetWidth());
        canvas.setHeight(absolutePanel.getOffsetHeight());

        eventBus = injector.getMainEventBus();
        groupDisplayer = new GroupDisplayer(this, canvas, eventBus, selectedGroups);
        relationDisplayer = new RelationDisplayer(this, canvas, eventBus, selectedRelations);
        occurenceDisplayer = new OccurenceDisplayer(occurencePanel, occurenceBar, scrollPanel);
        extraDisplayer = new ExtraFeatureDisplayer(canvas, eventBus, occurenceDisplayer);

        annMarkerMgr = new AnnotationMarkerManager();
        contentHTML.getElement().setId(annMarkerMgr.getDocContainerId());
        canvas.getElement().setId(annMarkerMgr.getDocumentCanvasId());
        
        annSetMenuItem.setHTML(SafeHtmlUtils.fromSafeConstant(AbstractImagePrototype.create(StanEditorResources.INSTANCE.AnnotationSetsIcon()).getHTML())); 

        // ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
        incLineSize.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                updateLineSize(+1);
            }
        });

        // ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
        decLineSize.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                updateLineSize(-1);
            }
        });

        // ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
        undoBtn.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                undoEdit();
            }
        });

        // ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
        redoBtn.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                redoEdit();
            }
        });
        // ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
        selectionModeBtn.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

            private boolean relationSelectionMode = false;

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                relationSelectionMode = !relationSelectionMode;
                if (relationSelectionMode) {
                    canvas.removeStyleName(style.BackGroundPos());
                    canvas.addStyleName(style.ForeGroundPos());
                } else {
                    canvas.removeStyleName(style.ForeGroundPos());
                    canvas.addStyleName(style.BackGroundPos());
                }
            }
        });
        // ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~

        // Catch key event for keyboard shortcuts
        focusPanel.addKeyPressHandler(new KeyPressHandler() {

            @Override
            public void onKeyPress(KeyPressEvent event) {
                keypressed(event);
            }
        });

        // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        mouseHandler = new MouseHandler();
        // Triggers event to respond to text selection changes
        contentHTML.addClickHandler(mouseHandler);
        contentHTML.addMouseUpHandler(mouseHandler);
        contentHTML.addMouseMoveHandler(mouseHandler);

        // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        // Repaint the arcs when the Document panel is resized
        resizeSpy.addResizeHandler(new ResizeHandler() {

            @Override
            public void onResize(ResizeEvent event) {
                drawExtra();
            }
        });

        // Repaint the arcs when the Browser Window is resized
        Window.addResizeHandler(new ResizeHandler() {

            @Override
            public void onResize(ResizeEvent event) {
                drawExtra();
            }
        });

        //relocate Drag and Drap widgets after scrolling of the document
        scrollPanel.addScrollHandler(new ScrollHandler() {

            @Override
            public void onScroll(ScrollEvent event) {
                if (annotationReificator != null) {
                    annotationReificator.resetWidgetPositions();
                }
            }
        });
        //
        titleBar.addDoubleClickHandler(new DoubleClickHandler() {

            @Override
            public void onDoubleClick(DoubleClickEvent event) {
                eventBus.fireEvent(new MaximizingWidgetEvent(DocumentUi.this, !maxmimized));
            }
        });
        // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

        buttonManager = new ButtonEnablementManager();

        // ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
        //FIXME compute toolbar top and height at runtime
        toolBarExpandCollapseHandler = new ToolBarExpandCollapseHandler(expandCollapseImg, layoutPanel, toolBar, 0, 25, docPanel);
        expandCollapseImg.addClickHandler(toolBarExpandCollapseHandler);

        //disable Annotation editing since no doc is loaded yet
        setReadOnly(true);

    }
    // ~・~・~・~・~・~・~・~・~・~・~・~・~・~・~・~・~・~・~・~・~・~・~・~・~・~・~ //

    static class OccurenceDisplayer {

        class OccurenceMark {

            int position;
            String color;
            String annotationId;

            public OccurenceMark(int position, String color, String annotationId) {
                this.position = position;
                this.color = color;
                this.annotationId = annotationId;
            }
        }
        private static int VERTICALMARGIN = 2;
        private static int HORIZONTALMARGIN = 2;
        private final DrawingArea canvas;
        private final Widget container;
        private final ScrollPanel scrollPanel;
        private int docPanelSize = 0;
        private int offset;
        private int occurBarHeight;
        private int occurBarWidth;
        private ArrayList<OccurenceMark> pending = new ArrayList<OccurenceMark>();

        public OccurenceDisplayer(Widget container, DrawingArea canvas, ScrollPanel scrollPanel) {
            this.canvas = canvas;
            this.container = container;
            this.scrollPanel = scrollPanel;
        }

        private int getBarPosFromDocPos(int positionInDoc) {
            return offset + (positionInDoc * occurBarHeight) / docPanelSize;
        }

        public void addMarkAtPosition(final int position, String color, String annotationId) {
            pending.add(new OccurenceMark(position, color, annotationId));

        }

        public void displayPendingChanges() {
            for (final OccurenceMark occur : pending) {
                if (docPanelSize > 0) {
                    int relPos = getBarPosFromDocPos(occur.position);
                    Path mark = new Path(HORIZONTALMARGIN, relPos);
                    mark.lineTo(HORIZONTALMARGIN + occurBarWidth / 2, relPos - 1);
                    mark.lineTo(HORIZONTALMARGIN + occurBarWidth, relPos);
                    mark.lineTo(HORIZONTALMARGIN + occurBarWidth / 2, relPos + 1);
                    mark.close();
                    mark.setStrokeWidth(1);
                    mark.setStrokeColor(occur.color);
                    mark.setStrokeOpacity(0.4);
                    mark.setFillOpacity(0.7);
                    mark.setStrokeOpacity(0.4);
                    mark.setFillColor(occur.color);
                    canvas.add(mark);
                    mark.addClickHandler(new ClickHandler() {

                        @Override
                        public void onClick(ClickEvent event) {
                            scrollPanel.setVerticalScrollPosition(occur.position - (scrollPanel.getElement().getClientHeight() / 2));
                        }
                    });

                }
            }
            pending.clear();
        }

        private void reset() {
            reset(0);
        }

        private void reset(int docPanelSize) {
            pending.clear();
            this.docPanelSize = docPanelSize;
            canvas.clear();
            canvas.setWidth(container.getOffsetWidth());
            canvas.setHeight(container.getOffsetHeight());
            this.offset = VERTICALMARGIN;
            this.occurBarHeight = canvas.getHeight() - 2 * VERTICALMARGIN;
            this.occurBarWidth = canvas.getWidth() - 2 * HORIZONTALMARGIN;
        }
    }

    // ~・~・~・~・~・~・~・~・~・~・~・~・~・~・~・~・~・~・~・~・~・~・~・~・~・~・~ //
    private String getCurrentAnnotationType() {
        return annTypeList.getItemText(annTypeList.getSelectedIndex());
    }

    //Annotation-type drop-down list decoration
    private void updatePanelAspect(Integer index) {
        if (index != null) {
            annTypeList.setSelectedIndex(index);
        }
        AnnotationSchemaDefinition schema = getDocument().getAnnotationSchema();

        String inlineStyle = "outline: " + schema.getAnnotationTypeDefinition(getCurrentAnnotationType()).getColor() + " solid 2px;";
        sp.getElement().setAttribute("style", inlineStyle);
    }

    private void upTypeSelection() {
        int currentIndex = annTypeList.getSelectedIndex();
        int newIndex = currentIndex - 1;
        if (newIndex >= 0) {
            updatePanelAspect(newIndex);
        }

    }

    private void downTypeSelection() {
        int currentIndex = annTypeList.getSelectedIndex();
        int newIndex = currentIndex + 1;
        if (newIndex < annTypeList.getItemCount()) {
            updatePanelAspect(newIndex);
        }
    }

    private void prepareAnnotationTypeList() {
        if (annTypeListRegistration != null) {
            annTypeListRegistration.removeHandler();
        }

        AnnotationSchemaDefinition schema = getDocument().getAnnotationSchema();
        for (String annType : schema.getAnnotationTypes()) {
            if (schema.getAnnotationTypeDefinition(annType).getAnnotationKind().equals(AnnotationKind.TEXT)) {
                annTypeList.addItem(annType);
            }
        }

        annTypeListRegistration = annTypeList.addChangeHandler(new ChangeHandler() {

            @Override
            public void onChange(ChangeEvent event) {
                updatePanelAspect(null);
            }
        });

        //change list selection when mousewheel is used
        annTypeList.addMouseWheelHandler(new MouseWheelHandler() {

            @Override
            public void onMouseWheel(MouseWheelEvent event) {
                if (event.isNorth()) {
                    upTypeSelection();
                } else {
                    downTypeSelection();
                }
                event.preventDefault();
            }
        });
        updatePanelAspect(0);
    }

    @UiHandler("addSelectionButton")
    void addSelectionPopupButtonClickHandler(ClickEvent event) {
        ArrayList<DOMRange> ranges = annMarkerMgr.getSelectedRanges();
        extendSelectedAnnotationWithRange(ranges);
    }

    @UiHandler("delSelectionButton")
    void delSelectionPopupButtonClickHandler(ClickEvent event) {
        ArrayList<DOMRange> ranges = annMarkerMgr.getSelectedRanges();
        pruneRangeFromSelectedAnnotation(ranges);
    }
    // ~・~・~・~・~・~・~・~・~・~・~・~・~・~・~・~・~・~・~・~・~・~・~・~・~・~・~ //

    private void undoEdit() {
        if (undoManager != null) {
            String presName = undoManager.getUndoPresentationName();
            try {
                if (undoManager.canUndo()) {
                    undoManager.undo();
                    eventBus.fireEvent(new InformationReleasedEvent(messages.undoing(presName)));
                } else {
                    eventBus.fireEvent(new InformationReleasedEvent(messages.nothingToUndo()));
                }
            } catch (CannotUndoException cannotUndoException) {
                //FIXME : should be displayed in dialogbox
                eventBus.fireEvent(new InformationReleasedEvent(messages.cannotUndo(cannotUndoException.getLocalizedMessage())));
            }
        }
    }

    private void redoEdit() {
        if (undoManager != null) {
            String presName = undoManager.getRedoPresentationName();
            try {
                if (undoManager.canRedo()) {
                    undoManager.redo();
                    eventBus.fireEvent(new InformationReleasedEvent(messages.redoing(presName)));
                } else {
                    eventBus.fireEvent(new InformationReleasedEvent(messages.nothingToRedo()));
                }
            } catch (CannotRedoException cannotRedoException) {
                //FIXME : should be displayed in dialogbox
                eventBus.fireEvent(new InformationReleasedEvent(messages.cannotRedo(cannotRedoException.getLocalizedMessage())));
            }
        }
    }
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    /**
     *
     * @return true is the point specified by its (x,y) coordinates is within this DocumentUi area
     */
    @Override
    public boolean isPointInside(int x, int y) {
        boolean inside = true;
        Element elmt = this.getElement();
        if ((y < elmt.getAbsoluteTop()) || (y > elmt.getAbsoluteBottom())) {
            inside = false;
        } else {
            if ((x < elmt.getAbsoluteLeft()) || (x > elmt.getAbsoluteRight())) {
                inside = false;
            }
        }
        return inside;
    }

    /**
     * Increase or reduce the interline space
     * @param step - the number of logical steps to move from current 
     * position (-1 or 1). 0 to reset to minimum size.
     */
    private void updateLineSize(int step) {
        int prevScrollPos = scrollPanel.getVerticalScrollPosition();
        int prevMaxScrollPos = scrollPanel.getMaximumVerticalScrollPosition();

        contentHTML.removeStyleName(GlobalStyles.getInterlineStyleName(interlineSizeIndex));

        if (step == 0) {
            interlineSizeIndex = 0;
        } else {
            interlineSizeIndex += step;
        }
        if (interlineSizeIndex <= 0) {
            interlineSizeIndex = 0;
        } else if (interlineSizeIndex >= GlobalStyles.getInterlineSizePx().length) {
            interlineSizeIndex = GlobalStyles.getInterlineSizePx().length - 1;
        }

        contentHTML.addStyleName(GlobalStyles.getInterlineStyleName(interlineSizeIndex));

        //reset scrolling in order to show the part of text that was visible before resizing the line
        setScrollPositionAtRatio(prevScrollPos, prevMaxScrollPos);

        buttonManager.updateButtonStatuses();
        drawExtra();

    }

    private boolean canReduceLineSize() {
        return interlineSizeIndex > 0;
    }

    private boolean canIncreaseLineSize() {
        return interlineSizeIndex < GlobalStyles.getInterlineSizePx().length - 1;
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    @Override
    public void onAnnotationSelectionChanged(GenericAnnotationSelectionChangedEvent event) {
        boolean refreshNeeded = false;
        if (event instanceof TextAnnotationSelectionChangedEvent) {
            refreshNeeded = (getDocument() != null) && getDocument().equals(event.getAnnotatedTextHandler().getAnnotatedText());
            if (!refreshNeeded) {
                return;
            }

            hideCurrentlySelectedAnnotations();
            clearCurrentlySelectedAnnotations();
            if (!(event instanceof TextAnnotationSelectionEmptiedEvent)) {
                replaceCurrentlySelectedAnnotations(event.getAnnotationSelection());
                displayCurrentlySelectedAnnotations();
            } else {
                //GWT.log("TextAnnotationSelectionEmptiedEvent ");
            }
        }
        if (event instanceof RelationSelectionChangedEvent) {
            if (event instanceof RelationSelectionEmptiedEvent) {
                selectedRelations.clear();
            } else {
                selectedRelations.replaceSelection(event.getAnnotationSelection());
            }
            relationDisplayer.refresh();
        }
        if (event instanceof GroupSelectionChangedEvent) {
            if (event instanceof GroupSelectionEmptiedEvent) {
                selectedGroups.clear();
            } else {
                selectedGroups.replaceSelection(event.getAnnotationSelection());
            }
            groupDisplayer.refresh();
        }
    }

    /*
     * Enable DnD Relation creation
     */
    private void initDragController(boolean readOnly) {
        if (readOnly) {
            dragController = null;
            //this event handler instance correspond to the Annotation Reificator
            if (textAnnotationSelectionChangedRegistration != null) {
                textAnnotationSelectionChangedRegistration.removeHandler();
                textAnnotationSelectionChangedRegistration = null;
            }
            annotationReificator = null;
        } else {
            //enable DnD support

            // workaround for GWT issue 1813
            // http://code.google.com/p/google-web-toolkit/issues/detail?id=1813
            //RootPanel.get().getElement().getStyle().setProperty("position", "relative");

            if (dragController == null) {
                //by default, it is not allowed to drag a term outside the document view
                AbsolutePanel boundaryPanel = absolutePanel;

                dragController = new PickupDragController(boundaryPanel, false);

                dragController.setBehaviorConstrainedToBoundaryPanel(true);
            }
            if (annotationReificator == null) {

                final DroppingAnnotationCallback droppingAnnotationCallback = new DroppingAnnotationCallback() {

                    @Override
                    public void annotationDropped(final String sourceAnnotationId, String targetAnnotationId, Element targetElement) {


                        List<Element> ascendantMarkerElts = AnnotatedTextProcessor.getAscendantMarkerElements(targetElement, getTextContainerId());
                        int nbAscendantMarker = ascendantMarkerElts.size();
                        Element e;
                        if (nbAscendantMarker > 1) {
                            final SelectAnnotationPopup selectAnnotationPopup = new SelectAnnotationPopup(getTextContainerId(), new SelectAnnotationPopup.SelectingAnnotationCallback() {

                                @Override
                                public void annotationSelected(String markerId, String annotationId) {
                                    ArrayList<String> annotationIds = new ArrayList<String>();
                                    annotationIds.add(sourceAnnotationId);
                                    annotationIds.add(annotationId);
                                    EditRelationDialog.startRelationCreation(getAnnotatedTextHandler(), annotationIds);
                                }

                                @Override
                                public void annotationSelectionAborted() {
                                }
                            });

                            final Element selectedTargetElement = ascendantMarkerElts.get(0);

                            //filter-out marker corresponding to the dragged annotation
                            Iterator<Element> iterator = ascendantMarkerElts.iterator();
                            while (iterator.hasNext()) {
                                e = iterator.next();
                                if (sourceAnnotationId.equals(AnnotatedTextProcessor.getAnnotationIdFromMarker(e))) {
                                    iterator.remove();
                                }
                            }

                            selectAnnotationPopup.setSelectableAnnotation(ascendantMarkerElts);
                            selectAnnotationPopup.setPopupPositionAndShow(new PositionCallback() {

                                @Override
                                public void setPosition(int offsetWidth, int offsetHeight) {
                                    selectAnnotationPopup.setPopupPosition(selectedTargetElement.getAbsoluteLeft() + selectedTargetElement.getOffsetWidth() - offsetWidth / 2, selectedTargetElement.getAbsoluteTop() + selectedTargetElement.getOffsetHeight() - offsetHeight / 2);
                                }
                            });


                        } else if (nbAscendantMarker == 1) {
                            ArrayList<String> annotationIds = new ArrayList<String>();
                            //GWT.log("Direct Relation : " + sourceAnnotationId + " <-> " + targetAnnotationId);

                            annotationIds.add(sourceAnnotationId);
                            annotationIds.add(targetAnnotationId);
                            EditRelationDialog.startRelationCreation(getAnnotatedTextHandler(), annotationIds);
                        }
                    }
                };

                annotationReificator = new AnnotationReificator(this, dragController, droppingAnnotationCallback, style, contentHTML, scrollPanel, absolutePanel);
            }
            textAnnotationSelectionChangedRegistration = TextAnnotationSelectionChangedEvent.register(eventBus, annotationReificator);
        }
    }

    @Override
    public AnnotatedText getDocument() {
        return mapper == null ? null : mapper.getAnnotatedText();
    }

    @Override
    public AnnotatedTextHandler getAnnotatedTextHandler() {
        return mapper == null ? null : mapper.getAnnotatedTextHandler();
    }

    @Override
    public UndoManager getUndoManager() {
        return undoManager;
    }

    /**
     * Display a document specified by string containing the serialized JSON
     * representation
     */
    public void setDocument(String json, Options options) {
        AnnotatedTextHandler handler = null;
        AnnotatedTextImpl newDoc = null;
        try {
            newDoc = AnnotatedTextImpl.createFromJSON(json);
            //create Modification Handler
            handler = AnnotatedTextHandler.createHandler(0, 0, newDoc);
        } catch (Exception e) {
        }
        setDocument(handler, options);
    }

    @Override
    public void setDocument(final AnnotatedTextHandler annotatedDoc, final boolean readOnly) {
        this.options.setReadOnly(readOnly);
        setDocument(annotatedDoc, options);
    }

    @Override
    public void setDocument(final AnnotatedTextHandler annotatedDoc, final Options options) {
        this.options = new Options(options);

        //reset toolbar visibility & collapsed state
        toolBarExpandCollapseHandler.setExpanded(!options.isHiddenToolbar() && !options.isCollapsedToolbar());
        expandCollapseImg.setVisible(!options.isHiddenToolbar());

        //reset TitleBar visibility
        if (options.isHiddenTitlebar()) {
            mainLayoutPanel.setWidgetTopHeight(titleBar, 0, Unit.PX, 0, Unit.PX);
            mainLayoutPanel.setWidgetTopBottom(layoutPanel, 0, Unit.PX, 0, Unit.PX);
        } else {
            mainLayoutPanel.setWidgetTopHeight(titleBar, 0, Unit.PX, 25, Unit.PX);
            mainLayoutPanel.setWidgetTopBottom(layoutPanel, 25, Unit.PX, 0, Unit.PX);
        }

        //reset OccurenceBar visibility
        if (options.isHiddenOccurencebar()) {
            docPanel.setWidgetRightWidth(occurencePanel, 0, Unit.PX, 0, Unit.PX);
            docPanel.setWidgetLeftRight(docTextPanel, 0, Unit.PX, 0, Unit.PX);
        } else {
            docPanel.setWidgetRightWidth(occurencePanel, 0, Unit.PX, 1.2, Unit.EM);
            docPanel.setWidgetLeftRight(docTextPanel, 0, Unit.PX, 1.2, Unit.EM);
        }

        final int interlineIndex = options.getInterlineSizeIndex()!=null ? options.getInterlineSizeIndex() : 3;

        //int title bar with optionnal hyperlink (e.g. link to a pdf file)
        if (annotatedDoc != null) {
            Properties props = (annotatedDoc == null) ? null : annotatedDoc.getAnnotatedText().getDocument().getProperties();
            if (props != null && props.getKeys().contains("hlink")) {
                titleLabel.setVisible(false);
                titleLabel.setText("");
                titleHLink.setVisible(true);
                titleHLink.setHref(props.getValues("hlink").get(0));
                titleHLink.setText(annotatedDoc.getAnnotatedText().getDocument().getDescription());
            } else {
                titleLabel.setVisible(true);
                titleLabel.setText(annotatedDoc.getAnnotatedText().getDocument().getDescription());
                titleHLink.setVisible(false);
                titleHLink.setText("");
            }
        } else {
            titleLabel.setVisible(false);
            titleLabel.setText("");
            titleHLink.setVisible(false);
            titleHLink.setText("");
        }

        //remove any remaining TextAnnotation marker (from previous document if any)
        clearAnchorMarkerSelection();
        contentHTML.addStyleName(style.Processing());

        //inform other components that a long processing is about to start (=>wait banner will be displayed)
        eventBus.fireEvent(new ApplicationStatusChangedEvent(ApplicationStatusChangedEvent.ApplicationStatusSwitching.Processing, null));

        //long processing is splitted into several command to avoid screen freezing
        Scheduler.get().scheduleIncremental(new RepeatingCommand() {

            ArrayList<Command> cmds = new ArrayList<Command>();

            {
                cmds.add(new Command() {

                    @Override
                    public void execute() {
                        buttonManager.setButtonsEnabled(false);
                        //drop undoable edits from previous document 
                        removeUndoManager();
                        //clear any remaining data related to SVG widget 
                        groupDisplayer.reset();
                        relationDisplayer.reset();
                        extraDisplayer.reset();

                        //clear SVG 
                        canvas.clear();
                        canvas.addStyleName(style.Processing());
                        //remove remaining text of the previous document
                        contentHTML.setHTML("");
                        //reset size of the SVG canvas to the size of the document panel
                        canvas.setWidth(absolutePanel.getOffsetWidth());
                        canvas.setHeight(absolutePanel.getOffsetHeight());
                        //canvas.setWidth(contentHTML.getOffsetWidth());
                        //canvas.setHeight(contentHTML.getOffsetHeight());

                        //clear toolbar's TextAnnotation type list from previous document schema
                        annTypeList.clear();
                        //reset AnnotationSet selection menu
                        resetAnnSetSelectionMenu();

                        //reset interline size
                        updateLineSize(0);
                        updateLineSize(interlineIndex);

                        //temporarily switch to readonly during init phase
                        setReadOnly(true);

                        //inform other components that this DocumentUI widget is clear of any document
                        eventBus.fireEvent(new WorkingDocumentChangedEvent(null, DocumentUi.this, WorkingDocumentChangedEvent.ChangeType.Unloaded));
                    }
                });

                if (annotatedDoc != null) {
                    //processings to perform only if a new Document is being displayed
                    cmds.add(new Command() {

                        @Override
                        public void execute() {

                            //create StyleSheet corresponding to the Document Annotation Schema 
                            Element inLinedStyle = Document.get().createStyleElement();
                            inLinedStyle.setInnerText(AnnotatedTextProcessor.getSampleCStyleSheet(annotatedDoc.getAnnotatedText().getAnnotationSchema()));
                            inLinedStyle.setAttribute("type", "text/css");
                            contentHTML.getElement().insertFirst(inLinedStyle);
                            //FIXME casting 
                            mapper = AnnotationDocumentViewMapper.newMapper(annotatedDoc, DocumentUi.this);
                            annMarkerMgr.reset(annotatedDoc);

                            //Veiled all Relations 
                            for (Annotation annotation : getDocument().getAnnotations(AnnotationKind.RELATION)) {
                                mapper.setVeiled(annotation.getId());
                            }

                        }
                    });

                    cmds.add(new Command() {

                        @Override
                        public void execute() {
                            //fill toolbar's TextAnnotation type list with current document annotation schema
                            prepareAnnotationTypeList();
                            //reinitialize AnnotationSet selection menu
                            reinitAnnSetSelectionMenu();
                            //regenerate HTML contening the document text, it's formatting and the TextAnnotation boxes 
                            annMarkerMgr.refreshDocument();
                        }
                    });

                } else {
                    //processing to perform if no Document is being displayed

                    cmds.add(new Command() {

                        @Override
                        public void execute() {
                            //remove remaining text of the previous document
                            contentHTML.setHTML("");
                            annMarkerMgr.reset(null);
                        }
                    });
                }

                cmds.add(new Command() {

                    @Override
                    public void execute() {
                        //set the intended readOnly status
                        DocumentUi.this.setReadOnly(options.isReadOnly());

                        //init agent in charge of displaying Relation, Group & occurences SVG representation 
                        groupDisplayer.setDocument(annotatedDoc, mapper, relationDisplayer);
                        relationDisplayer.setDocument(annotatedDoc, mapper, groupDisplayer);
                        extraDisplayer.setDocument(annotatedDoc, mapper);
                        //actually draw SVG representation 
                        drawExtra();
                        //
                        canvas.removeStyleName(style.Processing());
                        contentHTML.removeStyleName(style.Processing());

                        //inform other components that this DocumentUI widget is now displaying the specified document
                        eventBus.fireEvent(new WorkingDocumentChangedEvent(annotatedDoc, DocumentUi.this, WorkingDocumentChangedEvent.ChangeType.Loaded));
                        //inform other components that application long processing is finished (=>wait banner will be removed)
                        eventBus.fireEvent(new ApplicationStatusChangedEvent(ApplicationStatusChangedEvent.ApplicationStatusSwitching.Idle, null));
                        buttonManager.setButtonsEnabled(true);
                    }
                });

            }

            @Override
            public boolean execute() {
                //actually execute the commands initialized above
                if (cmds.size() > 0) {
                    Command nextCmd = cmds.remove(0);
                    nextCmd.execute();
                }
                return cmds.size() > 0;
            }
        });

    }

    // -------------------------------------------------------------------------
    @Override
    public Rect getAnnotatedTextClip(String annotationId) {
        ArrayList<String> mkrIds = mapper.getMarkerIdsFromAnnotationId(annotationId);
        String markId = mkrIds.get(0);

        Element srcElt = Document.get().getElementById(markId);
        return getAnnotatedTextClip(srcElt);
    }

    @Override
    public Rect getAnnotatedTextClip(Element srcElt) {
        int aLeft = srcElt.getAbsoluteLeft();
        int aRight;
        int aTop;
        int aBottom;

        aTop = srcElt.getAbsoluteTop();
        aBottom = srcElt.getAbsoluteBottom();
        aRight = srcElt.getAbsoluteRight();

        //if the Annotation Box is taller than the line-height, it surely means that the Annotation spans over 2 or more lines
        if ((aBottom - aTop) > GlobalStyles.getInterlineSizePx()[interlineSizeIndex]) {

            //Note : the inlined element used to show Text Annotation can be spanned over several lines.
            //       Thus, the value returned by Element.getAbsoluteLeft() correspond to the much larger 
            //       rectangle enclosing the several boxes created by the browser to display the annotation
            //
            //       A temporary unbreakable element created just before the annotation must have the same 
            //       left coordinate, otherwise it means that the annotation spans over 2 or more lines.
            //       
            Element tempElement = Document.get().createSpanElement();
            srcElt.getParentElement().insertBefore(tempElement, srcElt);
            tempElement.setInnerText(srcElt.getInnerText().substring(1, 1));

            if (tempElement.getAbsoluteLeft() != aLeft) {
                //determine the size of the box on the first line

                //keep the right coordinate of the source element
                aLeft = tempElement.getAbsoluteLeft();
                aTop = tempElement.getAbsoluteTop();
                aBottom = tempElement.getAbsoluteBottom();

                tempElement.removeFromParent();
            }
        }


        int sWidth = aRight - aLeft;
        int sHeight = aBottom - aTop;
        return new Rect(aLeft, aTop, sWidth, sHeight);
    }

    // -------------------------------------------------------------------------
    private void textSelectionChanged(final int x, final int y) {

        annMarkerMgr.clearLastSelection();

        eventBus.fireEvent(new ApplicationStatusChangedEvent(ApplicationStatusChangedEvent.ApplicationStatusSwitching.Processing, null));
        Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {

            @Override
            public boolean execute() {
                ArrayList<DOMRange> ranges = annMarkerMgr.getSelectedRanges();

                RangeSelectionChangedEvent rangeSelectChangedEvent = new RangeSelectionChangedEvent(ranges);
                eventBus.fireEvent(rangeSelectChangedEvent);

                List<Fragment> targets = annMarkerMgr.computeTargets(ranges);
                TargetSelectionChangedEvent targetSelectChangedEvent = new TargetSelectionChangedEvent(targets);
                eventBus.fireEvent(targetSelectChangedEvent);

                eventBus.fireEvent(new ApplicationStatusChangedEvent(ApplicationStatusChangedEvent.ApplicationStatusSwitching.Idle, null));
                return false;
            }
        }, 1);
    }
    // -------------------------------------------------------------------------

    // Process keyboard shortcuts
    private void keypressed(KeyPressEvent event) {
        //FIXME : check that this document UI is the active one

        ShortCutTriggeredActionType shortcut = ShortCutToActionTypeMapper.getHappenedActionType(event.getCharCode(), event.getNativeEvent().getKeyCode(), event.isControlKeyDown(), event.isAltKeyDown(), event.isMetaKeyDown(), event.isShiftKeyDown());
        if (shortcut != null) {
            switch (shortcut) {
                case INCREASELINESIZE:
                    updateLineSize(+1);
                    break;
                case DEREASELINESIZE:
                    updateLineSize(-1);
                    break;
                case TOGGLESELECTONMODE:
                    selectionModeBtn.setValue(!selectionModeBtn.getValue(), true);
                    break;
                default:
                    if (!isReadOnly()) {
                        switch (shortcut) {
                            case UNDO:
                                undoEdit();
                                break;
                            case REDO:
                                redoEdit();
                                break;
                            case CREATEANNOTATION:
                                createAnchorMarkersFromSelectedRanges();
                                break;
                            case REMOVEANNOTATION:
                                removeSelectedTextAnnotations();
                                break;
                        }
                    }
            }
        }
    }

    // -------------------------------------------------------------------------
    private void refreshAllVeiledStatus() {
        Set<String> veiledAnnotations = getMapper().getVeiledAnnotationIds();
        for (String annotationId : getMapper().getAnnotationIds()) {
            refreshElementVeiledStatus(annotationId, veiledAnnotations.contains(annotationId));
        }
    }

    private void refreshElementVeiledStatus(String annotationId, boolean veiled) {
        Annotation annotation = getMapper().getAnnotation(annotationId);
        if (annotation != null) {
            if (annotation.getAnnotationKind().equals(AnnotationKind.TEXT)) {
                ArrayList<String> markerIds = getMapper().getMarkerIdsFromAnnotationId(annotationId);
                for (String mId : markerIds) {
                    Element e = Document.get().getElementById(mId);
                    if (e != null) {
                        if (veiled) {
                            e.setAttribute(GlobalStyles.VeiledAttr, "true");
                        } else {
                            e.removeAttribute(GlobalStyles.VeiledAttr);
                        }
                    }
                }
                extraDisplayer.refreshVeiledStatus(annotationId);
            } else if (annotation.getAnnotationKind().equals(AnnotationKind.GROUP)) {

                groupDisplayer.refreshVeiledStatus(annotationId);
            } else if (annotation.getAnnotationKind().equals(AnnotationKind.RELATION)) {

                relationDisplayer.refreshVeiledStatus(annotationId);
            }
        }
    }

    private void refreshElementsVeiledStatus(List<String> annotationIds) {
        if (getMapper() != null) {
            for (String annotationId : annotationIds) {
                refreshElementVeiledStatus(annotationId, getMapper().isVeiled(annotationId));
            }
        }
    }

    // -------------------------------------------------------------------------
    private void drawExtra() {
        Scheduler.get().scheduleDeferred(new Command() {

            @Override
            public void execute() {
                _drawExtra();
            }
        });
    }

    public void _drawExtra() {
        staneCore.speedTracerlog("DrawExtra - Start...");
        canvas.clear();
        canvas.setWidth(absolutePanel.getOffsetWidth());
        canvas.setHeight(absolutePanel.getOffsetHeight());

        extraDisplayer.drawExtra();
        staneCore.speedTracerlog("DrawExtra - Add annotations...");
        addAnnotations();
        staneCore.speedTracerlog("DrawExtra - End.");
    }

    /*
     * Re-Draw all Groups & Relations
     */
    public void addAnnotations() {
        AnnotatedTextHandler handler = getAnnotatedTextHandler();
        if (handler != null) {

            groupDisplayer.clearCombinedWidget();
            relationDisplayer.clearCombinedWidget();
            relationDisplayer.setInterlineSize(GlobalStyles.getInterlineSizePx()[interlineSizeIndex]);
            List<Annotation> groups = getDocument().getAnnotations(AnnotationKind.GROUP);
            List<Annotation> relations = getDocument().getAnnotations(AnnotationKind.RELATION);


            List<Annotation> allAnnotations = new ArrayList<Annotation>();
            if (groups != null) {
                allAnnotations.addAll(groups);
            }
            if (relations != null) {
                allAnnotations.addAll(relations);
            }

            /*
             * Since Relations and Groups refer to other annotations, these referenced annotations 
             * must be drawn before.
             * Thus, Annotations can be organized by levels, TEXT annotation being on the Level 0 
             * (because they do not reference any other annotations)
             */

            //find out level 1 Relations and Groups (those that only refer to TEXT annotations).
            {
                List<Annotation> groupsToAdd = new ArrayList<Annotation>();
                List<Annotation> relationsToAdd = new ArrayList<Annotation>();
                Iterator<Annotation> i = allAnnotations.iterator();
                while (i.hasNext()) {
                    Annotation annotation = i.next();

                    AnnotationKind kind = annotation.getAnnotationKind();
                    boolean level1 = true;
                    if (kind.equals(AnnotationKind.GROUP)) {
                        for (AnnotationReference aRef : annotation.getAnnotationGroup().getComponentRefs()) {
                            String referencedId = aRef.getAnnotationId();
                            Annotation component = handler.getAnnotation(referencedId);
                            if (component == null) {
                                GWT.log("Missing referenced Annotation id= " + referencedId);
                            } else if (!component.getAnnotationKind().equals(AnnotationKind.TEXT)) {
                                level1 = false;
                                break;
                            }
                        }
                        if (level1) {
                            i.remove();
                            groupsToAdd.add(annotation);
                        }

                    } else if (kind.equals(AnnotationKind.RELATION)) {
                        for (AnnotationReference aRef : annotation.getRelation().getRolesArguments().values()) {
                            String referencedId = aRef.getAnnotationId();
                            Annotation argument = handler.getAnnotation(referencedId);
                            if (argument == null) {
                                GWT.log("Missing referenced Annotation id= " + referencedId);
                            } else if (!argument.getAnnotationKind().equals(AnnotationKind.TEXT)) {
                                level1 = false;
                                break;
                            }
                        }
                        if (level1) {
                            i.remove();
                            relationsToAdd.add(annotation);
                        }
                    }

                }
                for (Annotation group : groupsToAdd) {
                    groupDisplayer.addAnnotation(group);
                }
                for (Annotation relation : relationsToAdd) {
                    relationDisplayer.addAnnotation(relation);
                }
            }

            //Max number of levels that will be explored (deepness)
            //FIXME : hard coded value, but in pratice high deepness surely means corrupted data 
            int iterationTreshold = 50;
            boolean improved = true;
            while (!allAnnotations.isEmpty() && improved && --iterationTreshold > 0) {

                improved = false;
                List<Annotation> toAdd = new ArrayList<Annotation>();
                Iterator<Annotation> i = allAnnotations.iterator();
                while (i.hasNext()) {
                    Annotation annotation = i.next();

                    boolean rendered = true;
                    AnnotationKind kind = annotation.getAnnotationKind();

                    if (kind.equals(AnnotationKind.GROUP)) {
                        for (AnnotationReference aRef : annotation.getAnnotationGroup().getComponentRefs()) {
                            Annotation referenced = handler.getAnnotation(aRef.getAnnotationId());
                            AnnotationKind referencedKind = referenced.getAnnotationKind();
                            if (!referencedKind.equals(AnnotationKind.TEXT)) {
                                String referencedId = referenced.getId();
                                CombinedAnnotationWidget w = relationDisplayer.getWidget(referencedId);
                                if (w == null) {
                                    w = groupDisplayer.getWidget(referencedId);
                                    if (w == null) {
                                        rendered = false;
                                        break;
                                    }
                                }
                            }
                        }

                    } else if (kind.equals(AnnotationKind.RELATION)) {

                        for (AnnotationReference aRef : annotation.getRelation().getRolesArguments().values()) {
                            Annotation referenced = handler.getAnnotation(aRef.getAnnotationId());
                            AnnotationKind referencedKind = referenced.getAnnotationKind();
                            if (!referencedKind.equals(AnnotationKind.TEXT)) {
                                String referencedId = referenced.getId();
                                CombinedAnnotationWidget w = relationDisplayer.getWidget(referencedId);
                                if (w == null) {
                                    w = groupDisplayer.getWidget(referencedId);
                                    if (w == null) {
                                        rendered = false;
                                        break;
                                    }
                                }
                            }
                        }
                    } else if (kind.equals(AnnotationKind.TEXT)) {
                        rendered = true;
                    }

                    if (rendered) {
                        improved = true;
                        i.remove();
                        toAdd.add(annotation);
                    }
                }
                for (Annotation annotation : toAdd) {
                    AnnotationKind kind = annotation.getAnnotationKind();

                    if (kind.equals(AnnotationKind.GROUP)) {
                        groupDisplayer.addAnnotation(annotation);
                    } else if (kind.equals(AnnotationKind.RELATION)) {
                        relationDisplayer.addAnnotation(annotation);
                    }
                }
            }
        }

    }

    /**
     * Graphically renders TEXT annotation extra features, such as link between fragments
     */
    public static class ExtraFeatureDisplayer {

        private final EventBus eventBus;
        private final DrawingArea canvas;
        private AnnotatedTextHandler annotatedDoc;
        private AnnotationDocumentViewMapper mapper;
        private final HashMap<String, Group> extraFeat = new HashMap<String, Group>();
        private final OccurenceDisplayer occurenceDisplayer;

        private ExtraFeatureDisplayer(DrawingArea canvas, EventBus eventBus, OccurenceDisplayer occurenceDisplayer) {
            this.canvas = canvas;
            this.eventBus = eventBus;
            this.occurenceDisplayer = occurenceDisplayer;
        }

        private void setDocument(AnnotatedTextHandler annotatedDoc, AnnotationDocumentViewMapper mapper) {
            this.annotatedDoc = annotatedDoc;
            this.mapper = mapper;
        }

        //FIXME : annotation Marker can be composed of several box due to line wrapping. This methode does not handle correctly such case
        /**
         * Perform the rendering of extra info, such as Relations, which can not be displayed by standard html+css
         */
        public void drawExtra() {
            int top = canvas.getAbsoluteTop();
            int left = canvas.getAbsoluteLeft();

            occurenceDisplayer.reset(canvas.getHeight());
            Group annotationFeatures;

            if (mapper != null && annotatedDoc != null) {

                //Render links between fragments of discountinuous annotation
                for (String annotationId : mapper.getAnnotationIds()) {
                    Annotation ann = mapper.getAnnotation(annotationId);

                    if (ann == null || !ann.getAnnotationKind().equals(AnnotationKind.TEXT) || mapper.isFormattingAnnotation(annotationId)) {
                        continue;
                    }
                    annotationFeatures = new Group();
                    String color = annotatedDoc.getAnnotatedText().getAnnotationSchema().getAnnotationTypeDefinition(ann.getAnnotationType()).getColor();

                    //do not draw links between marker that are to close one from each other (for instance successive markers of the same fragment separated just by another annotation boundary)
                    ArrayList<String> markerIds = mapper.getMarkerIdsFromAnnotationId(annotationId);

                    int size = markerIds.size();

                    if (size > 0) {
                        String mId = markerIds.get(0);
                        Element e = Document.get().getElementById(mId);
                        if (e != null) {
                            int y = e.getAbsoluteTop() - top;
                            occurenceDisplayer.addMarkAtPosition(y, color, annotationId);
                        }
                    }

                    if (size > 1) {
                        Integer fromX = null;
                        Integer fromY = null;

                        //
                        Group previouslinkGroup = null;
                        Group currentlinkGroup = null;
                        int toX = 0, toY = 0;
                        for (int i = 0; i < markerIds.size(); i++) {
                            String mId = markerIds.get(i);

                            Element e = Document.get().getElementById(mId);
                            if (e != null) {

                                //top left corner of marker box : x, y
                                int x = e.getAbsoluteLeft() - left;
                                int y = e.getAbsoluteTop() - top;

                                boolean tooClose = Math.abs(x - toX) < 5 && Math.abs(y - toY) < 5;
                                //top right corner of marker box : : toX, toY
                                toX = e.getAbsoluteRight() - left;
                                toY = y;

                                int r = 4;
                                int xOff = 2;
                                int yOff = 1;

                                //outbound point :
                                int obX = toX + xOff + r;
                                int obY = toY + yOff + (r / 2);

                                if (i < size - 1) {
                                    currentlinkGroup = new Group();
                                    //outbound arrow head
                                    Path path = new Path(toX + xOff, toY + 1);
                                    path.lineTo(obX, obY);
                                    path.lineTo(toX + xOff, toY + yOff + r);
                                    path.close();
                                    path.setStrokeColor("red");
                                    path.setStrokeWidth(1);
                                    currentlinkGroup.add(path);
                                }

                                //inbound point :
                                int ibX = x - xOff - r;
                                int ibY = y + yOff + (r / 2);

                                if (fromX != null) {

                                    //inbound arrow head
                                    Path path = new Path(x - xOff - r, y + yOff);
                                    path.lineTo(x - xOff, y + yOff + (r / 2));
                                    path.lineTo(x - xOff - r, y + 1 + r);
                                    path.close();
                                    path.setStrokeColor("green");
                                    path.setStrokeWidth(1);
                                    previouslinkGroup.add(path);

                                    //link
                                    final Path link = new Path(fromX, fromY);
                                    link.arc(fromX, fromY, 0, false, false, ibX, ibY);
                                    link.setStrokeColor(color);
                                    link.setStrokeWidth(1);
                                    link.setStrokeOpacity(.7);
                                    link.setFillColor(null);
                                    link.addMouseOverHandler(new MouseOverHandler() {

                                        @Override
                                        public void onMouseOver(MouseOverEvent event) {
                                            link.setStrokeWidth(3);
                                        }
                                    });

                                    link.addMouseOutHandler(new MouseOutHandler() {

                                        @Override
                                        public void onMouseOut(MouseOutEvent event) {
                                            link.setStrokeWidth(1);
                                        }
                                    });

                                    previouslinkGroup.add(link);
                                    if (!tooClose) {
                                        annotationFeatures.add(previouslinkGroup);
                                        previouslinkGroup = null;
                                    }

                                }
                                fromX = obX;
                                fromY = obY;
                                previouslinkGroup = currentlinkGroup;
                            }
                        }
                        annotationFeatures.setVisible(!mapper.isVeiled(annotationId));
                        canvas.add(annotationFeatures);
                        extraFeat.put(annotationId, annotationFeatures);

                    }
                }

                occurenceDisplayer.displayPendingChanges();
            }
        }

        private void refreshVeiledStatus(String annotationId) {
            Group annotationFeatures = extraFeat.get(annotationId);
            if (annotationFeatures != null) {
                annotationFeatures.setVisible(!mapper.isVeiled(annotationId));
            }
        }

        private void reset() {
            annotatedDoc = null;
            extraFeat.clear();
            occurenceDisplayer.reset();
        }
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    // -------------------------------------------------------------------------
    @Override
    public void clearTextSelection(boolean setFocus) {
        annMarkerMgr.clearLastSelection();
        AnnotationMarkerManager.clearNativeSelection();
        if (setFocus && !isReadOnly()) {
            focusPanel.setFocus(true);
        }
    }

    private void clearCurrentlySelectedAnnotations() {
        selectedTextAnnotations.clear();
    }

    private void replaceCurrentlySelectedAnnotations(AnnotationSelections annotationSelection) {
        selectedTextAnnotations.replaceSelection(annotationSelection);
    }

    private GenericAnnotationSelection changeCurrentlySelectedAnnotations(Annotation annotation, String mainSelectedMark, boolean removeIfPreviouslySelected) {
        GenericAnnotationSelection selection = null;
        if (annotation != null) {
            if (removeIfPreviouslySelected && selectedTextAnnotations.isAnnotationSelected(annotation.getId())) {
                selectedTextAnnotations.removeAnnotationFromSelection(annotation.getId());
            } else {
                selection = selectedTextAnnotations.addAnnotationSelection(annotation, mainSelectedMark, getMapper().getMarkerIdsFromAnnotationId(annotation.getId()));
            }
        }
        return selection;
    }

    @Override
    public void clearAnchorMarkerSelection() {
        clearCurrentlySelectedAnnotations();
        //fire event to inform that annotation selection has changed
        AnnotatedTextHandler annotatedTextHandler = getAnnotatedTextHandler();
        if (annotatedTextHandler != null) {
            eventBus.fireEvent(new TextAnnotationSelectionEmptiedEvent(annotatedTextHandler));
        }
    }

    private void updateMarkerSelection(String markerId, String annotationId, boolean multiSelectKeyDown, boolean removeIfPreviouslySelected) {

        hideCurrentlySelectedAnnotations();
        //[Ctrl]/[Command] key used for multiselect
        if (!multiSelectKeyDown) {
            //clear previous selection
            clearAnchorMarkerSelection();
        }

        if (annotationId != null) {
            Annotation annotation = getMapper().getAnnotation(annotationId);
            changeCurrentlySelectedAnnotations(annotation, markerId, removeIfPreviouslySelected);

            //fire event to inform that annotation selection has changed
            if (selectedTextAnnotations.isEmpty()) {
                eventBus.fireEvent(new TextAnnotationSelectionEmptiedEvent(getAnnotatedTextHandler()));
            } else {
                eventBus.fireEvent(new TextAnnotationSelectionChangedEvent(getAnnotatedTextHandler(), selectedTextAnnotations));
            }
        }
    }

    /**
     * Remove graphical hints used to show that some annotation/marker are is selected
     */
    private void hideCurrentlySelectedAnnotations() {
        for (GenericAnnotationSelection selectedAnn : selectedTextAnnotations.getTextAnnotationSelection()) {
            if (selectedAnn instanceof TextAnnotationSelection) {
                ArrayList<String> markerIds = ((TextAnnotationSelection) selectedAnn).getSelectedMarkers();
                for (String selectId : markerIds) {
                    Element e = Document.get().getElementById(selectId);
                    if (e != null) {
                        e.removeClassName(GlobalStyles.SelectedAnnotation);
                        e.removeClassName(GlobalStyles.MainSelectedAnnotation);
                        e.setAttribute(GlobalStyles.SelectedAttr, "false");
                    }
                }
            }
        }
    }

    /**
     * Graphically highlight the current annotations/markers selection
     */
    private void displayCurrentlySelectedAnnotations() {
        Scheduler.get().scheduleFinally(new Command() {

            @Override
            public void execute() {
                boolean highlightMainMarker = true;
                for (int i = selectedTextAnnotations.getSelections().size() - 1; i >= 0; i--) {
                    GenericAnnotationSelection selectedAnn = selectedTextAnnotations.getSelections().get(i);
                    if (selectedAnn instanceof TextAnnotationSelection) {
                        ArrayList<String> markerIds = ((TextAnnotationSelection) selectedAnn).getSelectedMarkers();
                        //selection of the entire TEXT annotation
                        if (markerIds.isEmpty()) {
                            ArrayList<String> newMarkers = getMapper().getMarkerIdsFromAnnotationId(selectedAnn.getAnnotation().getId());
                            markerIds.addAll(newMarkers);
                        }
                        for (String mId : markerIds) {
                            Element e = Document.get().getElementById(mId);
                            if (e != null) {
                                if (highlightMainMarker) {
                                    e.addClassName(GlobalStyles.MainSelectedAnnotation);
                                    highlightMainMarker = false;
                                } else {
                                    e.addClassName(GlobalStyles.SelectedAnnotation);
                                }
                                e.setAttribute(GlobalStyles.SelectedAttr, "true");
                            }
                        }
                    }
                }
            }
        });
    }

// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    @Override
    public void extendSelectedAnnotationWithRange(final List<DOMRange> ranges) {
        if (isReadOnly()) {
            throw new UnsupportedOperationException("Modification not allowed on read-only Document!");
        }

        if (ranges != null && !ranges.isEmpty()) {
            final Annotation annotation = selectedTextAnnotations.getMainSelectedTextAnnotation();
            clearAnchorMarkerSelection();

            if (annotation != null) {
                eventBus.fireEvent(new ApplicationStatusChangedEvent(ApplicationStatusChangedEvent.ApplicationStatusSwitching.Processing, null));

                Scheduler.get().scheduleDeferred(new Command() {

                    @Override
                    public void execute() {
                        List<Fragment> targets = annMarkerMgr.computeTargets(ranges);
                        addFragmentToAnnotation(annotation.getId(), targets);
                        eventBus.fireEvent(new ApplicationStatusChangedEvent(ApplicationStatusChangedEvent.ApplicationStatusSwitching.Idle, null));
                    }
                });
            }
        }
    }

    @Override
    public void pruneRangeFromSelectedAnnotation(final List<DOMRange> ranges) {
        if (isReadOnly()) {
            throw new UnsupportedOperationException("Modification not allowed on read-only Document!");
        }

        if (ranges != null && !ranges.isEmpty()) {
            final Annotation annotation = selectedTextAnnotations.getMainSelectedTextAnnotation();
            clearAnchorMarkerSelection();

            if (annotation != null) {
                eventBus.fireEvent(new ApplicationStatusChangedEvent(ApplicationStatusChangedEvent.ApplicationStatusSwitching.Processing, null));

                Scheduler.get().scheduleDeferred(new Command() {

                    @Override
                    public void execute() {
                        List<Fragment> targets = annMarkerMgr.computeTargets(ranges);
                        removeFragmentFromAnnotation(annotation.getId(), targets);
                        eventBus.fireEvent(new ApplicationStatusChangedEvent(ApplicationStatusChangedEvent.ApplicationStatusSwitching.Idle, null));
                    }
                });
            }
        }
    }

    @Override
    public void createAnchorMarkersFromSelectedRanges() {
        annotationReificator.hideWidget();
        ArrayList<DOMRange> ranges = annMarkerMgr.getSelectedRanges();
        createAnchorMarkersFromRanges(getCurrentAnnotationType(), ranges);
    }

    @Override
    public void createAnchorMarkersFromRanges(final String newAnnotationType, final List<DOMRange> ranges) {
        if (isReadOnly()) {
            throw new UnsupportedOperationException("Modification not allowed on read-only Document!");
        }

        if (ranges != null && !ranges.isEmpty()) {
            clearAnchorMarkerSelection();

            eventBus.fireEvent(new ApplicationStatusChangedEvent(ApplicationStatusChangedEvent.ApplicationStatusSwitching.Processing, null));

            final AnnotationSchemaDefinition params = getDocument().getAnnotationSchema();

            Scheduler.get().scheduleDeferred(new Command() {

                @Override
                public void execute() {

                    List<Fragment> targets = annMarkerMgr.computeTargets(ranges);

                    //Check boundary constraints
                    Annotation annotation = ((AnnotatedTextImpl) getDocument()).createLooseTextAnnotation("new textAnnotation", newAnnotationType, targets);
                    BasicAnnotationSchemaValidator validator = new BasicAnnotationSchemaValidator();
                    BasicFaultListener faultLstnr = new BasicFaultListener();
                    faultLstnr.reset();
                    validator.setAnnotatedText(getDocument());
                    if (validator.checkBoundaries(faultLstnr, annotation, true)) {
                        annMarkerMgr.createMarkersForTargets(getAnnotatedTextHandler(), params, newAnnotationType, targets);
                        eventBus.fireEvent(new ApplicationStatusChangedEvent(ApplicationStatusChangedEvent.ApplicationStatusSwitching.Idle, null));
                    } else {

                        //display error image in the margin of the line containing the conflicting annotation
                        docTextPanel.setWidgetTopHeight(errorImage, 10, Unit.PX, errorImage.getHeight(), Unit.PX);
                        docTextPanel.setWidgetLeftWidth(errorImage, 10, Unit.PX, errorImage.getWidth(), Unit.PX);
                        Annotation a = faultLstnr.getConflictingAnnotation(faultLstnr.getMessages().size() - 1);
                        ArrayList<String> markerIds = mapper.getMarkerIdsFromAnnotationId(a.getId());

                        Element e = Document.get().getElementById(markerIds.get(0));
                        if (e != null) {
                            docTextPanel.setWidgetTopHeight(errorImage, e.getOffsetTop() - scrollPanel.getVerticalScrollPosition(), Unit.PX, 25, Unit.PX);
                        }
                        errorImage.setVisible(true);
                        new Blinker(errorImage, new int[]{120, 40, 100, 40, 100, 1200, 10}).start();

                        injector.getMainEventBus().fireEvent(new InformationReleasedEvent("<span style='color:red;'>" + faultLstnr.getLastMessage() + "</span>"));
                    }
                }
            });
        }

    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    @UiHandler("addAnnotButton")
    void handleAddAnnButtonClick(ClickEvent e) {
        try {
            addAnnotButton.setEnabled(false);
            createAnchorMarkersFromSelectedRanges();
            clearAnchorMarkerSelection();
            clearTextSelection(true);
        } finally {
            buttonManager.setButtonsEnabled(true);
        }
    }

    @UiHandler("deleteAnnButton")
    void handleDeleteMarkButtonClick(ClickEvent e) {
        try {
            deleteAnnButton.setEnabled(false);
            removeSelectedTextAnnotations();
        } finally {
            buttonManager.setButtonsEnabled(true);
        }
    }

    /*
    @UiHandler("editAnnButton")
    void handleEditMarkButtonClick(ClickEvent e) {
    try {
    editAnnButton.setEnabled(false);
    //
    } finally {
    buttonManager.setButtonsEnabled(true);
    }
    }
     *
     */
    public void removeSelectedTextAnnotations() {
        if (isReadOnly()) {
            throw new UnsupportedOperationException("Modification not allowed on read-only Document!");
        }
        List<String> annotationIds;
        if (!selectedTextAnnotations.isEmpty()) {
            if (selectedTextAnnotations.getSelections().size() > 1) {
                annotationIds = selectedTextAnnotations.getSelectedTextAnnotationIds();
                removeAnnotations(annotationIds);
            } else {
                Annotation annotation = selectedTextAnnotations.getMainSelectedTextAnnotation();
                removeAnnotation(annotation.getId());
            }
        }
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    @UiHandler("addGroupButton")
    void handleAddGroupButtonClick(ClickEvent e) {
        try {
            addGroupButton.setEnabled(false);
            createGroup();
        } finally {
            buttonManager.setButtonsEnabled(true);
        }
    }

    private void createGroup() {
        if (isReadOnly()) {
            throw new UnsupportedOperationException("Modification not allowed on read-only Document!");
        }
        ArrayList<GenericAnnotationSelection> selected = new ArrayList<GenericAnnotationSelection>();
        selected.addAll(selectedTextAnnotations.getSelections());
        selected.addAll(selectedGroups.getSelections());
        selected.addAll(selectedRelations.getSelections());

        if (selected != null && !selected.isEmpty()) {
            ArrayList<String> annotationIds = new ArrayList<String>();
            for (GenericAnnotationSelection s : selected) {
                annotationIds.add(s.getAnnotation().getId());
            }
            EditGroupDialog.startGroupCreation(getAnnotatedTextHandler(), annotationIds);
        }
    }

    @UiHandler("deleteGroupButton")
    void handleDeleteGroupButtonClick(ClickEvent e) {
        try {
            deleteGroupButton.setEnabled(false);
            removeMainSelectedGroup();
        } finally {
            buttonManager.setButtonsEnabled(true);
        }
    }

    private void removeMainSelectedGroup() {
        if (isReadOnly()) {
            throw new UnsupportedOperationException("Modification not allowed on read-only Document!");
        }
        Annotation selected = selectedGroups.getMainSelectedGroupAnnotation();
        if (selected != null) {
            removeGroup(selected);
        }
    }

    @UiHandler("editGroupButton")
    void handleEditGroupButtonClick(ClickEvent e) {
        try {
            editGroupButton.setEnabled(false);
            editGroup();
        } finally {
            buttonManager.setButtonsEnabled(true);
        }
    }

    private void editGroup() {
        if (isReadOnly()) {
            throw new UnsupportedOperationException("Modification not allowed on read-only Document!");
        }
        EditGroupDialog.startGroupEdition(getAnnotatedTextHandler(), selectedGroups.getMainSelectedAnnotation().getId());
    }
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    @UiHandler("addRelButton")
    void handleAddRelButtonClick(ClickEvent e) {
        try {
            addRelButton.setEnabled(false);
            createRelation();
        } finally {
            buttonManager.setButtonsEnabled(true);
        }
    }

    private void createRelation() {
        if (isReadOnly()) {
            throw new UnsupportedOperationException("Modification not allowed on read-only Document!");
        }
        ArrayList<GenericAnnotationSelection> selected = new ArrayList<GenericAnnotationSelection>();
        selected.addAll(selectedTextAnnotations.getSelections());
        selected.addAll(selectedGroups.getSelections());
        selected.addAll(selectedRelations.getSelections());

        if (selected != null && !selected.isEmpty()) {
            ArrayList<String> annotationIds = new ArrayList<String>();
            for (GenericAnnotationSelection s : selected) {
                annotationIds.add(s.getAnnotation().getId());
            }
            EditRelationDialog.startRelationCreation(getAnnotatedTextHandler(), annotationIds);
        }
    }

    @UiHandler("deleteRelButton")
    void handleDeleteRelButtonClick(ClickEvent e) {
        try {
            deleteRelButton.setEnabled(false);
            removeMainSelectedRelation();
        } finally {
            buttonManager.setButtonsEnabled(true);
        }
    }

    private void removeMainSelectedRelation() {
        if (isReadOnly()) {
            throw new UnsupportedOperationException("Modification not allowed on read-only Document!");
        }
        Annotation selected = selectedRelations.getMainSelectedRelationAnnotation();
        if (selected != null) {
            removeRelation(selected);
        }
    }

    @UiHandler("editRelButton")
    void handleEditRelButtonClick(ClickEvent e) {
        try {
            editRelButton.setEnabled(false);
            editRelation();
        } finally {
            buttonManager.setButtonsEnabled(true);
        }
    }

    private void editRelation() {
        if (isReadOnly()) {
            throw new UnsupportedOperationException("Modification not allowed on read-only Document!");
        }
        EditRelationDialog.startRelationEdition(getAnnotatedTextHandler(), selectedRelations.getMainSelectedAnnotation().getId());
    }
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    private void removeFragmentFromAnnotation(String annotationId, List<Fragment> fragments) {
        TextAnnotationFragmentsRemovalEdit edit = new TextAnnotationFragmentsRemovalEdit(getAnnotatedTextHandler(), annotationId, fragments);
        edit.redo();
    }

    private void addFragmentToAnnotation(String annotationId, List<Fragment> fragments) {
        TextAnnotationFragmentsAdditionEdit edit = new TextAnnotationFragmentsAdditionEdit(getAnnotatedTextHandler(), annotationId, fragments);
        edit.redo();
    }

    // -------------------------------------------------------------------------
    private void removeAnnotation(String annotationId) {
        TextAnnotationRemovalEdit edit = new TextAnnotationRemovalEdit(getAnnotatedTextHandler(), annotationId);
        String msg = edit.getPreventingCause();
        if (msg != null) {
            ResultMessageDialog d = new ResultMessageDialog(ResultMessageDialog.Error, "Can not remove annotation " + annotationId, msg);
            d.show();
        } else {
            edit.redo();
        }
    }

    private void removeAnnotations(List<String> annotationIds) {
        AnnotationCompoundEdit main = new AnnotationCompoundEdit();
        for (String annotationId : annotationIds) {
            TextAnnotationRemovalEdit edit = new TextAnnotationRemovalEdit(getAnnotatedTextHandler(), annotationId);
            String msg = edit.getPreventingCause();
            if (msg != null) {
                ResultMessageDialog d = new ResultMessageDialog(ResultMessageDialog.Error, "Can not remove annotation " + annotationId, msg);
                d.show();
                main = null;
                break;
            }
            main.addEdit(edit);
        }
        if (main != null) {
            clearAnchorMarkerSelection();
            main.redo();
        }
    }

    private void removeGroup(Annotation group) {
        AnnotationGroupRemovalEdit edit = new AnnotationGroupRemovalEdit(getAnnotatedTextHandler(), group);
        String msg = edit.getPreventingCause();
        if (msg != null) {
            ResultMessageDialog d = new ResultMessageDialog(ResultMessageDialog.Error, "Can not remove Group " + group.getId(), msg);
            d.show();
        } else {
            edit.redo();
        }
    }

    // -------------------------------------------------------------------------
    private void removeRelation(Annotation relation) {
        AnnotationRelationRemovalEdit edit = new AnnotationRelationRemovalEdit(getAnnotatedTextHandler(), relation);
        String msg = edit.getPreventingCause();
        if (msg != null) {
            ResultMessageDialog d = new ResultMessageDialog(ResultMessageDialog.Error, "Can not remove Relation " + relation.getId(), msg);
            d.show();
        } else {
            edit.redo();
        }
    }

    // =========================================================================
    @UiHandler("explainSchemaBtn")
    void handleExplainSchemaButtonClick(ClickEvent e) {
        if (getDocument() != null) {
            final DialogBox box = new DialogBox(true, true);
            box.setText("Annotation Schema");
            box.add(new ExplainSchemaPanel(getDocument().getAnnotationSchema()));
            box.setPopupPositionAndShow(new PositionCallback() {

                @Override
                public void setPosition(int offsetWidth, int offsetHeight) {
                    int left = (Window.getClientWidth() / 2) - offsetWidth  ;
                    int top = (Window.getClientHeight() / 2) - offsetHeight ;
                    box.setPopupPosition(left, top);
                }
            });
        }
    }
    
    // =========================================================================
    @UiHandler("validateButton")
    void handleValidateButtonClick(ClickEvent e) {
        try {
            validateButton.setEnabled(false);
            BasicAnnotationSchemaValidator validator = new BasicAnnotationSchemaValidator();
            BasicFaultListener faultLstnr = new BasicFaultListener();
            faultLstnr.reset();
            validator.setAnnotatedText(getDocument());
            validator.validate(faultLstnr, getDocument(), false);
            StringBuilder msgs = new StringBuilder();
            for (String m : faultLstnr.getMessages()) {
                msgs.append(m).append("\n");
            }
            ExportDialog dlg = new ExportDialog(msgs.toString());
            dlg.show();
            dlg.center();
        } finally {
            buttonManager.setButtonsEnabled(true);
        }
    }
    // =========================================================================
    SafeHtml checkedIcon = SafeHtmlUtils.fromSafeConstant(AbstractImagePrototype.create(StanEditorResources.INSTANCE.CheckedIcon()).getHTML());

    private void resetAnnSetSelectionMenu() {
        annSetMenuItem.setSubMenu(null);
    }

    private void reinitAnnSetSelectionMenu() {
        annSetMenuItem.setSubMenu(null);
        MenuBar annSetMenuBar = new MenuBar(true);
        if (getDocument() != null) {
            for (AnnotationSetInfo asi : getDocument().getAnnotationSetInfoList()) {

                if (getAnnotatedTextHandler().getLoadedAnnotationSets().contains(asi.getId())) {
                    SafeHtmlBuilder shbuilder = new SafeHtmlBuilder();
                    shbuilder.append(checkedIcon).appendEscaped("  ");
                    shbuilder.appendEscaped(asi.getDescription());
                    MenuItem item = annSetMenuBar.addItem(shbuilder.toSafeHtml(), (Command) null);
                    item.setEnabled(false);
                } else {
                    final int annSetId = asi.getId();
                    Command command = new Command() {

                        @Override
                        public void execute() {
                            getAnnotatedTextHandler().requestAdditionalAnnotationSet(annSetId);
                        }
                    };

                    MenuItem item = new MenuItem(asi.getDescription(), command);
                    item.setEnabled(true);
                    annSetMenuBar.addItem(item);
                }
            }
        }
        annSetMenuItem.setSubMenu(annSetMenuBar);
    }
    // =========================================================================

    class ButtonEnablementManager implements AnnotationSelectionChangedEventHandler, EditHappenedEventHandler, RangeSelectionChangedEventHandler {

        boolean globalEnabledDirective = true;

        public void setButtonsEnabled(boolean enabled) {
            globalEnabledDirective = enabled;
            updateButtonStatuses();
        }

        private void updateButtonStatuses() {
            Scheduler.get().scheduleFinally(new Command() {

                @Override
                public void execute() {
                    incLineSize.setEnabled(globalEnabledDirective && canIncreaseLineSize());
                    decLineSize.setEnabled(globalEnabledDirective && canReduceLineSize());

                    boolean readOnly = isReadOnly();
                    undoBtn.setEnabled(globalEnabledDirective && !readOnly && undoManager != null && undoManager.canUndo());
                    redoBtn.setEnabled(globalEnabledDirective && !readOnly && undoManager != null && undoManager.canRedo());

                    selectionModeBtn.setEnabled(globalEnabledDirective);

                    boolean someTextSelected = !annMarkerMgr.getSelectedRanges().isEmpty();

                    boolean someTextAnnSelected = false;
                    boolean someGroupSelected = false;
                    boolean someRelationSelected = false;

                    int nbModifiableTextAnnSelected = 0;
                    int nbModifiableGroupSelected = 0;
                    int nbModifiableRelationSelected = 0;

                    AnnotatedTextHandler handler = getAnnotatedTextHandler();
                    if (handler != null) {
                        int formattingAnnSetId = handler.getFormattingAnnotationSet() != null ? handler.getFormattingAnnotationSet().getId() : -1;
                        int userAnnSetId = handler.getUsersAnnotationSet() != null ? handler.getUsersAnnotationSet().getId() : -1;

                        // - Only the Annotations belonging to the user's Annotation Set can be modified
                        // - Annotation of any other Annotation Set (excepted Formatting AnnotationSet) can be referenced
                        // - Formatting Annotation can not be modified or referenced

                        for (GenericAnnotationSelection a : selectedTextAnnotations.getSelections()) {
                            int annAnnSetId = handler.getAnnotationSetId(a.getAnnotation().getId());
                            if (userAnnSetId == annAnnSetId) {
                                nbModifiableTextAnnSelected++;
                                if (nbModifiableTextAnnSelected > 1) {
                                    break;
                                }
                            } else if (formattingAnnSetId != annAnnSetId) {
                                someTextAnnSelected = true;
                            }
                        }
                        someTextAnnSelected = someTextAnnSelected || nbModifiableTextAnnSelected > 0;

                        //
                        for (GenericAnnotationSelection a : selectedGroups.getSelections()) {
                            int annAnnSetId = handler.getAnnotationSetId(a.getAnnotation().getId());
                            if (userAnnSetId == annAnnSetId) {
                                nbModifiableGroupSelected++;
                                if (nbModifiableGroupSelected > 1) {
                                    break;
                                }
                            } else if (formattingAnnSetId != annAnnSetId) {
                                someGroupSelected = true;
                            }
                        }
                        someGroupSelected = someGroupSelected || nbModifiableGroupSelected > 0;

                        //
                        for (GenericAnnotationSelection a : selectedRelations.getSelections()) {
                            int annAnnSetId = handler.getAnnotationSetId(a.getAnnotation().getId());
                            if (userAnnSetId == annAnnSetId) {
                                nbModifiableRelationSelected++;
                                if (nbModifiableRelationSelected > 1) {
                                    break;
                                }
                            } else if (formattingAnnSetId != annAnnSetId) {
                                someRelationSelected = true;
                            }
                        }
                        someRelationSelected = someRelationSelected || nbModifiableRelationSelected > 0;

                    }

                    boolean someAnnSelected = someTextAnnSelected || someGroupSelected || someRelationSelected;

                    annTypeList.setEnabled(globalEnabledDirective && !readOnly);
                    addAnnotButton.setEnabled(globalEnabledDirective && !readOnly && someTextSelected);
                    deleteAnnButton.setEnabled(globalEnabledDirective && !readOnly && nbModifiableTextAnnSelected > 0);
                    //editAnnButton.setEnabled(globalEnabledDirective && !readOnly && nbModifiableTextAnnSelected == 1);
                    addSelectionButton.setEnabled(globalEnabledDirective && !readOnly && someTextSelected && nbModifiableTextAnnSelected == 1);
                    delSelectionButton.setEnabled(globalEnabledDirective && !readOnly && someTextSelected && nbModifiableTextAnnSelected == 1);

                    addGroupButton.setEnabled(globalEnabledDirective && !readOnly && someAnnSelected);
                    deleteGroupButton.setEnabled(globalEnabledDirective && !readOnly && nbModifiableGroupSelected > 0);
                    editGroupButton.setEnabled(globalEnabledDirective && !readOnly && nbModifiableGroupSelected == 1);

                    addRelButton.setEnabled(globalEnabledDirective && !readOnly && someAnnSelected);
                    deleteRelButton.setEnabled(globalEnabledDirective && !readOnly && nbModifiableRelationSelected > 0);
                    editRelButton.setEnabled(globalEnabledDirective && !readOnly && nbModifiableRelationSelected == 1);

                    validateButton.setEnabled(globalEnabledDirective && !readOnly);
                }
            });
        }

        @Override
        public void onAnnotationSelectionChanged(GenericAnnotationSelectionChangedEvent event) {
            updateButtonStatuses();
        }

        @Override
        public void onEditHappened(EditHappenedEvent event) {
            updateButtonStatuses();
        }

        @Override
        public void onRangeSelectionChanged(RangeSelectionChangedEvent event) {
            updateButtonStatuses();
        }
    }
    // =========================================================================

    @Override
    public void setReadOnly(boolean readOnly) {
        if (!readOnly) {
            //deny read/write for MSIE browser
            float v = AnnotationMarkerManager.getInternetExplorerVersion();
            if (v >= 0 && v < 9) {
                readOnly = true;
                Window.alert("Only readOnly mode is supported by MS Internet Explorer");
            }
        }
        this.options.setReadOnly(readOnly);
        undoBtn.setVisible(!readOnly);
        redoBtn.setVisible(!readOnly);
        if (readOnly) {
            removeUndoManager();
            contentHTML.removeStyleName(style.SelectableText());
        } else {
            contentHTML.addStyleName(style.SelectableText());
            addUndoManager();
        }
        initDragController(readOnly);
    }

    private void removeUndoManager() {
        if (undoManager != null) {
            undoManager.discardAllEdits();
            EditHappenedEvent.unregister(undoManager);
            undoManager = null;
        }
    }

    private void addUndoManager() {
        if (!isReadOnly()) {
            undoManager = new UndoManager(getDocument());
            EditHappenedEvent.register(eventBus, undoManager);
        }
    }

    public void doRefresh(boolean includingTextAnnotation) {
        if (includingTextAnnotation) {
            annMarkerMgr.refreshDocument();
            displayCurrentlySelectedAnnotations();
        }
        refreshAllVeiledStatus();
        drawExtra();
    }

    @Override
    public void onEditHappened(EditHappenedEvent event) {
        boolean refreshNeeded = false;
        boolean includingTextAnnotation = false;
        if (event.getEdit() instanceof TextAnnotationCoverageEdit) {
            refreshNeeded = (getDocument() != null) && getDocument().equals(event.getEdit().getAnnotatedTextHandler().getAnnotatedText());
            if (!refreshNeeded) {
                return;
            }
            includingTextAnnotation = true;
        }
        doRefresh(includingTextAnnotation);
        refreshAllVeiledStatus();
        drawExtra();
    }

    @Override
    public void onAnnotationStatusChanged(AnnotationStatusChangedEvent event) {
        refreshElementsVeiledStatus(event.getAnnotationIds());
    }

    @Override
    public void onAnnotationFocusChanged(AnnotationFocusChangedEvent event) {
        //set scroll position to place the focused Annotation in the center of document panel
        if (scrollPanel.getMaximumVerticalScrollPosition() > 0) {
            boolean refreshNeeded = (getDocument() != null) && getDocument().equals(event.getAnnotatedTextHandler().getAnnotatedText());
            CombinedAnnotationWidget w;
            if (refreshNeeded) {
                Annotation annotation = event.getAnnotation();
                String annotationId = annotation.getId();
                Integer pointToDisplay = null;

                if (AnnotationKind.TEXT.equals(annotation.getAnnotationKind())) {
                    ArrayList<String> markerIds = mapper.getMarkerIdsFromAnnotationId(annotationId);
                    Element e = Document.get().getElementById(markerIds.get(0));
                    pointToDisplay = e.getOffsetTop() + (e.getOffsetHeight() / 2);

                } else if (AnnotationKind.GROUP.equals(annotation.getAnnotationKind())) {
                    w = groupDisplayer.getWidget(annotationId);
                    if (w != null) {
                        pointToDisplay = w.getCenterPoint().y;
                    }
                } else if (AnnotationKind.RELATION.equals(annotation.getAnnotationKind())) {
                    w = relationDisplayer.getWidget(annotationId);
                    if (w != null) {
                        pointToDisplay = w.getCenterPoint().y;
                    }
                }

                if (pointToDisplay != null) {
                    Integer centeredScrollPos = pointToDisplay - (scrollPanel.getElement().getClientHeight() / 2);
                    scrollPanel.setVerticalScrollPosition(centeredScrollPos);
                }
            }
        }
    }

    @Override
    public void onWorkingDocumentChanged(WorkingDocumentChangedEvent event) {
        AnnotatedTextHandler handler = getAnnotatedTextHandler();
        if (handler != null && handler.equals(event.getWorkingDocument()) && WorkingDocumentChangedEvent.ChangeType.AdditionalAnnotationSetLoaded.equals(event.getChangeType())) {
            //
            injector.getMainEventBus().fireEvent(new InformationReleasedEvent("<span style='color:green;'>Addtionnal AnnotationSet loaded!</span>"));
            reinitAnnSetSelectionMenu();
            doRefresh(true);
        }
    }

    private void setScrollPositionAtRatio(int prevScrollPos, int prevMaxScrollPos) {
        int newMaxScrollPos = scrollPanel.getMaximumVerticalScrollPosition();
        if (prevMaxScrollPos > 0 && newMaxScrollPos > 0) {
            int newScrollPos = prevScrollPos * newMaxScrollPos / prevMaxScrollPos;
            scrollPanel.setVerticalScrollPosition(newScrollPos);
        }
    }

    @Override
    public void onMaximizingWidget(MaximizingWidgetEvent event) {


        if (event.getWidget().equals(this)) {
            final int prevScrollPos = scrollPanel.getVerticalScrollPosition();
            final int prevmaxScrollPos = scrollPanel.getMaximumVerticalScrollPosition();

            maxmimized = event.isMaximizing();
            //expand/collapse toolbar
            toolBarExpandCollapseHandler.setExpanded(!maxmimized);

            //perform scrolling after that other components reacted to this event
            Scheduler.get().scheduleDeferred(new Command() {

                @Override
                public void execute() {
                    //reset scrolling in order to show the part of text that was visible before (un)maximization
                    setScrollPositionAtRatio(prevScrollPos, prevmaxScrollPos);
                }
            });
        }
        //regenerate SVG after widget resizing
        drawExtra();
    }

    @Override
    public boolean isReadOnly() {
        return options.isReadOnly();
    }

    @Override
    public AnnotationDocumentViewMapper getMapper() {
        return mapper;
    }

    @Override
    public String getTextContainerId() {
        return annMarkerMgr.getTextContainerId();
    }

    @Deprecated
    public void setPrintable(boolean printable) {
        if (printable) {
            Element el = contentHTML.getElement().getParentElement();
            String sizeStyle1 = "top: " + el.getOffsetTop() + "px; left: " + el.getOffsetLeft() + "px; width: " + el.getClientWidth() + "px; height: " + el.getClientHeight() + "px;";

            //remove padding 
            int realClientWidth = el.getClientWidth() - 39 - 13;
            int realClientHeight = el.getClientHeight() - 13 - 26;

            Element el2 = contentHTML.getElement();
            String sizeStyle2 = "top: " + el2.getOffsetTop() + "px; left: " + el2.getOffsetLeft() + "px; width: " + realClientWidth + "px; height: " + realClientHeight + "px;";

            Element el3 = canvas.getElement();
            String sizeStyle3 = "top: " + el3.getOffsetTop() + "px; left: " + el3.getOffsetLeft() + "px; width: 100%; height: 100%;";

            el.setAttribute("style", "position: fixed; " + sizeStyle1);
            canvas.getElement().setAttribute("style", "background-color: white; position: fixed; z-index:1; " + sizeStyle3);
            el2.setAttribute("style", "z-index:2; position: fixed; " + sizeStyle2);

            //contentHTML.getElement().getParentElement().addClassName(compId);
        } else {
            //contentHTML.getElement().getParentElement().removeClassName(compId);
        }
    }

    @Override
    protected void onAttach() {
        super.onAttach();
        TextAnnotationSelectionChangedEvent.register(eventBus, DocumentUi.this);
        RelationSelectionChangedEvent.register(eventBus, DocumentUi.this);
        EditHappenedEvent.register(eventBus, DocumentUi.this);
        AnnotationStatusChangedEvent.register(eventBus, DocumentUi.this);
        AnnotationFocusChangedEvent.register(eventBus, DocumentUi.this);
        WorkingDocumentChangedEvent.register(eventBus, DocumentUi.this);
        MaximizingWidgetEvent.register(eventBus, DocumentUi.this);

        GenericAnnotationSelectionChangedEvent.register(eventBus, buttonManager);
        EditHappenedEvent.register(eventBus, buttonManager);
        RangeSelectionChangedEvent.register(eventBus, buttonManager);
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        TextAnnotationSelectionChangedEvent.unregister(this);
        RelationSelectionChangedEvent.unregister(this);
        EditHappenedEvent.unregister(this);
        AnnotationStatusChangedEvent.unregister(this);
        AnnotationFocusChangedEvent.unregister(this);
        WorkingDocumentChangedEvent.unregister(this);
        MaximizingWidgetEvent.unregister(this);

        GenericAnnotationSelectionChangedEvent.unregister(buttonManager);
        EditHappenedEvent.unregister(buttonManager);
        RangeSelectionChangedEvent.unregister(buttonManager);
    }
}
