/*
 *
 *      This software is a result of Quaero project and its use must respect the rules of the Quaero Project Consortium Agreement.
 *
 *      Copyright Institut National de la Recherche Agronomique, 2010.
 *
 */
package fr.inra.mig_bibliome.stane.client.Document;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.shared.EventBus;
import fr.inra.mig_bibliome.stane.client.Config.GlobalStyles;
import fr.inra.mig_bibliome.stane.client.Config.ShortCutToActionTypeMapper;
import fr.inra.mig_bibliome.stane.client.Document.DocumentView.Rect;
import fr.inra.mig_bibliome.stane.client.Events.GroupSelectionChangedEvent;
import fr.inra.mig_bibliome.stane.client.Events.GroupSelectionEmptiedEvent;
import fr.inra.mig_bibliome.stane.client.Events.Selection.AnnotationSelections;
import fr.inra.mig_bibliome.stane.client.Events.Selection.GroupAnnotationSelection;
import fr.inra.mig_bibliome.stane.shared.data3.Annotation;
import fr.inra.mig_bibliome.stane.shared.data3.AnnotationKind;
import fr.inra.mig_bibliome.stane.shared.data3.AnnotationReference;
import fr.inra.mig_bibliome.stane.shared.data3.validation.AnnotationTypeDefinition;
import java.util.ArrayList;
import org.vaadin.gwtgraphics.client.DrawingArea;
import org.vaadin.gwtgraphics.client.Line;
import org.vaadin.gwtgraphics.client.VectorObject;
import org.vaadin.gwtgraphics.client.shape.Circle;
import org.vaadin.gwtgraphics.client.shape.Rectangle;

/**
 * Graphically renders Group annotations
 *
 * @author fpapazian
 */
public class GroupDisplayer extends CombinedAnnotationDisplayer {

    public class GroupWidget extends CombinedAnnotationWidget {

        private final Circle centerWidget;
        private final ArrayList<Line> backlines;
        private final ArrayList<Line> frontlines;
        private final ArrayList<Rectangle> shadows;
        private final Rect centerClip;

        private GroupWidget(String relationId, AnnotationTypeDefinition typedef, ArrayList<String> annotationIds, ArrayList<Rect> annotationClips) {
            super(relationId, typedef, annotationIds, annotationClips);

            shadows = new ArrayList<Rectangle>();

            int index = 0;
            //create "shadow" of the referenced Annotation
            for (Rect clip : getAnnotationClips()) {
                if (getMapper().getAnnotation(annotationIds.get(index)).getAnnotationKind().equals(AnnotationKind.TEXT)) {
                    Rectangle sShadow = new Rectangle(clip.left, clip.top, clip.width + 2, clip.height + 2);
                    sShadow.setStrokeWidth(1);
                    sShadow.setFillOpacity(0);
                    sShadow.setStrokeColor("black");
                    shadows.add(sShadow);
                    this.add(sShadow);
                }
                index++;
            }

            Point isoBar = getIsoBarycentre(getCenters());
            //apply a vertical shift if the center is probable on the same line as the referenced annotation (avoid to collide with the text)
            isoBar.x -= 12;
            isoBar.y += 12;

            backlines = new ArrayList<Line>();
            int annIndex = 0;
            for (Point center : getCenters()) {
                Line backline = new Line(center.x, center.y, isoBar.x, isoBar.y);
                backline.setStrokeOpacity(0.5);
                backlines.add(backline);
                String annotationId = annotationIds.get(annIndex);
//                    String role = roles.get(annIndex);
                //                  backline.setTitle(role + "(" + annotationId + ")");
                this.add(backline);
                annIndex++;
            }

            frontlines = new ArrayList<Line>();
            annIndex = 0;
            for (Point center : getCenters()) {
                Line frontline = new Line(center.x, center.y, isoBar.x, isoBar.y);
                frontline.setStrokeOpacity(0.5);
                frontlines.add(frontline);
                String annotationId = annotationIds.get(annIndex);
//                    String role = roles.get(annIndex);
//                    frontline.setTitle(role + "(" + annotationId + ")");
                this.add(frontline);
                annIndex++;
            }

            //draw center
            int xsize = 4;
            int ysize = 6;
            centerWidget = new Circle(isoBar.x, isoBar.y, xsize);
            centerWidget.setStrokeOpacity(0.6);
            centerWidget.setFillOpacity(0.4);

            /*
            centerWidget = new Path(isoBar.x - xsize, isoBar.y - ysize);
            centerWidget.lineTo(isoBar.x + xsize, isoBar.y - ysize);
            centerWidget.lineTo(isoBar.x + xsize, isoBar.y + ysize);
            centerWidget.lineTo(isoBar.x - xsize, isoBar.y + ysize);
            centerWidget.close();
             * 
             */
            centerWidget.setTitle(relationId);

            centerClip = new Rect(isoBar.x - xsize, isoBar.y - ysize, xsize, ysize);

            display(isAnnotationVeiled(), isAnnotationSelected(), false);

            this.add(centerWidget);
            this.addMouseOverHandler(new MouseOverHandler() {

                @Override
                public void onMouseOver(MouseOverEvent event) {
                    display(isAnnotationVeiled(), isAnnotationSelected(), true);
                }
            });

            this.addMouseOutHandler(new MouseOutHandler() {

                @Override
                public void onMouseOut(MouseOutEvent event) {
                    display(isAnnotationVeiled(), isAnnotationSelected(), false);
                }
            });

            this.addClickHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    //[Ctrl]/[Command] key used for multiselect
                    boolean multiSelectKeyDown = (!ShortCutToActionTypeMapper.isMacOs() && event.isControlKeyDown()) || (ShortCutToActionTypeMapper.isMacOs() && event.isMetaKeyDown());
                    if (!multiSelectKeyDown) {
                        selectedGroups.clear();
                    }

                    boolean selected = selectedGroups.isAnnotationSelected(getAnnotationId());
                    if (selected && multiSelectKeyDown) {
                        selectedGroups.isAnnotationSelected(getAnnotationId());
                        selected = false;
                    } else {
                        selectedGroups.getSelections().add(new GroupAnnotationSelection(getAnnotatedDoc().getAnnotation(getAnnotationId())));
                        selected = true;
                    }
                    if (selectedGroups.isEmpty()) {
                        getEventBus().fireEvent(new GroupSelectionEmptiedEvent(getAnnotatedTextHandler()));
                    } else {
                        getEventBus().fireEvent(new GroupSelectionChangedEvent(getAnnotatedTextHandler(), selectedGroups));
                    }
                    event.stopPropagation();
                }
            });

        }

        @Override
        public boolean isAnnotationSelected() {
            return selectedGroups.isAnnotationSelected(getAnnotationId());
        }

        @Override
        public void display(boolean veiled, boolean selected, boolean hovered) {
            centerWidget.setVisible(!veiled);
            for (Rectangle shadow : shadows) {
                shadow.setVisible(!veiled);
            }
            for (Line fronLine : frontlines) {
                fronLine.setVisible(!veiled);
            }
            for (Line backline : backlines) {
                backline.setVisible(!veiled);
            }
            if (!veiled) {
                if (hovered) {
                    if (selected) {
                        centerWidget.setStrokeColor(GlobalStyles.HoveredSelectedRelation.getColor());
                        centerWidget.setStrokeWidth(2);
                        centerWidget.setFillColor(getColor());

                        for (Line backline : backlines) {
                            backline.setStrokeColor(GlobalStyles.HoveredSelectedRelation.getColor());
                            backline.setStrokeWidth(5);
                        }
                    } else {
                        centerWidget.setStrokeColor(GlobalStyles.HoveredRelation.getColor());
                        centerWidget.setStrokeWidth(2);
                        centerWidget.setFillColor(getColor());
                        for (Line backline : backlines) {
                            backline.setStrokeColor(GlobalStyles.HoveredRelation.getColor());
                            backline.setStrokeWidth(5);
                        }
                    }
                } else {
                    if (selected) {
                        centerWidget.setStrokeColor(GlobalStyles.SelectedRelation.getColor());
                        centerWidget.setStrokeWidth(2);
                        centerWidget.setFillColor(getColor());

                        for (Line fronLine : frontlines) {

                            fronLine.setStrokeColor("silver");
                            fronLine.setStrokeWidth(1);
                        }

                        for (Line backline : backlines) {
                            backline.setStrokeColor(GlobalStyles.SelectedRelation.getColor());
                            backline.setStrokeWidth(3);
                        }
                    } else {
                        centerWidget.setStrokeColor("darkgrey");
                        centerWidget.setStrokeWidth(1);
                        centerWidget.setFillColor(getColor());


                        for (Line fronLine : frontlines) {
                            fronLine.setStrokeColor("#B8860B");
                            fronLine.setStrokeWidth(1);
                        }

                        for (Line backline : backlines) {
                            backline.setStrokeColor("white");
                            backline.setStrokeWidth(3);
                        }
                    }
                }
            }
        }

        @Override
        public VectorObject getCenterWidget() {
            return centerWidget;
        }

        @Override
        public Rect getCenterClip() {
            return centerClip;
        }
    }
    private final AnnotationSelections selectedGroups;

    public GroupDisplayer(DocumentView docView, DrawingArea canvas, final EventBus eventBus, AnnotationSelections selectedGroups) {
        super(docView, canvas, eventBus);
        this.selectedGroups = selectedGroups;
        //clear any relation selection when canvas is clicked
        getCanvas().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                clearSelection();
            }
        });

    }

    @Override
    public void clearSelection() {
        getEventBus().fireEvent(new GroupSelectionEmptiedEvent(getAnnotatedTextHandler()));
    }

    @Override
    public void addAnnotation(Annotation group) {
        if (getAnnotatedDoc() != null) {

            ArrayList<String> annotationIds = new ArrayList<String>();

            AnnotationTypeDefinition typedef = getAnnotatedDoc().getAnnotationSchema().getAnnotationTypeDefinition(group.getAnnotationType());
            ArrayList<Rect> annotationClips = new ArrayList<Rect>();

            boolean canBeDisplayed = !group.getAnnotationGroup().getComponentRefs().isEmpty();
            for (AnnotationReference aRef : group.getAnnotationGroup().getComponentRefs()) {
                Annotation component = getAnnotatedDoc().getAnnotation(aRef.getAnnotationId());
                String annotationId = component.getId();
                annotationIds.add(annotationId);
                if (component.getAnnotationKind().equals(AnnotationKind.TEXT)) {
                    Rect clip = getDocumentView().getAnnotatedTextClip(annotationId);
                    clip.left -= getCanvas().getAbsoluteLeft();
                    clip.top -= getCanvas().getAbsoluteTop();
                    annotationClips.add(clip);
                } else {
                    CombinedAnnotationWidget w = getWidget(annotationId);
                    if (w != null) {
                        annotationClips.add(w.getCenterClip());
                    } else {
                        w = getOtherDisplayer().getWidget(annotationId);
                        if (w != null) {
                            annotationClips.add(w.getCenterClip());
                        } else {
                            canBeDisplayed = false;
                            break;
                        }
                    }
                }
            }

            if (canBeDisplayed) {
                GroupWidget relGroup = new GroupWidget(group.getId(), typedef, annotationIds, annotationClips);
                addWidget(group.getId(), relGroup);
                //add at the background
                getCanvas().insert(relGroup, 0);
            } else {
                //FIXME not I18N
                GWT.log("Group" + group.getId() + " can not be displayed!");
                //eventBus.fireEvent(new InformationReleasedEvent("Relation " + relation.getId() + " can not be displayed!"));
            }
        }
    }
}
