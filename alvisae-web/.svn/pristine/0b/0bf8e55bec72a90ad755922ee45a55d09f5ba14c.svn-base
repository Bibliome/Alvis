/*
 *
 *      This software is a result of Quaero project and its use must respect the rules of the Quaero Project Consortium Agreement.
 *
 *      Copyright Institut National de la Recherche Agronomique, 2010-2012.
 *
 */
package fr.inra.mig_bibliome.stane.client.Annotation;

import static com.google.gwt.query.client.GQuery.$;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.SafeHtmlHeader;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.*;
import fr.inra.mig_bibliome.stane.client.Config.StaneClientBaseGinInjector;
import fr.inra.mig_bibliome.stane.client.Document.AnnotationDocumentViewMapper;
import fr.inra.mig_bibliome.stane.client.Document.DocumentView;
import fr.inra.mig_bibliome.stane.client.Document.ToolBarExpandCollapseHandler;
import fr.inra.mig_bibliome.stane.client.Edit.AnnotationEdit;
import fr.inra.mig_bibliome.stane.client.Events.Extension.TermAnnotationsExpositionEvent;
import fr.inra.mig_bibliome.stane.client.Events.Selection.AnnotationSelections;
import fr.inra.mig_bibliome.stane.client.Events.Selection.GenericAnnotationSelection;
import fr.inra.mig_bibliome.stane.client.Events.Selection.GroupAnnotationSelection;
import fr.inra.mig_bibliome.stane.client.Events.Selection.RelationAnnotationSelection;
import fr.inra.mig_bibliome.stane.client.Events.Selection.TextAnnotationSelection;
import fr.inra.mig_bibliome.stane.client.Events.*;
import fr.inra.mig_bibliome.stane.client.StanEditorResources;
import fr.inra.mig_bibliome.stane.client.data3.AnnotatedTextHandler;
import fr.inra.mig_bibliome.stane.client.data3.AnnotatedTextProcessor;
import fr.inra.mig_bibliome.stane.client.data3.AnnotationSchemaDefHandler;
import fr.inra.mig_bibliome.stane.client.data3.TextBindingImpl;
import fr.inra.mig_bibliome.stane.shared.data3.Annotation;
import fr.inra.mig_bibliome.stane.shared.data3.AnnotationKind;
import fr.inra.mig_bibliome.stane.shared.data3.AnnotationSetInfo;
import fr.inra.mig_bibliome.stane.shared.data3.Extension.TermAnnotation;
import fr.inra.mig_bibliome.stane.shared.data3.Extension.TermAnnotation.ResourceLocator;
import gwtquery.plugins.draggable.client.DraggableOptions;
import gwtquery.plugins.draggable.client.DraggableOptions.CursorAt;
import gwtquery.plugins.draggable.client.DraggableOptions.DragFunction;
import gwtquery.plugins.draggable.client.DraggableOptions.RevertOption;
import gwtquery.plugins.draggable.client.events.DragContext;
import gwtquery.plugins.droppable.client.DroppableOptions;
import gwtquery.plugins.droppable.client.gwt.DragAndDropDataGrid;
import gwtquery.plugins.droppable.client.gwt.DragAndDropColumn;
import java.util.Map.Entry;
import java.util.*;

/**
 * Table displaying the annotations associated to the currently selected
 * document
 *
 * @author fpapazian
 */
public class AnnotationTable extends Composite implements WorkingDocumentChangedEventHandler, EditHappenedEventHandler, AnnotationSelectionChangedEventHandler {

    interface AnnotationTableUiBinder extends UiBinder<Widget, AnnotationTable> {
    }
    private static AnnotationTableUiBinder uiBinder = GWT.create(AnnotationTableUiBinder.class);
    //
    private static final StaneClientBaseGinInjector injector = GWT.create(StaneClientBaseGinInjector.class);
    //    

    static {
        StanEditorResources.INSTANCE.css().ensureInjected();
    }

    //Override some rules of the default table CSS
    interface TableStyle extends DragAndDropDataGrid.Style {
    }

    interface TableResources extends DragAndDropDataGrid.Resources {

        @Source({DragAndDropDataGrid.Style.DEFAULT_CSS, "AnnotationTable.css"})
        @Override
        TableStyle dataGridStyle();
    }
    TableResources resources = GWT.create(TableResources.class);

    //
    interface Styles extends CssResource {
    }
    @UiField(provided = true)
    DragAndDropDataGrid<Annotation> annotationsGrid;
    @UiField
    LayoutPanel layoutPanel;
    @UiField
    FlowPanel toolBar;
    @UiField
    Image expandCollapseImg;
    @UiField
    LayoutPanel tablePanel;
    @UiField
    MenuBar filterMenu;
    @UiField
    MenuItem annSetMenuItem;
    //
    @UiField
    Styles style;
//
    private AnnotationIdColumn<Annotation> idColumn;
    private TextColumn<Annotation> annSetColumn;
    private AnnotationKindColumn<Annotation> kindColumn;
    private AnnotationTypeColumn<Annotation> typeColumn;
    private TermAnnotationColumn<Annotation> termColumn;
    private CombinedAnnotationColumn<Annotation> detailColumn;
    private AnnotationVeiledColumn<Annotation> veiledColumn;
//
    private AnnotatedTextHandler lastWorkingDocument = null;
    private AnnotationSchemaDefHandler schemaHandler = null;
    //for document exposing TermAnnotations referencing TyDI resource
    private ResourceLocator locator;
    //list of the displayed annotations
    private final ArrayList<Annotation> annotations = new ArrayList<Annotation>();
    private final ListDataProvider<Annotation> annotationProvider;
    private final ListHandler<Annotation> sortHandler;
    private final MultiSelectionModel<Annotation> selectionModel;
    //Set of Annotation selected by the user 
    private Set<Annotation> currentSelectedSet = new HashSet<Annotation>();
    private Set<Annotation> prevSelectedSet = new HashSet<Annotation>();
    private HashMap<Annotation, Integer> indexByAnnotation = new HashMap<Annotation, Integer>();
    private AnnotationSelections selectedTextAnnotations = new AnnotationSelections();
    private AnnotationSelections selectedGroupAnnotations = new AnnotationSelections();
    private AnnotationSelections selectedRelationAnnotations = new AnnotationSelections();
    //Label of AnnotationSets indexed by AnnotationSetId
    private HashMap<Integer, AnnotationSetInfo> annSetInfos = new HashMap<Integer, AnnotationSetInfo>();
    //List of Annotation Set that can be viewed by current user
    private HashSet<Integer> authorizedAnnSetIds = new HashSet<Integer>();
    private final ToolBarExpandCollapseHandler toolBarExpandCollapseHandler;
    //List of Annotation Set that are currently displayed in the Table
    private Set<Integer> selectedAnnSet = new HashSet<Integer>();
    //
    private CombinedAnnotationCell combinedAnnotationCell = new SelectorCombinedAnnotationCell();
    SafeHtml checkedIcon = SafeHtmlUtils.fromSafeConstant(AbstractImagePrototype.create(StanEditorResources.INSTANCE.CheckedIcon()).getHTML());
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    public static final ProvidesKey<Annotation> KEY_PROVIDER = new ProvidesKey<Annotation>() {

        @Override
        public Object getKey(Annotation item) {
            return item == null ? null : item.getId();
        }
    };
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    public interface AnnotationCellIdTemplates extends SafeHtmlTemplates {

        @Template("<span style='outline:thin solid silver;'>{0}</span>")
        public SafeHtml grayedSpan2(String text);
    }
    private static final AnnotationCellIdTemplates TEMPLATES = GWT.create(AnnotationCellIdTemplates.class);
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    public static abstract class AnnotationIdColumn<T> extends Column<T, Annotation> {

        static class DblClickableCell extends AbstractCell<Annotation> {

            public DblClickableCell() {
                super("dblclick");
            }

            @Override
            public void render(Context context, Annotation annotation, SafeHtmlBuilder sb) {
                if (annotation != null) {
                    sb.append(TEMPLATES.grayedSpan2(AnnotatedTextProcessor.getBriefId(annotation.getId())));
                }
            }
        }

        public AnnotationIdColumn() {
            super(new DblClickableCell());
        }
    };

// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    // this column contains a drag handle when the corresponding annoation can be dragged to a term ressource, and then acquire the corresponding Term and Class identifier
    public static class TermAnnotationCell extends AbstractCell<TermAnnotationBox> {

        public static interface TermAnnotationCellTemplates extends SafeHtmlTemplates {

            @Template("<div id='termAnnotationDragHelper' class='{0}'></div>")
            public SafeHtml outerHelper(String cssClassName);
        }
        public static final TermAnnotationCellTemplates TEMPLATES = GWT.create(TermAnnotationCellTemplates.class);
        private static final SafeHtml termIcon = SafeHtmlUtils.fromSafeConstant(AbstractImagePrototype.create(StanEditorResources.INSTANCE.TermDragIcon()).getHTML());

        @Override
        public void render(Context context, TermAnnotationBox value, SafeHtmlBuilder sb) {
            if (value != null) {
                sb.append(termIcon);
            }
        }
    }

    public abstract class TermAnnotationColumn<T> extends DragAndDropColumn<T, TermAnnotationBox> {

        public TermAnnotationColumn() {
            super(new TermAnnotationCell());
        }
    };

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    public class SelectorCombinedAnnotationCell extends CombinedAnnotationCell {

        public SelectorCombinedAnnotationCell() {
            super("dblclick");
        }

        @Override
        public void render(Context context, Annotation annotation, SafeHtmlBuilder sb) {
            indexByAnnotation.put(annotation, context.getIndex());
            renderDetail(getDocument(), annotation, sb);
        }

        @Override
        public void onBrowserEvent(Context context, Element parent, Annotation value, NativeEvent event, ValueUpdater<Annotation> valueUpdater) {
            super.onBrowserEvent(context, parent, value, event, valueUpdater);
            String eventType = event.getType();
            if ("dblclick".equals(eventType)) {
                EventTarget evtTarget = event.getEventTarget();
                Element targetElement = evtTarget.cast();
                String annotationId = targetElement.getAttribute("aae_refannid");
                final GenericAnnotationSelectionChangedEvent selEvent;
                if (annotationId != null && !annotationId.isEmpty()) {
                    Annotation annotation = lastWorkingDocument.getAnnotation(annotationId);
                    if (annotation != null) {
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
                            EventBus eventBus = AnnotationTable.injector.getMainEventBus();
                            eventBus.fireEvent(new TextAnnotationSelectionEmptiedEvent(lastWorkingDocument));
                            eventBus.fireEvent(new GroupSelectionEmptiedEvent(lastWorkingDocument));
                            eventBus.fireEvent(new RelationSelectionEmptiedEvent(lastWorkingDocument));
                            eventBus.fireEvent(selEvent);
                        }
                    } else {
                        GWT.log("NOT loaded referenced Annotation!!! id=*" + annotationId);
                    }
                }
            }
        }
    }
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    public abstract class CombinedAnnotationColumn<T> extends Column<T, Annotation> {

        public CombinedAnnotationColumn() {
            super(combinedAnnotationCell);
        }
    };
// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    public static class AnnotationKindCell extends CombinedAnnotationCell {

        @Override
        public void render(Context context, Annotation annotation, SafeHtmlBuilder sb) {
            renderKind(annotation.getAnnotationKind(), sb);
        }
    }

    public static abstract class AnnotationKindColumn<T> extends Column<T, Annotation> {

        public AnnotationKindColumn() {
            super(new AnnotationKindCell());
        }
    };

// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    public static class AnnotationTypeCell extends CombinedAnnotationCell {

        @Override
        public void render(Context context, Annotation annotation, SafeHtmlBuilder sb) {
            AnnotationSchemaCell.renderType(annotation.getAnnotationType(), sb);
        }
    }

    public abstract class AnnotationTypeColumn<T> extends Column<T, Annotation> {

        public AnnotationTypeColumn() {
            super(new AnnotationTypeCell());
        }
    };

// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    public class AnnotationVeiledCell extends AbstractCell<Annotation> {

        public AnnotationVeiledCell() {
            super("click");
        }

        @Override
        public void render(Context context, Annotation annotation, SafeHtmlBuilder sb) {

            if (annotation.getAnnotationKind().equals(AnnotationKind.TEXT) && lastWorkingDocument.isFormattingAnnotation(annotation.getId())) {
            } else {
                ImageResource image = null;
                AnnotationDocumentViewMapper mapper = AnnotationDocumentViewMapper.getMapper(lastWorkingDocument.getAnnotatedText());

                if (mapper.isVeiled(annotation.getId())) {
                    image = StanEditorResources.INSTANCE.VeiledAnnotationIcon();
                } else {
                    image = StanEditorResources.INSTANCE.UnVeiledAnnotationIcon();
                }
                sb.appendHtmlConstant(AbstractImagePrototype.create(image).getHTML());
            }
        }

        @Override
        public void onBrowserEvent(Context context, Element parent, Annotation annotation, NativeEvent event, ValueUpdater<Annotation> valueUpdater) {
            super.onBrowserEvent(context, parent, annotation, event, valueUpdater);

            String eventType = event.getType();
            if ("click".equals(eventType) && !event.getAltKey() && !event.getCtrlKey() && !event.getMetaKey()) {
                AnnotationDocumentViewMapper mapper = AnnotationDocumentViewMapper.getMapper(lastWorkingDocument.getAnnotatedText());

                //Note : currentSelectedSet is updated AFTER this click event
                // (i.e. SelectionChangeEvent happends after click event)
                if (currentSelectedSet.contains(annotation) && currentSelectedSet.size() > 1) {
                    //clicked line was previously selected and there is more than 1 line selected
                    for (Annotation a : currentSelectedSet) {
                        boolean veiledStatus = mapper.toggleVeiledStatus(a.getId());
                    }
                } else {
                    boolean veiledStatus = mapper.toggleVeiledStatus(annotation.getId());
                }
                annotationsGrid.redraw();
            }
        }
    }

    public abstract class AnnotationVeiledColumn<T> extends Column<T, Annotation> {

        public AnnotationVeiledColumn() {
            super(new AnnotationVeiledCell());
        }
    };
// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    public AnnotationTable() {
        annotationsGrid = new DragAndDropDataGrid<Annotation>(15, resources);
        initWidget(uiBinder.createAndBindUi(this));
        
        annSetMenuItem.setHTML(SafeHtmlUtils.fromSafeConstant(AbstractImagePrototype.create(StanEditorResources.INSTANCE.AnnotationSetsIcon()).getHTML())); 

        //FIXME compute toolbar top and height at runtime
        toolBarExpandCollapseHandler = new ToolBarExpandCollapseHandler(expandCollapseImg, layoutPanel, toolBar, 0, 25, tablePanel);
        expandCollapseImg.addClickHandler(toolBarExpandCollapseHandler);
        toolBarExpandCollapseHandler.setExpanded(false);

        annotationProvider = new ListDataProvider<Annotation>(annotations, KEY_PROVIDER);

        selectionModel = new MultiSelectionModel<Annotation>(KEY_PROVIDER);
        sortHandler = new ListHandler<Annotation>(annotationProvider.getList());
        initTable();
    }

    private void initTable() {

        annotationProvider.addDataDisplay(annotationsGrid);
        DefaultSelectionEventManager<Annotation> selectionHandler = DefaultSelectionEventManager.<Annotation>createDefaultManager();
        annotationsGrid.setSelectionModel(selectionModel, selectionHandler);

        selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {

            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                prevSelectedSet = currentSelectedSet;
                currentSelectedSet = selectionModel.getSelectedSet();
                updateSelection();
            }
        });

        annotationsGrid.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.ENABLED);
        annotationsGrid.addColumnSortHandler(sortHandler);

        //
        idColumn = new AnnotationIdColumn<Annotation>() {

            @Override
            public Annotation getValue(Annotation annotation) {
                return annotation;
            }

            @Override
            public void onBrowserEvent(Context context, Element elem, Annotation annotation, NativeEvent event) {
                super.onBrowserEvent(context, elem, annotation, event);
                String eventType = event.getType();
                if ("dblclick".equals(eventType)) {
                    injector.getMainEventBus().fireEvent(new AnnotationFocusChangedEvent(lastWorkingDocument, annotation));
                }
            }
        };


        Header<String> idFooter = new Header<String>(new TextCell()) {

            @Override
            public String getValue() {
                List<Annotation> items = annotationsGrid.getVisibleItems();
                if (items.isEmpty()) {
                    return "";
                } else {
                    return "nb= " + items.size();
                }
            }
        };

        annotationsGrid.addColumn(idColumn, new SafeHtmlHeader(SafeHtmlUtils.fromSafeConstant("Id")), idFooter);
        idColumn.setSortable(true);
        sortHandler.setComparator(idColumn, new Comparator<Annotation>() {

            @Override
            public int compare(Annotation o1, Annotation o2) {
                return o1.getId().compareTo(o2.getId());
            }
        });

        //
        annSetColumn = new TextColumn<Annotation>() {

            @Override
            public String getValue(Annotation annotation) {
                return annSetInfos.get(lastWorkingDocument.getAnnotationSetId(annotation.getId())).getDescription();
            }
        };

        annotationsGrid.addColumn(annSetColumn, "Annotation Set");
        annSetColumn.setSortable(true);
        sortHandler.setComparator(annSetColumn, new Comparator<Annotation>() {

            @Override
            public int compare(Annotation o1, Annotation o2) {
                return annSetColumn.getValue(o1).compareTo(annSetColumn.getValue(o2));
            }
        });

        //
        kindColumn = new AnnotationKindColumn<Annotation>() {

            @Override
            public Annotation getValue(Annotation annotation) {
                return annotation;
            }
        };
        annotationsGrid.addColumn(kindColumn, "Kind");
        kindColumn.setSortable(true);

        sortHandler.setComparator(kindColumn, new Comparator<Annotation>() {

            @Override
            public int compare(Annotation o1, Annotation o2) {
                int order = o1.getAnnotationKind().compareTo(o2.getAnnotationKind());
                if (order == 0 && AnnotationKind.TEXT.equals(o1.getAnnotationKind())) {
                    order = TextBindingImpl.COMPARATOR.compare(o1.getTextBinding(), o2.getTextBinding());
                }
                return order;
            }
        });

        //
        typeColumn = new AnnotationTypeColumn<Annotation>() {

            @Override
            public Annotation getValue(Annotation annotation) {
                return annotation;
            }
        };
        annotationsGrid.addColumn(typeColumn, "Type");
        typeColumn.setSortable(true);
        sortHandler.setComparator(typeColumn, new Comparator<Annotation>() {

            @Override
            public int compare(Annotation o1, Annotation o2) {
                return o1.getAnnotationType().compareTo(o2.getAnnotationType());
            }
        });

        //
        termColumn = new TermAnnotationColumn<Annotation>() {

            @Override
            public TermAnnotationBox getValue(Annotation annotation) {
                return (annotation instanceof TermAnnotationBox) ? (TermAnnotationBox) annotation : null;
            }
        };

        annotationsGrid.addColumn(termColumn, "");
        termColumn.setSortable(true);
        sortHandler.setComparator(termColumn, new Comparator<Annotation>() {

            @Override
            public int compare(Annotation o1, Annotation o2) {
                return isTermAnnotation(o1).compareTo(isTermAnnotation(o2));
            }
        });


        //The column "termColumn" is visible only if current schema support "Term Annotation" (i.e. which contains property to link it to a term or a semantic class)
        //The column "termColumn" contains a draggable icon only in the row where the corresponding annotation is a "Term Annotation"
        {
            //setup drag operation
            DraggableOptions dragOptions = termColumn.getDraggableOptions();

            dragOptions.setHelper($(TermAnnotationCell.TEMPLATES.outerHelper(StanEditorResources.INSTANCE.css().DragHelper()).asString()));

            // the cell being greater than the helper, force the position of the helper on the mouse cursor.
            dragOptions.setCursorAt(new CursorAt(10, 0, null, null));

            dragOptions.setRevert(RevertOption.ON_INVALID_DROP);

            // fill Helper when drag start 
            dragOptions.setOnBeforeDragStart(new DragFunction() {

                @Override
                public void f(DragContext context) {
                    context.getDraggableData();
                }
            });

            dragOptions.setOnDragStart(new DragFunction() {

                @Override
                public void f(gwtquery.plugins.draggable.client.events.DragContext context) {

                    if (context.getDraggableData() instanceof TermAnnotation) {
                        Annotation draggedAnnotation = context.getDraggableData();
                        SafeHtmlBuilder sb = new SafeHtmlBuilder();
                        CombinedAnnotationCell.renderDetail(lastWorkingDocument, draggedAnnotation, sb);
                        context.getHelper().setInnerHTML(sb.toSafeHtml().asString());
                    } else {
                        //Cancel Dragging : Should be available in later release of gwtquery-plugins, see: http://code.google.com/p/gwtquery-plugins/issues/detail?id=27
                        //In the meantime, just display a icon showing that this annoation should not be dragged 
                        SafeHtml fromSafeConstant = SafeHtmlUtils.fromSafeConstant(AbstractImagePrototype.create(StanEditorResources.INSTANCE.ForbiddenDragIcon()).getHTML());
                        context.getHelper().setInnerHTML(fromSafeConstant.asString());
                    }
                }
            });

            dragOptions.setOpacity((float) 0.9);

            dragOptions.setAppendTo("body");
            dragOptions.setCursor(Cursor.MOVE);
            dragOptions.setScope(TermAnnotation.DragNDropScope);

            //setup option operation
            DroppableOptions dropOptions = termColumn.getDroppableOptions();

            termColumn.setCellDraggableOnly();
        }
        //
        detailColumn = new CombinedAnnotationColumn<Annotation>() {

            @Override
            public Annotation getValue(Annotation annotation) {
                return annotation;
            }
        };

        annotationsGrid.addColumn(detailColumn, "Details");
        detailColumn.setSortable(true);
        sortHandler.setComparator(detailColumn,
                new Comparator<Annotation>() {

                    @Override
                    public int compare(Annotation o1, Annotation o2) {
                        int result = o1.getAnnotationKind().compareTo(o2.getAnnotationKind());
                        if (result == 0 && AnnotationKind.TEXT.equals(o1.getAnnotationKind())) {
                            String t1 = lastWorkingDocument.isFormattingAnnotation(o1.getId()) ? "" : AnnotatedTextProcessor.getAnnotationText(o1);
                            String t2 = lastWorkingDocument.isFormattingAnnotation(o2.getId()) ? "" : AnnotatedTextProcessor.getAnnotationText(o2);
                            result = t1.compareTo(t2);
                        }
                        return result;
                    }
                });

        //
        veiledColumn = new AnnotationVeiledColumn<Annotation>() {

            @Override
            public Annotation getValue(Annotation annotation) {
                return annotation;
            }
        };

        annotationsGrid.addColumn(veiledColumn, "Visible");
        veiledColumn.setSortable(true);
        sortHandler.setComparator(veiledColumn,
                new Comparator<Annotation>() {

                    @Override
                    public int compare(Annotation o1, Annotation o2) {
                        AnnotationDocumentViewMapper mapper = AnnotationDocumentViewMapper.getMapper(lastWorkingDocument.getAnnotatedText());

                        Boolean t1 = lastWorkingDocument.isFormattingAnnotation(o1.getId()) ? null : mapper.isVeiled(o1.getId());
                        Boolean t2 = lastWorkingDocument.isFormattingAnnotation(o2.getId()) ? null : mapper.isVeiled(o2.getId());

                        int result;
                        if (t1 == null && t2 == null) {
                            result = 0;
                        } else if (t1 == null) {
                            result = -1;
                        } else if (t2 == null) {
                            result = +1;
                        } else {
                            result = t1.compareTo(t2);
                        }
                        return result;
                    }
                });

        resetColumnSize(false);
    }

    private void resetColumnSize(boolean showTermColumn) {
        annotationsGrid.setWidth("100%");
        annotationsGrid.setColumnWidth(idColumn, 10.0, Unit.PCT);
        annotationsGrid.setColumnWidth(annSetColumn, 18.0, Unit.PCT);
        annotationsGrid.setColumnWidth(kindColumn, 26.0, Unit.PX);
        annotationsGrid.setColumnWidth(typeColumn, 15.0, Unit.PCT);
        annotationsGrid.setColumnWidth(termColumn, showTermColumn ? 26.0 : 0.0, Unit.PX);
        annotationsGrid.setColumnWidth(detailColumn, 54.0, Unit.PCT);
        annotationsGrid.setColumnWidth(veiledColumn, 36.0, Unit.PX);
    }

    private void reinitAnnSetFilter(boolean usersAnnSetOnly) {
        if (usersAnnSetOnly) {
            selectedAnnSet.add(lastWorkingDocument.getUsersAnnotationSet().getId());
        } else {
            HashSet<Integer> allButFormatting = new HashSet<Integer>(lastWorkingDocument.getLoadedAnnotationSets());
            if (lastWorkingDocument.getFormattingAnnotationSet() != null) {
                allButFormatting.remove(lastWorkingDocument.getFormattingAnnotationSet().getId());
            }
            selectedAnnSet.addAll(allButFormatting);
        }
    }

    private void reinitAnnSetFilterMenu() {
        MenuBar annSetMenuBar = new MenuBar(true);

        annSetMenuBar.addItem(SafeHtmlUtils.fromString("Reset filter"), new Command() {

            @Override
            public void execute() {
                reinitAnnSetFilter(false);
                filterAnnotationListAndRefresh(selectedAnnSet);
                reinitAnnSetFilterMenu();
            }
        });

        annSetMenuBar.addSeparator();


        for (Entry<Integer, AnnotationSetInfo> e : annSetInfos.entrySet()) {

            final int annSetId = e.getKey();
            AnnotationSetInfo asInfo = e.getValue();

            if (authorizedAnnSetIds.contains(annSetId)) {
                Command command = new Command() {

                    @Override
                    public void execute() {
                        if (selectedAnnSet.contains(annSetId)) {
                            selectedAnnSet.remove(annSetId);
                        } else {
                            selectedAnnSet.add(annSetId);
                        }
                        filterAnnotationListAndRefresh(selectedAnnSet);
                        reinitAnnSetFilterMenu();
                    }
                };

                SafeHtmlBuilder shbuilder = new SafeHtmlBuilder();

                if (selectedAnnSet.contains(annSetId)) {
                    shbuilder.append(checkedIcon).appendEscaped("  ");
                }

                shbuilder.appendEscaped(asInfo.getDescription());
                shbuilder.appendHtmlConstant("  ").appendEscaped("(").append(asInfo.getNbTextAnnotations()).appendEscaped("/").append(asInfo.getNbGroups()).appendEscaped("/").append(asInfo.getNbRelations()).appendEscaped(")");

                annSetMenuBar.addItem(shbuilder.toSafeHtml(), command);
            } else {
                MenuItem item = new MenuItem(asInfo.getDescription(), (Command) null);
                item.setEnabled(false);
                annSetMenuBar.addItem(item);
            }
        }
        annSetMenuItem.setSubMenu(annSetMenuBar);
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    private void updateSelection() {


        AnnotationDocumentViewMapper mapper = AnnotationDocumentViewMapper.getMapper(lastWorkingDocument.getAnnotatedText());

        boolean someTextAnnSelected = !selectedTextAnnotations.isEmpty();
        boolean someGroupSelected = !selectedGroupAnnotations.isEmpty();
        boolean someRelationSelected = !selectedRelationAnnotations.isEmpty();

        selectedTextAnnotations.clear();
        selectedGroupAnnotations.clear();
        selectedRelationAnnotations.clear();

        for (Annotation annotation : currentSelectedSet) {
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


        EventBus eventBus = injector.getMainEventBus();
        ArrayList<GenericAnnotationSelectionChangedEvent> events = new ArrayList<GenericAnnotationSelectionChangedEvent>();

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

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    private void displayAnnotationList(AnnotatedTextHandler workingDocument, DocumentView docView, boolean initialLoad) {
        lastWorkingDocument = workingDocument;
        combinedAnnotationCell.setDocument(lastWorkingDocument);

        TermAnnotationsExpositionEvent event = null;
        if (workingDocument != null) {
            schemaHandler = new AnnotationSchemaDefHandler(workingDocument.getAnnotatedText().getAnnotationSchema());

            //does the new schema include TermAnnotation referencing TyDI resource?
            if (schemaHandler.enableTyDIResourceRef()) {
                Set<String> urls = schemaHandler.getTyDIResourceBaseURLs();
                if (urls.size() > 1) {
                    throw new IllegalArgumentException("A schema should reference only one TyDI ressource location!");
                }
                //inform Structured Terminology widget that it should start displaying the specified Terminology resource
                try {
                    locator = new TermAnnotation.ResourceLocator(urls.iterator().next());
                    event = new TermAnnotationsExpositionEvent(TermAnnotationsExpositionEvent.ChangeType.Available, locator);
                } catch (IllegalArgumentException ex) {
                    Window.alert(ex.getMessage());
                }
            }
        } else {
            //if the editor was previously exposing TermAnnotation referencing TyDI resource
            if (schemaHandler != null && schemaHandler.enableTyDIResourceRef()) {
                //inform Structured Terminology widget that it should stop displaying the Terminology resource
                event = new TermAnnotationsExpositionEvent(TermAnnotationsExpositionEvent.ChangeType.Unavailable, locator);
            }
            schemaHandler = null;
        }

        if (event != null) {
            EventBus eventBus = AnnotationTable.injector.getMainEventBus();
            eventBus.fireEvent(event);
        }

        //show term colum if the Annotation schema allows references to TyDI resource
        resetColumnSize(schemaHandler != null && schemaHandler.enableTyDIResourceRef());

        if (lastWorkingDocument == null) {
            annotationsGrid.setRowCount(0);
        } else {
            annSetInfos.clear();
            for (AnnotationSetInfo asi : lastWorkingDocument.getAnnotatedText().getAnnotationSetInfoList()) {
                annSetInfos.put(asi.getId(), asi);
            }
            authorizedAnnSetIds.clear();
            authorizedAnnSetIds.addAll(lastWorkingDocument.getLoadedAnnotationSets());
            reinitAnnSetFilter(initialLoad);
            reinitAnnSetFilterMenu();
        }

        refreshAnnotationListDisplay();
    }

    private Boolean isTermAnnotation(Annotation annotation) {
        return (schemaHandler != null && schemaHandler.isTyDIResReferencingType(annotation.getAnnotationType()));
    }

    private void filterAnnotationListAndRefresh(Set<Integer> displayAnnSetIds) {
        annotationProvider.getList().clear();
        Collection<Annotation> annotationSrc;
        if (displayAnnSetIds == null) {
            annotationSrc = lastWorkingDocument.getAnnotations();
        } else {
            annotationSrc = lastWorkingDocument.getAnnotationsForAnnSets(displayAnnSetIds);
        }
        ArrayList<Annotation> annotationList = new ArrayList<Annotation>();
        for (Annotation a : annotationSrc) {
            if (isTermAnnotation(a)) {
                String tyDITermRefPropName = schemaHandler.getTyDITermRefPropName(a.getAnnotationType());
                String tyDIClassRefPropName = schemaHandler.getTyDISemClassRefPropName(a.getAnnotationType());
                annotationList.add(new TermAnnotationBox(lastWorkingDocument, a, tyDITermRefPropName, tyDIClassRefPropName));
            } else {
                annotationList.add(a);
            }
        }
        annotationProvider.getList().addAll(annotationList);
        annotationsGrid.setVisibleRange(0, annotationProvider.getList().size());
        //annotationProvider.refresh();
    }

    private void refreshAnnotationListDisplay() {
        if (lastWorkingDocument != null) {
            annotationProvider.getList().clear();
            indexByAnnotation.clear();

            filterAnnotationListAndRefresh(selectedAnnSet);
        }
    }

    @Override
    public void onWorkingDocumentChanged(WorkingDocumentChangedEvent event) {
        displayAnnotationList(event.getWorkingDocument(), event.getDocView(), !WorkingDocumentChangedEvent.ChangeType.AdditionalAnnotationSetLoaded.equals(event.getChangeType()));
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


        for (Annotation annotation : annotations) {
            String annotationId = annotation.getId();

            if (selectionModel.isSelected(annotation)) {
                if (!selectedIds.contains(annotationId)) {
                    selectionModel.setSelected(annotation, false);
                }
            } else {
                if (selectedIds.contains(annotationId)) {
                    selectionModel.setSelected(annotation, true);

                    //bring into view the row corresponding to the main annotation
                    if (mainSelectedId.contains(annotationId)) {
                        //
                        try {
                            Integer index = indexByAnnotation.get(annotation);
                            if (index != null) {
                                annotationsGrid.getRowElement(index).scrollIntoView();
                            } else {
                                //Case of a new line appended to the table when a annotation is just created
                                if (annotations.contains(annotation)) {
                                    index = annotationsGrid.getRowCount() - 1;
                                    annotationsGrid.getRowElement(index).scrollIntoView();
                                }

                            }
                        } catch (IndexOutOfBoundsException e) {
                            // scrolling not possible because the row is outside the the visible page
                            //FIXME : force to populate the table with the desired row...
                        }
                    }

                }
            }

        }
    }

    @Override
    public void onEditHappened(EditHappenedEvent event) {
        if (event.getEdit() instanceof AnnotationEdit) {
            if (((AnnotationEdit) event.getEdit()).getAnnotatedTextHandler().getAnnotatedText().equals(lastWorkingDocument.getAnnotatedText())) {
                refreshAnnotationListDisplay();
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
        EditHappenedEvent.unregister(this);
        TextAnnotationSelectionChangedEvent.unregister(this);
    }
}
