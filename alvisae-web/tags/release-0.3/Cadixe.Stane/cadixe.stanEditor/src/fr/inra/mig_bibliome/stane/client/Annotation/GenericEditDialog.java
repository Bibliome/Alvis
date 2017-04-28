/*
 *
 *      This software is a result of Quaero project and its use must respect the rules of the Quaero Project Consortium Agreement.
 *
 *      Copyright Institut National de la Recherche Agronomique, 2010-2011.
 *
 */
package fr.inra.mig_bibliome.stane.client.Annotation;

import fr.inra.mig_bibliome.stane.client.data3.AnnotatedTextProcessor;
import java.util.ArrayList;
import fr.inra.mig_bibliome.stane.shared.data3.Annotation;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.TableSectionElement;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.Widget;
import fr.inra.mig_bibliome.stane.client.Config.StaneClientBaseGinInjector;
import fr.inra.mig_bibliome.stane.client.Document.Blinker;
import fr.inra.mig_bibliome.stane.client.data3.AnnotatedTextHandler;
import fr.inra.mig_bibliome.stane.client.data3.validation.BasicAnnotationSchemaValidator;
import fr.inra.mig_bibliome.stane.shared.data3.AnnotationKind;
import fr.inra.mig_bibliome.stane.shared.data3.validation.AnnotationTypeDefinition;
import java.util.List;

/**
 * About dialog box to show credits and copyrights
 * @author fpapazian
 */
public abstract class GenericEditDialog extends PopupPanel {

    protected static final StaneClientBaseGinInjector injector = GWT.create(StaneClientBaseGinInjector.class);

    interface Binder extends UiBinder<Widget, GenericEditDialog> {
    }
    private static final Binder binder = GWT.create(Binder.class);
    private static BasicAnnotationSchemaValidator validator = new BasicAnnotationSchemaValidator();

    interface Styles extends CssResource {

        String Width100pct();

        String Hidden();
        
        String PopupMaxWidth();
    }
    private boolean applied;
    private Command applyCommand = null;
    private final List<AnnotationTypeDefinition> annTypeDefs;
    private List<String> annotationIds;
    private final AnnotatedTextHandler annotatedText;
    private Annotation mainAnnotation = null;
    private final Blinker warnBlinker;
    private final Blinker errorBlinker;
    @UiField
    Label title;
    @UiField
    TableSectionElement idPanel;
    @UiField
    HTML annotationId;
    @UiField
    HTML annotationType;
    @UiField
    HTML annotationDetail;
    @UiField
    ListBox annotationTypes;
    @UiField
    Grid argumentsGrid;
    @UiField
    PushButton cancelModifBtn;
    @UiField
    PushButton applyModifBtn;
    @UiField
    Image warnImage;
    @UiField
    Image errorImage;
    @UiField
    Styles style;

    public GenericEditDialog(AnnotatedTextHandler annotatedText, AnnotationKind kind) {
        super(false, true);
        setWidget(binder.createAndBindUi(this));
        this.addStyleName(style.PopupMaxWidth());
        this.annotatedText = annotatedText;

        annTypeDefs = annotatedText.getAnnotatedText().getAnnotationSchema().getAnnotationTypeDefinition(kind);
        for (AnnotationTypeDefinition type : annTypeDefs) {
            annotationTypes.addItem(type.getType());
        }
        setAnimationEnabled(true);
        setGlassEnabled(true);

        warnBlinker = new Blinker(warnImage);
        errorBlinker = new Blinker(errorImage);
    }

    public void setText(String title) {
        this.title.setText(title);
    }

    public void setApplyCommand(Command applyCommand) {
        this.applyCommand = applyCommand;
    }

    public void setCreating(List<String> annotationIds) {
        this.annotationIds = annotationIds;
        this.mainAnnotation = null;
        idPanel.addClassName(style.Hidden());
        prepareArgumentGrid(annotationIds);
        applyModifBtn.setFocus(true);
    }

    public void setEditing(Annotation mainAnnotation) {
        List<String> annIds = new ArrayList<String>();
        for (Annotation a : getAnnotatedTextHandler().getAnnotations()) {
            String otherAnnotationId = a.getId();
            //FIXME : filter out any NON-referenceable Annotation
            //filter out Formatting annotation
            if (!getAnnotatedTextHandler().isFormattingAnnotation(otherAnnotationId)) {
                //filter out Annotation referencing the main annoation being edited
                if (!getAnnotatedTextHandler().isEqualToOrReferencedBy(mainAnnotation.getId(), otherAnnotationId)) {
                    annIds.add(otherAnnotationId);
                }
            }
        }

        //display the type of the edited annotation
        for (int i = 0; i < annotationTypes.getItemCount(); i++) {
            if (mainAnnotation.getAnnotationType().equals(annotationTypes.getItemText(i))) {
                annotationTypes.setSelectedIndex(i);
                break;
            }
        }

        this.annotationIds = annIds;
        this.mainAnnotation = mainAnnotation;
        idPanel.removeClassName(style.Hidden());
        SafeHtmlBuilder sbId = new SafeHtmlBuilder();
        CombinedAnnotationCell.renderKind(mainAnnotation.getAnnotationKind(), sbId);
        sbId.appendHtmlConstant("&nbsp;");
        sbId.appendEscaped(AnnotatedTextProcessor.getBriefId(mainAnnotation.getId()));
        this.annotationId.setHTML(sbId.toSafeHtml());
        SafeHtmlBuilder sbDetail = new SafeHtmlBuilder();
        CombinedAnnotationCell.renderDetail(getAnnotatedTextHandler(), mainAnnotation, sbDetail);
        this.annotationDetail.setHTML(sbDetail.toSafeHtml());
        SafeHtmlBuilder sbKind = new SafeHtmlBuilder();
        AnnotationSchemaCell.renderType(mainAnnotation.getAnnotationType(), sbKind);
        this.annotationType.setHTML(sbKind.toSafeHtml());
        prepareArgumentGrid(annotationIds);
        applyModifBtn.setFocus(true);
    }

    protected void setWarning(String message) {
        if (message != null) {
            warnBlinker.start();
            warnImage.setTitle(message);
        } else {
            warnBlinker.cancel();
            warnImage.setVisible(false);
            warnImage.setTitle("");
        }
    }

    protected void setError(String message) {
        if (message != null) {
            errorBlinker.start();
            errorImage.setTitle(message);
        } else {
            errorBlinker.cancel();
            errorImage.setVisible(false);
            errorImage.setTitle("");
        }
    }

    protected abstract void prepareArgumentGrid(List<String> annotationIds);

    protected abstract void validateAnnotation();

    public AnnotatedTextHandler getAnnotatedTextHandler() {
        return annotatedText;
    }

    public static BasicAnnotationSchemaValidator getAnnotationSchemaValidator() {
        return validator;
    }

    public Annotation getMainAnnotation() {
        return mainAnnotation;
    }

    protected List<AnnotationTypeDefinition> getAnnotationTypes() {
        return annTypeDefs;
    }

    protected String getAnnotationType() {
        return getAnnotationTypeDef() != null ? getAnnotationTypeDef().getType() : null;
    }

    protected AnnotationTypeDefinition getAnnotationTypeDef() {
        int index = annotationTypes.getSelectedIndex();
        if (index == -1) {
            return null;
        } else {
            return annTypeDefs.get(index);
        }
    }

    protected String getShortDesc(String annotationId) {
        Annotation annotation = getAnnotatedTextHandler().getAnnotation(annotationId);
        StringBuilder result = new StringBuilder();
        switch (annotation.getAnnotationKind()) {
            case TEXT:
                result.append("TXT");
                break;
            case GROUP:
                result.append("GRP");
                break;
            case RELATION:
                result.append("REL");
                break;
        }
        result.append(" /").append(annotation.getAnnotationType()).append("/ ").append(AnnotatedTextProcessor.getBriefId(annotationId));
        return result.toString();
    }

    private void cancel() {
        applied = false;
        hide();
    }

    private void apply() {
        if (applyCommand != null) {
            applyCommand.execute();
        }
        applied = true;
        hide();
    }

    public boolean isApplied() {
        return applied;
    }

    @Override
    protected void onPreviewNativeEvent(NativePreviewEvent preview) {
        super.onPreviewNativeEvent(preview);
        NativeEvent evt = preview.getNativeEvent();
        if (evt.getType().equals("keydown")) {
            switch (evt.getKeyCode()) {
                case KeyCodes.KEY_ENTER:
                    apply();
                    break;
                case KeyCodes.KEY_ESCAPE:
                    cancel();
                    break;
            }
        }
    }

    @UiHandler("cancelModifBtn")
    void onCancelClicked(ClickEvent event) {
        cancel();
    }

    @UiHandler("applyModifBtn")
    void onApplyClicked(ClickEvent event) {
        apply();
    }

    @UiHandler("annotationTypes")
    void onAnnotationTypeChanged(ChangeEvent event) {
        prepareArgumentGrid(annotationIds);
    }
}
