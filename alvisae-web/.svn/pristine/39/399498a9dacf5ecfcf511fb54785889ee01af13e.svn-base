/*
 *
 *      This software is a result of Quaero project and its use must respect the rules of the Quaero Project Consortium Agreement.
 *
 *      Copyright Institut National de la Recherche Agronomique, 2010-2011.
 *
 */
package fr.inra.mig_bibliome.stane.client.data3;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONObject;
import fr.inra.mig_bibliome.stane.shared.data3.Annotation;
import fr.inra.mig_bibliome.stane.shared.data3.AnnotationReference;
import fr.inra.mig_bibliome.stane.shared.data3.Relation;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 
 * @author fpapazian
 */
public class RelationImpl extends JavaScriptObject implements Relation {

    public static final RelationImpl create() {
        return JavaScriptObject.createObject().cast();
    }

    protected RelationImpl() {
    }

    @Override
    public final Collection<String> getRoles() {
        return new JSONObject(this).keySet();
    }

    @Override
    public final Map<String, AnnotationReference> getRolesArguments() {
        Map<String, AnnotationReference> result = new HashMap<String, AnnotationReference>();
        for (String role : getRoles()) {
            result.put(role, getArgumentRef(role));
        }
        return result;
    }

    public final void setRolesArguments(Map<String, Annotation> rolesArguments) {
        for (Entry<String, Annotation> entry : rolesArguments.entrySet()) {
            if (entry.getKey() == null) {
                throw new NullPointerException("role should not be null");
            } else if (entry.getValue() == null) {
                throw new NullPointerException("argument should not be null");
            }
        }
        for (String role : getRoles()) {
            removeArgument(role);
        }
        for (Entry<String, Annotation> entry : rolesArguments.entrySet()) {
            setArgument(entry.getKey(), entry.getValue(), true);
        }
    }

    @Override
    public final AnnotationReference getArgumentRef(String role) {
        if (role == null) {
            throw new NullPointerException("role should not be null");
        }
        return _getArgumentRef(role);
    }

    private final native AnnotationReferenceImpl _getArgumentRef(String role) /*-{ return this[role]; }-*/;

    @Override
    public final boolean setArgument(String role, Annotation argument, boolean overwrite) {
        if (role == null) {
            throw new NullPointerException("role should not be null");
        } else if (argument == null) {
            throw new NullPointerException("argument should not be null");
        }
        boolean result = !_hasRole(role) || overwrite;
        if (result) {
            _setArgument(role, AnnotationReferenceImpl.create(argument.getId()));
        }
        return result;
    }

    private final native boolean _hasRole(String role) /*-{ return this.hasOwnProperty(role); }-*/;

    private final native void _setArgument(String role, AnnotationReferenceImpl argumentRef) /*-{ this[role]=argumentRef; }-*/;

    @Override
    public final Collection<String> getRoles(Annotation argument) {
        if (argument == null) {
            throw new NullPointerException("argument should not be null");
        }
        List<String> result = null;
        String argumentId = argument.getId();
        for (String role : getRoles()) {
            if (argumentId.equals(_getArgumentRef(role).getAnnotationId())) {
                result.add(role);
            }
        }
        return result;
    }

    @Override
    public final native void removeArgument(String role) /*-{ if (this.hasOwnProperty(role)) { delete this[role]; } }-*/;
    
}
