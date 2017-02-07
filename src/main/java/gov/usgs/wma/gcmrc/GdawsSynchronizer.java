package gov.usgs.wma.gcmrc;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.usgs.wma.gcmrc.dao.GdawsDaoFactory;
import gov.usgs.wma.gcmrc.service.AqToGdaws;
import gov.usgs.wma.gcmrc.service.AutoProc;
import gov.usgs.wma.gcmrc.util.ConfigLoader;

public class GdawsSynchronizer {
	private static final Logger LOG = LoggerFactory.getLogger(GdawsSynchronizer.class);
	
	private static final String SKIP_OPTION_PREFIX = "--skip";
	
	private static final String  AQUARIUS_SYNC_OPT = "AquariusSync";
	private static final String  BEDLOAD_OPT = "BedloadCalculations";
	private static final String  MERGE_CUMULATIVE_LOADS_OPT = "MergeCumulativeLoads";
	
	private static final String[] PROCESS_OPTIONS = new String[] {
			AQUARIUS_SYNC_OPT,
			BEDLOAD_OPT,
			MERGE_CUMULATIVE_LOADS_OPT
	};

	private static final String AQ_URL_PROP_NAME = "aquarius.service.endpoint";
	private static final String AQ_USER_PROP_NAME = "aquarius.service.user";
	private static final String AQ_PASS_PROP_NAME = "aquarius.service.password";
	private static final String NWIS_URL_PROP_NAME = "nwis-ra.service.url";
	private static final String NWIS_USER_PROP_NAME = "nwis-ra.service.user";
	private static final String NWIS_PASS_PROP_NAME = "nwis-ra.service.pass";
	private static final String GDAWS_HOST_PROP_NAME = "gdaws.dbHost";
	private static final String GDAWS_PORT_PROP_NAME = "gdaws.dbPort";
	private static final String GDAWS_NAME_PROP_NAME = "gdaws.dbName";
	private static final String GDAWS_USER_PROP_NAME = "gdaws.dbUser";
	private static final String GDAWS_PASS_PROP_NAME = "gdaws.dbPwd";
	private static final String AQ_SOURCE_PROP_NAME = "aquarius.source.id";
	private static final String OLD_GADSYNC_SOURCE_PROP_NAME = "old.gadsync.source.id";
	private static final String AUTO_PROC_SOURCE_PROP_NAME = "autoproc.source.id";
	private static final String BEDLOAD_GROUP_ID_PROP_NAME = "bedload.group.id";
	private static final String CUMULATIVE_BEDLOAD_GROUP_ID_PROP_NAME = "cumulative.bedload.group.id";
	private static final String SAND_LOAD_GROUP_ID_PROP_NAME = "cumulative.sand.load.group.id";
	private static final String FINES_LOAD_GROUP_ID_PROP_NAME = "cumulative.fines.load.group.id";
	private static final String DEFAULT_DAYS_TO_FETCH_FOR_NEW_TIMESERIES = "default.days.to.fetch.for.new.timeseries";
	private static final String SYNC_START_DATE_PROP_NAME = "sync.start.time";
	private static final String SYNC_END_DATE_PROP_NAME = "sync.end.time";
	private static final String SYNC_TIMESERIES_ID_LIST_PROP_NAME = "sync.timeseries.id.list";
	private static final String SYNC_LOG_LEVEL = "sync.loglevel";
	
	
	//prop names and descriptions
	private static final String[] REQUIRED_PROPS = new String[] {
			AQ_URL_PROP_NAME, "URL to Aquarius Service",
			AQ_USER_PROP_NAME, "Aquarius service username",
			AQ_PASS_PROP_NAME, "Aquarius service password",
			NWIS_URL_PROP_NAME, "URL to NWIS-RA webservices",
			NWIS_USER_PROP_NAME, "NWIS-RA  service username",
			NWIS_PASS_PROP_NAME, "NWIS-RA  service password",
			GDAWS_HOST_PROP_NAME, "GDAWS Database hostname",
			GDAWS_PORT_PROP_NAME, "GDAWS Database port",
			GDAWS_NAME_PROP_NAME, "GDAWS Database name",
			GDAWS_USER_PROP_NAME, "GDAWS Database user",
			GDAWS_PASS_PROP_NAME, "GDAWS Database password",
			AQ_SOURCE_PROP_NAME, "The source id to mark incoming records from aquarius with",
			OLD_GADSYNC_SOURCE_PROP_NAME, "The source id of the old GADSYNC records, which can be safely overwritten by the new AQ source",
			AUTO_PROC_SOURCE_PROP_NAME, "The source id to mark calculated values with",
			BEDLOAD_GROUP_ID_PROP_NAME, "The group id to mark instantaneous calculated bed load values with",
			CUMULATIVE_BEDLOAD_GROUP_ID_PROP_NAME, "The group id for mark cumulative bedload calculations with",
			SAND_LOAD_GROUP_ID_PROP_NAME, "The group id to mark cumulative sand load values with",
	};
	
	//prop names and descriptions
	private static final String[] OPTIONAL_PROPS = new String[] {
			ConfigLoader.CONFIG_FILE_PROP_NAME, "File used to set all required props",
			DEFAULT_DAYS_TO_FETCH_FOR_NEW_TIMESERIES, "If a site has not been fetched before, this is the number of days to fetch.",
			SYNC_START_DATE_PROP_NAME, "Optional date & time to force synchronizing to start from.  Assumed to be in MST.",
			SYNC_END_DATE_PROP_NAME, "Optional date & time to force synchronizing to end at.  Assumed to be in MST.",
			SYNC_TIMESERIES_ID_LIST_PROP_NAME, "Optional list of TS GUIDS to limit synchronizing to.",
			SYNC_LOG_LEVEL, "One of the Logback log levels: DEBUG, ERROR, INFO, TRACE, WARN, ALL, OFF. "
			+ "Debug starts to show the number of records retrieved and insterted, Trace even more detail."
	};
	
	private static final int HELP_COLUMN_SIZE = 30;
	
	public static void main(String[] args) {
		
		RunConfiguration runState = RunConfiguration.instance();
		
		if(validateArguments(args, runState.getProperties())) {
			LOG.info("Arguments valid, proceeding with processing");
			
			GdawsDaoFactory gdawsDaoFactory = new GdawsDaoFactory(runState.getProperties());
			
			
			//
			//This sets the logging level of AqToGdaws and relies on the logging API
			//being implemented as Logback.  No way to do it w/o knowing the implementation.
			if (runState.getProperty(SYNC_LOG_LEVEL, null) != null) {
				String strLvl = runState.getProperty(SYNC_LOG_LEVEL, null);
				Level level = Level.toLevel(strLvl);
				LOG.info("Sync log level {} interpreted as {}", strLvl, level.toString());
				LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
				lc.getLogger(AqToGdaws.class).setLevel(level);
			}
			
			
			if(!isSkip(args, AQUARIUS_SYNC_OPT)) {
				LOG.info("Starting AQ to GDAWS Sync");
				AqToGdaws aqToGdaws = new AqToGdaws(
						runState.getAquariusDataService(), 
						gdawsDaoFactory, 
						runState.getIntProperty(DEFAULT_DAYS_TO_FETCH_FOR_NEW_TIMESERIES, null),
						runState.getIntProperty(AQ_SOURCE_PROP_NAME, null),
						runState.getIntProperty(OLD_GADSYNC_SOURCE_PROP_NAME, null),
						runState.getDateTimeProperty(SYNC_START_DATE_PROP_NAME, null),
						runState.getDateTimeProperty(SYNC_END_DATE_PROP_NAME, null),
						runState.getArrayProperty(SYNC_TIMESERIES_ID_LIST_PROP_NAME, String.class)
				);
				aqToGdaws.migrateAqData();
				LOG.info("Finished AQ to GDAWS Sync");
			} else {
				LOG.info("Skipped AQ to GDAWS Sync");
			}
			
			AutoProc autoProc = new AutoProc(gdawsDaoFactory, runState.getIntProperty(AUTO_PROC_SOURCE_PROP_NAME, null));
			
			if(!isSkip(args, BEDLOAD_OPT)) {
				LOG.info("Starting Bedload Calculations");
				autoProc.processBedloadCalculations(
						runState.getIntProperty(BEDLOAD_GROUP_ID_PROP_NAME, null),
						runState.getIntProperty(CUMULATIVE_BEDLOAD_GROUP_ID_PROP_NAME, null));
				LOG.info("Finished Bedload Calculations");
			} else {
				LOG.info("Skipping Bedload Calculations");
			}

			if(!isSkip(args, MERGE_CUMULATIVE_LOADS_OPT)) {
				List<Integer> cumulativeGroupIds = new LinkedList<>();
				cumulativeGroupIds.add(runState.getIntProperty(SAND_LOAD_GROUP_ID_PROP_NAME, null));
				cumulativeGroupIds.add(runState.getIntProperty(CUMULATIVE_BEDLOAD_GROUP_ID_PROP_NAME, null));
				cumulativeGroupIds.add(runState.getIntProperty(FINES_LOAD_GROUP_ID_PROP_NAME, null));
				
				LOG.info("Starting Merge Cumulative Load Calculations");
				autoProc.processMergeCumulativeLoadCalculations(cumulativeGroupIds);
				LOG.info("Finished Merge Cumulative Load Calculations");
			} else {
				LOG.info("Skipping Merge Cumulative Load Calculations");
			}
		}
	}
	
	private static boolean validateArguments(String[] args, Properties props) {
		//check to see that any arguments are not valid
		for(String arg : args) {
			if(!isSupportedArg(arg)) {
				LOG.info("Argument \"" + arg + "\" is not supported. See --help for more information.");
				return false;
			}
		}
		
		//help
		if(args.length > 0 && (args[0].equals("--help") || args[0].equals("-h"))) {
			printHelp();
			return false; //return false because the rest of the process will not proceed
		}
		
		//check for required properties
		//NOTE: we use DROP to verify the properties are there, but the properties are actually used inside of the
		//aqcu-data-core library.
		for(int i = 0; i < REQUIRED_PROPS.length; i += 2) {
			String p = REQUIRED_PROPS[i];
			if(StringUtils.isBlank(props.getProperty(p))) {
				LOG.info("Property \"" + p + "\" must be defined. See --help for more information.");
				return false;
			}
		}
		
		return true;
	}
	
	private static void printHelp() {
		StringBuilder message = new StringBuilder();
		message.append("GDAWS/Aquarius Synchronizer Help");
		message.append(System.lineSeparator());
		message.append(System.lineSeparator());
		
		message.append("Usage: java -jar [JVM ARGUMENTS] gdaws-aq-sync.jar [OPTIONS]");
		message.append(System.lineSeparator());
		message.append(System.lineSeparator());
		
		message.append("Required properties. Can be set as environment variables and/or JVM -D arguments.");
		appendRequiredOptions(message);
		message.append(System.lineSeparator());
		message.append(System.lineSeparator());
		
		message.append("Optional properties. Can be set as environment variables and/or JVM -D arguments.");
		appendOptionalOptions(message);
		message.append(System.lineSeparator());
		message.append(System.lineSeparator());

		message.append("OPTIONS:");
		appendOption(message, "--help,-h", "This help menu");
		appendSkipOptions(message);

		//print out
		LOG.info(message.toString());
	}
	
	private static void appendRequiredOptions(StringBuilder message) {
		for(int i = 0; i < REQUIRED_PROPS.length; i += 2) {
			appendOption(message, REQUIRED_PROPS[i], REQUIRED_PROPS[i+1]);
		}
	}
	
	private static void appendOptionalOptions(StringBuilder message) {
		for(int i = 0; i < OPTIONAL_PROPS.length; i += 2) {
			appendOption(message, OPTIONAL_PROPS[i], OPTIONAL_PROPS[i+1]);
		}
	}
	
	private static void appendSkipOptions(StringBuilder message) {
		appendOption(message, SKIP_OPTION_PREFIX + "[step]", "Skips execution of the step. Steps available to skip:");
		//this appends all process options to the previous description
		for(String step : PROCESS_OPTIONS) {
			message.append(" " + step + ",");
		}
		message.delete(message.length()-1, message.length());
		message.append(" (EG: " + SKIP_OPTION_PREFIX + PROCESS_OPTIONS[0] + ")");
	}
	
	private static void appendOption(StringBuilder message, String option, String description) {
		message.append(System.lineSeparator());
		message.append(option);
		for(int i = option.length(); i < HELP_COLUMN_SIZE; i++) {
			message.append(" ");
		}
		message.append(description);
	}
	
	private static boolean isSupportedArg(String arg) {
		if(arg.equals("--help") || arg.equals("-h")) {
			return true;
		}
		
		//is valid skip option
		for(String step : PROCESS_OPTIONS) {
			if(arg.equals(SKIP_OPTION_PREFIX + step)) {
				return true;
			}
		}
		
		return false; //option not found
	}
	
	public static boolean isSkip(String[] args, String processStep) {
		for(String arg : args) {
			if(arg.equals(SKIP_OPTION_PREFIX + processStep)) {
				return true;
			}
		}
			
		return false; //not found in skip;
	}
}
