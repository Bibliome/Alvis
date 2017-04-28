/*
 *
 *      This software is a result of Quaero project and its use must respect the rules of the Quaero Project Consortium Agreement.
 *
 *      Copyright Institut National de la Recherche Agronomique, 2010-2012.
 *
 */
package fr.inra.mig_bibliome.stane.shared.data3;

/**
 * @author fpapazian
 */
public interface DetailedResponse {
    
    public final int TermExists = 1;

    public int getMessageNumber();

    public String getMessageText();
}