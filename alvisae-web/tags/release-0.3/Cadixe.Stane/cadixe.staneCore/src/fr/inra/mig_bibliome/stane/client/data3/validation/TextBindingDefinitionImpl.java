/*
 *
 *      This software is a result of Quaero project and its use must respect the rules of the Quaero Project Consortium Agreement.
 *
 *      Copyright Institut National de la Recherche Agronomique, 2010.
 *
 */
package fr.inra.mig_bibliome.stane.client.data3.validation;

import com.google.gwt.core.client.JavaScriptObject;
import fr.inra.mig_bibliome.stane.shared.data3.validation.TextBindingDefinition;

/**
 *
 * @author fpapazian
 */
public class TextBindingDefinitionImpl extends JavaScriptObject implements TextBindingDefinition {

    protected TextBindingDefinitionImpl() {
    }

    @Override
    public final native int getMinFragments() /*-{ return this.minFrag; }-*/;

    @Override
    public final native int getMaxFragments() /*-{ return this.maxFrag; }-*/;

    @Override
    public final native String getBoundariesReferenceType() /*-{ return this.boundRef; }-*/;

    @Override
    public final native boolean isCrossingAllowed() /*-{ return this.crossingAllowed; }-*/;
}
