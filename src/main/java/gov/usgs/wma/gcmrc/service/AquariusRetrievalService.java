package gov.usgs.wma.gcmrc.service;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aquaticinformatics.aquarius.sdk.timeseries.AquariusClient;

import net.servicestack.client.IReturn;
import net.servicestack.client.WebServiceException;

public class AquariusRetrievalService {
	private static final Logger LOG = LoggerFactory.getLogger(AquariusRetrievalService.class);
	public static final String NO_ACTIVE_SESSION_ERROR_DESCRIPTION = "NoActiveSessionException";

	private String aquariusUrl;
	private String aquariusUser;
	private String aquariusPassword;
	private int aquariusUnauthorizedRetryCount;
	
	public AquariusRetrievalService(String aquariusUrl, String aquariusUser, String aquariusPassword, int aquariusUnauthorizedRetryCount){
		this.aquariusUrl = aquariusUrl;
		this.aquariusUser = aquariusUser;
		this.aquariusPassword = aquariusPassword;
		this.aquariusUnauthorizedRetryCount = aquariusUnauthorizedRetryCount;
	}

	protected <TResponse> TResponse executePublishApiRequest(IReturn<TResponse> request) throws Exception {
		String errorMessage = "";
		int unauthorizedRetryCounter = 0;
		boolean doRetry;

		do {
			doRetry = false;
			try {
				//Despite this being AutoCloseable we CANNOT close it or it will delete the session in AQ for our service account
				//and cause any other in-flight requests to fail.
				AquariusClient client = AquariusClient.createConnectedClient(aquariusUrl.replace("/AQUARIUS/", ""), aquariusUser, aquariusPassword);
				return client.Publish.get(request);
			} catch (WebServiceException e) {
				if((isAuthError(e) || isInvalidTokenError(e)) && unauthorizedRetryCounter < aquariusUnauthorizedRetryCount) {
					doRetry = true;
					unauthorizedRetryCounter++;
					errorMessage = "Failed to get authorization for Aquarius Web Request " + request.toString() + 
					". Retrying (" + unauthorizedRetryCounter + " / " + aquariusUnauthorizedRetryCount + ")...";
					LOG.warn(errorMessage);
				} else {
					errorMessage = "A Web Service Exception occurred while executing a Publish API Request against Aquarius:\n{" +
					"\nAquarius Instance: " + aquariusUrl +
					"\nRequest: " + request.toString() +
					"\nStatus: " + e.getStatusCode() + 
					"\nDescription: " + e.getStatusDescription() +
					"\nCause: " + e.getErrorMessage() +
					"\nDetails: " + e.getServerStackTrace() + "\n}\n";
					LOG.error(errorMessage);
					throw new Exception(errorMessage);
				}
			} catch (Exception e) {
				LOG.error("An unexpected error occurred while attempting to fetch data from Aquarius: \n" +
					"Request: " + request.toString() + "\n Error: ", e);
				throw new Exception(e.getMessage());
			}
		} while(doRetry);

		LOG.error("Failed to retireve data from Aquarius for request " + request.toString() + ". Out of retires.");
		throw new Exception("Failed to retrieve data from Aquarius for request " + request.toString());
	}

	protected boolean isAuthError(WebServiceException e) {
		return e.getStatusCode() == HttpStatus.SC_UNAUTHORIZED || e.getStatusCode() == HttpStatus.SC_FORBIDDEN;
	}

	protected boolean isInvalidTokenError(WebServiceException e) {
		return e.getStatusCode() == HttpStatus.SC_INTERNAL_SERVER_ERROR && 
				e.getStatusDescription().toLowerCase().contains(NO_ACTIVE_SESSION_ERROR_DESCRIPTION.toLowerCase());
	}
}