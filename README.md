gdaws-aq-sync does both syncing with aquarius to the GDAWS/GCMRC database at EROS, as well as performs calculations with the data after they are updated, in order to keep the data in the GDAWS database that uses the incoming time series data up to date with the latest discharge data. 

To run locally:

1. build the jar
2. next to the jar create a config file (call it whatever but for example gcmrcsync.props) and in the config file enter the properties.  An example can  be found in this repository in src/main/resources/gcmrc-sync-config.properties.
3. Run process: 
	`java -Dgcmrc.config.file=gcmrcsync.props -Xms1024m -Xmx1024m -jar the_jar_file_you_downloaded.jar --skipAquariusSync`
	
NOTE: --skipAquariusSync only included if you don’t want to load data from AQ. See java “-jar the_jar_file\_you\_downloaded.jar --help” for all options.

These options skip the calculations
--skipBedloadCalculations --skipMergeCumulativeLoads --skipTotalSuspendedSedimentCalc

So to test  JUST the sync we want to do something like this so it skips the calculations but does do the syncing with AQ.

`java -Dgcmrc.config.file=gcmrcsync.props -Xms1024m -Xmx1024m -jar the_jar_file_you_downloaded.jar --skipBedloadCalculations --skipMergeCumulativeLoads`

