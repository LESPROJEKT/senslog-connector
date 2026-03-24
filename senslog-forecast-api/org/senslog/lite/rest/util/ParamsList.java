package org.senslog.lite.rest.util;

/**
 * Class providing single list of param names for all REST services 
 * @author mkepka
 *
 */
public class ParamsList {
        
	/*
	 * Timestamps
	 */
	public static final String FROM_TIME = "from_time";
	public static final String TO_TIME = "to_time";
	public static final String START_FROM = "start_from";
	public static final String START_TO = "start_to";
    
	/*
	* Unit
	*/    
	public static final String UNIT_ID = "unit_id";
	public static final String UNIT_IDs = "unit_ids";
	
	/*
	* Geometry filter
	*/
	public static final String BBOX = "bbox";
	public static final String POINT = "point"; 
    
	/*
	* Values for parameters for REST services 
	*/    
	public static final String FID = "fid";
}