package gov.usgs.wma.gcmrc.logic;

import gov.usgs.wma.gcmrc.model.SiteConfiguration;
import gov.usgs.wma.gcmrc.model.RunConfiguration;
import java.util.List;

public class AutoProc {
	
	public AutoProc(RunConfiguration runState, List<SiteConfiguration> sitesToLoad) {
		/* TODO load site specific calculation configuration, this will specify how to 
		 * calculate basic acoustic load, paria lake loads, MinorTribFinesPulseProxy,
		 * and MinorTribSimpleShadowLoad for respective sites.
		 */
	}
	
	public void processBedloadCalculations() {
		//TODO perform all autoproc calculations using loaded configuration
	}

}
