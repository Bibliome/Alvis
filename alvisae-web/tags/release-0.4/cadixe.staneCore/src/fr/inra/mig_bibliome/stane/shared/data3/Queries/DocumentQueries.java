/*
 *
 *      This software is a result of Quaero project and its use must respect the rules of the Quaero Project Consortium Agreement.
 *
 *      Copyright Institut National de la Recherche Agronomique, 2010-2011.
 *
 */
package fr.inra.mig_bibliome.stane.shared.data3.Queries;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.core.client.JavaScriptObject;
import fr.inra.mig_bibliome.stane.client.data3.AnnotatedTextHandler;
import fr.inra.mig_bibliome.stane.client.data3.AnnotatedTextImpl;
import fr.inra.mig_bibliome.stane.client.data3.AnnotationSetListImpl;
import fr.inra.mig_bibliome.stane.client.data3.CDXWS_UserAlvisNLPDocId_Response;
import fr.inra.mig_bibliome.stane.shared.data3.AnnotationSet;
import java.util.Set;

/**
 *
 * @author fpapazian
 */
public interface DocumentQueries {

    // [baseurl] :  bibliome.jouy.inra.fr/test/stane/dev/api
    /**
     *
     * <b>Rest Method & URL:</b>
     * GET http://[baseurl]/user/[userId]/campaign/[campaignId]/document/[documentId]
     *
     * @throws IllegalArgumentException if the specified userId, campaignId or documentId does not exist (400 Bad Request)
     * @throws SecurityException if the current user is not authenticated (401 Unauthorized) or has no authorization to perform the operation (403 Forbidden)
     */
    void getAnnotatedDocument(int userId, int campaignId, int documentId, final AsyncCallback<AnnotatedTextImpl> resultCallback);
    /**
     *
     * <b>Rest Method & URL:</b>
     * GET http://[baseurl]/user/[userId]/AlvisNLPID/[alvisNLPId]
     *
     * @throws IllegalArgumentException if the specified userId or alvisNLPId does not exist (400 Bad Request)
     * @throws SecurityException if the current user is not authenticated (401 Unauthorized) or has no authorization to perform the operation (403 Forbidden)
     */
    void getAnnotatedDocument(final int userId, final String docExternalId, final AsyncCallback<CDXWS_UserAlvisNLPDocId_Response> resultCallback);


    /**
     *
     * <b>Rest Method & URL:</b>
     * GET http://[baseurl]/annotation?ids=[comma separated list of annotationSetId]
     *
     * @throws SecurityException if the current user is not authenticated (401 Unauthorized) or has no authorization to perform the operation (403 Forbidden)
     */
    void getAdditionalAnnotationSet(AnnotatedTextHandler handler, Set<Integer> annotationSetIds, final AsyncCallback<AnnotationSetListImpl> resultCallback);
    
    /**
     * 
     * <b>Rest Method & URL:</b>
     * PUT http://[baseurl]/user/[userId]/campaign/[campaignId]/document/[documentId]
     *
     * @throws IllegalArgumentException if the specified userId, campaignId or documentId does not exist (400 Bad Request)
     * @throws SecurityException if the current user is not authenticated (401 Unauthorized) or has no authorization to perform the operation (403 Forbidden)
     */
    void saveAnnotationSet(int userId, int campaignId, int documentId, AnnotationSet annotationSet, final AsyncCallback<JavaScriptObject> resultCallback);
    
    /**
     * 
     * <b>Rest Method & URL:</b>
     * PUT http://[baseurl]/user/[userId]/campaign/[campaignId]/document/[documentId]/
     *
     * @throws IllegalArgumentException if the specified userId, campaignId or documentId does not exist (400 Bad Request)
     * @throws SecurityException if the current user is not authenticated (401 Unauthorized) or has no authorization to perform the operation (403 Forbidden)
     */
    void finalizeDocument(int userId, int campaignId, int documentId, final AsyncCallback<JavaScriptObject> resultCallback);
    
}
