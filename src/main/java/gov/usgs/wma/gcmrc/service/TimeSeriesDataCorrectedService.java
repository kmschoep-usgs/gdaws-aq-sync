package gov.usgs.wma.gcmrc.service;

import java.time.Instant;

import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesDataCorrectedServiceRequest;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesDataServiceResponse;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class TimeSeriesDataCorrectedService {
	private static final Logger LOG = LoggerFactory.getLogger(TimeSeriesDataCorrectedService.class);
	
	private AquariusRetrievalService aquariusRetrievalService;

	public TimeSeriesDataCorrectedService(
		AquariusRetrievalService aquariusRetrievalService
	) {
		this.aquariusRetrievalService = aquariusRetrievalService;
	}

	public TimeSeriesDataServiceResponse getRawResponse(String primaryTimeseriesIdentifier, Instant startDate, Instant endDate) throws Exception {
		TimeSeriesDataCorrectedServiceRequest request = new TimeSeriesDataCorrectedServiceRequest()
				.setTimeSeriesUniqueId(primaryTimeseriesIdentifier)
				.setQueryFrom(startDate)
				.setIncludeGapMarkers(false)
				.setQueryTo(endDate)
				.setApplyRounding(true);
		LOG.trace("Staring data pull for TS ID: " + primaryTimeseriesIdentifier);
		long startTime = System.nanoTime();
		TimeSeriesDataServiceResponse timeSeriesResponse = aquariusRetrievalService.executePublishApiRequest(request);
		long durationMs = (System.nanoTime() - startTime)/1000000;
		LOG.trace("Finished data full for TS ID: " + primaryTimeseriesIdentifier + " in " + durationMs + "ms");
		return timeSeriesResponse;
	}
}
