/*
 *
 *      This software is a result of Quaero project and its use must respect the rules of the Quaero Project Consortium Agreement.
 *
 *      Copyright Institut National de la Recherche Agronomique, 2010-2011.
 *
 */
package fr.inra.mig_bibliome.stane.client.Config.History;

import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.gwt.place.shared.WithTokenizers;
import fr.inra.mig_bibliome.stane.client.Start.DefaultPlace;
import fr.inra.mig_bibliome.stane.client.Document.DocEditingPlace;
import fr.inra.mig_bibliome.stane.client.Campaign.DocSelectingPlace;
import fr.inra.mig_bibliome.stane.client.Document.DocDisplayPlace;
import fr.inra.mig_bibliome.stane.client.SignIn.SignInPlace;
/**
 *
 * @author fpapazian
 */

/**
 * PlaceHistoryMapper interface is used to attach all places which the
 * PlaceHistoryHandler should be aware of. This is done via the @WithTokenizers
 * annotation or by extending PlaceHistoryMapperWithFactory and creating a
 * separate TokenizerFactory.
 */
@WithTokenizers( { DefaultPlace.Tokenizer.class, SignInPlace.Tokenizer.class, DocSelectingPlace.Tokenizer.class, DocEditingPlace.Tokenizer.class, DocDisplayPlace.Tokenizer.class })
public interface StanePlaceHistoryMapper extends PlaceHistoryMapper {
}
