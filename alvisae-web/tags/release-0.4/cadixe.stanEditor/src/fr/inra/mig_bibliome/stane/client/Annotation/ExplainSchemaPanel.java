/*
 *
 *      This software is a result of Quaero project and its use must respect the rules of the Quaero Project Consortium Agreement.
 *
 *      Copyright Institut National de la Recherche Agronomique, 2012.
 *
 */
package fr.inra.mig_bibliome.stane.client.Annotation;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.view.client.NoSelectionModel;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SelectionModel;
import fr.inra.mig_bibliome.stane.shared.data3.AnnotationKind;
import fr.inra.mig_bibliome.stane.shared.data3.AnnotationSchemaDefinition;
import fr.inra.mig_bibliome.stane.shared.data3.validation.AnnotationTypeDefinition;
import java.util.ArrayList;

/**
 *
 * @author fpapazian
 */
public class ExplainSchemaPanel extends Composite {

    public static class AnnotationTypeInfo implements Comparable<AnnotationTypeInfo> {

        public static final ProvidesKey<AnnotationTypeInfo> KEY_PROVIDER = new ProvidesKey<AnnotationTypeInfo>() {

            @Override
            public Object getKey(AnnotationTypeInfo item) {
                return item == null ? null : item.getAnnTypeDef().getType();
            }
        };
        private final AnnotationTypeDefinition annotationTypeDef;

        public AnnotationTypeInfo(AnnotationTypeDefinition annotationTypeDef) {
            this.annotationTypeDef = annotationTypeDef;
        }

        public AnnotationTypeDefinition getAnnTypeDef() {
            return annotationTypeDef;
        }

        @Override
        public final int compareTo(AnnotationTypeInfo o) {
            int result = this.getAnnTypeDef().getAnnotationKind().compareTo(o.getAnnTypeDef().getAnnotationKind());
            if (result == 0) {
                result = this.getAnnTypeDef().getType().compareTo(o.getAnnTypeDef().getType());
            }
            return result;
        }
    }

    public static class AnnotationTypeCell extends AbstractCell<AnnotationTypeInfo> {

        public static interface AnnotationTypeCellTemplates extends SafeHtmlTemplates {

            @SafeHtmlTemplates.Template("<span class='{0}'>{1}</span> ")
            public SafeHtml textAnnotation(String className, String annotationTypeName);

            @SafeHtmlTemplates.Template("<b>{0}</b>")
            public SafeHtml boldedSpan(String text);
        }
        private static final AnnotationTypeCellTemplates TEMPLATES = GWT.create(AnnotationTypeCellTemplates.class);
        private final AnnotationSchemaDefinition schemaDefinition;

        public AnnotationTypeCell(AnnotationSchemaDefinition schemaDefinition) {
            this.schemaDefinition = schemaDefinition;
        }

        @Override
        public void render(Context context, AnnotationTypeInfo value, SafeHtmlBuilder sb) {

            boolean once = false;
            AnnotationKind kind = value.getAnnTypeDef().getAnnotationKind();

            sb.appendHtmlConstant("<div style='border-bottom:1px solid silver; min-height:2.4em; margin: 0.4em;'>");
            sb.appendHtmlConstant("<div>");
            CombinedAnnotationCell.renderKind(value.getAnnTypeDef().getAnnotationKind(), sb);
            String typeName = value.getAnnTypeDef().getType();
            AnnotationSchemaCell.renderNamedType(typeName, sb);
            if (!kind.equals(AnnotationKind.TEXT)) {
                sb.appendHtmlConstant("&nbsp;:");
            }
            sb.appendHtmlConstant("</div>");

            switch (kind) {
                case TEXT:
                    break;

                case GROUP:
                    sb.appendHtmlConstant("<div style='margin-left: 2em; height:auto;'>");
                    sb.append(TEMPLATES.boldedSpan("{ "));
                    for (String ct : value.getAnnTypeDef().getAnnotationGroupDefinition().getComponentsTypes()) {
                        if (once) {
                            sb.append(TEMPLATES.boldedSpan(" / "));
                        } else {
                            once = true;
                        }

                        AnnotationTypeDefinition refAnnType = schemaDefinition.getAnnotationTypeDefinition(ct);
                        CombinedAnnotationCell.renderKind(refAnnType.getAnnotationKind(), sb);
                        sb.appendHtmlConstant("&nbsp;");
                        String refTypeName = refAnnType.getType();
                        AnnotationSchemaCell.renderNamedType(refTypeName, sb);

                    }
                    sb.append(TEMPLATES.boldedSpan(" }"));
                    sb.appendHtmlConstant("</div>");
                    break;
                case RELATION:
                    sb.appendHtmlConstant("<div style='margin-left: 2em; height:auto;'>");
                    for (String role : value.getAnnTypeDef().getRelationDefinition().getSupportedRoles()) {
                        if (once) {
                            sb.append(TEMPLATES.boldedSpan(" + "));
                        } else {
                            once = true;
                        }
                        sb.appendHtmlConstant("<i>").appendEscaped(role).appendHtmlConstant("</i> ");

                        sb.append(TEMPLATES.boldedSpan("( "));

                        boolean severalTypesForRole = false;
                        for (String rolefTypeName : value.getAnnTypeDef().getRelationDefinition().getArgumentTypes(role)) {
                            if (severalTypesForRole) {
                                sb.append(TEMPLATES.boldedSpan(" / "));
                            }
                            AnnotationTypeDefinition refAnnType = schemaDefinition.getAnnotationTypeDefinition(rolefTypeName);
                            CombinedAnnotationCell.renderKind(refAnnType.getAnnotationKind(), sb);
                            sb.appendHtmlConstant("&nbsp;");
                            String refTypeName = refAnnType.getType();
                            AnnotationSchemaCell.renderNamedType(refTypeName, sb);

                            severalTypesForRole = true;
                        }

                        sb.append(TEMPLATES.boldedSpan(" )"));
                    }
                    sb.appendHtmlConstant("</div>");
                    break;
            }

            sb.appendHtmlConstant("</div>");

        }
    }
    private final CellList<AnnotationTypeInfo> cellList;

    public ExplainSchemaPanel(AnnotationSchemaDefinition schemaDefinition) {

        cellList = new CellList<AnnotationTypeInfo>(new AnnotationTypeCell(schemaDefinition));

        SelectionModel<AnnotationTypeInfo> selectionModel = new NoSelectionModel<AnnotationTypeInfo>();
        cellList.setSelectionModel(selectionModel);

        initWidget(new ScrollPanel(cellList));

        ArrayList<AnnotationTypeInfo> annotationTypeDefs = new ArrayList<AnnotationTypeInfo>();
        for (String typeName : schemaDefinition.getAnnotationTypes()) {
            annotationTypeDefs.add(new AnnotationTypeInfo(schemaDefinition.getAnnotationTypeDefinition(typeName)));
        }

        cellList.setRowCount(annotationTypeDefs.size(), true);
        cellList.setRowData(0, annotationTypeDefs);
    }
}
