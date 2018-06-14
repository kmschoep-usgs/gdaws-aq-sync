package gov.usgs.wma.gcmrc.service;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesDataCorrectedServiceRequest;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesDataServiceResponse;

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
				.setIncludeGapMarkers(true)
				.setQueryTo(endDate);
		TimeSeriesDataServiceResponse timeSeriesResponse = aquariusRetrievalService.executePublishApiRequest(request);
		return timeSeriesResponse;
	}
}
