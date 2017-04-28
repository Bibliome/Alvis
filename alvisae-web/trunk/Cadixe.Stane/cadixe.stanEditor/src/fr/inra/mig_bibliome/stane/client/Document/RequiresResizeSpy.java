/*
 *
 *      This software is a result of Quaero project and its use must respect the rules of the Quaero Project Consortium Agreement.
 *
 *      Copyright Institut National de la Recherche Agronomique, 2010.
 *
 */

package fr.inra.mig_bibliome.stane.client.Document;

import com.google.gwt.event.logical.shared.HasResizeHandlers;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.LayoutPanel;


/**
 * Convenience class used to track resize events
 * @author fpapazian
 */
public class RequiresResizeSpy extends LayoutPanel implements HasResizeHandlers {
    
    public HandlerRegistration addResizeHandler(ResizeHandler handler) {
        return addHandler(handler, ResizeEvent.getType());
    }

    @Override
    public void onResize() {
        ResizeEvent.fire(this, this.getOffsetWidth(), this.getOffsetHeight());
    }
}
