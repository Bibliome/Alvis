/*
 *
 *      This software is a result of Quaero project and its use must respect the rules of the Quaero Project Consortium Agreement.
 *
 *      Copyright Institut National de la Recherche Agronomique, 2010-2011.
 *
 */
package fr.inra.mig_bibliome.stane.shared.data3.Queries;

import com.google.gwt.user.client.rpc.AsyncCallback;
import fr.inra.mig_bibliome.stane.client.data3.CampaignListImpl;
import fr.inra.mig_bibliome.stane.client.data3.DocumentInfoListImpl;

/**
 *
 * @author fpapazian
 */
public interface CampaignQueries {

    /**
     *
     * <b>Rest Method & URL:</b>
     * GET http://[baseurl]/users/[userId]/campaigns
     *
     * @param userId Id of an User (might be different from current user)
     * @param result asynchronous callback which will be called when the query ends (either with a response or on failure)
     *
     * @return list of Annotation Campaign to which the specified user can participate to
     * @throws IllegalArgumentException if the specified userId does not exist (400 Bad Request)
     * @throws SecurityException if the current user is not authenticated (401 Unauthorized) or has no authorization to perform the operation (403 Forbidden)
     */
    void getCampaignList(int userId, AsyncCallback<CampaignListImpl> resultCallback);

    /**
     * <b>Rest Method & URL:</b>
     * GET http://[baseurl]/campaigns/[campaignId]/users/[userId]/documents
     *
     * @param userId Id of an User (might be different from current user)
     * @param campaignId Id of an Annotation Campaign
     * @param result asynchronous callback which will be called when the query ends (either with a response or on failure)
     *
     * @return list of partial info about the Documents assigned to the specified user in the Campaign
     * @throws IllegalArgumentException if the specified campaignId does not exist (400 Bad Request)
     * @throws IllegalArgumentException if the specified userId  does not exist (400 Bad Request)
     * @throws SecurityException if the current user is not authenticated (401 Unauthorized) or has no authorization to perform the operation (403 Forbidden)
     */
    void getDocumentInfoList(int userId, int campaignId, AsyncCallback<DocumentInfoListImpl> resultCallback);
}
