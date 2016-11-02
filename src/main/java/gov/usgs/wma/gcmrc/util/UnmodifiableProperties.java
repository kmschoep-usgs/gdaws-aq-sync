package gov.usgs.wma.gcmrc.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

/**
 *
 * @author eeverman
 */
public class UnmodifiableProperties extends Properties {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UnmodifiableProperties() {
	}

	public UnmodifiableProperties(Properties protectedProperties) {
		super.putAll(protectedProperties);
	}

	@Override
	public synchronized void loadFromXML(InputStream in) throws IOException, InvalidPropertiesFormatException {
		throw new UnsupportedOperationException("This Properties class is unmodifiable so load and edit operations are not permitted.");
	}

	@Override
	public synchronized void load(InputStream inStream) throws IOException {
		throw new UnsupportedOperationException("This Properties class is unmodifiable so load and edit operations are not permitted.");
	}

	@Override
	public synchronized void load(Reader reader) throws IOException {
		throw new UnsupportedOperationException("This Properties class is unmodifiable so load and edit operations are not permitted.");
	}

	@Override
	public synchronized Object setProperty(String key, String value) {
		throw new UnsupportedOperationException("This Properties class is unmodifiable so load and edit operations are not permitted.");
	}
	
	
}
