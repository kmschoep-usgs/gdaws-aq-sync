package gov.usgs.wma.gcmrc.mapper;

import java.util.List;
import java.util.Map;

import gov.usgs.wma.gcmrc.model.TimeSeriesRecord;

/**
 *
 * @author eeverman
 */
public interface CumulativeBedloadMapper {

	void calcCumulatieBedloadToStageTable(Map<String, Object> parms);

}
