package gov.usgs.wma.gcmrc.mapper;

import java.util.Map;


/**
 *
 * @author kmschoep
 */
public interface CumulativeSandLoadMapper {

	void calcCumulativeSandLoadToStageTable(Map<String, Object> parms);

}
