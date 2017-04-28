/*
 *
 *      This software is a result of Quaero project and its use must respect the rules of the Quaero Project Consortium Agreement.
 *
 *      Copyright Institut National de la Recherche Agronomique, 2010-2011.
 *
 */
package fr.inra.mig_bibliome.stane.shared.data3;

/**
 *
 * @author fpapazian
 */
public interface DocumentInfo {

    /**
     *
     * @return the Id of this document
     */
    int getId();

    String getDescription();

    String getStartedAt();

    String getFinishedAt();
}
