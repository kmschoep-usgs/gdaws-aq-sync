package gov.usgs.wma.gcmrc.mapper;

import java.util.Map;


/**
 *
 * @author kmschoep
 */
public interface MergeCumulativeLoadCalcsMapper {

	void calcCumulativeLoadCalcsToStageTable(Map<String, Object> parms);

}
