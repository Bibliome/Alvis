/*
 *
 *      This software is a result of Quaero project and its use must respect the rules of the Quaero Project Consortium Agreement.
 *
 *      Copyright Institut National de la Recherche Agronomique, 2010.
 *
 */
package fr.inra.mig_bibliome.stane.client.Annotation.Metadata;

import com.google.gwt.user.client.ui.Grid;
import fr.inra.mig_bibliome.stane.client.data3.AnnotatedTextHandler;
import fr.inra.mig_bibliome.stane.shared.data3.Annotation;
import fr.inra.mig_bibliome.stane.shared.data3.AnnotationSchemaDefinition;
import fr.inra.mig_bibliome.stane.shared.data3.validation.AnnotationTypeDefinition;
import fr.inra.mig_bibliome.stane.shared.data3.validation.PropertyDefinition;
import java.util.Collection;

/**
 * UI component used to display Annotation properties in a tabular form
 * @author fpapazian
 */
public class PropertyGrid extends Grid {

    private Annotation annotation;

    public PropertyGrid() {
        super();
        this.resize(1, 2);
        this.setText(0, 0, "Name");
        this.setText(0, 1, "Value");
        PropertyDisplayer pdisp = new PropertyDisplayer(null, null);
        this.getRowFormatter().setStyleName(0, pdisp.getStyle().DetailGridFirstRow());
    }

    /**
     * Display the property of the specified annotation
     */
    public void display(AnnotatedTextHandler document, Annotation annotation) {
        PropertyDisplayer displayer;
        if (annotation == null) {
            this.resize(1, 2);
        } else {
            this.annotation = annotation;
            AnnotationSchemaDefinition schema = annotation.getAnnotatedText().getAnnotationSchema();
            AnnotationTypeDefinition annTypeDef = schema.getAnnotationTypeDefinition(annotation.getAnnotationType());
            Collection<PropertyDefinition> propDefs = null;
            int size = 3;
            if (annTypeDef != null) {
                propDefs = annTypeDef.getPropertiesDefinition().getPropertyDefinitions();
                size += propDefs.size();
            }
            this.resize(size, 2);
            this.setText(1, 0, "id");
            this.setText(1, 1, annotation.getId());
            this.setText(2, 0, "type");
            this.setText(2, 1, annotation.getAnnotationType());
            if (annTypeDef != null) {
                int r = 3;
                for (PropertyDefinition propDef : propDefs) {
                    this.setText(r, 0, propDef.getKey());
                    displayer = new PropertyDisplayer(document, annotation);
                    displayer.setProperty(annotation.getProperties(), propDef);
                    setWidget(r, 1, displayer);
                    r++;
                }
            }
        }
    }
}
