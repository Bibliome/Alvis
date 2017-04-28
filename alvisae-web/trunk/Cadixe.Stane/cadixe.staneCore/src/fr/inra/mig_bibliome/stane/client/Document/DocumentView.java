/*
 *
 *      This software is a result of Quaero project and its use must respect the rules of the Quaero Project Consortium Agreement.
 *
 *      Copyright Institut National de la Recherche Agronomique, 2010-2012.
 *
 */
package fr.inra.mig_bibliome.stane.client.Document;

import com.google.gwt.dom.client.Element;
import com.google.gwt.json.client.*;
import com.google.gwt.user.client.ui.IsWidget;
import fr.inra.mig_bibliome.stane.client.Edit.undo.UndoManager;
import fr.inra.mig_bibliome.stane.client.data3.AnnotatedTextHandler;
import fr.inra.mig_bibliome.stane.client.data3.DOMRange;
import fr.inra.mig_bibliome.stane.shared.data3.AnnotatedText;
import java.util.List;

/**
 * Interface of the classes that can display document text and the related
 * annotations
 *
 * @author fpapazian
 */
public interface DocumentView extends IsWidget {

    public static class Rect {

        int left;
        int top;
        int width;
        int height;

        public Rect(int left, int top, int width, int height) {
            this.left = left;
            this.top = top;
            this.width = width;
            this.height = height;
        }
    }

    public static class Options {

        private boolean readOnly;
        private boolean hiddenToolbar;
        private boolean collapsedToolbar;
        private boolean hiddenTitlebar;
        private boolean hiddenOccurencebar;
        private Integer interlineSizeIndex = null;

        public Options(boolean readOnly, boolean hideToolbar, boolean collapsedToolbar, boolean hideTitlebar, boolean hideOccurencebar, Integer interlineSizeIndex) {
            this.readOnly = readOnly;
            this.hiddenToolbar = hideToolbar;
            this.collapsedToolbar = collapsedToolbar;
            this.hiddenTitlebar = hideTitlebar;
            this.hiddenOccurencebar = hideOccurencebar;
            this.interlineSizeIndex = interlineSizeIndex;
        }

        public Options() {
            this(true, false, false, false, false, null);
        }

        public Options(Options options) {
            this(options.isReadOnly(), options.isHiddenToolbar(), options.isCollapsedToolbar(), options.isHiddenTitlebar(), options.isHiddenOccurencebar(), options.getInterlineSizeIndex());
        }

        public Options(String jsonOptionsStr) {
            this();
            if (jsonOptionsStr != null) {
                JSONValue opt = JSONParser.parseStrict(jsonOptionsStr);
                JSONObject options = opt.isObject();

                if (options != null) {
                    for (String optKey : options.keySet()) {
                        JSONBoolean optVal = options.get(optKey).isBoolean();
                        if (optVal != null) {
                            if ("readOnly".equals(optKey)) {
                                setReadOnly(optVal.booleanValue());
                            } else if ("hiddenToolbar".equals(optKey)) {
                                setHideToolbar(optVal.booleanValue());
                            } else if ("hiddenTitlebar".equals(optKey)) {
                                setHideTitlebar(optVal.booleanValue());
                            } else if ("hiddenOccurencebar".equals(optKey)) {
                                setHideOccurencebar(optVal.booleanValue());
                            } else if ("collapsedToolbar".equals(optKey)) {
                                setCollapsedToolbar(optVal.booleanValue());
                            }
                        } else {
                            JSONNumber optNum = options.get(optKey).isNumber();
                            if ("interlineSizeIndex".equals(optKey)) {
                                setInterlineSizeIndex(new Double(optNum.doubleValue()).intValue());
                            }
                        }
                    }
                }
            }
        }

        public boolean isCollapsedToolbar() {
            return collapsedToolbar;
        }

        public boolean isHiddenToolbar() {
            return hiddenToolbar;
        }

        public boolean isHiddenTitlebar() {
            return hiddenTitlebar;
        }

        public boolean isHiddenOccurencebar() {
            return hiddenOccurencebar;
        }

        public boolean isReadOnly() {
            return readOnly;
        }

        public Integer getInterlineSizeIndex() {
            return interlineSizeIndex;
        }

        protected void setCollapsedToolbar(boolean collapsedToolbar) {
            this.collapsedToolbar = collapsedToolbar;
        }

        protected void setHideToolbar(boolean hideToolbar) {
            this.hiddenToolbar = hideToolbar;
        }

        protected void setHideTitlebar(boolean hiddenTitlebar) {
            this.hiddenTitlebar = hiddenTitlebar;
        }

        protected void setHideOccurencebar(boolean hiddenOccurencebar) {
            this.hiddenOccurencebar = hiddenOccurencebar;
        }

        protected void setReadOnly(boolean readOnly) {
            this.readOnly = readOnly;
        }

        protected void setInterlineSizeIndex(Integer interlineSizeIndex) {
            this.interlineSizeIndex = interlineSizeIndex;
        }
    }

    /**
     * Set the AnnotatedText displayed by the view
     *
     * @param document the document that will be displayed with its associated
     * annotations
     * @param readOnly true to disable any Annotation editing
     */
    public void setDocument(AnnotatedTextHandler document, boolean readOnly);

    public void setDocument(AnnotatedTextHandler document, Options options);

    /**
     *
     * @return the AnnotatedText currently displayed by this view
     */
    public AnnotatedText getDocument();

    public AnnotatedTextHandler getAnnotatedTextHandler();

    public UndoManager getUndoManager();

    /**
     * Enable or Disable the possibility of creating/remove/modifying annotation
     * via this view
     *
     * @param readOnly true to disable any Annotation editing
     */
    public void setReadOnly(boolean readOnly);

    /**
     *
     * @return true if Annotation editing is disabled
     */
    public boolean isReadOnly();

    public void createAnchorMarkersFromSelectedRanges();

    public void clearTextSelection(boolean setFocus);

    public void clearAnchorMarkerSelection();

    public void createAnchorMarkersFromRanges(String annotationType, List<DOMRange> ranges);

    public AnnotationDocumentViewMapper getMapper();

    /**
     * @return true if the point specified by x,y coordinates is inside this
     * view
     */
    public boolean isPointInside(int x, int y);

    /**
     * Reduce the coverage of the main selected Annotation by the specified text
     * range
     *
     * @param ranges
     */
    public void pruneRangeFromSelectedAnnotation(List<DOMRange> ranges);

    /**
     * Extend the coverage of the main selected Annotation with the specified
     * text range
     *
     * @param ranges
     */
    public void extendSelectedAnnotationWithRange(List<DOMRange> ranges);

    /**
     * @param annotationId
     * @return The coordinate of the rectangular area occupied by a Text
     * Annotation. In case of annotation spanning over several line, the area is
     * the beginning of the annotation on the first line.
     */
    public Rect getAnnotatedTextClip(String annotationId);

    /**
     * @param Element representing the Text annotation (span)
     * @return The coordinate of the rectangular area occupied by a Text
     * Annotation. In case of annotation spanning over several line, the area is
     * the beginning of the annotation on the first line.
     */
    public Rect getAnnotatedTextClip(Element marker);

    /**
     *
     * @return the Id of the HTML Element containing the Text of the displayed
     * document
     */
    public String getTextContainerId();
}
