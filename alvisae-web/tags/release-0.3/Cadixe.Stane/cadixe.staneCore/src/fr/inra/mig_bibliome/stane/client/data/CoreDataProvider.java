/*
 *
 *      This software is a result of Quaero project and its use must respect the rules of the Quaero Project Consortium Agreement.
 *
 *      Copyright Institut National de la Recherche Agronomique, 2010-2011.
 *
 */
package fr.inra.mig_bibliome.stane.client.data;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.user.client.rpc.AsyncCallback;
import fr.inra.mig_bibliome.stane.client.data.Retrieve.RequestManager;
import fr.inra.mig_bibliome.stane.client.data3.AnnotatedTextImpl;
import fr.inra.mig_bibliome.stane.client.data3.AnnotatedTextHandler;
import fr.inra.mig_bibliome.stane.client.data3.AnnotationSetListImpl;
import fr.inra.mig_bibliome.stane.client.data3.Basic_UserMe_Response;
import fr.inra.mig_bibliome.stane.client.data3.CDXWS_UserAlvisNLPDocId_Response;
import fr.inra.mig_bibliome.stane.client.data3.CDXWS_UserCampaignDocuments_Response;
import fr.inra.mig_bibliome.stane.client.data3.CDXWS_UserMe_Response;
import fr.inra.mig_bibliome.stane.client.data3.CampaignListImpl;
import fr.inra.mig_bibliome.stane.client.data3.DocumentInfoListImpl;
import fr.inra.mig_bibliome.stane.shared.data3.Annotation;
import fr.inra.mig_bibliome.stane.shared.data3.AnnotationSet;
import fr.inra.mig_bibliome.stane.shared.data3.AnnotationSetInfo;
import fr.inra.mig_bibliome.stane.shared.data3.Queries.CampaignQueries;
import fr.inra.mig_bibliome.stane.shared.data3.Queries.DocumentQueries;
import java.util.Set;

/**
 * Singleton in charge of providing data and saving changes
 * @author fpapazian
 */
public class CoreDataProvider implements DocumentQueries, CampaignQueries {

    private final EventBus eventBus;
    private final RequestManager requestManager;

    public CoreDataProvider(EventBus eventBus) {
        this.eventBus = eventBus;
        this.requestManager = new RequestManager(eventBus, "user/me");
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
    @Override
    public void getCampaignList(int userId, final AsyncCallback<CampaignListImpl> resultCallback) {
        if (userId != requestManager.getCurrentUserId()) {
            throw new UnsupportedOperationException("Not supported yet. (retrieval of other user's annotation set)");
        }

        String methodUrl = requestManager.getServerBaseUrl() + "user/me";
        requestManager.genericCall(methodUrl, RequestBuilder.GET, null, null,
                new GenericRequestCallback<CampaignListImpl>(eventBus, resultCallback, getRequestManager()) {

                    @Override
                    public CampaignListImpl decode(String responseText) {
                        CDXWS_UserMe_Response parsedResponse = (CDXWS_UserMe_Response) Basic_UserMe_Response.createFromJSON(responseText);
                        return parsedResponse.getCampaignList();
                    }
                });
    }

    @Override
    public void getDocumentInfoList(int userId, int campaignId, final AsyncCallback<DocumentInfoListImpl> resultCallback) {

        String methodUrl = requestManager.getServerBaseUrl() + "user/" + userId + "/campaign/" + campaignId + "/documents";
        requestManager.genericCall(methodUrl, RequestBuilder.GET, null, null,
                new GenericRequestCallback<DocumentInfoListImpl>(eventBus, resultCallback, getRequestManager()) {

                    @Override
                    public DocumentInfoListImpl decode(String responseText) {
                        CDXWS_UserCampaignDocuments_Response parsedResponse = CDXWS_UserCampaignDocuments_Response.createFromJSON(responseText);
                        return parsedResponse.getDocumentInfoList();
                    }
                });
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
    @Override
    public void getAnnotatedDocument(final int userId, final int campaignId, int documentId, final AsyncCallback<AnnotatedTextImpl> resultCallback) {

        String methodUrl = requestManager.getServerBaseUrl() + "user/" + userId + "/campaign/" + campaignId + "/document/" + documentId;
        requestManager.genericCall(methodUrl, RequestBuilder.GET, null, null,
                new GenericRequestCallback<AnnotatedTextImpl>(eventBus, resultCallback, getRequestManager()) {

                    @Override
                    public AnnotatedTextImpl decode(String responseText) {
                        AnnotatedTextImpl parsedResponse = AnnotatedTextImpl.createFromJSON(responseText);
                        //create Modification Handler
                        AnnotatedTextHandler.createHandler(userId, campaignId, parsedResponse);
                        return parsedResponse;
                    }
                });
    }

    @Override
    public void getAnnotatedDocument(final int userId, final String docExternalId, final AsyncCallback<CDXWS_UserAlvisNLPDocId_Response> resultCallback) {

        String methodUrl = requestManager.getServerBaseUrl() + "user/" + userId + "/AlvisNLPID/" + docExternalId;
        requestManager.genericCall(methodUrl, RequestBuilder.GET, null, null,
                new GenericRequestCallback<CDXWS_UserAlvisNLPDocId_Response>(eventBus, resultCallback, getRequestManager()) {

                    @Override
                    public CDXWS_UserAlvisNLPDocId_Response decode(String responseText) {
                        CDXWS_UserAlvisNLPDocId_Response parsedResponse = CDXWS_UserAlvisNLPDocId_Response.createFromJSON(responseText);
                        //create Modification Handler
                        AnnotatedTextHandler.createHandler(userId, parsedResponse.getCampaignId(), parsedResponse.getAnnotatedText());
                        return parsedResponse;
                    }
                });
    }

    @Override
    public void getAdditionalAnnotationSet(AnnotatedTextHandler handler, Set<Integer> annotationSetIds, AsyncCallback<AnnotationSetListImpl> resultCallback) {
        StringBuilder methodUrl = new StringBuilder(requestManager.getServerBaseUrl()).append("annotation?ids=");
        for (Integer aId : annotationSetIds) {
            methodUrl.append(aId).append(",");
        }

        requestManager.genericCall(methodUrl.toString(), RequestBuilder.GET, null, null,
                new GenericRequestCallback<AnnotationSetListImpl>(eventBus, resultCallback, getRequestManager()) {

                    @Override
                    public AnnotationSetListImpl decode(String responseText) {
                        AnnotationSetListImpl parsedResponse = AnnotationSetListImpl.createFromJSON(responseText);
                        return parsedResponse;
                    }
                });
    }

    @Override
    public void saveAnnotationSet(int userId, int campaignId, int documentId, AnnotationSet annotationSet, final AsyncCallback<JavaScriptObject> resultCallback) {
        String methodUrl = requestManager.getServerBaseUrl() + "user/" + userId + "/campaign/" + campaignId + "/document/" + documentId;
        requestManager.genericCall(methodUrl, RequestBuilder.PUT, null, annotationSet.getJSON(),
                new GenericRequestCallback<JavaScriptObject>(eventBus, resultCallback, getRequestManager()) {

                    @Override
                    public JavaScriptObject decode(String responseText) {
                        return null;
                    }
                });
    }

    @Override
    public void finalizeDocument(int userId, int campaignId, int documentId, AsyncCallback<JavaScriptObject> resultCallback) {
        String methodUrl = requestManager.getServerBaseUrl() + "user/" + userId + "/campaign/" + campaignId + "/document/" + documentId + "/finalize";
        requestManager.genericCall(methodUrl, RequestBuilder.PUT, null, null,
                new GenericRequestCallback<JavaScriptObject>(eventBus, resultCallback, getRequestManager()) {

                    @Override
                    public JavaScriptObject decode(String responseText) {
                        return null;
                    }
                });
    }

    // =========================================================================
    public void testRequest(final AsyncCallback<CampaignListImpl> resultCallback) {

        requestManager.setAutoSignedIn();

        int userId = requestManager.getCurrentUserId();
        int campaignId = 1;

        GWT.log("getDocumentInfoList ");
        getDocumentInfoList(userId, campaignId, new AsyncCallback<DocumentInfoListImpl>() {

            @Override
            public void onFailure(Throwable caught) {
                GWT.log("onFailure " + caught);
            }

            @Override
            public void onSuccess(DocumentInfoListImpl result) {
                GWT.log("onSuccess " + result);
            }
        });

        int docId = 1;
        GWT.log("getAnnotatedDocument ");
        getAnnotatedDocument(userId, campaignId, docId, new AsyncCallback<AnnotatedTextImpl>() {

            @Override
            public void onFailure(Throwable caught) {
                GWT.log("onFailure " + caught);
            }

            @Override
            public void onSuccess(AnnotatedTextImpl result) {
                GWT.log("onSuccess " + result);
                GWT.log("onSuccess " + result.getDocument().getId() + " >> " + result.getDocument().getContents());
                for (AnnotationSetInfo as : result.getAnnotationSetInfoList()) {
                    GWT.log("  " + as.getId() + "  " + as.getType() + " : " + as.getDescription());
                }
                for (AnnotationSet as : result.getAnnotationSetList()) {
                    GWT.log("  " + as.getId() + "  " + as.getType() + " : " + as.getDescription());
                    for (Annotation a : as.getTextAnnotations()) {
                        GWT.log("    " + a.getId() + " : " + a.getAnnotationKind().name() + " " + a.getAnnotationType());
                    }
                }
            }
        });


    }
    // =========================================================================
    public RequestManager getRequestManager() {
        return requestManager;
    }
    // =========================================================================
}
