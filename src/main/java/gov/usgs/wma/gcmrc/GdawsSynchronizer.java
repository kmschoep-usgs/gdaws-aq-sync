package gov.usgs.wma.gcmrc;

import java.util.List;

import gov.usgs.aqcu.data.service.DataService;
import gov.usgs.aqcu.gson.ISO8601TemporalSerializer;
import gov.usgs.aqcu.model.TimeSeries;
import gov.usgs.aqcu.model.TimeSeriesPoint;

public class GdawsSynchronizer {
	
	public static void main(String[] args){
		
		//Potential workflow based on the orginial gadsync
		
		//load list of sites to migrate to gdaws from a config file
		String[] stationIds = new String[] { "01010000" };
		
		//load the data from the source
		DataService dataService = new DataService(); // this requires the follow properties to be defined: aquarius.service.endpoint, aquarius.service.user, aquarius.service.password
		List<String> tsUids = dataService.getTimeSeriesUniqueIdsAtSite(stationIds[0], null, null, null, null, null);

		//Take data and transform if needed, then insert int GDAWS database, will need connection info
		for(String uid : tsUids) {
			TimeSeries retrieved = dataService.getTimeSeriesData(stationIds[0], uid, "2015-01-01T00:00:00.000-05:00", "2016-01-01T00:00:00.000-05:00", false, false);
			
			System.out.println("Retrieved " + retrieved.getName() + " " + retrieved.getDescription());
			for(TimeSeriesPoint p : retrieved.getPoints()) {
				System.out.println(ISO8601TemporalSerializer.print(p.getTime()) + " " + p.getValue());
			}
		}
		
	}
}
