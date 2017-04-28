/*
 *
 *      This software is a result of Quaero project and its use must respect the rules of the Quaero Project Consortium Agreement.
 *
 *      Copyright Institut National de la Recherche Agronomique, 2010.
 *
 */
package fr.inra.mig_bibliome.stane.client.Annotation;

import fr.inra.mig_bibliome.stane.client.Document.AnnotationDocumentViewMapper;
import fr.inra.mig_bibliome.stane.client.Events.EditHappenedEvent;
import fr.inra.mig_bibliome.stane.client.Events.TextAnnotationSelectionChangedEvent;
import fr.inra.mig_bibliome.stane.client.Events.AnnotationSelectionChangedEventHandler;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;
import fr.inra.mig_bibliome.stane.client.Annotation.Metadata.PropertyGrid;
import fr.inra.mig_bibliome.stane.client.Config.GlobalStyles;
import fr.inra.mig_bibliome.stane.client.Config.StaneClientBaseGinInjector;
import fr.inra.mig_bibliome.stane.client.Edit.AnnotationModificationEdit;
import fr.inra.mig_bibliome.stane.client.Events.EditHappenedEventHandler;
import fr.inra.mig_bibliome.stane.client.Events.GenericAnnotationSelectionChangedEvent;
import fr.inra.mig_bibliome.stane.client.data3.AnnotatedTextHandler;
import fr.inra.mig_bibliome.stane.client.data3.AnnotatedTextProcessor;
import fr.inra.mig_bibliome.stane.shared.data3.AnnotatedText;
import fr.inra.mig_bibliome.stane.shared.data3.Annotation;
import fr.inra.mig_bibliome.stane.shared.data3.AnnotationKind;
import fr.inra.mig_bibliome.stane.shared.data3.Fragment;

/**
 *
 * @author fpapazian
 */
public class AnnotationDetailsUi extends Composite implements AnnotationSelectionChangedEventHandler, EditHappenedEventHandler {

    interface AnnotationDetailsUiBinder extends UiBinder<Widget, AnnotationDetailsUi> {
    }
    private static AnnotationDetailsUiBinder uiBinder = GWT.create(AnnotationDetailsUiBinder.class);
    //
    private static final StaneClientBaseGinInjector injector = GWT.create(StaneClientBaseGinInjector.class);

    interface Styles extends CssResource {

        String SmallerTreeItem();
    }
    //
    @UiField
    Label annotationLabel;
    @UiField
    Label annotationId;
    @UiField
    ScrollPanel annotationTargetPanel;
    @UiField
    PropertyGrid propertyGrid;
    @UiField
    Styles style;

    private AnnotatedTextHandler annotatedText;
    private Annotation annotation;
    private String mainSelectedMark;

    public AnnotationDetailsUi() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    public void displayAnnotationList(AnnotatedTextHandler annotatedText, Annotation annotation, String mainSelectedMark) {

        annotationTargetPanel.clear();

        this.annotatedText =annotatedText;
        this.annotation = annotation;
        this.mainSelectedMark = mainSelectedMark;
        if (annotatedText != null && annotation != null) {
            annotationLabel.setText("Selected Annotation : ");
            annotationId.setText(AnnotatedTextProcessor.getBriefId(annotation.getId()));

            Tree t = new Tree();
            t.addStyleName(style.SmallerTreeItem());
            Label mainSelected = null;
            int tnum = 0;
            int i = 0;

            AnnotationDocumentViewMapper mapper = AnnotationDocumentViewMapper.getMapper(annotatedText.getAnnotatedText());
            if (annotation.getAnnotationKind().equals(AnnotationKind.TEXT)) {


                for (Fragment f : annotation.getTextBinding().getFragments()) {

                    tnum++;
                    i++;
                    TreeItem root = new TreeItem(String.valueOf(tnum) + ".  [" + f.getStart() + ".." + f.getEnd() + "]");

                    for (String s : mapper.getMarkerIdsForFragment(annotation.getId(), f)) {
                        i++;
                        Element elt = Document.get().getElementById(s);

                        StringBuilder label = new StringBuilder();
                        label.append("{").append(s).append("}\t").append(elt.getInnerText());
                        if (s.equals(mainSelectedMark)) {
                            mainSelected = new Label(label.toString());
                            mainSelected.addStyleName(GlobalStyles.SelectedRelation.getOutlineStyleName());
                            root.addItem(mainSelected);
                        } else {
                            root.addItem(label.toString());
                        }

                    }
                    root.setState(true);
                    t.addItem(root);
                }
                annotationTargetPanel.add(t);
                if (mainSelected != null) {
                    annotationTargetPanel.ensureVisible(mainSelected);
                }
            }

            propertyGrid.display(annotatedText, annotation);
        } else {
            propertyGrid.display(null, null);
            annotationLabel.setText("no selected annotation");
            annotationId.setText("");
        }
    }

    @Override
    public void onAnnotationSelectionChanged(GenericAnnotationSelectionChangedEvent event) {
        String mainMarker = null;
        if (event instanceof TextAnnotationSelectionChangedEvent) {
            mainMarker = ((TextAnnotationSelectionChangedEvent) event).getMainSelectedMarker();
        }
        displayAnnotationList(event.getAnnotatedTextHandler(), event.getMainSelectedAnnotation(), mainMarker);
    }

    @Override
    public void onEditHappened(EditHappenedEvent event) {
        if (event.getEdit() instanceof AnnotationModificationEdit) {
            AnnotationModificationEdit edit = (AnnotationModificationEdit) event.getEdit();
            Annotation editedAnnotation = edit.getAnnotation();
            if ((editedAnnotation.getAnnotatedText().equals(annotatedText)) && (editedAnnotation.getId().equals(annotation.getId()))) {
                displayAnnotationList(annotatedText, editedAnnotation, mainSelectedMark);
            }
        }
    }

    @Override
    protected void onAttach() {
        super.onAttach();
        TextAnnotationSelectionChangedEvent.register(injector.getMainEventBus(), this);
        EditHappenedEvent.register(injector.getMainEventBus(), this);
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        TextAnnotationSelectionChangedEvent.unregister(this);
        EditHappenedEvent.unregister(this);
    }
}
