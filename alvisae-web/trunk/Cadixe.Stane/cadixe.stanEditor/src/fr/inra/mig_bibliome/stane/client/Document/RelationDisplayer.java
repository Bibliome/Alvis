/*
 *
 *      This software is a result of Quaero project and its use must respect the rules of the Quaero Project Consortium Agreement.
 *
 *      Copyright Institut National de la Recherche Agronomique, 2010.
 *
 */
package fr.inra.mig_bibliome.stane.client.Document;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
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
import fr.inra.mig_bibliome.stane.client.Events.RelationSelectionChangedEvent;
import fr.inra.mig_bibliome.stane.client.Events.RelationSelectionEmptiedEvent;
import fr.inra.mig_bibliome.stane.client.Events.Selection.AnnotationSelections;
import fr.inra.mig_bibliome.stane.client.Events.Selection.RelationAnnotationSelection;
import fr.inra.mig_bibliome.stane.client.data3.AnnotatedTextProcessor;
import fr.inra.mig_bibliome.stane.shared.data3.Annotation;
import fr.inra.mig_bibliome.stane.shared.data3.AnnotationKind;
import fr.inra.mig_bibliome.stane.shared.data3.AnnotationReference;
import fr.inra.mig_bibliome.stane.shared.data3.validation.AnnotationTypeDefinition;
import java.util.ArrayList;
import org.vaadin.gwtgraphics.client.DrawingArea;
import org.vaadin.gwtgraphics.client.VectorObject;
import org.vaadin.gwtgraphics.client.shape.Path;
import org.vaadin.gwtgraphics.client.shape.Rectangle;

/**
 * Graphically renders Relation annotations
 *
 * @author fpapazian
 */
public class RelationDisplayer extends CombinedAnnotationDisplayer {

    public class RelationWidget extends CombinedAnnotationWidget {

        private final Path centerWidget;
        private final ArrayList<Path> backlines;
        private final ArrayList<Path> frontlines;
        private final ArrayList<Rectangle> shadows;
        private final Rect centerClip;

        protected RelationWidget(String relationId, AnnotationTypeDefinition typedef, ArrayList<String> annotationIds, ArrayList<Rect> annotationClips, ArrayList<String> roles) {
            super(relationId, typedef, annotationIds, annotationClips);

            shadows = new ArrayList<Rectangle>();

            int typicalHeight = getAnnotationClips().get(0).height;
            int interlineSize = getInterlineSize();
            if (interlineSize == 0) {
                interlineSize = (int) (typicalHeight * 1.25);
            }
            int interlineSpaceAvail = interlineSize - typicalHeight / 2;
            double coef[] = {.5, .4, .6, .3, .7, .2, .55, .45, .65, .35, .25,};

            int lowestAnnotationIdx = 0;
            int lowestAnnotationFloor = 0;
            int highestAnnotationIdx = 0;
            int highestAnnotationFloor = 999999;

            int leftAnnotationIdx = 0;
            int leftAnnotationCenter = 999999;
            int rightAnnotationIdx = 0;
            int rightAnnotationCenter = 0;

            //create shadow boxes under the TEXT annotations
            int index = 0;
            for (Rect clip : getAnnotationClips()) {
                if (getMapper().getAnnotation(annotationIds.get(index)).getAnnotationKind().equals(AnnotationKind.TEXT)) {
                    Rectangle sShadow = new Rectangle(clip.left, clip.top, clip.width + 2, clip.height + 2);
                    sShadow.setStrokeWidth(1);
                    sShadow.setFillOpacity(0);
                    sShadow.setStrokeColor("black");
                    shadows.add(sShadow);
                    this.add(sShadow);

                    int annotationFloor = clip.top + clip.height;
                    if (annotationFloor > lowestAnnotationFloor) {
                        lowestAnnotationFloor = annotationFloor;
                        lowestAnnotationIdx = index;
                    }
                    if (annotationFloor < highestAnnotationFloor) {
                        highestAnnotationFloor = annotationFloor;
                        highestAnnotationIdx = index;
                    }
                    int annotationCenter = clip.left + clip.width / 2;
                    if (leftAnnotationCenter > annotationCenter) {
                        leftAnnotationCenter = annotationCenter;
                        leftAnnotationIdx = index;
                    }
                    if (rightAnnotationCenter < annotationCenter) {
                        rightAnnotationCenter = annotationCenter;
                        rightAnnotationIdx = index;
                    }

                }
                index++;
            }


            backlines = new ArrayList<Path>();
            frontlines = new ArrayList<Path>();

            Point coreCenter = getIsoBarycentre(getCenters());
            int arity = getAnnotationClips().size();
            boolean stackedAnnotations = false;

            int interlineIndex = lowestAnnotationFloor / interlineSize;
            int occupancy = getInterlineOccupancy(interlineIndex);

            int verticalOffset = (int) (interlineSpaceAvail * coef[occupancy % coef.length]);

            coreCenter.y = lowestAnnotationFloor + verticalOffset;
            incInterlineOccupancy(interlineIndex);

            if (arity == 2) {

                //if the centers are too close, apply an horizontal shift to the center
                if ((rightAnnotationCenter - leftAnnotationCenter) < 20) {
                    coreCenter.x = getAnnotationClips().get(leftAnnotationIdx).left - 10;
                    stackedAnnotations = true;
                }

                int annIndex = 0;
                for (Point center : getCenters()) {
                    Path backline = new Path(center.x, center.y);
                    if (stackedAnnotations && (annIndex == highestAnnotationIdx)) {
                        backline.lineTo(coreCenter.x, center.y);
                    } else {
                        backline.lineTo(center.x, coreCenter.y);
                    }
                    backline.lineTo(coreCenter.x, coreCenter.y);
                    backline.setFillColor(null);
                    backlines.add(backline);

                    String annotationId = annotationIds.get(annIndex);
                    String role = roles.get(annIndex);
                    backline.setTitle(role + "(" + AnnotatedTextProcessor.getBriefId(annotationId) + ")");
                    this.add(backline);
                    annIndex++;
                }

                annIndex = 0;
                for (Point center : getCenters()) {
                    Path frontline = new Path(center.x, center.y);
                    if (stackedAnnotations && (annIndex == highestAnnotationIdx)) {
                        frontline.lineTo(coreCenter.x, center.y);
                    } else {
                        frontline.lineTo(center.x, coreCenter.y);
                    }
                    frontline.lineTo(coreCenter.x, coreCenter.y);
                    frontline.setFillColor(null);
                    frontlines.add(frontline);
                    String annotationId = annotationIds.get(annIndex);
                    String role = roles.get(annIndex);
                    frontline.setTitle(role + "(" + AnnotatedTextProcessor.getBriefId(annotationId) + ")");
                    this.add(frontline);
                    annIndex++;
                }

            } else {


                int annIndex = 0;
                for (Point center : getCenters()) {
                    Path backline = new Path(center.x, center.y);
                    backline.lineTo(center.x, coreCenter.y);
                    backline.lineTo(coreCenter.x, coreCenter.y);
                    backline.setFillColor(null);
                    backlines.add(backline);
                    String annotationId = annotationIds.get(annIndex);
                    String role = roles.get(annIndex);
                    backline.setTitle(role + "(" + AnnotatedTextProcessor.getBriefId(annotationId) + ")");
                    this.add(backline);
                    annIndex++;
                }

                annIndex = 0;
                for (Point center : getCenters()) {
                    Path frontline = new Path(center.x, center.y);
                    frontline.lineTo(center.x, coreCenter.y);
                    frontline.lineTo(coreCenter.x, coreCenter.y);
                    frontline.setFillColor(null);
                    frontlines.add(frontline);
                    String annotationId = annotationIds.get(annIndex);
                    String role = roles.get(annIndex);
                    frontline.setTitle(role + "(" + AnnotatedTextProcessor.getBriefId(annotationId) + ")");
                    this.add(frontline);
                    annIndex++;
                }
            }

            //draw center
            int xsize = 4;
            int ysize = 6;
            centerWidget = new Path(coreCenter.x, coreCenter.y - ysize);
            centerWidget.lineTo(coreCenter.x + xsize, coreCenter.y);
            centerWidget.lineTo(coreCenter.x, coreCenter.y + ysize);
            centerWidget.lineTo(coreCenter.x - xsize, coreCenter.y);
            centerWidget.close();
            centerWidget.setTitle("Relation '" + typedef.getType() + "' (" + AnnotatedTextProcessor.getBriefId(relationId) + ")");

            centerClip = new Rect(coreCenter.x - xsize, coreCenter.y - ysize, xsize, ysize);

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
                        selectedRelations.clear();
                    }

                    boolean selected = selectedRelations.isAnnotationSelected(getAnnotationId());
                    //[Ctrl] key used for multiselect
                    if (selected && multiSelectKeyDown) {
                        selectedRelations.isAnnotationSelected(getAnnotationId());
                        selected = false;
                    } else {
                        selectedRelations.getSelections().add(new RelationAnnotationSelection(getAnnotatedDoc().getAnnotation(getAnnotationId())));
                        selected = true;
                    }
                    if (selectedRelations.isEmpty()) {
                        getEventBus().fireEvent(new RelationSelectionEmptiedEvent(getAnnotatedTextHandler()));
                    } else {
                        getEventBus().fireEvent(new RelationSelectionChangedEvent(getAnnotatedTextHandler(), selectedRelations));
                    }
                    event.stopPropagation();
                }
            });

        }

        @Override
        public boolean isAnnotationSelected() {
            return selectedRelations.isAnnotationSelected(getAnnotationId());
        }

        @Override
        public void display(boolean veiled, boolean selected, boolean hovered) {
            centerWidget.setVisible(!veiled);
            for (Rectangle shadow : shadows) {
                shadow.setVisible(!veiled);
            }
            for (Path fronLine : frontlines) {
                fronLine.setVisible(!veiled);
            }
            for (Path backline : backlines) {
                backline.setVisible(!veiled);
            }
            if (!veiled) {
                if (hovered) {
                    if (selected) {
                        centerWidget.setStrokeColor(GlobalStyles.HoveredSelectedRelation.getColor());
                        centerWidget.setStrokeWidth(2);
                        centerWidget.setFillColor(getColor());

                        for (Path backline : backlines) {
                            backline.setStrokeColor(GlobalStyles.HoveredSelectedRelation.getColor());
                            backline.setStrokeWidth(4);
                        }
                    } else {
                        centerWidget.setStrokeColor(GlobalStyles.HoveredRelation.getColor());
                        centerWidget.setStrokeWidth(2);
                        centerWidget.setFillColor(getColor());
                        for (Path backline : backlines) {
                            backline.setStrokeColor(GlobalStyles.HoveredRelation.getColor());
                            backline.setStrokeWidth(5);
                        }
                    }
                } else {
                    if (selected) {
                        centerWidget.setStrokeColor(GlobalStyles.SelectedRelation.getColor());
                        centerWidget.setStrokeWidth(2);
                        centerWidget.setFillColor(getColor());

                        for (Path fronLine : frontlines) {

                            fronLine.setStrokeColor(getColor());
                            fronLine.setStrokeWidth(3);
                        }

                        for (Path backline : backlines) {
                            backline.setStrokeColor(GlobalStyles.SelectedRelation.getColor());
                            backline.setStrokeWidth(4);
                        }
                    } else {
                        centerWidget.setStrokeColor("darkgrey");
                        centerWidget.setStrokeWidth(1);
                        centerWidget.setFillColor(getColor());


                        for (Path fronLine : frontlines) {
                            //FIXME the color of the segment linking the argument to the center should be specific of the role
                            fronLine.setStrokeColor(getColor());
                            fronLine.setStrokeWidth(1);
                        }

                        for (Path backline : backlines) {
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
    private final AnnotationSelections selectedRelations;

    public RelationDisplayer(DocumentView docView, DrawingArea canvas, EventBus eventBus, AnnotationSelections selectedRelations) {
        super(docView, canvas, eventBus);
        this.selectedRelations = selectedRelations;
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
        getEventBus().fireEvent(new RelationSelectionEmptiedEvent(getAnnotatedTextHandler()));
    }

    @Override
    public void addAnnotation(Annotation relation) {
        if (getAnnotatedDoc() != null) {

            ArrayList<String> annotationIds = new ArrayList<String>();
            ArrayList<String> roles = new ArrayList<String>();

            AnnotationTypeDefinition typedef = getAnnotatedDoc().getAnnotationSchema().getAnnotationTypeDefinition(relation.getAnnotationType());
            ArrayList<Rect> annotationClips = new ArrayList<Rect>();


            //check that relation is well formed (has at least one member)
            boolean canBeDisplayed = !relation.getRelation().getRoles().isEmpty();

            //the annotation can be displayed if the other annotations it refers to are already (Of course, no cyclic ref allowed)
            for (String role : relation.getRelation().getRoles()) {
                AnnotationReference aRef = relation.getRelation().getArgumentRef(role);
                Annotation argument = getAnnotatedDoc().getAnnotation(aRef.getAnnotationId());
                
                String annotationId = argument.getId();
                annotationIds.add(annotationId);
                roles.add(role);
                if (argument.getAnnotationKind().equals(AnnotationKind.TEXT)) {
                    Rect clip = getDocumentView().getAnnotatedTextClip(annotationId);
                    clip.left -= getCanvas().getAbsoluteLeft();
                    clip.top -= getCanvas().getAbsoluteTop();
                    annotationClips.add(clip);
                } else {
                    //if current annotation is not a TEXT annotation, it may already displayed by one of the CombinedAnnotationWidget

                    //FIXME : bad design
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
                RelationWidget relGroup = new RelationWidget(relation.getId(), typedef, annotationIds, annotationClips, roles);
                addWidget(relation.getId(), relGroup);
                //add at the background
                getCanvas().insert(relGroup, 0);
            } else {
                //FIXME not I18N
                GWT.log("Relation " + relation.getId() + " can not be displayed!");
                //eventBus.fireEvent(new InformationReleasedEvent("Relation " + relation.getId() + " can not be displayed!"));
            }
        }
    }
}
