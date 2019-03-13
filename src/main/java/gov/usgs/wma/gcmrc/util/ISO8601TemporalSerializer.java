package gov.usgs.wma.gcmrc.util;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;


/**
 * This is a Gson JSON serializer for ISO8601 formatted Temporal objects.
 * 
 * This should mimic the functionality of gov.usgs.cida.aquarius-domain.DateConverter, consider eventually 
 * refactoring into common dependency
 *
 * @author thongsav
 *
 */
public class ISO8601TemporalSerializer implements JsonSerializer<Temporal>, JsonDeserializer<Temporal> {
	private static final Logger log = LoggerFactory.getLogger(ISO8601TemporalSerializer.class);
	
	private static DateTimeFormatter timeFormat = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
	private static DateTimeFormatter localDateTimeFormat = DateTimeFormatter.ISO_DATE_TIME;
	private static DateTimeFormatter dateFormat = DateTimeFormatter.ISO_LOCAL_DATE;
	
	private static final String DATE_FORMAT = "\\d\\d\\d\\d-\\d\\d-\\d\\d"; 
	
	private final static String AGGREGATED_TIME_MARKER = "T24:00";
	private final static String AGGREGATED_TIME_REPLACEMENT = "T00:00";
	private static Gson gson = new Gson();
	@Override
	public JsonElement serialize(Temporal value, Type arg1,
			JsonSerializationContext arg2) {
		return gson.toJsonTree(print(value), String.class);
	}
	
	@Override
	public Temporal deserialize(JsonElement dateTime, Type type,
			JsonDeserializationContext arg2) throws JsonParseException {
		if(dateTime != null) {
			return parse(dateTime.getAsString());
		}
		return null;
	}
	
	/**
	 * Parses a properly formatted ISO8601 string into an equivalent Temporal object.
	 * 
	 * @param value The ISO8601 String to convert into a Temporal.
	 * @return
	 */
	public static Temporal parse(String value) {
		Temporal result = null;
		
		if (null != value) {
			try {
				if(value.contains(AGGREGATED_TIME_MARKER)) { //This represents a daily value, or other non-instant time
					TemporalAccessor parsedDate = timeFormat.parse(value.replace(AGGREGATED_TIME_MARKER, AGGREGATED_TIME_REPLACEMENT));
					result = LocalDate.from(parsedDate);
				} else if(value.matches(DATE_FORMAT)){
					TemporalAccessor parsedDate = dateFormat.parse(value);
					result = LocalDate.from(parsedDate);
				}  else { //This should represent a DateTime as closely as possible
					TemporalAccessor parsedDateTime;
					try {
						parsedDateTime = timeFormat.parse(value);
						result = OffsetDateTime.from(parsedDateTime);
					} catch (DateTimeParseException e) { //assume no offset
						parsedDateTime = localDateTimeFormat.parse(value); 
						result = LocalDateTime.from(parsedDateTime);
					}
					
				}
			} catch (Exception e) {
				log.warn("Problem parsing date string " + value + ", null is being returned.", e);
			}
		}
		return result;
	}
	
	/**
	 * Prints a temporal object out to a string that is properly formatted according
	 * to the ISO8601 standard.
	 * 
	 * @param value The Temporal to print to an ISO8601 string.
	 * @return
	 */
	public static String print(Temporal value) {
		String result = null;
		
		if (null != value) {
			if(value instanceof LocalDate) {
				result = dateFormat.format(value);
			} else if(value instanceof OffsetDateTime) {
				result = timeFormat.format(value);
			} else {
				result = localDateTimeFormat.format(value);
			}
		}
		
		return result;
	}
	
	/**
	 * LocalDate objects (with no time) are produced when AQ returns a day with 2400. This method allows users to return the DV back to
	 * a instant (with time). 2400 is not supported in Java Time so this returns 0000 on the next day.
	 * 
	 * @param t The LocalDate object to convert to a Temporal.
	 * @param z The Timezone that the resultant Temporal should be set to.
	 * @return
	 */
	public static Temporal toOffsetDateTime(LocalDate t, ZoneOffset z) {
		LocalDate newTime = t.plus(1, ChronoUnit.DAYS);
		String newTimeString = print(newTime) + "T00:00:00" + z.toString();
		TemporalAccessor parsedDateTime = timeFormat.parse(newTimeString);
		return OffsetDateTime.from(parsedDateTime);
	}
}
