package gov.usgs.wma.gcmrc.mapper;

import java.util.Map;


/**
 *
 * @author eeverman
 */
public interface CumulativeBedloadMapper {

	void calcCumulativeBedloadToStageTable(Map<String, Object> parms);

}
