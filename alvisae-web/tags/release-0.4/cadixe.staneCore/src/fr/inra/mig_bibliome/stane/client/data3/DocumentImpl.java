/*
 *
 *      This software is a result of Quaero project and its use must respect the rules of the Quaero Project Consortium Agreement.
 *
 *      Copyright Institut National de la Recherche Agronomique, 2010-2012.
 *
 */
package fr.inra.mig_bibliome.stane.client.data3;

import com.google.gwt.core.client.JavaScriptObject;
import fr.inra.mig_bibliome.stane.shared.data3.Document;
import fr.inra.mig_bibliome.stane.shared.data3.Properties;


/**
 *
 * @author fpapazian
 */
public class DocumentImpl extends JavaScriptObject implements Document {

    protected DocumentImpl() {
    }

    @Override
    public final native int getId() /*-{ return this.id; }-*/;

    @Override
    public final native String getDescription() /*-{ return this.description; }-*/;

    @Override
    public final native String getContents()  /*-{ return this.contents; }-*/;

    @Override
    public final native void setContents(String contents) /*-{ this.contents=contents; }-*/;

    @Override
    public final native int getOwner() /*-{ return this.owner; }-*/;

    @Override
    public final native Properties getProperties() /*-{ return this.props; }-*/;

    private final static String INVALIDFIELD_PREFIX = "Invalid field";
    
    /**
     * Check that the Document parsed from a JSON string conforms to the expected structure 
     * @throws IllegalArgumentException
     */
    public final void checkStructure() {
        Integer id = null;
        try {
            id = getId();
        } catch (Exception ex) {
            throw new IllegalArgumentException(INVALIDFIELD_PREFIX + "Document.Id", ex);
        }
        if (id == null) {
            throw new IllegalArgumentException(INVALIDFIELD_PREFIX + "-> Document.Id should not be empty");
        }
        try {
            try {
                String content = getContents();
                if (content == null) {
                    throw new IllegalArgumentException(INVALIDFIELD_PREFIX + "-> Content should not be empty");
                }
            } catch (IllegalArgumentException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new IllegalArgumentException(INVALIDFIELD_PREFIX + ".Content", ex);
            }
            try {
                String description = getDescription();
            } catch (Exception ex) {
                throw new IllegalArgumentException(INVALIDFIELD_PREFIX + "Description", ex);
            }
            try {
                int owner = getOwner();
            } catch (Exception ex) {
                throw new IllegalArgumentException(INVALIDFIELD_PREFIX + "Owner", ex);
            }
            try {
                Properties props = getProperties();
            } catch (Exception ex) {
                throw new IllegalArgumentException(INVALIDFIELD_PREFIX + "Properties", ex);
            }
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(INVALIDFIELD_PREFIX + " in Document Id=" + id, ex);
        }
    }

}
