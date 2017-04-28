/*
 *
 *      This software is a result of Quaero project and its use must respect the rules of the Quaero Project Consortium Agreement.
 *
 *      Copyright Institut National de la Recherche Agronomique, 2010.
 *
 */
package fr.inra.mig_bibliome.stane.client.data3.validation;

import com.google.gwt.core.client.JavaScriptObject;
import fr.inra.mig_bibliome.stane.shared.data3.AnnotationKind;
import fr.inra.mig_bibliome.stane.shared.data3.validation.AnnotationGroupDefinition;
import fr.inra.mig_bibliome.stane.shared.data3.validation.AnnotationTypeDefinition;
import fr.inra.mig_bibliome.stane.shared.data3.validation.PropertiesDefinition;
import fr.inra.mig_bibliome.stane.shared.data3.validation.RelationDefinition;
import fr.inra.mig_bibliome.stane.shared.data3.validation.TextBindingDefinition;

/**
 *
 * @author fpapazian
 */
public class AnnotationTypeDefinitionImpl extends JavaScriptObject implements AnnotationTypeDefinition {

    protected AnnotationTypeDefinitionImpl() {
    }

    @Override
    public final native String getType() /*-{ return this.type; }-*/;

    @Override
    public final AnnotationKind getAnnotationKind() {
        return AnnotationKind.values()[_getKind()];
    }

    private final native int _getKind() /*-{ return this.kind; }-*/;

    public final native String getColor() /*-{ return this.color; }-*/;

    @Override
    public final native PropertiesDefinition getPropertiesDefinition() /*-{ return this.propDef; }-*/;

    @Override
    public final native TextBindingDefinition getTextBindingDefinition() /*-{ return this.txtBindingDef; }-*/;

    @Override
    public final native AnnotationGroupDefinition getAnnotationGroupDefinition() /*-{ return this.groupDef; }-*/;

    @Override
    public final native RelationDefinition getRelationDefinition() /*-{ return this.relationDef; }-*/;
}
