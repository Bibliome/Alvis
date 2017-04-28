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
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.rpc.AsyncCallback;
import fr.inra.mig_bibliome.stane.client.data.Retrieve.AbstractRequestManager;
import fr.inra.mig_bibliome.stane.client.data.Retrieve.RequestManager;
import fr.inra.mig_bibliome.stane.client.data3.*;
import fr.inra.mig_bibliome.stane.shared.data3.Annotation;
import fr.inra.mig_bibliome.stane.shared.data3.AnnotationSet;
import fr.inra.mig_bibliome.stane.shared.data3.AnnotationSetInfo;
import fr.inra.mig_bibliome.stane.shared.data3.Queries.AdministrativeQueries;
import fr.inra.mig_bibliome.stane.shared.data3.Queries.CampaignQueries;
import fr.inra.mig_bibliome.stane.shared.data3.Queries.DocumentQueries;
import java.util.Set;

/**
 * Singleton in charge of providing data and saving changes
 *
 * @author fpapazian
 */
public class CoreDataProvider implements DocumentQueries, CampaignQueries, AdministrativeQueries {

    private final EventBus eventBus;
    private final RequestManager requestManager;

    public CoreDataProvider(EventBus eventBus) {
        this.eventBus = eventBus;
        this.requestManager = new RequestManager(eventBus, "user/me?wzauths=true");
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
    @Override
    public void getUserList(AsyncCallback<ExtendedUserInfoListImpl> resultCallback) {
        String methodUrl = getRequestManager().getServerBaseUrl() + "user?wzauths=true";
        requestManager.genericCall(methodUrl, RequestBuilder.GET, null, null,
                new GenericRequestCallback<ExtendedUserInfoListImpl>(eventBus, resultCallback, getRequestManager()) {

                    @Override
                    public ExtendedUserInfoListImpl decode(String responseText) {
                        ExtendedUserInfoListImpl parsedResponse = ExtendedUserInfoListImpl.createFromJSON(responseText);
                        return parsedResponse;
                    }
                });
    }

    @Override
    public void getAuthorizationList(AsyncCallback<AuthorizationListImpl> resultCallback) {
        String methodUrl = getRequestManager().getServerBaseUrl() + "authorizations";
        requestManager.genericCall(methodUrl, RequestBuilder.GET, null, null,
                new GenericRequestCallback<AuthorizationListImpl>(eventBus, resultCallback, getRequestManager()) {

                    @Override
                    public AuthorizationListImpl decode(String responseText) {
                        CDXWS_Authorizations_Response parsedResponse = (CDXWS_Authorizations_Response) CDXWS_Authorizations_Response.createFromJSON(responseText);
                        return parsedResponse.getAuthorizations();
                    }
                });
    }

    @Override
    public void getUserAuthorizations(int userId, AsyncCallback<UserAuthorizationsImpl> resultCallback) {
        String methodUrl = getRequestManager().getServerBaseUrl() + "user/" + userId + "/authorizations";
        requestManager.genericCall(methodUrl, RequestBuilder.GET, null, null,
                new GenericRequestCallback<UserAuthorizationsImpl>(eventBus, resultCallback, getRequestManager()) {

                    @Override
                    public UserAuthorizationsImpl decode(String responseText) {
                        UserAuthorizationsImpl parsedResponse = UserAuthorizationsImpl.createFromJSON(responseText);
                        return parsedResponse;
                    }
                });
    }

    @Override
    public void setUserAuthorizations(final int userId, UserAuthorizationsImpl newAuths, AsyncCallback<UserAuthorizationsImpl> resultCallback) {
        String methodUrl = getRequestManager().getServerBaseUrl() + "user/" + userId + "/authorizations";
        requestManager.genericCall(methodUrl, RequestBuilder.PUT, null, newAuths.getJSON(),
                new GenericRequestCallback<UserAuthorizationsImpl>(eventBus, resultCallback, getRequestManager()) {

                    @Override
                    public UserAuthorizationsImpl decode(String responseText) {
                        UserAuthorizationsImpl parsedResponse = UserAuthorizationsImpl.createFromJSON(responseText);
                        //change authorization infos if current user was changed 
                        if (getRequestManager().getCurrentUserId() == userId) {
                            getRequestManager().resetAuthenticationInfoAuths(parsedResponse);
                        }
                        return parsedResponse;
                    }
                });
    }
    
    @Override
    public void createUser(String login, boolean isAdmin, String password, AsyncCallback<ExtendedUserInfoImpl> resultCallback) {
        AbstractRequestManager.Entry[] params = new AbstractRequestManager.Entry[]{
            new AbstractRequestManager.Entry("login", login),
            new AbstractRequestManager.Entry("is_admin", String.valueOf(isAdmin)),
            new AbstractRequestManager.Entry("passwd", password),};

        String methodUrl = getRequestManager().getServerBaseUrl() + "user";

        getRequestManager().genericCall(methodUrl, RequestBuilder.POST, params, null,
                new GenericRequestCallback<ExtendedUserInfoImpl>(eventBus, resultCallback, getRequestManager()) {

                    @Override
                    public ExtendedUserInfoImpl decode(String responseText) {
                        ExtendedUserInfoImpl parsedResponse = (ExtendedUserInfoImpl) Basic_UserMe_Response.createFromJSON(responseText);
                        return parsedResponse;
                    }
                });
    }

    @Override
    public void updateUser(final int userId, String login, boolean isAdmin, AsyncCallback<ExtendedUserInfoImpl> resultCallback) {
        AbstractRequestManager.Entry[] params = new AbstractRequestManager.Entry[]{
            new AbstractRequestManager.Entry("login", login),
            new AbstractRequestManager.Entry("is_admin", String.valueOf(isAdmin)),};

        String methodUrl = getRequestManager().getServerBaseUrl() + "user/" + userId + "?" + AbstractRequestManager.buildQueryString(params);

        getRequestManager().genericCall(methodUrl, RequestBuilder.PUT, params, null,
                new GenericRequestCallback<ExtendedUserInfoImpl>(eventBus, resultCallback, getRequestManager()) {

                    @Override
                    public ExtendedUserInfoImpl decode(String responseText) {
                        ExtendedUserInfoImpl parsedResponse = (ExtendedUserInfoImpl) Basic_UserMe_Response.createFromJSON(responseText);
                        //update authorization infos if current user was changed 
                        if (getRequestManager().getCurrentUserId() == userId) {
                            getRequestManager().resetAuthenticationInfoAdminStatus(parsedResponse.isAdmin());
                        }
                        return parsedResponse;
                    }
                });
    }

    @Override
    public void updateUserPassword(final int userId, final String newPassword, final AsyncCallback<JavaScriptObject> resultCallback) {
        AbstractRequestManager.Entry[] params = new AbstractRequestManager.Entry[]{
            new AbstractRequestManager.Entry("passwd", newPassword),};

        String methodUrl = getRequestManager().getServerBaseUrl() + "user/" + userId + "/chpasswd";

        //POST method used here to avoid transmitting the new password as an URL parameter
        getRequestManager().genericCall(methodUrl, RequestBuilder.POST, params, null,
                new GenericRequestCallback<JavaScriptObject>(eventBus, resultCallback, getRequestManager()) {

                    @Override
                    public JavaScriptObject decode(String responseText) {
                        //change authorization infos if password was changed for current user
                        if (getRequestManager().getCurrentUserId() == userId) {
                            CDXWS_UserMe_Response parsedResponse = (CDXWS_UserMe_Response) Basic_UserMe_Response.createFromJSON(responseText);
                            AuthenticationInfoImpl authInfo = getRequestManager().changeAuthenticationInfo(parsedResponse, newPassword);
                        }
                        return null;
                    }
                });
    }
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
    @Override
    public void getCampaignList(AsyncCallback<CampaignListImpl> resultCallback) {
        String methodUrl = getRequestManager().getServerBaseUrl() + "campaigns";
        requestManager.genericCall(methodUrl, RequestBuilder.GET, null, null,
                new GenericRequestCallback<CampaignListImpl>(eventBus, resultCallback, getRequestManager()) {

                    @Override
                    public CampaignListImpl decode(String responseText) {
                        CDXWS_UserMe_Response tmp =  (CDXWS_UserMe_Response) Basic_UserMe_Response.createFromJSON(responseText);
                        CampaignListImpl parsedResponse = tmp.getUserCampaignList();
                        return parsedResponse;
                    }
                });
    }


    @Override
    public void getUserCampaignList(int userId, final AsyncCallback<CampaignListImpl> resultCallback) {
        //FIXME
        if (userId != requestManager.getCurrentUserId()) {
            throw new UnsupportedOperationException("Not supported yet. (retrieval of other user's annotation set)");
        }

        String methodUrl = requestManager.getServerBaseUrl() + "user/me";
        requestManager.genericCall(methodUrl, RequestBuilder.GET, null, null,
                new GenericRequestCallback<CampaignListImpl>(eventBus, resultCallback, getRequestManager()) {

                    @Override
                    public CampaignListImpl decode(String responseText) {
                        CDXWS_UserMe_Response parsedResponse = (CDXWS_UserMe_Response) Basic_UserMe_Response.createFromJSON(responseText);
                        return parsedResponse.getUserCampaignList();
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
