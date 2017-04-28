/*
 *
 *      This software is a result of Quaero project and its use must respect the rules of the Quaero Project Consortium Agreement.
 *
 *      Copyright Institut National de la Recherche Agronomique, 2010-2011.
 *
 */
package fr.inra.mig_bibliome.stane.shared.data3.validation;

/**
 *
 * @author fpapazian
 */
public interface PropType_TyDISemClassRef extends PropTypeType {

    public final static String NAME = "TyDI_semClassRef";
    
    /**
     *
     * @return the base URL used as TyDI external reference
     */
    String getSemClassRefBaseURL();
    
}
