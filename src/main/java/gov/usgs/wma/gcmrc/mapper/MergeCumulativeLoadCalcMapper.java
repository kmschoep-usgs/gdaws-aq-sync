package gov.usgs.wma.gcmrc.mapper;

import java.util.Map;


/**
 *
 * @author kmschoep
 */
public interface MergeCumulativeLoadCalcMapper {

	void calcCumulativeLoadCalcToStageTable(Map<String, Object> parms);

}
