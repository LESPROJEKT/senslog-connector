package org.senslog.lite.util;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Utility class for processing weather forecasts of different types
 *  @author mkepka
 */
public class ForecastUtil {
    
	private static final String FORECAST_SCHEMA = "forecast";
	private static final String ERA5LAND_FORECAST_TABLE = "era5land_complete";
	private static final String OPEN_METEO_FORECAST_TABLE = "open_meteo_complete";
	private static final String ALIANCE_FORECAST_TABLE = "aliance_forecast_complete";
    
	/*
	* ---------------------------------- Alliance forecast --------------------------------------------------------------------------------------------------
	*/
    
    /**
     * Service processing incoming Alicance forecast
     * @param forecast - Aliance forecast JSON as String
     * @return boolean - true if forecast was processed, false when any error happened
     * @throws SQLException 
     * @throws IOException 
     */
    public static boolean processAlianceForecast(String forecast) throws SQLException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        String STATION_LOCATION = "station_location";
        String ZERO_TIME = "zero_time";
        String FORECAST_ARR = "weather_forecast_offset";
        
        String sqlGeom = "";
        
        try {
            JsonNode forecastJson = mapper.readTree(forecast);
            
            JsonNode location = forecastJson.get(STATION_LOCATION);
            if(location != null) {
            	double xCoord = location.get("longitude").asDouble();
            	double yCoord = location.get("latitude").asDouble();
            	sqlGeom = "ST_SetSRID(ST_MakePoint("+xCoord+", "+yCoord+"), 4326)";
            }
            
            String zeroTS = forecastJson.get(ZERO_TIME).asText();
            ArrayNode timeseriesArr = (ArrayNode) forecastJson.get(FORECAST_ARR);
            int timeserLen = timeseriesArr.size();
            double beginHours = timeseriesArr.get(0).get("forecast_offset_hours").asDouble();
            double endHours = timeseriesArr.get(timeserLen-1).get("forecast_offset_hours").asDouble();
            
            String beginTS = "(timestamp with time zone '"+zeroTS+"' + interval '"+ beginHours +" hours')";
            String endTS = "(timestamp with time zone '"+zeroTS+"' + interval '"+ endHours +" hours')";

            
            String sqlIns = "INSERT INTO forecast.aliance_forecast_complete(geometry, begin_forecast, end_forecast, forecast_json)\r\n"
                    + "    VALUES ("+sqlGeom+", "+beginTS+", "+endTS+", '"+forecast+"');";
            
            int inserted = SQLExecutor.executeUpdate(sqlIns);
            if(inserted >= 0) {
                return true;
            }
            else {
                return false;
            }            
        } catch (IOException e) {
            throw new IOException(e.getMessage());
        }
    }
    
    /**
     * Get list of available forecasts
     * @return
     * @throws SQLException
     * @throws IOException
     */
    public static ArrayNode getAlianceForecastList() throws SQLException, IOException {
        String sql = "SELECT fid, begin_forecast, end_forecast, inserted, st_asgeojson(geometry)"
        		+ " FROM forecast."+ALIANCE_FORECAST_TABLE+" ORDER BY inserted;";
        ResultSet res = SQLExecutor.getInstance().executeQuery(sql);
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode jsonList = mapper.createArrayNode();
        if(res != null) {
            while(res.next()) {
                ObjectNode forecast = mapper.createObjectNode();
                forecast.put("fid", res.getInt("fid"));
                forecast.put("begin_timestamp", res.getString("begin_forecast"));
                forecast.put("end_timestamp", res.getString("end_forecast"));
                forecast.put("inserted_at", res.getString("inserted"));
                JsonNode resJSON = mapper.readTree(res.getString("st_asgeojson")); 
                forecast.set("geometry", resJSON);
                 
                jsonList.add(forecast);
            }
        }
        return jsonList;        
    }
    
    /**
     * Method selects forecast JSON based on given FID
     * @param fid - ID of forecast
     * @return
     * @throws SQLException
     * @throws IOException
     */
    public static ObjectNode getAlianceForecastByFid(Integer fid) throws SQLException, IOException {
        return getForecastByFid(fid, ALIANCE_FORECAST_TABLE);
    }
    
    /**
     * Method selects forecast JSON based on given filtering parameters  
     * @param fromTime - beginning of forecast (mandatory)
     * @param toTime - end of forecast (mandatory)
     * @return
     * @throws SQLException
     * @throws IOException
     */
    public static ObjectNode getAlianceForecastByDates(String fromTime, String toTime) throws SQLException, IOException {
    	return getForecastByDates(fromTime, toTime, ALIANCE_FORECAST_TABLE);
    }
    
    /**
     * Method selects forecast JSON based on given filtering parameters  
     * @param fromTime - beginning of forecast (mandatory)
     * @param toTime - end of forecast (mandatory)
     * @param bbox - BBOX where to select reference point of forecast, pattern [minx, miny, maxx, maxy]
     * @return
     * @throws SQLException
     * @throws IOException
     */
    public static ArrayNode getAlianceForecastByDatesByBbox(String fromTime, String toTime, String bbox) throws SQLException, IOException {
    	return getForecastByDatesByBbox(fromTime, toTime, bbox, ALIANCE_FORECAST_TABLE);
    }
    
    /**
     * Method selects forecast JSON based on given filtering parameters  
     * @param fromTime - begin of forecast (mandatory)
     * @param toTime - end of forecast (mandatory)
     * @param point - location of point to select the nearest forecast, max. 3 Kilometres
     * @return
     * @throws SQLException
     * @throws IOException
     */
    public static ArrayNode getAlianceForecastByDatesByPoint(String fromTime, String toTime, String point) throws SQLException, IOException {
        return getForecastByDatesByPoint(fromTime, toTime, point, ALIANCE_FORECAST_TABLE);
    }
    
    /**
     * Method selects forecast JSON based on given filtering parameters  
     * @param fromTime - begin of forecast (mandatory)
     * @param toTime - end of forecast (mandatory)
     * @param unitId - ID of unit for which forecast is selected, max 3 km distance
     * @return
     * @throws IOException 
     * @throws SQLException 
     */
    public static ArrayNode getAlianceForecastByDatesByUnitId(String fromTime, String toTime, Long unitId) throws SQLException, IOException {
        return getForecastByDatesByUnitId(fromTime, toTime, unitId, ALIANCE_FORECAST_TABLE);
    }
    
    /**
     * Method selects forecast JSON based on given filtering parameters 
     * @param startFrom - begin of forecast (mandatory)
     * @param startTo - last begin of forecast (mandatory)
     * @param bbox - BBOX where to select reference point of forecast, pattern [minx, miny, maxx, maxy]
     * @return
     * @throws SQLException
     * @throws IOException
     */
    public static ArrayNode getAlianceForecastByStartDatesByBbox(String startFrom, String startTo, String bbox) throws SQLException, IOException {
        return getForecastByStartDatesByBbox(startFrom, startTo, bbox, ALIANCE_FORECAST_TABLE);
    }
    
    /**
     * Method selects forecast JSON based on given filtering parameters 
     * @param startFrom - begin of forecast (mandatory)
     * @param startTo - last begin of forecast (mandatory)
     * @param point - location of point to select the nearest forecast, max. 3 km
     * @return
     * @throws SQLException
     * @throws IOException
     */
    public static ArrayNode getAlianceForecastByStartDatesByPoint(String startFrom, String startTo, String point) throws SQLException, IOException {
        return getForecastByStartDatesByPoint(startFrom, startTo, point, ALIANCE_FORECAST_TABLE);
    }
    
    /**
     * Method selects forecast JSON based on given filtering parameters 
     * @param startFrom - begin of forecast (mandatory)
     * @param startTo - last begin of forecast (mandatory)
     * @param unitId - ID of unit for which forecast is selected, max 3 km distance
     * @return
     * @throws SQLException
     * @throws IOException
     */
    public static ArrayNode getAlianceForecastByStartDatesByUnitId(String startFrom, String startTo, Long unitId) throws SQLException, IOException {
        return getForecastByStartDatesByUnitId(startFrom, startTo, unitId, ALIANCE_FORECAST_TABLE);
    }
    
    /**
     * Method selects the last forecast JSON based on given filtering parameters
     * @param bbox - BBOX where to select reference point of forecast, pattern [minx, miny, maxx, maxy]
     * @return
     * @throws SQLException
     * @throws IOException
     */
    public static ArrayNode getAlianceForecastByBbox(String bbox) throws SQLException, IOException {
        return getForecastByBbox(bbox, ALIANCE_FORECAST_TABLE);
    }
    
    /**
     * Method selects the last forecast JSON based on given filtering parameters
     * @param point - location of point to select the nearest forecast, max. 3 km
     * @return
     * @throws SQLException
     * @throws IOException
     */
    public static ArrayNode getAlianceForecastByPoint(String point) throws SQLException, IOException {
        return getForecastByPoint(point, ALIANCE_FORECAST_TABLE);
    }
    
    /**
     * Method selects the last forecast JSON based on given filtering parameters
     * @param unitId - ID of unit for which forecast is selected, max 3 km distance
     * @return
     * @throws SQLException
     * @throws IOException
     */
    public static ArrayNode getAlianceForecastByUnitId(Long unitId) throws SQLException, IOException {
        return getForecastByUnitId(unitId, ALIANCE_FORECAST_TABLE);
    }
    
    /**
     * Method selects the last forecast JSON based on given filtering parameters
     * @param bbox - BBOX where to select reference point of forecast, pattern [minx, miny, maxx, maxy]
     * @return
     * @throws SQLException
     * @throws IOException
     */
    public static ArrayNode getAlianceForecastLastByBbox(String bbox) throws SQLException, IOException {
        return getLastForecastByBbox(bbox, ALIANCE_FORECAST_TABLE);
    }
    
    /**
     * Method selects the last forecast JSON based on given filtering parameters
     * @param point - location of point to select the nearest forecast, max. 3 km
     * @return
     * @throws SQLException
     * @throws IOException
     */
    public static ArrayNode getAlianceForecastLastByPoint(String point) throws SQLException, IOException {
        return getLastForecastByPoint(point, ALIANCE_FORECAST_TABLE);
    }
    
    /**
     * Method selects the last forecast JSON based on given filtering parameters
     * @param unitId - ID of unit for which forecast is selected, max 3 km distance
     * @return
     * @throws SQLException
     * @throws IOException
     */
    public static ArrayNode getAlianceForecastLastByUnitId(Long unitId) throws SQLException, IOException {
        return getLastForecastByUnitId(unitId, ALIANCE_FORECAST_TABLE);
    }
    
    /**
     * Method selects the list of forecast metadata based on given filtering parameters
     * @param bbox - BBOX where to select reference point of forecast, pattern [minx, miny, maxx, maxy]
     * @return Array with object of Forecast (fid, begin_forecast, end_forecast, inserted, geojson)
     * @throws SQLException
     * @throws IOException
     */
    public static ArrayNode getAlianceForecastListByBbox(String bbox) throws SQLException, IOException {
    	return getForecastListByBbox(bbox, ALIANCE_FORECAST_TABLE);
    }
    
    /**
     * Method returns list of forecasts metadata for specified forecast table based on given filtering parameters
     * @param point - location of point to select the nearest forecast
     * @param distance in meters to search for the forecast reference point 
     * @return Array with object of Forecast (fid, begin_forecast, end_forecast, inserted, geojson)
     * @throws SQLException
     * @throws IOException
     */
    public static ArrayNode getAlianceForecastListByPoint(String point, int distance) throws SQLException, IOException {
    	return getForecastListByPoint(point, ALIANCE_FORECAST_TABLE, distance);
    }
    
    /**
     * Method returns list of forecasts metadata for specified forecast table based on given filtering parameters
     * @param unitId - ID of unit to search for the forecast 
     * @param distance in meters to search for the forecast reference point
     * @return Array with object of Forecast (fid, begin_forecast, end_forecast, inserted, geojson)
     * @throws SQLException
     * @throws IOException
     */
    public static ArrayNode getAlianceForecastListByUnitId(Long unitId, int distance) throws SQLException, IOException {
    	return getForecastListByUnitId(unitId, ALIANCE_FORECAST_TABLE, distance);
    }  
    
    /*
     * ---------------------------------- ERA5Land -----------------------------------------------------
     */
    
    /**
     * Method provides list of localities to download ERA5Land data
     * @return Array of localities
     * @throws SQLException 
     * @throws IOException 
     */
    public static ArrayNode getEra5LandPlan() throws SQLException, IOException {
        String sql = "SELECT id, description, inserted, st_asgeojson(geometry) FROM forecast.era5land_planner WHERE is_active = true ORDER BY inserted;";
        ResultSet res = SQLExecutor.getInstance().executeQuery(sql);
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode jsonList = mapper.createArrayNode();
        if(res != null) {
            while(res.next()) {
                ObjectNode forecast = mapper.createObjectNode();
                forecast.put("id", res.getInt("id"));
                forecast.put("description", res.getString("description"));
                forecast.put("inserted_at", res.getString("inserted"));
                JsonNode resJSON = mapper.readTree(res.getString("st_asgeojson")); 
                forecast.set("geometry", resJSON);
                 
                jsonList.add(forecast);
            }
        }
        return jsonList;
    }
    
    /**
     * Method processes new ERA5Land dataset for defined locality
     * @param forecast JSON as String
     * @return true if the dataset was processed, false when any error happenned
     * @throws IOException
     * @throws SQLException
     */
    public static boolean processEra5Land(String forecast) throws IOException, SQLException {
        ObjectMapper mapper = new ObjectMapper();
        String TIMEZONE_TYPE_NAME = "GMT";
        
        String sqlGeom = "";
        String beginForecast = "";
        String endForecast = "";
        
        try {
            JsonNode forecastJson = mapper.readTree(forecast);
            
            double xCoord = forecastJson.get("longitude").asDouble();
            double yCoord = forecastJson.get("latitude").asDouble();
            sqlGeom = "ST_SetSRID(ST_MakePoint("+xCoord+", "+yCoord+"),4326)";
            
            String timezone = forecastJson.get("timezone").asText();
            
            JsonNode hourlyData = forecastJson.get("hourly");
            ArrayNode timeArr = (ArrayNode) hourlyData.get("time");
            
            int timeArrLen = timeArr.size();
            beginForecast = timeArr.get(0).asText("");
            endForecast = timeArr.get(timeArrLen-1).asText("");
            if(timezone.equalsIgnoreCase(TIMEZONE_TYPE_NAME)) {
                beginForecast = beginForecast+"Z";
                endForecast = endForecast+"Z";
            }
            String sqlIns = "INSERT INTO forecast.era5land_complete(geometry, begin_forecast, end_forecast, forecast_json)\r\n"
                    + "    VALUES ("+sqlGeom+", '"+beginForecast+"', '"+endForecast+"', '"+forecast+"');";

            int inserted = SQLExecutor.executeUpdate(sqlIns);
            if(inserted >= 0) {
                return true;
            }
            else {
                return false;
            }            
        } catch (IOException e) {
            throw new IOException(e.getMessage());
        }
    }
    
    /**
     * Method returns metadata of all ERA5Land datasets available in the database
     * @return
     * @throws SQLException
     * @throws IOException
     */
    public static ArrayNode getEra5LandList() throws SQLException, IOException {
        String sql = "SELECT fid, begin_forecast, end_forecast, inserted, st_asgeojson(geometry)"
        		+ " FROM forecast.era5land_complete ORDER BY inserted;";
        ResultSet res = SQLExecutor.getInstance().executeQuery(sql);
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode jsonList = mapper.createArrayNode();
        if(res != null) {
            while(res.next()) {
                ObjectNode forecast = mapper.createObjectNode();
                forecast.put("fid", res.getInt("fid"));
                forecast.put("begin_timestamp", res.getString("begin_forecast"));
                forecast.put("end_timestamp", res.getString("end_forecast"));
                forecast.put("inserted_at", res.getString("inserted"));
                JsonNode resJSON = mapper.readTree(res.getString("st_asgeojson")); 
                forecast.set("geometry", resJSON);
                 
                jsonList.add(forecast);
            }
        }
        return jsonList;        
    }
    
    /**
     * Method provides ERA5Land datasets for given time period
     * @param fromTime - beginning of forecast (mandatory)
     * @param toTime - end of forecast (mandatory)
     * @return
     * @throws SQLException
     * @throws IOException
     */
    public static ArrayNode getEra5LandByDates(String fromTime, String toTime) throws SQLException, IOException {
        String sql = "SELECT forecast_json FROM forecast.era5land_complete"
                + " WHERE begin_forecast >= '"+fromTime+"'"
                + " AND end_forecast < '"+toTime+"'"
                + "ORDER BY inserted;";
        ResultSet res = SQLExecutor.getInstance().executeQuery(sql);
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode forecastArr = mapper.createArrayNode();
        if(res != null) {
            while(res.next()) {
                String resultJSON = res.getString("forecast_json");
                JsonNode resJSON = mapper.readTree(resultJSON); 
                forecastArr.add(resJSON);
            }
        }
        return forecastArr;
    }

    /**
     * Method selects forecast JSON based on given filtering parameters  
     * @param fromTime - beginning of forecast (mandatory)
     * @param toTime - end of forecast (mandatory)
     * @param bbox - BBOX where to select reference point of forecast, pattern [minx, miny, maxx, maxy]
     * @return
     * @throws SQLException, IllegalArgumentException 
     */
    public static ArrayNode getEra5LandByDatesByBbox(String fromTime, String toTime, String bbox) throws SQLException, IllegalArgumentException, IOException {
        // check of parameters
        if (fromTime == null || toTime == null || bbox == null || bbox.isEmpty()) {
            throw new IllegalArgumentException("Parameters fromTime, toTime and bbox must not be NULL or empty!");
        }
        String[] bboxParts = bbox.split(",");
        if (bboxParts.length != 4) {
            throw new IllegalArgumentException("Parameter bbox must contain 4 values (minx, miny, maxx, maxy) delimited by comma!");
        }
        
        String sql = "SELECT forecast_json FROM forecast.era5land_complete"
                + " WHERE begin_forecast >= '"+fromTime+"' AND end_forecast < '"+toTime+"'"
                + " AND ST_Intersects(geometry, ST_MakeEnvelope("+bbox+", 4326)) ORDER BY begin_forecast;";
        
        ResultSet res = SQLExecutor.getInstance().executeQuery(sql);
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode forecastArr = mapper.createArrayNode();
        if(res != null) {
            while(res.next()) {
                String resultJSON = res.getString("forecast_json");
                JsonNode resJSON = mapper.readTree(resultJSON); 
                forecastArr.add(resJSON);
            }
        }
        return forecastArr;
    }

    /**
     * Method selects forecast JSON based on given filtering parameters  
     * @param fromTime - begin of forecast (mandatory)
     * @param toTime - end of forecast (mandatory)
     * @param unitId - ID of unit for which forecast is selected, max 3 km distance
     * @return
     * @throws SQLException
     * @throws IOException
     */
    public static ArrayNode getEra5LandByDatesByUnitId(String fromTime, String toTime, Long unitId) throws SQLException, IOException {
        // check of parameters
        if (fromTime == null && toTime == null || unitId == null) {
            throw new IllegalArgumentException("Parameters fromTime or toTime and unitId must not be NULL!");
        }
        
        String sql = "SELECT forecast_json FROM forecast.era5land_complete"
                + " WHERE begin_forecast >= '"+fromTime+"' AND end_forecast < '"+toTime+"'"
                + " AND ST_DWithin(geography(geometry), geography((SELECT the_geom FROM public.last_units_positions WHERE unit_id = "+unitId+")), 3000, true)"
                + " ORDER BY begin_forecast";
        
        ResultSet res = SQLExecutor.getInstance().executeQuery(sql);
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode forecastArr = mapper.createArrayNode();
        if(res != null) {
            while(res.next()) {
                String resultJSON = res.getString("forecast_json");
                JsonNode resJSON = mapper.readTree(resultJSON); 
                forecastArr.add(resJSON);
            }
        }
        return forecastArr;
    }

    /**
     * Method selects forecast JSON based on given filtering parameters  
     * @param fromTime - begin of forecast (mandatory)
     * @param toTime - end of forecast (mandatory)
     * @param point - location of point to select the nearest forecast, max. 3 km
     * @return
     * @throws SQLException
     * @throws IOException
     */
    public static ArrayNode getEra5LandByDatesByPoint(String fromTime, String toTime, String point) throws SQLException, IOException {
        // check of parameters
        if (fromTime == null && toTime == null || point == null) {
            throw new IllegalArgumentException("Parameters fromTime or toTime and unitId must not be NULL!");
        }
        String[] pointParts = point.split(",");
        if (pointParts.length != 2) {
            throw new IllegalArgumentException("Parameter point must contain 2 values (x, y) delimited by comma!");
        }
        
        
        String sql = "SELECT forecast_json FROM forecast.era5land_complete"
                + " WHERE begin_forecast >= '"+fromTime+"' AND end_forecast < '"+toTime+"'"
                + " AND ST_DWithin(geography(geometry), geography(ST_SetSRID(ST_Point("+point+"), 4326)), 3000, true)"
                + " ORDER BY begin_forecast;";
        
        ResultSet res = SQLExecutor.getInstance().executeQuery(sql);
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode forecastArr = mapper.createArrayNode();
        if(res != null) {
            while(res.next()) {
                String resultJSON = res.getString("forecast_json");
                JsonNode resJSON = mapper.readTree(resultJSON); 
                forecastArr.add(resJSON);
            }
        }
        return forecastArr;
    }
    
    /**
     * Method selects forecast JSON based on given filtering parameters 
     * @param startFrom - begin of forecast (mandatory)
     * @param startTo - last begin of forecast (mandatory)
     * @param bbox - BBOX where to select reference point of forecast, pattern [minx, miny, maxx, maxy]
     * @return
     * @throws SQLException
     * @throws IllegalArgumentException
     * @throws IOException
     */
    public static ArrayNode getEra5LandByStartDatesByBbox(String startFrom, String startTo, String bbox) throws SQLException, IllegalArgumentException, IOException {
        // check of parameters
        if (startFrom == null && startTo == null || bbox == null || bbox.isEmpty()) {
            throw new IllegalArgumentException("Parameters fromTime or toTime and bbox must not be NULL or empty!");
        }

        String[] bboxParts = bbox.split(",");
        if (bboxParts.length != 4) {
            throw new IllegalArgumentException("Parameter bbox must contain 4 values (minx, miny, maxx, maxy) delimited by comma!");
        }
        
        String sql = "SELECT forecast_json FROM forecast.era5land_complete"
                + " WHERE begin_forecast >= '"+startFrom+"' AND begin_forecast < '"+startTo+"'"
                + " AND ST_Intersects(geometry, ST_MakeEnvelope("+bbox+", 4326)) ORDER BY begin_forecast;";
        
        ResultSet res = SQLExecutor.getInstance().executeQuery(sql);
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode forecastArr = mapper.createArrayNode();
        if(res != null) {
            while(res.next()) {
                String resultJSON = res.getString("forecast_json");
                JsonNode resJSON = mapper.readTree(resultJSON); 
                forecastArr.add(resJSON);
            }
        }
        return forecastArr;
    }

    /**
     * Method selects forecast JSON based on given filtering parameters 
     * @param startFrom - begin of forecast (mandatory)
     * @param startTo - last begin of forecast (mandatory)
     * @param point - location of point to select the nearest forecast, max. 3 km
     * @return
     * @throws SQLException
     * @throws IOException
     */
    public static ArrayNode getEra5LandByStartDatesByPoint(String startFrom, String startTo, String point) throws SQLException, IOException {
        // check of parameters
        if (startFrom == null && startTo == null || point == null || point.isEmpty()) {
            throw new IllegalArgumentException("Parameters startFrom or startTo and Point must not be NULL or empty!");
        }
        String[] pointParts = point.split(",");
        if (pointParts.length != 2) {
            throw new IllegalArgumentException("Parameter point must contain 2 values (x, y) delimited by comma!");
        }
        
        String sql = "SELECT forecast_json FROM forecast.era5land_complete"
                + " WHERE begin_forecast >= '"+startFrom+"' AND begin_forecast < '"+startTo+"'"
                + " AND ST_DWithin(geography(geometry), geography(ST_SetSRID(ST_Point("+point+"), 4326)), 3000, true)"
                + " ORDER BY begin_forecast;";
        
        ResultSet res = SQLExecutor.getInstance().executeQuery(sql);
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode forecastArr = mapper.createArrayNode();
        if(res != null) {
            while(res.next()) {
                String resultJSON = res.getString("forecast_json");
                JsonNode resJSON = mapper.readTree(resultJSON); 
                forecastArr.add(resJSON);
            }
        }
        return forecastArr;
    }

    /**
     * Method selects forecast JSON based on given filtering parameters 
     * @param startFrom - begin of forecast (mandatory)
     * @param startTo - last begin of forecast (mandatory)
     * @param unitId - ID of unit for which forecast is selected, max 3 km distance
     * @return
     * @throws SQLException
     * @throws IOException
     */
    public static ArrayNode getEra5LandByStartDatesByUnitId(String startFrom, String startTo, Long unitId) throws SQLException, IOException {
        // check of parameters
        if (startFrom == null && startTo == null || unitId == null) {
            throw new IllegalArgumentException("Parameters startFrom or startTo and unitId must not be NULL!");
        }

        String sql = "SELECT forecast_json FROM forecast.era5land_complete"
                + " WHERE begin_forecast >= '"+startFrom+"' AND begin_forecast < '"+startTo+"'"
                + " AND ST_DWithin(geography(geometry), geography((SELECT the_geom FROM public.last_units_positions WHERE unit_id = "+unitId+")), 3000, true)"
                + " ORDER BY begin_forecast;";
        
        ResultSet res = SQLExecutor.getInstance().executeQuery(sql);
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode forecastArr = mapper.createArrayNode();
        if(res != null) {
            while(res.next()) {
                String resultJSON = res.getString("forecast_json");
                JsonNode resJSON = mapper.readTree(resultJSON); 
                forecastArr.add(resJSON);
            }
        }
        return forecastArr;
    }

    /**
     * Method selects forecast JSON based on given filtering parameters
     * @param bbox - BBOX where to select reference point of forecast, pattern [minx, miny, maxx, maxy]
     * @return
     * @throws SQLException
     * @throws IOException
     */
    public static ArrayNode getEra5LandByBbox(String bbox) throws SQLException, IOException {
    	return getForecastByBbox(bbox, ERA5LAND_FORECAST_TABLE);
    }

    /**
     * Method selects forecast JSON based on given filtering parameters
     * @param point - location of point to select the nearest forecast, max. 3 km
     * @return
     * @throws SQLException
     * @throws IOException
     */
    public static ArrayNode getEra5LandByPoint(String point) throws SQLException, IOException {
    	return getForecastByPoint(point, ERA5LAND_FORECAST_TABLE);
    }
    
    /**
     * Method selects forecast JSON based on given filtering parameters
     * @param unitId - ID of unit for which forecast is selected, max 3 km distance
     * @return
     * @throws SQLException
     * @throws IOException
     */
    public static ArrayNode getEra5LandByUnitId(Long unitId) throws SQLException, IOException {
    	return getForecastByUnitId(unitId, ERA5LAND_FORECAST_TABLE);
    }
    
    /**
     * Method selects forecast JSON based on given filtering parameters
     * @param bbox - BBOX where to select reference point of forecast, pattern [minx, miny, maxx, maxy]
     * @return
     * @throws SQLException
     * @throws IOException
     */
    public static ArrayNode getEra5LandLastByBbox(String bbox) throws SQLException, IOException {
        return getLastForecastByBbox(bbox, ERA5LAND_FORECAST_TABLE);
    }
    
    /**
     * Method selects forecast JSON based on given filtering parameters
     * @param point - location of point to select the nearest forecast, max. 3 km
     * @return
     * @throws SQLException
     * @throws IOException
     */
    public static ArrayNode getEra5LandLastByPoint(String point) throws SQLException, IOException {
    	return getLastForecastByPoint(point, ERA5LAND_FORECAST_TABLE);
    }
    
    /**
     * Method selects forecast JSON based on given filtering parameters
     * @param unitId - ID of unit for which forecast is selected, max 3 km distance
     * @return
     * @throws SQLException
     * @throws IOException
     */
    public static ArrayNode getEra5LandLastByUnitId(Long unitId) throws SQLException, IOException {
    	return getLastForecastByUnitId(unitId, ERA5LAND_FORECAST_TABLE);
    }
    
    /**
     * Method selects the list of forecast metadata based on given filtering parameters
     * @param bbox - BBOX where to select reference point of forecast, pattern [minx, miny, maxx, maxy]
     * @return Array with object of Forecast (fid, begin_forecast, end_forecast, inserted, geojson)
     * @throws SQLException
     * @throws IOException
     */
    public static ArrayNode getEra5LandListByBbox(String bbox) throws SQLException, IOException {
    	return getForecastListByBbox(bbox, ERA5LAND_FORECAST_TABLE);
    }
    
    /**
     * Method returns list of forecasts metadata for specified forecast table based on given filtering parameters
     * @param point - location of point to select the nearest forecast
     * @param distance in meters to search for the forecast reference point 
     * @return Array with object of Forecast (fid, begin_forecast, end_forecast, inserted, geojson)
     * @throws SQLException
     * @throws IOException
     */
    public static ArrayNode getEra5LandListByPoint(String point, int distance) throws SQLException, IOException {
    	return getForecastListByPoint(point, ERA5LAND_FORECAST_TABLE, distance);
    }
    
    /**
     * Method returns list of forecasts metadata for specified forecast table based on given filtering parameters
     * @param unitId - ID of unit to search for the forecast 
     * @param distance in meters to search for the forecast reference point
     * @return Array with object of Forecast (fid, begin_forecast, end_forecast, inserted, geojson)
     * @throws SQLException
     * @throws IOException
     */
    public static ArrayNode getEra5LandListByUnitId(Long unitId, int distance) throws SQLException, IOException {
    	return getForecastListByUnitId(unitId, ERA5LAND_FORECAST_TABLE, distance);
    }
    
    /**
     * Method selects forecast JSON based on given FID
     * @param fid - ID of forecast
     * @return
     * @throws SQLException
     * @throws IOException
     */
    public static ObjectNode getEra5LandByFid(Integer fid) throws SQLException, IOException {
        return getForecastByFid(fid, ERA5LAND_FORECAST_TABLE);
    }
    
    /*
     * ---------------------------------- OpenMeteo -----------------------------------------------------
     */
    
    /**
     * 
     * @return
     * @throws SQLException 
     * @throws IOException 
     */
    public static ArrayNode getOpenMeteoPlan() throws SQLException, IOException {
        String sql = "SELECT id, description, inserted, st_asgeojson(geometry) FROM forecast.open_meteo_planner WHERE is_active = true ORDER BY inserted;";
        ResultSet res = SQLExecutor.getInstance().executeQuery(sql);
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode jsonList = mapper.createArrayNode();
        if(res != null) {
            while(res.next()) {
                ObjectNode forecast = mapper.createObjectNode();
                forecast.put("id", res.getInt("id"));
                forecast.put("description", res.getString("description"));
                forecast.put("inserted_at", res.getString("inserted"));
                JsonNode resJSON = mapper.readTree(res.getString("st_asgeojson")); 
                forecast.set("geometry", resJSON);
                 
                jsonList.add(forecast);
            }
        }
        return jsonList;
    }
    
    /**
     * 
     * @param forecast
     * @return
     * @throws IOException
     * @throws SQLException 
     */
    public static boolean processOpenMeteoForecast(String forecast) throws IOException, SQLException {
        ObjectMapper mapper = new ObjectMapper();
        String TIMEZONE_TYPE_NAME = "GMT";
        
        String sqlGeom = "";
        String beginForecast = "";
        String endForecast = "";
        
        try {
            JsonNode forecastJson = mapper.readTree(forecast);
            
            double xCoord = forecastJson.get("longitude").asDouble();
            double yCoord = forecastJson.get("latitude").asDouble();
            sqlGeom = "ST_SetSRID(ST_MakePoint("+xCoord+", "+yCoord+"),4326)";
            
            String timezone = forecastJson.get("timezone").asText();
            
            JsonNode hourlyData = forecastJson.get("hourly");
            ArrayNode timeArr = (ArrayNode) hourlyData.get("time");
            
            int timeArrLen = timeArr.size();
            beginForecast = timeArr.get(0).asText("");
            endForecast = timeArr.get(timeArrLen-1).asText("");
            if(timezone.equalsIgnoreCase(TIMEZONE_TYPE_NAME)) {
                beginForecast = beginForecast+"Z";
                endForecast = endForecast+"Z";
            }
            String sqlIns = "INSERT INTO forecast.open_meteo_complete(geometry, begin_forecast, end_forecast, forecast_json)\r\n"
                    + "    VALUES ("+sqlGeom+", '"+beginForecast+"', '"+endForecast+"', '"+forecast+"');";
            
	    int inserted = SQLExecutor.executeUpdate(sqlIns);
            if(inserted >= 0) {
                return true;
            }
            else {
                return false;
            }            
        } catch (IOException e) {
            throw new IOException(e.getMessage());
        }
    }
    
    /**
     * 
     * @return
     * @throws SQLException
     * @throws IOException
     */
    public static ArrayNode getOpenMeteoForecastList() throws SQLException, IOException {
        String sql = "SELECT fid, begin_forecast, end_forecast, inserted, st_asgeojson(geometry)"
        		+ " FROM forecast.open_meteo_complete ORDER BY inserted;";
        ResultSet res = SQLExecutor.getInstance().executeQuery(sql);
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode jsonList = mapper.createArrayNode();
        if(res != null) {
            while(res.next()) {
                ObjectNode forecast = mapper.createObjectNode();
                forecast.put("fid", res.getInt("fid"));
                forecast.put("begin_timestamp", res.getString("begin_forecast"));
                forecast.put("end_timestamp", res.getString("end_forecast"));
                forecast.put("inserted_at", res.getString("inserted"));
                JsonNode resJSON = mapper.readTree(res.getString("st_asgeojson")); 
                forecast.set("geometry", resJSON);
                 
                jsonList.add(forecast);
            }
        }
        return jsonList;        
    }
    
    /**
     * 
     * @param fromTime
     * @param toTime
     * @return
     * @throws SQLException
     * @throws IOException
     */
    public static ArrayNode getOpenMeteoForecastByDates(String fromTime, String toTime) throws SQLException, IOException {
        String sql = "SELECT forecast_json FROM forecast.open_meteo_complete"
                + " WHERE begin_forecast >= '"+fromTime+"'"
                + " AND end_forecast < '"+toTime+"'"
                + "ORDER BY inserted;";
        ResultSet res = SQLExecutor.getInstance().executeQuery(sql);
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode forecastArr = mapper.createArrayNode();
        if(res != null) {
            while(res.next()) {
                String resultJSON = res.getString("forecast_json");
                JsonNode resJSON = mapper.readTree(resultJSON); 
                forecastArr.add(resJSON);
            }
        }
        return forecastArr;
    }



    /**
     * Method selects forecast JSON based on given filtering parameters  
     * @param fromTime - beginning of forecast (mandatory)
     * @param toTime - end of forecast (mandatory)
     * @param bbox - BBOX where to select reference point of forecast, pattern [minx, miny, maxx, maxy]
     * @return
     * @throws SQLException
     * @throws IOException
     */
    public static ArrayNode getOpenMeteoByDatesByBbox(String fromTime, String toTime, String bbox) throws SQLException, IOException {
    	return getForecastByDatesByBbox(fromTime, toTime, bbox, OPEN_METEO_FORECAST_TABLE);
    }

    /**
     * Method selects forecast JSON based on given filtering parameters  
     * @param fromTime - begin of forecast (mandatory)
     * @param toTime - end of forecast (mandatory)
     * @param point - location of point to select the nearest forecast, max. 3 km
     * @return
     * @throws SQLException
     * @throws IOException
     */
    public static ArrayNode getOpenMeteoByDatesByPoint(String fromTime, String toTime, String point) throws SQLException, IOException {
        return getForecastByDatesByPoint(fromTime, toTime, point, OPEN_METEO_FORECAST_TABLE);
    }
    
    /**
     * Method selects forecast JSON based on given filtering parameters  
     * @param fromTime - begin of forecast (mandatory)
     * @param toTime - end of forecast (mandatory)
     * @param unitId - ID of unit for which forecast is selected, max 3 km distance
     * @return
     * @throws IOException 
     * @throws SQLException 
     */
    public static ArrayNode getOpenMeteoByDatesByUnitId(String fromTime, String toTime, Long unitId) throws SQLException, IOException {
        return getForecastByDatesByUnitId(fromTime, toTime, unitId, OPEN_METEO_FORECAST_TABLE);
    }
    
    /**
     * Method selects forecast JSON based on given filtering parameters 
     * @param startFrom - begin of forecast (mandatory)
     * @param startTo - last begin of forecast (mandatory)
     * @param bbox - BBOX where to select reference point of forecast, pattern [minx, miny, maxx, maxy]
     * @return
     * @throws SQLException
     * @throws IOException
     */
    public static ArrayNode getOpenMeteoByStartDatesByBbox(String startFrom, String startTo, String bbox) throws SQLException, IOException {
        return getForecastByStartDatesByBbox(startFrom, startTo, bbox, OPEN_METEO_FORECAST_TABLE);
    }
    
    /**
     * Method selects forecast JSON based on given filtering parameters 
     * @param startFrom - begin of forecast (mandatory)
     * @param startTo - last begin of forecast (mandatory)
     * @param point - location of point to select the nearest forecast, max. 3 km
     * @return
     * @throws SQLException
     * @throws IOException
     */
    public static ArrayNode getOpenMeteoByStartDatesByPoint(String startFrom, String startTo, String point) throws SQLException, IOException {
        return getForecastByStartDatesByPoint(startFrom, startTo, point, OPEN_METEO_FORECAST_TABLE);
    }
    
    /**
     * Method selects forecast JSON based on given filtering parameters 
     * @param startFrom - begin of forecast (mandatory)
     * @param startTo - last begin of forecast (mandatory)
     * @param unitId - ID of unit for which forecast is selected, max 3 km distance
     * @return
     * @throws SQLException
     * @throws IOException
     */
    public static ArrayNode getOpenMeteoByStartDatesByUnitId(String startFrom, String startTo, Long unitId) throws SQLException, IOException {
        return getForecastByStartDatesByUnitId(startFrom, startTo, unitId, OPEN_METEO_FORECAST_TABLE);
    }

    /**
     * Method selects forecast JSON based on given filtering parameters
     * @param bbox - BBOX where to select reference point of forecast, pattern [minx, miny, maxx, maxy]
     * @return
     * @throws SQLException
     * @throws IOException
     */
    public static ArrayNode getOpenMeteoByBbox(String bbox) throws SQLException, IOException {
        return getForecastByBbox(bbox, OPEN_METEO_FORECAST_TABLE);
    }
    
    /**
     * Method selects forecast JSON based on given filtering parameters
     * @param point - location of point to select the nearest forecast, max. 3 km
     * @return
     * @throws SQLException
     * @throws IOException
     */
    public static ArrayNode getOpenMeteoByPoint(String point) throws SQLException, IOException {
        return getForecastByPoint(point, OPEN_METEO_FORECAST_TABLE);
    }
    
    /**
     * Method selects forecast JSON based on given filtering parameters
     * @param unitId - ID of unit for which forecast is selected, max 3 km distance
     * @return
     * @throws SQLException
     * @throws IOException
     */
    public static ArrayNode getOpenMeteoByUnitId(Long unitId) throws SQLException, IOException {
        return getForecastByUnitId(unitId, OPEN_METEO_FORECAST_TABLE);
    }
    
    /**
     * Method selects forecast JSON based on given filtering parameters
     * @param bbox - BBOX where to select reference point of forecast, pattern [minx, miny, maxx, maxy]
     * @return
     * @throws SQLException
     * @throws IOException
     */
    public static ArrayNode getOpenMeteoLastByBbox(String bbox) throws SQLException, IOException {
        return getLastForecastByBbox(bbox, OPEN_METEO_FORECAST_TABLE);
    }
    
    /**
     * Method selects the last forecast JSON based on given filtering parameters
     * @param point - location of point to select the nearest forecast, max. 3 km
     * @return
     * @throws SQLException
     * @throws IOException
     */
    public static ArrayNode getOpenMeteoLastByPoint(String point) throws SQLException, IOException {
        return getLastForecastByPoint(point, OPEN_METEO_FORECAST_TABLE);
    }
    
    /**
     * Method selects the last forecast JSON based on given filtering parameters
     * @param unitId - ID of unit for which forecast is selected, max 3 km distance
     * @return
     * @throws SQLException
     * @throws IOException
     */
    public static ArrayNode getOpenMeteoLastByUnitId(Long unitId) throws SQLException, IOException {
        return getLastForecastByUnitId(unitId, OPEN_METEO_FORECAST_TABLE);
    }
    
    /**
     * Method selects the list of forecast metadata based on given filtering parameters
     * @param bbox - BBOX where to select reference point of forecast, pattern [minx, miny, maxx, maxy]
     * @return Array with object of Forecast (fid, begin_forecast, end_forecast, inserted, geojson)
     * @throws SQLException
     * @throws IOException
     */
    public static ArrayNode getOpenMeteoListByBbox(String bbox) throws SQLException, IOException {
    	return getForecastListByBbox(bbox, OPEN_METEO_FORECAST_TABLE);
    }
    
    /**
     * Method returns list of forecasts metadata for specified forecast table based on given filtering parameters
     * @param point - location of point to select the nearest forecast
     * @param distance in meters to search for the forecast reference point 
     * @return Array with object of Forecast (fid, begin_forecast, end_forecast, inserted, geojson)
     * @throws SQLException
     * @throws IOException
     */
    public static ArrayNode getOpenMeteoListByPoint(String point, int distance) throws SQLException, IOException {
    	return getForecastListByPoint(point, OPEN_METEO_FORECAST_TABLE, distance);
    }
    
    /**
     * Method returns list of forecasts metadata for specified forecast table based on given filtering parameters
     * @param unitId - ID of unit to search for the forecast 
     * @param distance in meters to search for the forecast reference point
     * @return Array with object of Forecast (fid, begin_forecast, end_forecast, inserted, geojson)
     * @throws SQLException
     * @throws IOException
     */
    public static ArrayNode getOpenMeteoListByUnitId(Long unitId, int distance) throws SQLException, IOException {
    	return getForecastListByUnitId(unitId, OPEN_METEO_FORECAST_TABLE, distance);
    }
    
    /**
     * Method selects forecast JSON based on given FID
     * @param fid - ID of forecast
     * @return
     * @throws SQLException
     * @throws IOException
     */
    public static ObjectNode getOpenMeteoByFid(Integer fid) throws SQLException, IOException {
        return getForecastByFid(fid, OPEN_METEO_FORECAST_TABLE);
    }
    
/*
 * ---------------------------------- universal methods --------------------------------------------------------------------------------------------------
 */
    /**
     * Method selects forecast JSON based on given filtering parameters  
     * @param fromTime - beginning of forecast (mandatory)
     * @param toTime - end of forecast (mandatory)
     * @param forecastTable - name of the forecast table
     * @return
     * @throws SQLException
     * @throws IOException
     */
    public static ObjectNode getForecastByDates(String fromTime, String toTime, String forecastTable) throws SQLException, IOException {
        String sql = "SELECT forecast_json FROM "+FORECAST_SCHEMA+"."+forecastTable+""
                + " WHERE begin_forecast >= '"+fromTime+"'"
                + " AND end_forecast < '"+toTime+"'"
                + "ORDER BY inserted;";
        ResultSet res = SQLExecutor.getInstance().executeQuery(sql);
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode geojson = mapper.createObjectNode();
        geojson.put("type", "FeatureCollection");
        ArrayNode features = mapper.createArrayNode();
        if(res != null) {
            while(res.next()) {
                String resultJSON = res.getString("forecast_json");
                JsonNode resJSON = mapper.readTree(resultJSON); 
                features.add(resJSON);
            }
        }
        geojson.set("features",features);
        return geojson;
    }
    
    /**
     * Method selects forecast JSON based on given filtering parameters  
     * @param fromTime - beginning of forecast (mandatory)
     * @param toTime - end of forecast (mandatory)
     * @param bbox - BBOX where to select reference point of forecast, pattern [minx, miny, maxx, maxy]
     * @return
     * @throws SQLException
     * @throws IOException
     */
    public static ArrayNode getForecastByDatesByBbox(String fromTime, String toTime, String bbox, String forecastTable) throws SQLException, IOException {
        // check of parameters
        if (fromTime == null || toTime == null || bbox == null || bbox.isEmpty()) {
            throw new IllegalArgumentException("Parameters fromTime, toTime and bbox must not be NULL or empty!");
        }
        String[] bboxParts = bbox.split(",");
        if (bboxParts.length != 4) {
            throw new IllegalArgumentException("Parameter bbox must contain 4 values (minx, miny, maxx, maxy) delimited by comma!");
        }
        
        String sql = "SELECT forecast_json FROM "+FORECAST_SCHEMA+"."+forecastTable+""
                + " WHERE begin_forecast >= '"+fromTime+"' AND end_forecast < '"+toTime+"'"
                + " AND ST_Intersects(geometry, ST_MakeEnvelope("+bbox+", 4326)) ORDER BY begin_forecast;";
        
        ResultSet res = SQLExecutor.getInstance().executeQuery(sql);
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode forecastArr = mapper.createArrayNode();
        if(res != null) {
            while(res.next()) {
                String resultJSON = res.getString("forecast_json");
                JsonNode resJSON = mapper.readTree(resultJSON); 
                forecastArr.add(resJSON);
            }
        }
        return forecastArr;
    }
    
    /**
     * Method selects forecast JSON based on given filtering parameters  
     * @param fromTime - begin of forecast (mandatory)
     * @param toTime - end of forecast (mandatory)
     * @param point - location of point to select the nearest forecast, max. 3 km
     * @param forecastTable - name of the forecast table
     * @return
     * @throws SQLException
     * @throws IOException
     */
    public static ArrayNode getForecastByDatesByPoint(String fromTime, String toTime, String point, String forecastTable) throws SQLException, IOException {
        // check of parameters
        if (fromTime == null && toTime == null || point == null) {
            throw new IllegalArgumentException("Parameters fromTime or toTime and unitId must not be NULL!");
        }
        String[] pointParts = point.split(",");
        if (pointParts.length != 2) {
            throw new IllegalArgumentException("Parameter point must contain 2 values (x, y) delimited by comma!");
        }
        
        
        String sql = "SELECT forecast_json FROM forecast."+forecastTable+""
                + " WHERE begin_forecast >= '"+fromTime+"' AND end_forecast < '"+toTime+"'"
                + " AND ST_DWithin(geography(geometry), geography(ST_SetSRID(ST_Point("+point+"), 4326)), 3000, true)"
                + " ORDER BY begin_forecast;";
        
        ResultSet res = SQLExecutor.getInstance().executeQuery(sql);
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode forecastArr = mapper.createArrayNode();
        if(res != null) {
            while(res.next()) {
                String resultJSON = res.getString("forecast_json");
                JsonNode resJSON = mapper.readTree(resultJSON); 
                forecastArr.add(resJSON);
            }
        }
        return forecastArr;
    }
    
    /**
     * Method selects forecast JSON based on given filtering parameters  
     * @param fromTime - begin of forecast (mandatory)
     * @param toTime - end of forecast (mandatory)
     * @param unitId - ID of unit for which forecast is selected, max 3 km distance
     * @param forecastTable - name of the forecast table
     * @return
     * @throws SQLException
     * @throws IOException
     */
    public static ArrayNode getForecastByDatesByUnitId(String fromTime, String toTime, Long unitId, String forecastTable) throws SQLException, IOException {
        // check of parameters
        if (fromTime == null && toTime == null || unitId == null) {
            throw new IllegalArgumentException("Parameters fromTime or toTime and unitId must not be NULL!");
        }
        
        String sql = "SELECT forecast_json FROM forecast."+forecastTable+""
                + " WHERE begin_forecast >= '"+fromTime+"' AND end_forecast < '"+toTime+"'"
                + " AND ST_DWithin(geography(geometry), geography((SELECT the_geom FROM public.last_units_positions WHERE unit_id = "+unitId+")), 3000, true)"
                + " ORDER BY begin_forecast";
        
        ResultSet res = SQLExecutor.getInstance().executeQuery(sql);
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode forecastArr = mapper.createArrayNode();
        if(res != null) {
            while(res.next()) {
                String resultJSON = res.getString("forecast_json");
                JsonNode resJSON = mapper.readTree(resultJSON); 
                forecastArr.add(resJSON);
            }
        }
        return forecastArr;
    }

    /**
     * Method selects forecast JSON based on given filtering parameters 
     * @param startFrom - begin of forecast (mandatory)
     * @param startTo - last begin of forecast (mandatory)
     * @param bbox - BBOX where to select reference point of forecast, pattern [minx, miny, maxx, maxy]
     * @param forecastTable - name of the forecast table
     * @return
     * @throws SQLException
     * @throws IOException
     */
    public static ArrayNode getForecastByStartDatesByBbox(String startFrom, String startTo, String bbox, String forecastTable) throws SQLException, IOException {
        // check of parameters
        if (startFrom == null && startTo == null || bbox == null || bbox.isEmpty()) {
            throw new IllegalArgumentException("Parameters fromTime or toTime and bbox must not be NULL or empty!");
        }

        String[] bboxParts = bbox.split(",");
        if (bboxParts.length != 4) {
            throw new IllegalArgumentException("Parameter bbox must contain 4 values (minx, miny, maxx, maxy) delimited by comma!");
        }
        
        String sql = "SELECT forecast_json FROM forecast."+forecastTable+""
                + " WHERE begin_forecast >= '"+startFrom+"' AND begin_forecast < '"+startTo+"'"
                + " AND ST_Intersects(geometry, ST_MakeEnvelope("+bbox+", 4326)) ORDER BY begin_forecast;";
        
        ResultSet res = SQLExecutor.getInstance().executeQuery(sql);
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode forecastArr = mapper.createArrayNode();
        if(res != null) {
            while(res.next()) {
                String resultJSON = res.getString("forecast_json");
                JsonNode resJSON = mapper.readTree(resultJSON); 
                forecastArr.add(resJSON);
            }
        }
        return forecastArr;
    }
   
    /**
     * Method selects forecast JSON based on given filtering parameters 
     * @param startFrom - begin of forecast (mandatory)
     * @param startTo - last begin of forecast (mandatory)
     * @param point - location of point to select the nearest forecast, max. 3 km
     * @param forecastTable - name of the forecast table
     * @return
     * @throws SQLException
     * @throws IOException
     */
    public static ArrayNode getForecastByStartDatesByPoint(String startFrom, String startTo, String point, String forecastTable) throws SQLException, IOException {
        // check of parameters
        if (startFrom == null && startTo == null || point == null || point.isEmpty()) {
            throw new IllegalArgumentException("Parameters startFrom or startTo and Point must not be NULL or empty!");
        }
        String[] pointParts = point.split(",");
        if (pointParts.length != 2) {
            throw new IllegalArgumentException("Parameter point must contain 2 values (x, y) delimited by comma!");
        }
        
        String sql = "SELECT forecast_json FROM forecast."+forecastTable+""
                + " WHERE begin_forecast >= '"+startFrom+"' AND begin_forecast < '"+startTo+"'"
                + " AND ST_DWithin(geography(geometry), geography(ST_SetSRID(ST_Point("+point+"), 4326)), 3000, true)"
                + " ORDER BY begin_forecast;";
        
        ResultSet res = SQLExecutor.getInstance().executeQuery(sql);
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode forecastArr = mapper.createArrayNode();
        if(res != null) {
            while(res.next()) {
                String resultJSON = res.getString("forecast_json");
                JsonNode resJSON = mapper.readTree(resultJSON); 
                forecastArr.add(resJSON);
            }
        }
        return forecastArr;
    }
   
    /**
     * Method selects forecast JSON based on given filtering parameters 
     * @param startFrom - begin of forecast (mandatory)
     * @param startTo - last begin of forecast (mandatory)
     * @param unitId - ID of unit for which forecast is selected, max 3 km distance
     * @param forecastTable - name of the forecast table
     * @return
     * @throws SQLException
     * @throws IOException
     */
    public static ArrayNode getForecastByStartDatesByUnitId(String startFrom, String startTo, Long unitId, String forecastTable) throws SQLException, IOException {
        // check of parameters
        if (startFrom == null && startTo == null || unitId == null) {
            throw new IllegalArgumentException("Parameters startFrom or startTo and unitId must not be NULL!");
        }

        String sql = "SELECT forecast_json FROM forecast."+forecastTable+""
                + " WHERE begin_forecast >= '"+startFrom+"' AND begin_forecast < '"+startTo+"'"
                + " AND ST_DWithin(geography(geometry), geography((SELECT the_geom FROM public.last_units_positions WHERE unit_id = "+unitId+")), 3000, true)"
                + " ORDER BY begin_forecast;";
        
        ResultSet res = SQLExecutor.getInstance().executeQuery(sql);
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode forecastArr = mapper.createArrayNode();
        if(res != null) {
            while(res.next()) {
                String resultJSON = res.getString("forecast_json");
                JsonNode resJSON = mapper.readTree(resultJSON); 
                forecastArr.add(resJSON);
            }
        }
        return forecastArr;
    }
    
    /**
     * Method selects forecast JSON based on given filtering parameters
     * @param bbox - BBOX where to select reference point of forecast, pattern [minx, miny, maxx, maxy]
     * @param forecastTable - name of the forecast table
     * @return
     * @throws SQLException
     * @throws IOException
     */
    public static ArrayNode getForecastByBbox(String bbox, String forecastTable) throws SQLException, IOException {
        // check of parameters
        if (bbox == null || bbox.isEmpty()) {
            throw new IllegalArgumentException("Parameter BBOX must not be NULL or empty!");
        }
        String[] bboxParts = bbox.split(",");
        if (bboxParts.length != 4) {
            throw new IllegalArgumentException("Parameter BBOX must contain 4 values (minx, miny, maxx, maxy) delimited by comma!");
        }
        
        String sql = "SELECT forecast_json FROM forecast."+forecastTable+""
                + " WHERE ST_Intersects(geometry, ST_MakeEnvelope("+bbox+", 4326))"
                + " ORDER BY begin_forecast;";
        
        ResultSet res = SQLExecutor.getInstance().executeQuery(sql);
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode forecastArr = mapper.createArrayNode();
        if(res != null) {
            while(res.next()) {
                String resultJSON = res.getString("forecast_json");
                JsonNode resJSON = mapper.readTree(resultJSON); 
                forecastArr.add(resJSON);
            }
        }
        return forecastArr;
    }
    
    /**
     * Method selects forecast JSON based on given filtering parameters
     * @param point - location of point to select the nearest forecast, max. 3 km
     * @param forecastTable - name of the forecast table
     * @return
     * @throws SQLException
     * @throws IOException
     */
    public static ArrayNode getForecastByPoint(String point, String forecastTable) throws SQLException, IOException {
        // check of parameters
        if (point == null || point.isEmpty()) {
            throw new IllegalArgumentException("Parameter Point must not be NULL or empty!");
        }
        String[] pointParts = point.split(",");
        if (pointParts.length != 2) {
            throw new IllegalArgumentException("Parameter Point must contain 2 values (x, y) delimited by comma!");
        }
        
        String sql = "SELECT forecast_json FROM forecast."+forecastTable+""
                + " WHERE ST_DWithin(geography(geometry), geography(ST_SetSRID(ST_Point("+point+"), 4326)), 3000, true)"
                + " ORDER BY begin_forecast;";
        
        ResultSet res = SQLExecutor.getInstance().executeQuery(sql);
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode forecastArr = mapper.createArrayNode();
        if(res != null) {
            while(res.next()) {
                String resultJSON = res.getString("forecast_json");
                JsonNode resJSON = mapper.readTree(resultJSON); 
                forecastArr.add(resJSON);
            }
        }
        return forecastArr;
    }
   
    /**
     * Method selects forecast JSON based on given filtering parameters
     * @param unitId - ID of unit for which forecast is selected, max 3 km distance
     * @param forecastTable - name of the forecast table
     * @return
     * @throws SQLException
     * @throws IOException
     */
    public static ArrayNode getForecastByUnitId(Long unitId, String forecastTable) throws SQLException, IOException {
        // check of parameters
        if (unitId == null) {
            throw new IllegalArgumentException("Parameter unitId must not be NULL!");
        }

        String sql = "SELECT forecast_json FROM forecast."+forecastTable+""
                + " WHERE ST_DWithin(geography(geometry), geography((SELECT the_geom FROM public.last_units_positions WHERE unit_id = "+unitId+")), 3000, true)"
                + " ORDER BY begin_forecast;";
        
        ResultSet res = SQLExecutor.getInstance().executeQuery(sql);
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode forecastArr = mapper.createArrayNode();
        if(res != null) {
            while(res.next()) {
                String resultJSON = res.getString("forecast_json");
                JsonNode resJSON = mapper.readTree(resultJSON); 
                forecastArr.add(resJSON);
            }
        }
        return forecastArr;
    }
    
    /**
     * Method selects forecast JSON
     * @param unitId - ID of unit for which forecast is selected, max 3 km distance
     * @param forecastTable - name of the forecast table
     * @return
     * @throws SQLException
     * @throws IOException
     */
    public static ObjectNode getForecastByFid(Integer fid, String forecastTable) throws SQLException, IOException {
        // check of parameters
        if (fid == null) {
            throw new IllegalArgumentException("Parameter fid must not be NULL!");
        }

        String sql = "SELECT forecast_json FROM "+FORECAST_SCHEMA+"."+forecastTable+""
                + " WHERE fid = "+fid+";";
        
        ResultSet res = SQLExecutor.getInstance().executeQuery(sql);
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode forecastObj = mapper.createObjectNode();
        if(res != null) {
            while(res.next()) {
                String resultJSON = res.getString("forecast_json");
                forecastObj = (ObjectNode) mapper.readTree(resultJSON);
            }
        }
        return forecastObj;
    }
    
    /**
     * Method selects the last forecast JSON based on given filtering parameters
     * @param bbox - BBOX where to select reference point of forecast, pattern [minx, miny, maxx, maxy]
     * @param forecastTable - name of the forecast table
     * @return
     * @throws SQLException
     * @throws IOException
     */
    public static ArrayNode getLastForecastByBbox(String bbox, String forecastTable) throws SQLException, IOException {
        // check of parameters
        if (bbox == null || bbox.isEmpty()) {
            throw new IllegalArgumentException("Parameter BBOX must not be NULL or empty!");
        }
        String[] bboxParts = bbox.split(",");
        if (bboxParts.length != 4) {
            throw new IllegalArgumentException("Parameter BBOX must contain 4 values (minx, miny, maxx, maxy) delimited by comma!");
        }
        
        String sql = "SELECT forecast_json FROM forecast."+forecastTable+""
                + " WHERE ST_Intersects(geometry, ST_MakeEnvelope("+bbox+", 4326))"
                + " ORDER BY begin_forecast DESC LIMIT 1;";
        
        ResultSet res = SQLExecutor.getInstance().executeQuery(sql);
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode forecastArr = mapper.createArrayNode();
        if(res != null) {
            while(res.next()) {
                String resultJSON = res.getString("forecast_json");
                JsonNode resJSON = mapper.readTree(resultJSON); 
                forecastArr.add(resJSON);
            }
        }
        return forecastArr;
    }
    
    /**
     * Method selects the last forecast JSON based on given filtering parameters
     * @param point - location of point to select the nearest forecast, max. 3 km
     * @param forecastTable - name of the forecast table
     * @return
     * @throws SQLException
     * @throws IOException
     */
    public static ArrayNode getLastForecastByPoint(String point, String forecastTable) throws SQLException, IOException {
        // check of parameters
        if (point == null || point.isEmpty()) {
            throw new IllegalArgumentException("Parameter Point must not be NULL or empty!");
        }
        String[] pointParts = point.split(",");
        if (pointParts.length != 2) {
            throw new IllegalArgumentException("Parameter Point must contain 2 values (x, y) delimited by comma!");
        }
        
        String sql = "SELECT forecast_json FROM forecast."+forecastTable+""
                + " WHERE ST_DWithin(geography(geometry), geography(ST_SetSRID(ST_Point("+point+"), 4326)), 3000, true)"
                + " ORDER BY begin_forecast DESC LIMIT 1;";
        
        ResultSet res = SQLExecutor.getInstance().executeQuery(sql);
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode forecastArr = mapper.createArrayNode();
        if(res != null) {
            while(res.next()) {
                String resultJSON = res.getString("forecast_json");
                JsonNode resJSON = mapper.readTree(resultJSON); 
                forecastArr.add(resJSON);
            }
        }
        return forecastArr;
    }
    
    /**
     * Method selects the last forecast JSON based on given filtering parameters
     * @param unitId - ID of unit for which forecast is selected, max 3 km distance
     * @param forecastTable - name of the forecast table
     * @return
     * @throws SQLException
     * @throws IOException
     */
    public static ArrayNode getLastForecastByUnitId(Long unitId, String forecastTable) throws SQLException, IOException {
        // check of parameters
        if (unitId == null) {
            throw new IllegalArgumentException("Parameter unitId must not be NULL!");
        }

        String sql = "SELECT forecast_json FROM forecast."+forecastTable+""
                + " WHERE ST_DWithin(geography(geometry), geography((SELECT the_geom FROM public.last_units_positions WHERE unit_id = "+unitId+")), 3000, true)"
                + " ORDER BY begin_forecast DESC LIMIT 1;";
        
        ResultSet res = SQLExecutor.getInstance().executeQuery(sql);
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode forecastArr = mapper.createArrayNode();
        if(res != null) {
            while(res.next()) {
                String resultJSON = res.getString("forecast_json");
                JsonNode resJSON = mapper.readTree(resultJSON); 
                forecastArr.add(resJSON);
            }
        }
        return forecastArr;
    }
    
    /**
     * Method returns list of forecasts metadata for specified forecast table
     * @return Array with object of Forecast (fid, begin_forecast, end_forecast, inserted, geojson)
     * @throws SQLException
     * @throws IOException
     */
    public static ArrayNode getForecastList(String forecastTable) throws SQLException, IOException {
        String sql = "SELECT fid, begin_forecast, end_forecast, inserted, st_asgeojson(geometry)"
        		+ " FROM forecast."+forecastTable+""
        		+ " ORDER BY inserted;";
        ResultSet res = SQLExecutor.getInstance().executeQuery(sql);
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode jsonList = mapper.createArrayNode();
        if(res != null) {
            while(res.next()) {
                ObjectNode forecast = mapper.createObjectNode();
                forecast.put("fid", res.getInt("fid"));
                forecast.put("begin_timestamp", res.getString("begin_forecast"));
                forecast.put("end_timestamp", res.getString("end_forecast"));
                forecast.put("inserted_at", res.getString("inserted"));
                JsonNode resJSON = mapper.readTree(res.getString("st_asgeojson")); 
                forecast.set("geometry", resJSON);
                 
                jsonList.add(forecast);
            }
        }
        return jsonList;        
    }
    
    /**
     * Method returns list of forecasts metadata for specified forecast table based on given filtering parameters
     * @param bbox - BBOX where to select reference point of forecast, pattern [minx, miny, maxx, maxy]
     * @param forecastTable - name of the forecast table
     * @return Array with object of Forecast (fid, begin_forecast, end_forecast, inserted, geojson)
     * @throws SQLException
     * @throws IOException
     */
    public static ArrayNode getForecastListByBbox(String bbox, String forecastTable) throws SQLException, IOException {
        // check of parameters
        if (bbox == null || bbox.isEmpty()) {
            throw new IllegalArgumentException("Parameter BBOX must not be NULL or empty!");
        }
        String[] bboxParts = bbox.split(",");
        if (bboxParts.length != 4) {
            throw new IllegalArgumentException("Parameter BBOX must contain 4 values (minx, miny, maxx, maxy) delimited by comma!");
        }
        String sql = "SELECT fid, begin_forecast, end_forecast, inserted, st_asgeojson(geometry)"
        		+ " FROM forecast."+forecastTable+""
        		+ " WHERE ST_Intersects(geometry, ST_MakeEnvelope("+bbox+", 4326))"
        		+ " ORDER BY begin_forecast;";
        ResultSet res = SQLExecutor.getInstance().executeQuery(sql);
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode forecastArr = mapper.createArrayNode();
        if(res != null) {
            while(res.next()) {
                ObjectNode forecast = mapper.createObjectNode();
                forecast.put("fid", res.getInt("fid"));
                forecast.put("begin_timestamp", res.getString("begin_forecast"));
                forecast.put("end_timestamp", res.getString("end_forecast"));
                forecast.put("inserted_at", res.getString("inserted"));
                JsonNode resJSON = mapper.readTree(res.getString("st_asgeojson")); 
                forecast.set("geometry", resJSON);
                 
                forecastArr.add(forecast);
            }
        }
        return forecastArr;
    }
    
    /**
     * Method returns list of forecasts metadata for specified forecast table based on given filtering parameters
     * @param point - location of point to select the nearest forecast
     * @param forecastTable - name of the forecast table
     * @param distance in meters to search for the forecast reference point 
     * @return Array with object of Forecast (fid, begin_forecast, end_forecast, inserted, geojson)
     * @throws SQLException
     * @throws IOException
     */
    public static ArrayNode getForecastListByPoint(String point, String forecastTable, int distance) throws SQLException, IOException {
        // check of parameters
        if (point == null || point.isEmpty()) {
            throw new IllegalArgumentException("Parameter Point must not be NULL or empty!");
        }
        String[] pointParts = point.split(",");
        if (pointParts.length != 2) {
            throw new IllegalArgumentException("Parameter Point must contain 2 values (x, y) delimited by comma!");
        }
        
        String sql = "SELECT fid, begin_forecast, end_forecast, inserted, st_asgeojson(geometry)"
        		+ " FROM forecast."+forecastTable+""
        		+ " WHERE ST_DWithin(geography(geometry), geography(ST_SetSRID(ST_Point("+point+"), 4326)), "+distance+", true)"
        		+ " ORDER BY begin_forecast;";
        ResultSet res = SQLExecutor.getInstance().executeQuery(sql);
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode forecastArr = mapper.createArrayNode();
        if(res != null) {
            while(res.next()) {
                ObjectNode forecast = mapper.createObjectNode();
                forecast.put("fid", res.getInt("fid"));
                forecast.put("begin_timestamp", res.getString("begin_forecast"));
                forecast.put("end_timestamp", res.getString("end_forecast"));
                forecast.put("inserted_at", res.getString("inserted"));
                JsonNode resJSON = mapper.readTree(res.getString("st_asgeojson")); 
                forecast.set("geometry", resJSON);
                 
                forecastArr.add(forecast);
            }
        }
        return forecastArr;
    }
    
    /**
     * Method returns list of forecasts metadata for specified forecast table based on given filtering parameters
     * @param unitId - ID of unit to search for the forecast 
     * @param forecastTable - name of the forecast table
     * @param distance in meters to search for the forecast reference point
     * @return Array with object of Forecast (fid, begin_forecast, end_forecast, inserted, geojson)
     * @throws SQLException
     * @throws IOException
     */
    public static ArrayNode getForecastListByUnitId(Long unitId, String forecastTable, int distance) throws SQLException, IOException {
        // check of parameters
        if (unitId == null) {
            throw new IllegalArgumentException("Parameter unitId must not be NULL!");
        }
        
        String sql = "SELECT fid, begin_forecast, end_forecast, inserted, st_asgeojson(geometry)"
        		+ " FROM forecast."+forecastTable+""
        		+ " WHERE ST_DWithin(geography(geometry), geography((SELECT the_geom FROM public.last_units_positions WHERE unit_id = "+unitId+")), "+distance+", true)"
        		+ " ORDER BY begin_forecast;";
        ResultSet res = SQLExecutor.getInstance().executeQuery(sql);
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode forecastArr = mapper.createArrayNode();
        if(res != null) {
            while(res.next()) {
                ObjectNode forecast = mapper.createObjectNode();
                forecast.put("fid", res.getInt("fid"));
                forecast.put("begin_timestamp", res.getString("begin_forecast"));
                forecast.put("end_timestamp", res.getString("end_forecast"));
                forecast.put("inserted_at", res.getString("inserted"));
                JsonNode resJSON = mapper.readTree(res.getString("st_asgeojson")); 
                forecast.set("geometry", resJSON);                 
                
                forecastArr.add(forecast);
            }
        }
        return forecastArr;
    }
}