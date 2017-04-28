/*
 *
 *      This software is a result of Quaero project and its use must respect the rules of the Quaero Project Consortium Agreement.
 *
 *      Copyright Institut National de la Recherche Agronomique, 2010-2012.
 *
 */
package fr.inra.mig_bibliome.stane.client.data3;

import fr.inra.mig_bibliome.stane.shared.data3.AnnotationSchemaDefinition;
import fr.inra.mig_bibliome.stane.shared.data3.AnnotationSchemaDefinition.TypeUrlEntry;
import fr.inra.mig_bibliome.stane.shared.data3.validation.PropType_TyDISemClassRef;
import fr.inra.mig_bibliome.stane.shared.data3.validation.PropType_TyDITermRef;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author fpapazian
 */
public class AnnotationSchemaDefHandler {

    private final AnnotationSchemaDefinition schemaDefinition;
    private Map<String, Map<String, TypeUrlEntry>> tyDIResReferencingTypes = null;

    public AnnotationSchemaDefHandler(AnnotationSchemaDefinition schemaDefinition) {
        this.schemaDefinition = schemaDefinition;
    }

    private void initTyDIResReferencingTypes() {
        tyDIResReferencingTypes = schemaDefinition.getTyDIResourceReferencingTypes();
    }

    public AnnotationSchemaDefinition getSchemaDefinition() {
        return schemaDefinition;
    }

    /**
     * 
     * @return true if this Schema allows some Annotation to reference external TyDI resource such as Term or Semantic classes
     */
    public boolean enableTyDIResourceRef() {
        if (tyDIResReferencingTypes == null) {
            initTyDIResReferencingTypes();
        }
        return !tyDIResReferencingTypes.isEmpty();
    }

    /**
     * 
     * @return true if the specified annotationType allows to reference external TyDI resource such as Term or Semantic classes
     */
    public boolean isTyDIResReferencingType(String annotationType) {
        if (tyDIResReferencingTypes == null) {
            initTyDIResReferencingTypes();
        }
        return tyDIResReferencingTypes.keySet().contains(annotationType);
    }

    public String getTyDITermRefPropName(String annotationType) {
        String result = null;
        Map<String, TypeUrlEntry> forType = tyDIResReferencingTypes.get(annotationType);
        if (forType != null) {
            for (Entry<String, TypeUrlEntry> e : forType.entrySet()) {
                if (PropType_TyDITermRef.NAME.equals(e.getValue().getTypeName())) {
                    result = e.getKey();
                    break;
                }
            }
        }
        return result;
    }
        public String getTyDISemClassRefPropName(String annotationType) {
        String result = null;
        Map<String, TypeUrlEntry> forType = tyDIResReferencingTypes.get(annotationType);
        if (forType != null) {
            for (Entry<String, TypeUrlEntry> e : forType.entrySet()) {
                if (PropType_TyDISemClassRef.NAME.equals(e.getValue().getTypeName())) {
                    result = e.getKey();
                    break;
                }
            }
        }
        return result;
    }

    /**
     * 
     * @return the set of the distinct base URL pointing to external TyDI resource 
     */
    public HashSet<String> getTyDIResourceBaseURLs() {
        HashSet<String> result = new HashSet<String>();
        for (Map<String, TypeUrlEntry> forType : tyDIResReferencingTypes.values()) {
            for (TypeUrlEntry e : forType.values()) {
                result.add(e.getUrl());
            }
        }
        return result;
    }
}
