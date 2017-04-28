/*
 *
 *      This software is a result of Quaero project and its use must respect the rules of the Quaero Project Consortium Agreement.
 *
 *      Copyright Institut National de la Recherche Agronomique, 2011-2012.
 *
 */
package fr.inra.mig_bibliome.stane.client.SemClass;

import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayInteger;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Response;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.cellview.client.CellTree;
import com.google.gwt.user.cellview.client.TreeNode;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SingleSelectionModel;
import fr.inra.mig_bibliome.stane.client.Config.StaneClientBaseGinInjector;
import fr.inra.mig_bibliome.stane.client.Config.StaneClientExtGinInjector;
import fr.inra.mig_bibliome.stane.client.Document.Blinker;
import fr.inra.mig_bibliome.stane.client.Document.ToolBarExpandCollapseHandler;
import fr.inra.mig_bibliome.stane.client.Events.Extension.TermAnnotationsExpositionEvent;
import fr.inra.mig_bibliome.stane.client.Events.Extension.TermAnnotationsExpositionEventHandler;
import fr.inra.mig_bibliome.stane.client.SemClass.CellFactory.SemClassNodeCell;
import fr.inra.mig_bibliome.stane.client.SemClass.SmallDialogs.SmallDialogCell;
import fr.inra.mig_bibliome.stane.client.SemClass.SmallDialogs.SmallDialogInfo;
import fr.inra.mig_bibliome.stane.client.StructTermResources;
import fr.inra.mig_bibliome.stane.client.data.Retrieve.AsyncResponseHandler;
import fr.inra.mig_bibliome.stane.client.data.Retrieve.DetailedAsyncResponseHandler;
import fr.inra.mig_bibliome.stane.client.data.Retrieve.NetworkActivityDisplayer;
import fr.inra.mig_bibliome.stane.client.data.Retrieve.RequestManager;
import fr.inra.mig_bibliome.stane.client.data3.*;
import fr.inra.mig_bibliome.stane.shared.data3.Extension.TermAnnotation;
import fr.inra.mig_bibliome.stane.shared.data3.Extension.TermAnnotation.ResourceLocator;
import fr.inra.mig_bibliome.stane.shared.data3.Queries.AuthenticationInfo;
import fr.inra.mig_bibliome.stane.shared.data3.SemClass;
import gwtquery.plugins.draggable.client.events.DragStartEvent;
import gwtquery.plugins.draggable.client.events.DragStartEvent.DragStartEventHandler;
import gwtquery.plugins.draggable.client.events.DragStopEvent;
import gwtquery.plugins.draggable.client.events.DragStopEvent.DragStopEventHandler;
import gwtquery.plugins.droppable.client.DroppableOptions;
import gwtquery.plugins.droppable.client.DroppableOptions.DroppableFunction;
import gwtquery.plugins.droppable.client.events.DragAndDropContext;
import gwtquery.plugins.droppable.client.events.DropEvent;
import gwtquery.plugins.droppable.client.events.DropEvent.DropEventHandler;
import gwtquery.plugins.droppable.client.events.OutDroppableEvent;
import gwtquery.plugins.droppable.client.events.OutDroppableEvent.OutDroppableEventHandler;
import gwtquery.plugins.droppable.client.events.OverDroppableEvent;
import gwtquery.plugins.droppable.client.events.OverDroppableEvent.OverDroppableEventHandler;
import gwtquery.plugins.droppable.client.gwt.DragAndDropCellTree;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author fpapazian
 */
public class StructTermUi extends Composite implements TermAnnotationsExpositionEventHandler {

    private static final Logger log = Logger.getLogger(StructTermUi.class.getName());
    private static final StaneClientExtGinInjector termInjector = GWT.create(StaneClientExtGinInjector.class);
    private static final StaneClientBaseGinInjector injector = GWT.create(StaneClientBaseGinInjector.class);

    public static interface SemClassTreeDnDManager {

        public void expandTo(int HyperClassId, int semClassId);

        public DroppableFunction getSemClassDropHandler();

        public String getProjectName();
    }

    //class used to recursively visit nodes of a tree 
    static class TreeNodeTraveller {

        static interface NodeCondition {

            boolean evaluateAndContinue(TreeNode parentNode, int childNodeIndex);
        }

        void travelOpenNodes(TreeNode parentNode, NodeCondition nodeCondition) {
            for (int i = 0; i < parentNode.getChildCount(); i++) {
                boolean goDeeper = false;
                try {
                    goDeeper = nodeCondition.evaluateAndContinue(parentNode, i);
                } catch (Exception e) {
                    ;
                }
                if (!goDeeper) {
                    break;
                } else {
                    if (parentNode.isChildOpen(i)) {
                        TreeNode node = parentNode.setChildOpen(i, true);
                        travelOpenNodes(node, nodeCondition);
                    }
                }
            }
        }
    }
    private static TreeNodeTraveller nodeTraveller = new TreeNodeTraveller();

    /**
     * Used to open a CellTree which are using AsynchDataProvider : this
     * expander is able to wait for children info to be retrieved from remote
     * server before continuing to expand deeper. (Hence its iterative
     * implementation)
     */
    static abstract class TreeExpander {

        private final SingleSelectionModel<SemClassInfo> selectionModel;
        private boolean deepening;
        private ArrayList<SemClassImpl> levels;
        private TreeNode parentNode;
        private int childNodeIndex;
        private int levelDepth;

        public TreeExpander(SingleSelectionModel<SemClassInfo> selectionModel) {
            this.selectionModel = selectionModel;
        }

        private void openLevel() {
            Scheduler.get().scheduleFixedDelay(
                    new Scheduler.RepeatingCommand() {

                        @Override
                        public boolean execute() {

                            boolean result = false;
                            main:
                            {
                                if (levelDepth > levels.size()) {
                                    result = false;
                                    break main;
                                }

                                if (levelDepth == levels.size()) {
                                    //select final node
                                    SemClassExtendedInfo nodeInfo = (SemClassExtendedInfo) parentNode.getChildValue(childNodeIndex);
                                    selectionModel.setSelected(nodeInfo, true);
                                    result = false;
                                    break main;
                                }

                                //Check if AsynchDataprovider finished to generate the child info
                                TreeNode node = parentNode.setChildOpen(childNodeIndex, true);
                                if (node.getChildCount() == 0) {
                                    //need to wait a little more
                                    result = true;
                                    break main;
                                }

                                SemClassImpl level = levels.get(levelDepth);
                                for (int i = 0; i < node.getChildCount(); i++) {
                                    SemClassExtendedInfo childInfo = (SemClassExtendedInfo) node.getChildValue(i);
                                    if (childInfo.getId() == level.getId()) {
                                        scrollIntoView(childInfo);
                                        levelDepth++;
                                        parentNode = node;
                                        childNodeIndex = i;
                                        result = true;
                                        break main;
                                    }
                                }
                            }

                            deepening = (result != false);
                            if (!deepening) {
                                onFinish();
                            }

                            return result;
                        }
                    }, 100);

        }

        private void scrollIntoView(SemClassExtendedInfo info) {
            Element elt = Document.get().getElementById(info.getExtendedSemClassId());
            if (elt != null) {
                elt.scrollIntoView();
            }
        }

        public void start(TreeNode parentNode, ArrayList<SemClassImpl> levels) {
            deepening = true;
            this.parentNode = parentNode;
            this.levels = levels;
            childNodeIndex = 0;
            levelDepth = 0;
            onStart();
            openLevel();
        }

        abstract void onStart();

        abstract void onFinish();
    }

    // =========================================================================
    interface StructTermUiBinder extends UiBinder<Widget, StructTermUi> {
    }
    private static StructTermUiBinder uiBinder = GWT.create(StructTermUiBinder.class);
    //

    interface TreeResources extends CellTree.Resources {

        @Source("SemClassTree.css")
        CellTree.Style cellTreeStyle();
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    interface Styles extends CssResource {

        String SmallerTreeItem();

        String BackGroundPos();

        String ForeGroundPos();

        String MessageOutdated();

        String ScrollAreaHovered();
    }

    //Handle the attachement of the dragged semantic class to the hyperonym which is the drop target and refresh UI accordingly
    private class SemClassTreeDnDHandler implements SemClassTreeDnDManager, DroppableOptions.DroppableFunction, NativePreviewHandler, DragStartEventHandler, DragStopEventHandler, OverDroppableEventHandler, OutDroppableEventHandler, DropEventHandler {

        private boolean isShiftKeyDown = false;
        private boolean isDragging = false;
        private DragAndDropCellTree classesTree;
        private ArrayList<HandlerRegistration> handlerRegistrations = new ArrayList<HandlerRegistration>();
        private ScrollerMouseHandler upScrollAreaMouseHandler;
        private ScrollerMouseHandler downScrollAreaMouseHandler;
        private ScrollPanel scrollPanel;

        private class ScrollerMouseHandler extends HandlesAllMouseEvents {

            private HTML scrollArea;
            private ArrayList<HandlerRegistration> handlerRegistrations = new ArrayList<HandlerRegistration>();
            private Scheduler.RepeatingCommand command;
            private Timer timer = null;

            ScrollerMouseHandler(HTML scrollArea, final Scheduler.RepeatingCommand command) {
                this.scrollArea = scrollArea;
                this.command = command;
                handlerRegistrations.add(scrollArea.addMouseOutHandler(this));
                handlerRegistrations.add(scrollArea.addMouseOverHandler(this));
                handlerRegistrations.add(scrollArea.addMouseMoveHandler(this));
                timer = new Timer() {

                    @Override
                    public void run() {
                        if (!ScrollerMouseHandler.this.command.execute()) {
                            cancel();
                        }
                    }
                };
            }

            @Override
            public void onMouseDown(MouseDownEvent event) {
            }

            @Override
            public void onMouseUp(MouseUpEvent event) {
            }

            @Override
            public void onMouseMove(MouseMoveEvent event) {
                timer.cancel();
                if (classesTree != null && isDragging) {
                    if (command != null) {
                        timer.scheduleRepeating(250);
                    }
                }
            }

            @Override
            public void onMouseOut(MouseOutEvent event) {
                timer.cancel();
                if (isDragging) {
                    scrollArea.removeStyleName(style.ScrollAreaHovered());
                }
            }

            @Override
            public void onMouseOver(MouseOverEvent event) {
                if (isDragging) {
                    scrollArea.addStyleName(style.ScrollAreaHovered());
                }
            }

            @Override
            public void onMouseWheel(MouseWheelEvent event) {
            }

            void clear() {
                for (HandlerRegistration hr : handlerRegistrations) {
                    hr.removeHandler();
                }
                handlerRegistrations.clear();
                scrollArea = null;
                command = null;
                timer = null;

            }
        }

        //keep track of shift key status
        @Override
        public void onPreviewNativeEvent(NativePreviewEvent event) {
            isShiftKeyDown = event.getNativeEvent().getShiftKey();
        }

        @Override
        public void f(DragAndDropContext context) {
            if (context.getDraggableData() instanceof SemClassExtendedInfo) {
                //DnD of a semantic class => change hyperonym
                SemClassExtendedInfo droppedSemClass = context.getDroppableData();
                SemClassExtendedInfo draggedSemClass = context.getDraggableData();
                performHyperonymChange(draggedSemClass, droppedSemClass, isShiftKeyDown);
            } else if (context.getDraggableData() instanceof TermAnnotation) {
                //DnD of a term => create term as a synonym in a new or a prexisting semantic class
                SemClassExtendedInfo droppedSemClass = context.getDroppableData();
                TermAnnotation termAnnot = context.getDraggableData();
                performTermCreation(droppedSemClass, termAnnot);
            }
        }

        @Override
        public void expandTo(int HyperClassId, int semClassId) {
            expandAndSelect(HyperClassId, semClassId);
        }

        @Override
        public DroppableFunction getSemClassDropHandler() {
            return this;
        }

        @Override
        public void onDragStart(DragStartEvent event) {
            isDragging = true;
        }

        @Override
        public void onDragStop(DragStopEvent event) {
            isDragging = false;
        }

        @Override
        public void onOverDroppable(OverDroppableEvent event) {
            if (event.getDraggableData() instanceof SemClassExtendedInfo) {
                //highlight drop target
                Element droppable = event.getDragDropContext().getDroppable();
                if (droppable != null) {
                    SemClassExtendedInfo droppedSemClass = event.getDroppableData();
                    SemClassExtendedInfo draggedSemClass = event.getDraggableData();
                    boolean validTarget = isValidTarget(draggedSemClass, droppedSemClass);
                    if (validTarget) {
                        event.getDragDropContext().getDroppable().addClassName(StructTermResources.INSTANCE.css().DroppableHover());
                    } else {
                        event.getDragDropContext().getDroppable().addClassName(StructTermResources.INSTANCE.css().UnDroppableHover());
                    }
                }
            } else if (event.getDraggableData() instanceof TermAnnotation) {
                Element droppable = event.getDragDropContext().getDroppable();
                if (droppable != null) {
                    event.getDragDropContext().getDroppable().addClassName(StructTermResources.INSTANCE.css().TermDroppableHover());
                }
            }
        }

        @Override
        public void onOutDroppable(OutDroppableEvent event) {
            removeDropTargetHighlighting(event.getDragDropContext());
        }

        @Override
        public void onDrop(DropEvent event) {
            removeDropTargetHighlighting(event.getDragDropContext());
        }

        private void removeDropTargetHighlighting(DragAndDropContext dragDropContext) {
            Element droppable = dragDropContext.getDroppable();
            if (droppable != null) {
                dragDropContext.getDroppable().removeClassName(StructTermResources.INSTANCE.css().DroppableHover());
                dragDropContext.getDroppable().removeClassName(StructTermResources.INSTANCE.css().UnDroppableHover());
                dragDropContext.getDroppable().removeClassName(StructTermResources.INSTANCE.css().TermDroppableHover());
            }
        }

        private void setClassTreeWidgets(DragAndDropCellTree classesTree, ScrollPanel scrollPanel, HTML upScrollArea, HTML downScrollArea) {
            unsetClassTreeWidgets();

            if (classesTree != null) {
                handlerRegistrations.add(classesTree.addDragStartHandler(this));
                handlerRegistrations.add(classesTree.addDragStopHandler(this));
                handlerRegistrations.add(classesTree.addOverDroppableHandler(this));
                handlerRegistrations.add(classesTree.addOutDroppableHandler(this));
                handlerRegistrations.add(classesTree.addDropHandler(this));
            }
            this.classesTree = classesTree;
            this.scrollPanel = scrollPanel;

            //FIXME test revealed that DragNDrop is disturbed by when scrolling is performed this way.
            //And since autoscrolling is not working either, we need to find a workaround....
            upScrollAreaMouseHandler = new ScrollerMouseHandler(upScrollArea, new Scheduler.RepeatingCommand() {

                @Override
                public boolean execute() {
                    boolean again = true;
                    ScrollPanel scrollP = SemClassTreeDnDHandler.this.scrollPanel;
                    int currPos = scrollP.getVerticalScrollPosition();
                    int minPos = scrollP.getMinimumVerticalScrollPosition();
                    if (currPos > minPos) {
                        currPos -= 16;
                        if (currPos < minPos) {
                            currPos = minPos;
                            again = false;
                        }
                    }
                    scrollP.setVerticalScrollPosition(currPos);
                    return again;
                }
            });

            downScrollAreaMouseHandler = new ScrollerMouseHandler(downScrollArea, new Scheduler.RepeatingCommand() {

                @Override
                public boolean execute() {
                    boolean again = true;
                    ScrollPanel scrollP = SemClassTreeDnDHandler.this.scrollPanel;
                    int currPos = scrollP.getVerticalScrollPosition();
                    int minPos = scrollP.getMaximumVerticalScrollPosition();
                    if (currPos < minPos) {
                        currPos += 16;
                        if (currPos > minPos) {
                            currPos = minPos;
                            again = false;
                        }
                    }
                    scrollP.setVerticalScrollPosition(currPos);
                    return again;
                }
            });
        }

        private void unsetClassTreeWidgets() {
            for (HandlerRegistration hr : handlerRegistrations) {
                hr.removeHandler();
            }
            handlerRegistrations.clear();
            if (upScrollAreaMouseHandler != null) {
                upScrollAreaMouseHandler.clear();
            }
            if (downScrollAreaMouseHandler != null) {
                downScrollAreaMouseHandler.clear();
            }
        }

        @Override
        public String getProjectName() {
            return projectName;
        }
    };
    //
    // --
    @UiField
    Styles style;
    // --
    @UiField
    TextBox tydiwsurl;
    @UiField
    TextBox tydiProjectId;
    @UiField
    TextBox loginName;
    @UiField
    PasswordTextBox password;
    @UiField
    Button authenticateButton;
    @UiField
    Panel messagePanel;
    @UiField
    Image warningImage;
    @UiField
    Label messageText;
    // --
    @UiField
    TextBox searchPattern;
    @UiField
    PushButton searchButton;
    @UiField
    Image searchResult;
    @UiField
    PushButton refreshButton;
    // --
    @UiField
    NetworkActivityDisplayer networkActivityDisplayer;
    // --
    @UiField
    Panel disabledPanel;
    @UiField
    Panel globalClassesPanel;
    @UiField
    Label tydiTitle;
    @UiField
    LayoutPanel mainLayoutPanel;
    @UiField
    Panel authenticationPanel;
    @UiField
    Panel glassPanel;
    @UiField
    Panel detailGlassPanel;
    @UiField
    Panel detailPanel;
    @UiField
    Panel toolBar;
    @UiField
    Panel mainPanel;
    @UiField
    Image expandCollapseImg;
    @UiField
    ScrollPanel treePanel;
    @UiField
    ScrollPanel classPanel;
    @UiField
    Image errorImage;
    @UiField
    Label errorMessage;
    @UiField
    HTML upScrollArea;
    @UiField
    HTML downScrollArea;
    @UiField
    Panel dialogPanel;
    @UiField
    HTMLPanel expandPanel;
    @UiField
    FocusPanel expandBtn;
    // --
    private final ToolBarExpandCollapseHandler toolBarExpandCollapseHandler;
    private final CellListPanel<SemClassInfo> searchResultPanel;
    private final SingleSelectionModel<SemClassInfo> selectionModel = new SingleSelectionModel<SemClassInfo>();
    private DragAndDropCellTree classesTree = null;
    private ClassDetailTreeViewModel classDetailTreeModel = null;
    private CellTree classDetailTreeView = null;
    private Panel[] globalExclusivePanels;
    private Panel[] detailExclusivePanels;
    private Integer projectId = null;
    private String projectName = null;
    private final String dragContainerId;
    private boolean errorBeingDisplayed = false;
    private final CellListPanel<SmallDialogInfo> smallDialogsPanel;
    private final SmallDialogCell smallDialogsCell;
    private final ValueUpdater<SmallDialogInfo> smallDialogsUpdater;
    private final ArrayList<SmallDialogInfo> smallDislogsList;
    //
    private SemClassTreeDnDHandler semClassTreeDnDHandler = new SemClassTreeDnDHandler();

    public StructTermUi() {

        initWidget(uiBinder.createAndBindUi(this));

        dragContainerId = HTMLPanel.createUniqueId();
        treePanel.getElement().setId(dragContainerId);


        //set-up popup to display result of pattern search
        searchResultPanel = new CellListPanel<SemClassInfo>(new CellListPanel.TriggerHandler() {

            @Override
            public void setEnabled(boolean enabled) {
                searchResult.setVisible(enabled);
            }

            @Override
            public int getPopupLeft() {
                return searchResult.getAbsoluteLeft();
            }

            @Override
            public int getPopupTop() {
                return searchResult.getAbsoluteTop() + searchResult.getHeight();
            }

            @Override
            public HandlerRegistration addClickHandler(ClickHandler handler) {
                return searchResult.addClickHandler(handler);
            }

            @Override
            public void fireEvent(GwtEvent<?> event) {
                searchResult.fireEvent(event);
            }
        }, new SemClassNodeCell(), null);

        searchResultPanel.getSelectionModel().addSelectionChangeHandler(new Handler() {

            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                resetTreeErrorMessage();
                if (searchResultPanel.getSelectionModel().getSelectedObject() instanceof SemClassInfo) {
                    //a new selection amongst the search result
                    SemClassInfo selectedResult = (SemClassInfo) searchResultPanel.getSelectionModel().getSelectedObject();
                    expandAndSelect(selectedResult);
                    searchResultPanel.getSelectionModel().setSelected(selectedResult, false);
                }
                searchResultPanel.hide();
            }
        });


        //handle selection within the Semantic Classes treeview
        selectionModel.addSelectionChangeHandler(
                new Handler() {

                    @Override
                    public void onSelectionChange(SelectionChangeEvent event) {
                        resetTreeErrorMessage();
                        if (selectionModel.getSelectedObject() instanceof SemClassInfo) {

                            displayClassDetails((SemClassInfo) selectionModel.getSelectedObject());
                        }
                    }
                });


        smallDialogsCell = new SmallDialogs.SmallDialogCell();
        //
        smallDialogsUpdater = new SmallDialogsUpdater();
        smallDislogsList = new ArrayList<SmallDialogs.SmallDialogInfo>();

        smallDialogsPanel = new CellListPanel<SmallDialogInfo>(new CellListPanel.Trigger() {

            @Override
            public void setEnabled(boolean enabled) {
                dialogPanel.setVisible(enabled);
                expandPanel.setVisible(enabled);
            }

            @Override
            public int getPopupLeft() {
                return dialogPanel.getAbsoluteLeft();
            }

            @Override
            public int getPopupTop() {
                return dialogPanel.getAbsoluteTop();
            }

            @Override
            public HandlerRegistration addClickHandler(ClickHandler handler) {
                return expandBtn.addClickHandler(handler);
            }

            @Override
            public void fireEvent(GwtEvent<?> event) {
                expandBtn.fireEvent(event);
            }

            @Override
            public String getPreferredWidth() {
                return dialogPanel.getOffsetWidth() + "px";
            }

            @Override
            public void onBeforeShowPopUp() {
                dialogPanel.clear();
            }

            @Override
            public void onAfterHidePopUp() {
                displayPreviewSmallDialogs(smallDislogsList);
            }
        }, smallDialogsCell, smallDialogsUpdater);


        //set-up the widget displaying network activity
        networkActivityDisplayer.setRequestManager(termInjector.getTermDataProvider().getRequestManager());

        //trigger search when pressing Enter in the search pattern TextBox
        searchPattern.addKeyPressHandler(new KeyPressHandler() {

            @Override
            public void onKeyPress(KeyPressEvent event) {
                if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
                    performSearch();
                }
            }
        });

        //trigger authentication when pressing Enter in the password TextBox
        password.addKeyPressHandler(new KeyPressHandler() {

            @Override
            public void onKeyPress(KeyPressEvent event) {
                if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
                    doAuthentication(false);
                }
            }
        });

        globalExclusivePanels = new Panel[]{globalClassesPanel, authenticationPanel, disabledPanel};
        detailExclusivePanels = new Panel[]{detailGlassPanel, detailPanel};

        showInForeGround(globalExclusivePanels, disabledPanel);

        //FIXME compute toolbar top and height at runtime
        toolBarExpandCollapseHandler = new ToolBarExpandCollapseHandler(expandCollapseImg, mainLayoutPanel, toolBar, 0, 25, mainPanel);
        expandCollapseImg.addClickHandler(toolBarExpandCollapseHandler);

        //Key handler to track shift key status when the drop occurs
        Event.addNativePreviewHandler(semClassTreeDnDHandler);
    }
    // -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-

    private void setForegroundPanel(Panel frontPanel, boolean isForeground) {
        if (isForeground) {
            frontPanel.removeStyleName(style.BackGroundPos());
            frontPanel.addStyleName(style.ForeGroundPos());
        } else {
            frontPanel.addStyleName(style.BackGroundPos());
            frontPanel.removeStyleName(style.ForeGroundPos());
        }
    }

    //put the specified panel in the foreground and the others panel in background 
    private void showInForeGround(Panel[] exclusivePanels, Panel frontPanel) {
        for (Panel p : exclusivePanels) {
            setForegroundPanel(p, frontPanel.equals(p));
        }
    }

    //enable/disable the glass panel which is global to the Structured Terminology widget
    private void showGlassPanel(boolean visible) {
        glassPanel.setVisible(visible);
        setForegroundPanel(glassPanel, visible);
    }

    //remove previous tree and detail display 
    private void resetDisplayers() {
        if (classesTree != null) {
            treePanel.remove(classesTree);
            classesTree = null;
            semClassTreeDnDHandler.unsetClassTreeWidgets();
        }
        if (classDetailTreeView != null) {
            classPanel.remove(classDetailTreeView);
            classDetailTreeView = null;
            classDetailTreeModel = null;
        }

        searchResultPanel.reset();
        searchPattern.setText("");
    }

    private void initDisplayers() {
        CellTree.Resources resource = GWT.create(TreeResources.class);
        SemClassTreeViewModel classTreeModel = new SemClassTreeViewModel(selectionModel, getProjectId(), getDragContainerId(), semClassTreeDnDHandler);
        classesTree = new DragAndDropCellTree(classTreeModel, null, resource);

        treePanel.add(classesTree);
        classesTree.setAnimationEnabled(false);
        semClassTreeDnDHandler.setClassTreeWidgets(classesTree, treePanel, upScrollArea, downScrollArea);

        classDetailTreeModel = new ClassDetailTreeViewModel(new SingleSelectionModel<TermInfo>(), getProjectId());
        classDetailTreeView = new CellTree(classDetailTreeModel, null, resource);
        classPanel.add(classDetailTreeView);
        classDetailTreeView.setAnimationEnabled(false);
    }

    public void resetTyDIParameters(ResourceLocator locator) {
        if (this.projectId != null) {
            ProviderStore.forProject(projectId).clear();
        }

        resetDisplayers();

        this.projectId = locator != null ? locator.getProjectId() : null;
        if (projectId != null) {
            RequestManager requestMgr = termInjector.getTermDataProvider().getRequestManager();
            requestMgr.setServerBaseUrl(locator.getResourceUrl());
            tydiwsurl.setText(termInjector.getTermDataProvider().getRequestManager().getServerBaseUrl());
            tydiProjectId.setText(String.valueOf(projectId));

            tydiTitle.setText(locator.getResourceUrl() + "    [" + projectId + "]");
            loginName.setText(requestMgr.getApplicationOptions().getLastLoginName());

            startAuthentication();
        } else {
            showInForeGround(globalExclusivePanels, authenticationPanel);
        }
    }

    private int getProjectId() {
        return projectId;
    }

    private String getDragContainerId() {
        return dragContainerId;
    }

    // -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
    private void startAuthentication() {
        showInForeGround(globalExclusivePanels, authenticationPanel);

        if (loginName.getText().trim().isEmpty()) {
            loginName.setFocus(true);
        } else if (password.getText().trim().isEmpty()) {
            password.setFocus(true);
        } else {
            authenticateButton.setFocus(true);
        }

        //perform unattended authentication if possible
        if (termInjector.getTermDataProvider().getRequestManager().canPerformReSignIn()) {
            doAuthentication(true);
        }
    }

    private void doAuthentication(boolean reSignIn) {
        //
        showInForeGround(globalExclusivePanels, authenticationPanel);
        messagePanel.setVisible(false);
        showGlassPanel(true);
        AsyncResponseHandler<AuthenticationInfo> responseHandler = new AsyncResponseHandler<AuthenticationInfo>() {

            @Override
            public void onSuccess(AuthenticationInfo result) {

                termInjector.getTermDataProvider().getUserProjects(result.getId(), new AsyncResponseHandler<ProjectInfoListImpl>() {

                    @Override
                    public void onSuccess(ProjectInfoListImpl result) {
                        showGlassPanel(false);


                        boolean authorized = false;
                        for (int i = 0; i < result.length(); i++) {
                            if (result.get(i).getProjectId() == getProjectId()) {
                                authorized = true;

                                projectName = result.get(i).getName();
                                tydiTitle.setText(tydiTitle.getText());
                                break;
                            }
                        }

                        if (authorized) {
                            showInForeGround(globalExclusivePanels, globalClassesPanel);

                            initDisplayers();

                        } else {
                            //FIXME not I18N
                            showInForeGround(globalExclusivePanels, authenticationPanel);
                            displayGlobalWarning("User not authorized for this project!!!");
                        }
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        displayGlobalWarning(caught.getMessage());
                    }
                });


            }

            @Override
            public void onFailure(Throwable caught) {
                displayGlobalWarning(caught.getMessage());
            }
        };
        if (reSignIn) {
            termInjector.getTermDataProvider().getRequestManager().reSignIn(responseHandler);
        } else {
            termInjector.getTermDataProvider().getRequestManager().signIn(loginName.getText(), password.getText(), responseHandler);
        }

    }

    // -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
    private void displayClassDetails(SemClassInfo semClassInfo) {
        /*
         * if (classDetailTreeView.getRootTreeNode() != null &&
         * classDetailTreeView.getRootTreeNode().getChildCount() > 0 &&
         * classDetailTreeView.getRootTreeNode().isChildOpen(0)) {
         * classDetailTreeView.getRootTreeNode().setChildOpen(0, false); }
         */
        showInForeGround(detailExclusivePanels, detailGlassPanel);
        classDetailTreeModel.setSemanticClass(semClassInfo);
        showInForeGround(detailExclusivePanels, detailPanel);
        if (semClassInfo != null && !semClassInfo.isRoot()) {
            classDetailTreeView.getRootTreeNode().setChildOpen(0, true);
        }
    }

    // -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
    //search Semantic classes whose term matches the specified search pattern
    private void performSearch() {
        searchPattern.setReadOnly(true);
        searchButton.setEnabled(false);

        termInjector.getTermDataProvider().searchSemanticClassesByPattern(getProjectId(), searchPattern.getText().trim(), new AsyncResponseHandler<SemClassListImpl>() {

            @Override
            public void onSuccess(SemClassListImpl result) {
                ArrayList<SemClassInfo> rlist = new ArrayList<SemClassInfo>();
                for (int i = 0; i < result.length(); i++) {
                    SemClassImpl t = result.get(i);
                    ProviderStore.forProject(projectId).cacheSemClass(t);
                    rlist.add(new SemClassInfo(projectId, t.getId()));
                }
                searchPattern.setReadOnly(false);
                searchButton.setEnabled(true);
                searchResultPanel.prepareForDisplay(rlist);
            }

            @Override
            public void onFailure(Throwable caught) {
                super.onFailure(caught);
                searchPattern.setReadOnly(false);
                searchButton.setEnabled(true);
            }
        });
    }

    private void expandAndSelect(SemClassInfo info) {
        expandAndSelect(SemClass.ROOT_ID, info.getId());
    }

    private void expandAndSelect(final int HyperClassId, int semClassId) {

        termInjector.getTermDataProvider().getBranchesBetweenClasses(projectId, SemClass.ROOT_ID, semClassId, new AsyncResponseHandler<BranchesImpl>() {

            private Image expandingImg = new Image(StructTermResources.INSTANCE.SearchExpandTreeIcon());
            private Image normalImg = new Image(StructTermResources.INSTANCE.SearchTreeIcon());
            private TreeExpander expander = new TreeExpander(selectionModel) {

                @Override
                void onStart() {
                    searchButton.getUpFace().setImage(expandingImg);
                    showGlassPanel(true);
                }

                @Override
                void onFinish() {
                    searchButton.getUpFace().setImage(normalImg);
                    showGlassPanel(false);
                }
            };

            @Override
            public void onSuccess(BranchesImpl result) {
                TreeNode parentNode = classesTree.getRootTreeNode();
                ArrayList<SemClassImpl> levels = new ArrayList<SemClassImpl>();

                for (PathImpl path : new JsArrayDecorator<PathImpl>(result.getPaths())) {
                    JsArray<SemClassImpl> pl = path.getLevels();
                    //choose the path which pass through the specified Hyperonym
                    if (pl.get(pl.length() - 1).getId() == HyperClassId) {
                        levels.addAll(new JsArrayDecorator<SemClassImpl>(pl));
                        break;
                    }
                }

                if (levels.isEmpty()) {
                    levels.addAll(new JsArrayDecorator<SemClassImpl>(result.getPaths().get(0).getLevels()));
                }
                levels.add(result.getToSemClass());
                expander.start(parentNode, levels);
            }
        });

    }
    // -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-

    //Return false when the drop target is invalid, i.e. when the dropped class can not be an hyperonym of the gragged class
    private boolean isValidTarget(SemClassExtendedInfo draggedSemClass, SemClassExtendedInfo droppedSemClass) {
        int newHyperSemClassId = droppedSemClass.getId();
        if (newHyperSemClassId == draggedSemClass.getId()) {
            //the class has been droppped on (one of) the node representing itself
            return false;
        }

        JsArrayInteger hyperId = draggedSemClass.getFromCache().getHyperGroupIds();
        for (int i = 0; i < hyperId.length(); i++) {
            if (newHyperSemClassId == hyperId.get(i)) {
                //the class has been droppped on (one of) its current parent(s)
                return false;
            }
        }

        return true;
    }

    private void performHyperonymChange(SemClassExtendedInfo draggedSemClass, SemClassExtendedInfo droppedSemClass, boolean shiftKeyDown) {

        resetTreeErrorMessage();
        //the class has bee droppped on (one of) its current parent(s), no update necessary
        if (!isValidTarget(draggedSemClass, droppedSemClass)) {
            return;
        }

        showGlassPanel(true);

        final int semClassId = draggedSemClass.getId();
        final int semClassVersion = draggedSemClass.getFromCache().getVersion();
        final int realPrevHyperSemClassId = draggedSemClass.getParentClassId();

        final int newHyperSemClassId = droppedSemClass.getId();
        final int newHyperSemClassVersion = droppedSemClass.getFromCache().getVersion();

        final int paramPrevHyperSemClassId;
        final int paramPrevHyperSemClassVersion;
        if (shiftKeyDown) {
            //Shift key is pressed, then add a new Hyperonym to the class, by specifying ROOT_ID for previous Hyperonym parameter
            paramPrevHyperSemClassId = SemClass.ROOT_ID;
            paramPrevHyperSemClassVersion = 0;
        } else {
            paramPrevHyperSemClassId = realPrevHyperSemClassId;
            paramPrevHyperSemClassVersion = ProviderStore.forProject(draggedSemClass.getProjectId()).getCacheSemClassTreeLevel(realPrevHyperSemClassId).getVersion();
        }


        //Perform actual modification in the remote DB
        termInjector.getTermDataProvider().replaceHyperonym(projectId, semClassId, semClassVersion, paramPrevHyperSemClassId, paramPrevHyperSemClassVersion, newHyperSemClassId, newHyperSemClassVersion, new DetailedAsyncResponseHandler<SemClassTreeLevelImpl>() {

            @Override
            public void onSuccess(SemClassTreeLevelImpl result) {

                final SemClassInfo selected = selectionModel.getSelectedObject();
                if (selected != null) {
                    selectionModel.setSelected(selected, false);
                }

                //close the node of the old parent only if no more child after DnD
                nodeTraveller.travelOpenNodes(classesTree.getRootTreeNode(), new TreeNodeTraveller.NodeCondition() {

                    @Override
                    public boolean evaluateAndContinue(TreeNode parentNode, int childNodeIndex) {
                        boolean result;

                        SemClassExtendedInfo childInfo = (SemClassExtendedInfo) parentNode.getChildValue(childNodeIndex);
                        if (childInfo != null && childInfo.getId() == realPrevHyperSemClassId) {
                            //close the node of the semantic class if it will have no more child after update
                            SemClass semClass = childInfo.getFromCache();
                            if (semClass != null && semClass.getHypoGroupIds().length() == 1) {
                                parentNode.setChildOpen(childNodeIndex, false);
                            }
                            //since this is a DAG, no need to go deeper in this branch 
                            result = false;

                        } else {
                            result = true;
                        }
                        return result;
                    }
                });

                //need to reload grand parent classes to update leaf status on the parent
                SemClassDataProvider.reloadSemClassParents(projectId, realPrevHyperSemClassId);
                SemClassDataProvider.reloadSemClassParents(projectId, newHyperSemClassId);

                //need to reload parent classes to display child changes
                SemClassDataProvider.reloadSemClass(projectId, realPrevHyperSemClassId);
                SemClassDataProvider.reloadSemClass(projectId, newHyperSemClassId);

                //Need to refresh semantic classe to display hyperonym changes (number and navigate menu), when :
                // - explicit branching to a new Hyperonym by pressing the shift key,
                // - possible removal from one of its Hyperonym by dropping of Root node.
                // - any replacement of Hyperonym.
                SemClassDataProvider.reloadSemClassParents(projectId, semClassId);
                SemClassDataProvider.refreshSemClassNodes(projectId, semClassId);


                //when all the above queries will be answered....
                termInjector.getTermDataProvider().getRequestManager().addMilestone(new Command() {

                    @Override
                    public void execute() {
                        //FIXME : does not open previously closed node
                        nodeTraveller.travelOpenNodes(classesTree.getRootTreeNode(), new TreeNodeTraveller.NodeCondition() {

                            @Override
                            public boolean evaluateAndContinue(TreeNode parentNode, int childNodeIndex) {
                                boolean result;
                                SemClassExtendedInfo childInfo = (SemClassExtendedInfo) parentNode.getChildValue(childNodeIndex);

                                if (childInfo != null && childInfo.getId() == newHyperSemClassId) {

                                    //open the node of the new parent semantic class since it will have at least one child after update
                                    SemClass semClass = childInfo.getFromCache();
                                    if (semClass != null && semClass.getHypoGroupIds().length() == 1) {
                                        parentNode.setChildOpen(childNodeIndex, true);
                                    }

                                    //since this is a DAG, no need to go deeper in this branch 
                                    result = false;

                                } else {
                                    result = true;
                                }
                                return result;
                            }
                        });


                        //FIXME restore selection to new location

                        // finally remove glaspanel
                        showGlassPanel(false);
                    }
                });
            }

            @Override
            public void onFailure(Response response) {
                int statusCode = response.getStatusCode();
                String responseText = response.getText();
                displayTreeErrorMessage(statusCode, responseText);
                showGlassPanel(false);
            }
        });

    }

    // -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
    //Message template for the user after a Term creation could not be performed 
    static interface TermExistsResponseTemplates extends SafeHtmlTemplates {

        @SafeHtmlTemplates.Template("The term '{0}' already exists, but does not belong to any class")
        public SafeHtml alreadyExists(String termForm);

        @SafeHtmlTemplates.Template("The term '{0}' is already a synonym of an existing class ('{1}')")
        public SafeHtml alreadySynonym(String termForm, String otherClass);

        @SafeHtmlTemplates.Template("The term '{0}' is already a synonym of another class ('{1}')")
        public SafeHtml alreadySynonymOtherClass(String termForm, String otherClass);

        @SafeHtmlTemplates.Template("The term '{0}' is already a synonym of several other classes ({1})")
        public SafeHtml alreadySynonyms(String termForm, int nbOtherClasses);

        @SafeHtmlTemplates.Template("The term '{0}' is already a synonym of the targeted class ('{1}')")
        public SafeHtml alreadySynonymOfTargetClass(String termForm, String targetClass);

        @SafeHtmlTemplates.Template("The term '{0}' is already the representative of the targeted class")
        public SafeHtml alreadyRepresentativeOfTargetClass(String termForm);

        @SafeHtmlTemplates.Template("The term '{0}' is already the representative of a preexisting class")
        public SafeHtml alreadyRepresentativeOfPrexistingClass(String termForm);

        @SafeHtmlTemplates.Template("The term '{0}' is already the representative of another class")
        public SafeHtml alreadyRepresentativeOfAnotherClass(String termForm);

        @SafeHtmlTemplates.Template("or")
        public SafeHtml or();

        @SafeHtmlTemplates.Template("and")
        public SafeHtml and();

        @SafeHtmlTemplates.Template("<br>No modification has been performed")
        public SafeHtml noModifPerformed();

        @SafeHtmlTemplates.Template("<br>No modification could be performed")
        public SafeHtml noModifPerformable();

        @SafeHtmlTemplates.Template("Would you like to add it as synonym to the targeted class ('{0}') ?")
        public SafeHtml addSynonymToTargetClass(String targetClass);

        @SafeHtmlTemplates.Template("Would you like to create a new class with '{0}' as representative ?")
        public SafeHtml createClassforTerm(String targetClass);

        @SafeHtmlTemplates.Template("Would you like to merge '{0}' and '{1}' ?")
        public SafeHtml mergeClasses(String class1, String class2);
    }
    static final TermExistsResponseTemplates TEMPLATES = GWT.create(TermExistsResponseTemplates.class);

    //Generate messages and futher possible actions for the user depending on the server response to previous call
    private ArrayList<SmallDialogs.SmallDialogInfo> computeActionsForTermExistsResponse(TermExistsResponseImpl teResp, final TermAnnotation termAnnot, final Integer semClassId, final Integer semClassVersion, final Integer hyperId, final Integer hyperClassVersion) {
        ArrayList<SmallDialogInfo> result = new ArrayList<SmallDialogs.SmallDialogInfo>();

        String termForm = teResp.getSurfaceForm();
        boolean isClassRepresentative = teResp.isClassRepresentative();
        boolean isAddedToExistingClass = semClassId != null;

        if (isClassRepresentative) {
            final int otherRepresentedClassId = teResp.getRepresentativeOf();
            SemClass otherRepresentedClass = ProviderStore.forProject(projectId).getCacheSemClass(otherRepresentedClassId);
            String otherRepresentedClassLabel = otherRepresentedClass != null ? otherRepresentedClass.getCanonicLabel() : String.valueOf(teResp.getRepresentativeOf());

            if (isAddedToExistingClass) {
                String targetClass = ProviderStore.forProject(projectId).getCacheSemClass(semClassId).getCanonicLabel();
                boolean isRepresentativeOfTargetClass = semClassId.intValue() == teResp.getRepresentativeOf();

                if (isRepresentativeOfTargetClass) {
                    GWT.log("ActionsForTermExistsResp A");
                    result.add(
                            new SmallDialogInfo(
                            SmallDialogs.Type.Info,
                            TEMPLATES.alreadyRepresentativeOfTargetClass(termForm),
                            SmallDialogs.AllowedAction.None));
                    result.add(
                            new SmallDialogInfo(
                            SmallDialogs.Type.None,
                            TEMPLATES.noModifPerformable(),
                            SmallDialogs.AllowedAction.Cancel));

                } else {
                    GWT.log("ActionsForTermExistsResp B");

                    final int termId = teResp.getTermId();

                    result.add(
                            new SmallDialogInfo(
                            SmallDialogs.Type.Warning,
                            //otherRepresentedClassLabel
                            TEMPLATES.alreadyRepresentativeOfAnotherClass(termForm),
                            SmallDialogs.AllowedAction.None));

                    //check if it is also synonym of the currentclass
                    boolean synonymOfTargetClass = false;

                    for (int i = 0; i < teResp.getTermMemberships().length(); i++) {
                        TermMembershipImpl mbship = teResp.getTermMemberships().get(i);
                        if (mbship.getId() == semClassId) {
                            synonymOfTargetClass = true;
                            break;
                        }
                    }

                    if (synonymOfTargetClass) {
                        result.add(
                                new SmallDialogInfo(
                                SmallDialogs.Type.None,
                                TEMPLATES.and(),
                                SmallDialogs.AllowedAction.None));

                        result.add(
                                new SmallDialogInfo(
                                SmallDialogs.Type.Info,
                                TEMPLATES.alreadySynonymOfTargetClass(termForm, targetClass),
                                SmallDialogs.AllowedAction.None));

                    } else {
                        result.add(
                                new SmallDialogInfo(
                                SmallDialogs.Type.None,
                                TEMPLATES.addSynonymToTargetClass(targetClass),
                                SmallDialogs.AllowedAction.YesCancel, new Command() {

                            @Override
                            public void execute() {
                                //Force adding synonym to target class
                                addTermSynonym(projectId, termAnnot, termId, semClassId, semClassVersion, hyperId);

                            }
                        }));
                        result.add(
                                new SmallDialogInfo(
                                SmallDialogs.Type.None,
                                TEMPLATES.or(),
                                SmallDialogs.AllowedAction.None));
                    }

                    final int otherRepresentedClassVersion = otherRepresentedClass.getVersion();
                    result.add(
                            new SmallDialogInfo(
                            SmallDialogs.Type.None,
                            TEMPLATES.mergeClasses(targetClass, otherRepresentedClassLabel),
                            SmallDialogs.AllowedAction.YesCancel, new Command() {

                        @Override
                        public void execute() {
                            mergeClasses(termAnnot, semClassId, semClassVersion, otherRepresentedClassId, otherRepresentedClassVersion);
                        }
                    }));

                }
            } else {
                GWT.log("ActionsForTermExistsResp C");
                result.add(
                        new SmallDialogInfo(
                        SmallDialogs.Type.Warning,
                        //otherRepresentedClassLabel
                        TEMPLATES.alreadyRepresentativeOfPrexistingClass(termForm),
                        SmallDialogs.AllowedAction.None));
                result.add(
                        new SmallDialogInfo(
                        SmallDialogs.Type.None,
                        //otherRepresentedClassLabel
                        TEMPLATES.noModifPerformable(),
                        SmallDialogs.AllowedAction.Cancel));

            }
        } else {
            //not class representative

            int membershipsNb = teResp.getTermMemberships().length();

            if (membershipsNb == 0) {
                GWT.log("ActionsForTermExistsResp D");
                result.add(
                        new SmallDialogInfo(
                        SmallDialogs.Type.Info,
                        TEMPLATES.alreadyExists(termForm),
                        SmallDialogs.AllowedAction.Cancel));

                //Does not happen : modification is allowed is that case

            } else if (membershipsNb == 1) {
                //

                final int otherSynonymClassId = teResp.getTermMemberships().get(0).getId();
                final int otherSynonymClassVersion = teResp.getTermMemberships().get(0).getVersion();
                String otherSynonymClassLabel = teResp.getTermMemberships().get(0).getCanonicLabel();

                if (isAddedToExistingClass) {
                    String targetClass = ProviderStore.forProject(projectId).getCacheSemClass(semClassId).getCanonicLabel();

                    boolean isMemberOfTargetClass = semClassId.intValue() == otherSynonymClassId;
                    if (isMemberOfTargetClass) {
                        GWT.log("ActionsForTermExistsResp E");

                        result.add(
                                new SmallDialogInfo(
                                SmallDialogs.Type.Info,
                                TEMPLATES.alreadySynonymOfTargetClass(termForm, otherSynonymClassLabel),
                                SmallDialogs.AllowedAction.None));

                        result.add(
                                new SmallDialogInfo(
                                SmallDialogs.Type.None,
                                TEMPLATES.noModifPerformed(),
                                SmallDialogs.AllowedAction.Cancel));

                    } else {
                        GWT.log("ActionsForTermExistsResp F");
                        final int termId = teResp.getTermId();

                        result.add(
                                new SmallDialogInfo(
                                SmallDialogs.Type.Info,
                                TEMPLATES.alreadySynonymOtherClass(termForm, otherSynonymClassLabel),
                                SmallDialogs.AllowedAction.None));

                        result.add(
                                new SmallDialogInfo(
                                SmallDialogs.Type.None,
                                TEMPLATES.addSynonymToTargetClass(targetClass),
                                SmallDialogs.AllowedAction.YesCancel, new Command() {

                            @Override
                            public void execute() {
                                //Force adding synonym to target class
                                addTermSynonym(projectId, termAnnot, termId, semClassId, semClassVersion, hyperId);
                            }
                        }));

                        result.add(
                                new SmallDialogInfo(
                                SmallDialogs.Type.None,
                                TEMPLATES.or(),
                                SmallDialogs.AllowedAction.None));

                        result.add(
                                new SmallDialogInfo(
                                SmallDialogs.Type.None,
                                TEMPLATES.mergeClasses(targetClass, otherSynonymClassLabel),
                                SmallDialogs.AllowedAction.YesCancel, new Command() {

                            @Override
                            public void execute() {
                                mergeClasses(termAnnot, semClassId, semClassVersion, otherSynonymClassId, otherSynonymClassVersion);
                            }
                        }));
                    }

                } else {
                    GWT.log("ActionsForTermExistsResp G");

                    result.add(
                            new SmallDialogInfo(
                            SmallDialogs.Type.Info,
                            TEMPLATES.alreadySynonym(termForm, otherSynonymClassLabel),
                            SmallDialogs.AllowedAction.None));


                    result.add(
                            new SmallDialogInfo(
                            SmallDialogs.Type.None,
                            TEMPLATES.createClassforTerm(termForm),
                            SmallDialogs.AllowedAction.YesCancel, new Command() {

                        @Override
                        public void execute() {
                            createClassAndRepresentativeTerm(projectId, termAnnot, hyperId, hyperClassVersion, true);
                        }
                    }));
                }
            } else {
                result.add(
                        new SmallDialogInfo(
                        SmallDialogs.Type.Info,
                        TEMPLATES.alreadySynonyms(termForm, teResp.getTermMemberships().length()),
                        SmallDialogs.AllowedAction.None));

                if (isAddedToExistingClass) {
                    GWT.log("ActionsForTermExistsResp H");
                    String targetClass = ProviderStore.forProject(projectId).getCacheSemClass(semClassId).getCanonicLabel();

                    //check if it is also synonym of the currentclass
                    boolean synonymOfTargetClass = false;

                    for (int i = 0; i < teResp.getTermMemberships().length(); i++) {
                        TermMembershipImpl mbship = teResp.getTermMemberships().get(i);
                        if (mbship.getId() == semClassId) {
                            synonymOfTargetClass = true;
                            break;
                        }
                    }

                    if (synonymOfTargetClass) {

                        result.add(
                                new SmallDialogInfo(
                                SmallDialogs.Type.None,
                                TEMPLATES.and(),
                                SmallDialogs.AllowedAction.None));

                        result.add(
                                new SmallDialogInfo(
                                SmallDialogs.Type.Warning,
                                TEMPLATES.alreadySynonymOfTargetClass(termForm, targetClass),
                                SmallDialogs.AllowedAction.None));

                        result.add(
                                new SmallDialogInfo(
                                SmallDialogs.Type.None,
                                TEMPLATES.noModifPerformable(),
                                SmallDialogs.AllowedAction.Cancel));
                    } else {
                        final int termId = teResp.getTermId();

                        result.add(
                                new SmallDialogInfo(
                                SmallDialogs.Type.None,
                                TEMPLATES.addSynonymToTargetClass(targetClass),
                                SmallDialogs.AllowedAction.YesCancel, new Command() {

                            @Override
                            public void execute() {
                                //Force adding synonym to target class
                                addTermSynonym(projectId, termAnnot, termId, semClassId, semClassVersion, hyperId);
                            }
                        }));
                    }
                } else {
                    GWT.log("ActionsForTermExistsResp I");
                    result.add(
                            new SmallDialogInfo(
                            SmallDialogs.Type.None,
                            TEMPLATES.createClassforTerm(termForm),
                            SmallDialogs.AllowedAction.YesCancel, new Command() {

                        @Override
                        public void execute() {
                            createClassAndRepresentativeTerm(projectId, termAnnot, hyperId, hyperClassVersion, true);
                        }
                    }));
                }
            }
        }
        return result;
    }

    class SmallDialogsUpdater implements ValueUpdater<SmallDialogInfo> {

        @Override
        public void update(SmallDialogInfo value) {
            //An Action has been choosen by the user

            //clear the dialogs list
            smallDialogsPanel.reset();

            //perform the action choosen by the user in the small dialogs
            if (value != null) {
                if (SmallDialogs.Action.Apply.equals(value.getActionToBePerformed())) {
                    Command action = value.getApplyActionCommand();
                    if (action != null) {
                        Scheduler.get().scheduleDeferred(action);
                    }
                }
            }
        }
    };

    // -.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-
    private void mergeClasses(final TermAnnotation termAnnot, final int semClassId1, int semClassVersion1, final int semClassId2, int semClassVersion2) {
        termInjector.getTermDataProvider().mergeClasses(projectId, semClassId1, semClassVersion1, semClassId2, semClassVersion2, new DetailedAsyncResponseHandler<SemClassTreeLevelImpl>() {

            @Override
            public void onSuccess(SemClassTreeLevelImpl result) {
                ProviderStore.forProject(projectId).cacheSemClassTreeLevel(result);

                // finally remove glaspanel
                showGlassPanel(false);

                //Refresh class details 
                SemClassDataProvider.reloadSemClassParents(projectId, semClassId1);
                SemClassDataProvider.reloadSemClassParents(projectId, semClassId2);
                
                SemClassDataProvider.reloadSemClass(projectId, semClassId1);
                ClassDetailMembersDataProvider.reloadClassDetails(projectId, semClassId1);
                displayClassDetails(new SemClassExtendedInfo(projectId, result.getId(), semClassId1));
                
                //termAnnot.setSemClassExternalId
            }
            
            @Override
            public void onFailure(Response response) {
                int statusCode = response.getStatusCode();
                String responseText = response.getText();
                displayTreeErrorMessage(statusCode, responseText);
                showGlassPanel(false);
            }
        });
    }

    // -.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-
    private void createTermSynonym(final int projectId, final TermAnnotation termAnnot, final int semClassId, final int semClassVersion, final int parentClassId) {
        termInjector.getTermDataProvider().createTermSynonym(projectId, termAnnot.getSurfaceForm(), termAnnot.getLemma(), semClassId, semClassVersion, new DetailedAsyncResponseHandler<SemClassTreeLevelImpl>() {

            @Override
            public void onSuccess(SemClassTreeLevelImpl result) {
                ProviderStore.forProject(projectId).cacheSemClassTreeLevel(result);

                // finally remove glaspanel
                showGlassPanel(false);

                //Refresh class details 
                SemClassDataProvider.reloadSemClass(projectId, semClassId);
                ClassDetailMembersDataProvider.reloadClassDetails(projectId, semClassId);
                displayClassDetails(new SemClassExtendedInfo(projectId, result.getId(), semClassId));

                Integer addedTermId = result.getAddedTermId();
                termAnnot.setTermExternalId(termInjector.getTermDataProvider().getTermExternalId(projectId, addedTermId));
            }

            @Override
            public void onFailure(Response response) {
                int statusCode = response.getStatusCode();
                String responseText = response.getText();
                boolean isJsonResponse = response.getHeader("Content-Type").contains("application/json");
                if (statusCode == 422 && isJsonResponse) {
                    TermExistsResponseImpl teResp = TermExistsResponseImpl.createFromJSON(responseText);
                    List<SmallDialogs.SmallDialogInfo> msg = computeActionsForTermExistsResponse(teResp, termAnnot, semClassId, semClassVersion, parentClassId, null);
                    displaySmallDialogs(msg);
                } else {
                    displayTreeErrorMessage(statusCode, responseText);
                }
                showGlassPanel(false);
            }
        });
    }

    private void addTermSynonym(final Integer projectId, final TermAnnotation termAnnot, final int termId, final int semClassId, final int semClassVersion, final int parentClassId) {
        termInjector.getTermDataProvider().addSynonymTogetSemanticClass(projectId, termId, semClassId, semClassVersion, new AsyncResponseHandler<SemClassNTermsImpl>() {

            @Override
            public void onSuccess(SemClassNTermsImpl result) {
                ProviderStore.forProject(projectId).cacheSemClassNTerms(result);

                // finally remove glaspanel
                showGlassPanel(false);

                //Refresh class details 
                SemClassDataProvider.reloadSemClassParents(projectId, semClassId);
                ClassDetailMembersDataProvider.reloadClassDetails(projectId, semClassId);
                displayClassDetails(new SemClassExtendedInfo(projectId, semClassId, parentClassId));

                termAnnot.setTermExternalId(termInjector.getTermDataProvider().getTermExternalId(projectId, termId));

            }
        });
    }

    private void createClassAndRepresentativeTerm(final Integer projectId, final TermAnnotation termAnnot, final int newHyperSemClassId, final int newHyperSemClassVersion, boolean force) {

        termInjector.getTermDataProvider().createClassAndRepresentativeTerm(projectId, termAnnot.getSurfaceForm(), termAnnot.getLemma(), newHyperSemClassId, newHyperSemClassVersion, force, new DetailedAsyncResponseHandler<SemClassTreeLevelImpl>() {

            @Override
            public void onSuccess(SemClassTreeLevelImpl result) {
                ProviderStore.forProject(projectId).cacheSemClassTreeLevel(result);

                // finally remove glaspanel
                showGlassPanel(false);

                //Reload hyperonym class to display newly created class
                SemClassDataProvider.reloadSemClass(projectId, newHyperSemClassId);
                displayClassDetails(new SemClassExtendedInfo(projectId, newHyperSemClassId, newHyperSemClassVersion));

                Integer addedTermId = result.getAddedTermId();
                termAnnot.setTermExternalId(termInjector.getTermDataProvider().getTermExternalId(projectId, addedTermId));
            }

            @Override
            public void onFailure(Response response) {
                int statusCode = response.getStatusCode();
                String responseText = response.getText();

                boolean isJsonResponse = response.getHeader("Content-Type").contains("application/json");
                if (statusCode == 422 && isJsonResponse) {

                    TermExistsResponseImpl teResp = TermExistsResponseImpl.createFromJSON(responseText);
                    List<SmallDialogs.SmallDialogInfo> msg = computeActionsForTermExistsResponse(teResp, termAnnot, null, null, newHyperSemClassId, newHyperSemClassVersion);
                    displaySmallDialogs(msg);
                } else {
                    displayTreeErrorMessage(statusCode, responseText);
                }
                showGlassPanel(false);
            }
        });
    }

    private void performTermCreation(final SemClassExtendedInfo droppedSemClass, final TermAnnotation termAnnot) {
        //GWT.log("Term '" + termAnnot.getSurfaceForm() + "' dropped on " + droppedSemClass.getCanonicLabel());
        resetTreeErrorMessage();
        showGlassPanel(true);

        if (droppedSemClass.isRoot()) {
            //Creation of a new Term as Canonic representative of a new Semantic Class
            int hyperId = droppedSemClass.getId();
            int hyperVersion = droppedSemClass.getFromCache().getVersion();
            createClassAndRepresentativeTerm(projectId, termAnnot, hyperId, hyperVersion, false);

        } else {
            //Creation of a new Term as synonym of an existing Semantic Class
            int semClassId = droppedSemClass.getId();
            int semClassVersion = droppedSemClass.getFromCache().getVersion();
            createTermSynonym(projectId, termAnnot, semClassId, semClassVersion, droppedSemClass.getParentClassId());
        }

    }

    // -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
    private void displayGlobalWarning(String warningmsg) {
        showInForeGround(globalExclusivePanels, authenticationPanel);
        showGlassPanel(false);
        messageText.setText(warningmsg);
        messagePanel.setVisible(true);
        warningImage.setVisible(true);
        dialogPanel.setVisible(false);
        new Blinker(warningImage, new int[]{120, 40, 100, 40, 100, 1200, 10}).start();
    }
    // -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-

    private void resetTreeErrorMessage() {
        errorMessage.setText("");
        errorImage.setVisible(false);
        errorMessage.setVisible(false);
        errorMessage.removeStyleName(style.MessageOutdated());
        smallDialogsPanel.reset();
        dialogPanel.setVisible(false);
    }

    private void displayTreeErrorMessage(int statusCode, String responseText) {
        if (statusCode == Response.SC_CONFLICT) {
            responseText = "Your changes have been canceled : " + responseText + "\nPlease refresh the view and retry";
        }
        errorBeingDisplayed = true;
        errorMessage.setText(responseText);
        errorImage.setVisible(true);
        errorMessage.setVisible(true);
        errorMessage.removeStyleName(style.MessageOutdated());
        dialogPanel.setVisible(false);

        //first highlight the message to get user's attention 
        new Blinker(errorImage, new int[]{120, 40, 100, 40, 100, 1200, 10}).start(new Command() {

            @Override
            public void execute() {
                errorBeingDisplayed = false;
                //remove highlighting, but give a chance to read message again
                errorMessage.addStyleName(style.MessageOutdated());
                //hide the message after a few seconds
                Scheduler.get().scheduleFixedDelay(new Scheduler.RepeatingCommand() {

                    @Override
                    public boolean execute() {
                        if (!errorBeingDisplayed) {
                            errorMessage.setVisible(false);
                        }
                        return false;
                    }
                }, 4500);
            }
        });
    }

    private void displayPreviewSmallDialogs(List<SmallDialogInfo> msgList) {
        dialogPanel.clear();
        CellList<SmallDialogInfo> cellList = new CellList<SmallDialogInfo>(smallDialogsCell);
        cellList.setValueUpdater(smallDialogsUpdater);
        cellList.setRowCount(msgList.size(), true);
        cellList.setRowData(0, msgList);
        dialogPanel.add(cellList);
    }

    private void displaySmallDialogs(List<SmallDialogInfo> msgList) {
        errorImage.setVisible(false);
        errorMessage.setVisible(false);

        smallDislogsList.clear();
        if (!msgList.isEmpty()) {
            errorBeingDisplayed = true;
            smallDislogsList.addAll(msgList);
            dialogPanel.setVisible(true);
            smallDialogsPanel.reset();

            displayPreviewSmallDialogs(msgList);
            smallDialogsPanel.prepareForDisplay(msgList);
        } else {
            errorBeingDisplayed = false;
            dialogPanel.setVisible(false);
        }
    }

    // -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
    @UiHandler("authenticateButton")
    void handleAuthenticateButtonClick(ClickEvent e) {
        doAuthentication(false);
    }

    @UiHandler("signOutBtn")
    void handleSignOutButtonClick(ClickEvent e) {
        resetDisplayers();
        ProviderStore.clear();
        showInForeGround(globalExclusivePanels, authenticationPanel);
    }

    @UiHandler("searchButton")
    void handleSearchButtonClick(ClickEvent e) {
        performSearch();
    }

    @UiHandler("refreshButton")
    void handleRefreshButtonClick(ClickEvent e) {
        resetTreeErrorMessage();
        if (classesTree != null) {
            SemClassInfo selected = (SemClassInfo) selectionModel.getSelectedObject();
            //if no selection or root node selected, then perform global reset
            if (selected == null || selected.isRoot()) {
                resetDisplayers();
                ProviderStore.forProject(projectId).clear();
                initDisplayers();
            } else {
                //refresh only the selected class
                final int semClassId = selected.getId();

                //close the node of the refreshed semantic class 
                nodeTraveller.travelOpenNodes(classesTree.getRootTreeNode(), new TreeNodeTraveller.NodeCondition() {

                    @Override
                    public boolean evaluateAndContinue(TreeNode parentNode, int childNodeIndex) {
                        boolean result;

                        SemClassExtendedInfo childInfo = (SemClassExtendedInfo) parentNode.getChildValue(childNodeIndex);
                        if (childInfo != null && childInfo.getId() == semClassId) {
                            SemClass semClass = childInfo.getFromCache();
                            if (semClass != null) {
                                parentNode.setChildOpen(childNodeIndex, false);
                            }
                            result = false;

                        } else {
                            result = true;
                        }
                        return result;
                    }
                });

                SemClassDataProvider.reloadSemClassParents(projectId, semClassId);
            }
        }
    }

    @Override
    public void onTermAnnotationsExpositionChanged(TermAnnotationsExpositionEvent event) {
        if (TermAnnotationsExpositionEvent.ChangeType.Available.equals(event.getChangeType())) {
            resetTyDIParameters(event.getLocator());
        } else {
            resetTyDIParameters(null);
        }
    }

    @Override
    protected void onAttach() {
        super.onAttach();
        TermAnnotationsExpositionEvent.register(injector.getMainEventBus(), this);
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        resetDisplayers();
        TermAnnotationsExpositionEvent.unregister(this);
    }
}
