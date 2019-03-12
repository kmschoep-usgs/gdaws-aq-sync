package gov.usgs.wma.gcmrc.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.usgs.wma.gcmrc.exception.AquariusProcessingException;
import gov.usgs.wma.gcmrc.exception.AquariusRetrievalException;

import com.aquaticinformatics.aquarius.sdk.timeseries.AquariusClient;

import net.servicestack.client.IReturn;
import net.servicestack.client.WebServiceException;

public class AquariusRetrievalService {
	private static final Logger LOG = LoggerFactory.getLogger(AquariusRetrievalService.class);
	public static final String NO_ACTIVE_SESSION_ERROR_DESCRIPTION = "NoActiveSessionException";

	private String aquariusUrl;
	private String aquariusUser;
	private String aquariusPassword;
	private int aquariusTimeoutMs;
	private int aquariusUnauthorizedRetryCount;
	
	public AquariusRetrievalService(String aquariusUrl, String aquariusUser, String aquariusPassword, int aquariusUnauthorizedRetryCount, int aquariusTimeoutMs){
		this.aquariusUrl = aquariusUrl;
		this.aquariusUser = aquariusUser;
		this.aquariusPassword = aquariusPassword;
		this.aquariusUnauthorizedRetryCount = aquariusUnauthorizedRetryCount;
		this.aquariusTimeoutMs = aquariusTimeoutMs;
	}

	protected <TResponse> TResponse executePublishApiRequest(IReturn<TResponse> request) throws AquariusRetrievalException {
		return executePublishApiRequest(request, null);
	}

	protected <TResponse> TResponse executePublishApiRequest(IReturn<TResponse> request, Integer timeoutMsOverride) throws AquariusRetrievalException {
		String errorMessage = "";
		int unauthorizedRetryCounter = 0;
		boolean doRetry;

		do {
			doRetry = false;
			try {
				// Despite this being AutoCloseable we CANNOT close it or it will delete the session in AQ for our service account
				// and cause any other in-flight requests to fail.
				AquariusClient client = AquariusClient.createConnectedClient(aquariusUrl.replace("/AQUARIUS/", ""), aquariusUser, aquariusPassword);
				
				// Allow specifying a timeout MS per-request
				if(timeoutMsOverride != null) {
					client.Publish.setTimeout(timeoutMsOverride);
				} else {
					client.Publish.setTimeout(aquariusTimeoutMs);
				}
				
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
					throw new AquariusRetrievalException(errorMessage);
				}
			} catch (Exception e) {
				LOG.error("An unexpected error occurred while attempting to fetch data from Aquarius: \n" +
					"Request: " + request.toString() + "\n Error: ", e);
				throw new AquariusProcessingException(e.getMessage());
			}
		} while(doRetry);

		LOG.error("Failed to retireve data from Aquarius for request " + request.toString() + ". Out of retires.");
		throw new AquariusRetrievalException("Failed to retrieve data from Aquarius for request " + request.toString());
	}

	protected boolean isAuthError(WebServiceException e) {
		return e.getStatusCode() == 401 || e.getStatusCode() == 403;
	}

	protected boolean isInvalidTokenError(WebServiceException e) {
		return e.getStatusCode() == 500 && 
				e.getStatusDescription().toLowerCase().contains(NO_ACTIVE_SESSION_ERROR_DESCRIPTION.toLowerCase());
	}
}