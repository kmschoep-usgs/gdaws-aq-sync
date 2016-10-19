package gov.usgs.wma.gcmrc;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.usgs.wma.gcmrc.service.AqToGdaws;
import gov.usgs.wma.gcmrc.service.AutoProc;
import gov.usgs.wma.gcmrc.service.GdawsConfigLoader;

public class GdawsSynchronizer {
	private static final Logger LOG = LoggerFactory.getLogger(GdawsSynchronizer.class);
	
	private static final String SKIP_OPTION_PREFIX = "--skip";
	
	private static final String  AQUARIUS_SYNC_OPT = "AquariusSync";
	private static final String  BEDLOAD_OPT = "BedloadCalculations";
	private static final String[] PROCESS_OPTIONS = new String[] {
			AQUARIUS_SYNC_OPT,
			BEDLOAD_OPT
	};
	
	//prop names and descriptions
	private static final String[] REQUIRED_PROPS = new String[] {
			"aquarius.service.endpoint", "URL to Aquarius Service",
			"aquarius.service.user", "Aquarius service username",
			"aquarius.service.password", "Aquarius service password"
	};
	
	private static AqToGdaws aqToGdaws = new AqToGdaws();
	private static AutoProc autoProc = new AutoProc();
	
	private static final int HELP_COLUMN_SIZE = 30;
	
	public static void main(String[] args){
		if(validateArguments(args)) {
			LOG.debug("Arguments valid, proceeding with processing");
			
			if(!isSkip(args, AQUARIUS_SYNC_OPT)) {
				aqToGdaws.migrateAqData();
			}

			if(!isSkip(args, BEDLOAD_OPT)) {
				autoProc.processBedloadCalculations();
			}
		}
	}
	
	private static boolean validateArguments(String[] args) {
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
			if(StringUtils.isBlank(GdawsConfigLoader.getProperty(p))) {
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
