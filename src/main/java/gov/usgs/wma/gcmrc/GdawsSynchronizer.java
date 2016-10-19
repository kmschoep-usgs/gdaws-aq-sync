package gov.usgs.wma.gcmrc;

import gov.usgs.wma.gcmrc.service.AqToGdaws;
import gov.usgs.wma.gcmrc.service.AutoProc;
import gov.usgs.wma.gcmrc.service.DataPullRunState;

public class GdawsSynchronizer {
	
	private static AqToGdaws aqToGdaws = new AqToGdaws();
	private static AutoProc autoProc = new AutoProc();
	
	public static void main(String[] args){
		//TODO parse args to optionally turn these settings off
		boolean doAqSync = true;
		
		DataPullRunState runState = DataPullRunState.instance();
		
		//TODO look at error handling and transaction code
		if(doAqSync) {
			aqToGdaws.migrateAqData();
		}
		
		autoProc.processData();
	}
}
