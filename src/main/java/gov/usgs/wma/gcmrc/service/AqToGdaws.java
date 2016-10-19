package gov.usgs.wma.gcmrc.service;

import java.util.List;

import gov.usgs.aqcu.data.service.DataService;
import gov.usgs.aqcu.gson.ISO8601TemporalSerializer;
import gov.usgs.aqcu.model.TimeSeries;
import gov.usgs.wma.gcmrc.model.SiteConfiguration;

public class AqToGdaws {
	private List<SiteConfiguration> sitesToLoad;
	private DataPullRunState runState;
	
	// this requires the follow properties to be defined: aquarius.service.endpoint, aquarius.service.user, aquarius.service.password
	DataService dataService = new DataService(); 

	public AqToGdaws(DataPullRunState runState, List<SiteConfiguration> sitesToLoad) {
		this.runState = runState;
		this.sitesToLoad = sitesToLoad;
	}
	
	public void migrateAqData() {
		for(SiteConfiguration c : sitesToLoad) {
			String siteId = c.getSiteId();
			
			//load the data from the source. TODO, determine if we only use primary/published/UV series 
			List<String> tsUids = dataService.getTimeSeriesUniqueIdsAtSite(
					siteId, null, null, c.getParameter(), null, null);
			
			for(String uid: tsUids) {
				//TODO build start/end times
				TimeSeries retrieved = dataService.getTimeSeriesData(
						siteId, uid, "2015-01-01T00:00:00.000-05:00", "2016-01-01T00:00:00.000-05:00", false, false);
				
				//TODO transform and load into GDAWS
				Integer numOfPoints = retrieved.getPoints().size();
				System.out.println("Retrieved " + retrieved.getName() + " " + retrieved.getDescription() + 
						", which contains " + numOfPoints + " points");
				if(numOfPoints > 0) {
					System.out.println("First point: " + 
							ISO8601TemporalSerializer.print(retrieved.getPoints().get(0).getTime()) + 
							" " + retrieved.getPoints().get(0).getValue());
				}
			}
		}
	}
}
