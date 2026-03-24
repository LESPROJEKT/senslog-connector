/**
 * 
 */
package org.senslog.lite.rest.services

import java.io.IOException;
import java.sql.SQLException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.eclipse.jetty.http.HttpStatus;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.senslog.lite.rest.bean.ExceptionBean;
import org.senslog.lite.rest.util.ParamsList;
import org.senslog.lite.util.ForecastUtil;

/**
 * Class maintains REST endpoint for storing and publishing weather forecasts from different providers 
 */
@Path("/forecast/")
public class ForecastRest {

    public static Logger logger = Logger.getLogger(ForecastRest.class);
    
    /**
     * Empty constructor
     */
    public ForecastRest() {
        super();
    }
    
    /*
     * ---------------------------------- Open Meteo -----------------------------------------------------
     */    
    
    /**
     * http://localhost:8080/senslog/rest/forecast/openmeteo/plan
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/openmeteo/plan")
    public Response getOpenMeteoPlan() {
        try {
            ArrayNode forecastsList = ForecastUtil.getOpenMeteoPlan();
            return Response.ok()
                    .entity(forecastsList)
                    .build();
            
        } catch (SQLException e) {
            return Response.status(HttpStatus.INTERNAL_SERVER_ERROR_500)
                .entity(new ExceptionBean(e.getClass().getName(), e.getLocalizedMessage()))
                .build();
        } catch (IOException e) {
            return Response.status(HttpStatus.INTERNAL_SERVER_ERROR_500)
                    .entity(new ExceptionBean(e.getClass().getName(), e.getLocalizedMessage()))
                    .build();
        }
    }
    
    /**
     * http://localhost:8080/senslog/rest/forecast/openmeteo
     * @param forecast
     * @return
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/openmeteo")
    public Response insertOpenMeteoForecast(String forecast) {
        boolean inserted;
        try {
            inserted = ForecastUtil.processOpenMeteoForecast(forecast);
            if(inserted) {
                return Response.ok().build();
            }
            else {
                return Response.status(Status.NOT_MODIFIED).build();
            }
        } catch (IOException e) {
            return Response.serverError().entity(e.getMessage()).build();
        } catch (SQLException e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }
    
    /**
     * http://localhost:8080/senslog/rest/forecast/openmeteo?from_time=2025-03-07&to_time=2025-03-20
     * @param fromTime
     * @param toTime
     * @return
     */
    /**
     * Method manages the service for forecast publication with various filtering options
     * http://localhost:8080/senslog/rest/forecast/openmeteo?from_time=2025-06-01&to_time=2025-06-10
     * http://localhost:8080/senslog/rest/forecast/openmeteo?from_time=2025-06-01&to_time=2025-06-10&bbox=16.72822,48.80794,16.76688,48.83841
     * http://localhost:8080/senslog/rest/forecast/openmeteo?start_from=2025-06-07&start_to=2025-06-10&bbox=16.72822,48.80794,16.76688,48.83841
     * http://localhost:8080/senslog/rest/forecast/openmeteo?start_from=2025-06-07&start_to=2025-06-10&point=16.7430941,48.8325757
     * http://localhost:8080/senslog/rest/forecast/openmeteo?start_from=2025-06-07&start_to=2025-06-10&unit_id=1305167562273589
     * 
     * @param fromTime - beginning of forecast (mandatory)
     * @param toTime - end of forecast (mandatory)
     * @param startFrom - min beginning of forecast (mandatory)
     * @param startTo - max beginning of forecast (mandatory)
     * @param Bbox - String providing BBOX to filter in patter - {minx,miny,maxx,maxy}
     * @param unitId - ID of unit to find nearest forecast in max. 3 Km distance
     * @param point - Point with given coordinates in {x,y} to select corresponding grid of forecast
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/openmeteo")
    public Response getOpenMeteoForecast(
            @QueryParam(ParamsList.FROM_TIME) String fromTime,
            @QueryParam(ParamsList.TO_TIME) String toTime,
            @QueryParam(ParamsList.START_FROM) String startFrom,
            @QueryParam(ParamsList.START_TO) String startTo,
            @QueryParam(ParamsList.BBOX) String Bbox,
            @QueryParam(ParamsList.POINT) String point,
            @QueryParam(ParamsList.UNIT_ID) Long unitId) {

        try {
            ArrayNode forecastJson = new ObjectMapper().createArrayNode();

            boolean hasDate = (fromTime != null && !fromTime.isEmpty()) || (toTime != null && !toTime.isEmpty());
            boolean hasStart = (startFrom != null && !startFrom.isEmpty()) || (startTo != null && !startTo.isEmpty());
            boolean hasBbox = Bbox != null && !Bbox.isEmpty();
            boolean hasPoint = point != null && !point.isEmpty();
            boolean hasUnitId = unitId != null;
            boolean hasSpatial = hasBbox || hasPoint || hasUnitId;
            int spatialCount = (hasBbox ? 1 : 0) + (hasPoint ? 1 : 0) + (hasUnitId ? 1 : 0);
            if (spatialCount > 1) {
                return Response.status(HttpStatus.BAD_REQUEST_400)
                        .entity(new ExceptionBean("BadRequest", "Only one spatial parameter should be defined (BBOX, Point or UnitID)!"))
                        .build();
            }

            if ((hasDate || hasStart) && hasSpatial) {
                if (hasDate) {
                    if (hasBbox) {
                        forecastJson = ForecastUtil.getOpenMeteoByDatesByBbox(fromTime, toTime, Bbox);
                    } else if (hasPoint) {
                        forecastJson = ForecastUtil.getOpenMeteoByDatesByPoint(fromTime, toTime, point);
                    } else if (hasUnitId) {
                        forecastJson = ForecastUtil.getOpenMeteoByDatesByUnitId(fromTime, toTime, unitId);
                    } else {
                return Response.status(HttpStatus.BAD_REQUEST_400)
                        .entity(new ExceptionBean("BadRequest","Wrong combination of parameters! Valid: fromTime, toTime + (bbox,point,unit_id)"))
                        .build();
                    }

                } else if (hasStart) {
                    if (hasBbox) {
                        forecastJson = ForecastUtil.getOpenMeteoByStartDatesByBbox(startFrom, startTo, Bbox);
                    } else if (hasPoint) {
                        forecastJson = ForecastUtil.getOpenMeteoByStartDatesByPoint(startFrom, startTo, point);
                    } else if (hasUnitId) {
                        forecastJson = ForecastUtil.getOpenMeteoByStartDatesByUnitId(startFrom, startTo, unitId);
                    } else {
                return Response.status(HttpStatus.BAD_REQUEST_400)
                        .entity(new ExceptionBean("BadRequest","Wrong combination of parameters! Valid: startFrom, startTo + (bbox,point,unit_id)"))
                        .build();
                    }
                }

            } else if (!hasDate && !hasStart && hasSpatial) {
                if (hasBbox) {
                    forecastJson = ForecastUtil.getOpenMeteoByBbox(Bbox);
                } else if (hasPoint) {
                    forecastJson = ForecastUtil.getOpenMeteoByPoint(point);
                } else if (hasUnitId) {
                    forecastJson = ForecastUtil.getOpenMeteoByUnitId(unitId);
            } else {
                return Response.status(HttpStatus.BAD_REQUEST_400)
                        .entity(new ExceptionBean("BadRequest","Wrong combination of parameters! Valid: bbox, point, unit_id"))
                        .build();
                }

            } else {
                return Response.status(HttpStatus.BAD_REQUEST_400)
                        .entity(new ExceptionBean("BadRequest","Wrong combination of parameters!"))
                        .build();
            }

            return Response.ok()
                    .entity(forecastJson)
                    .build();

        } catch (SQLException | IOException e) {
            return Response.status(HttpStatus.INTERNAL_SERVER_ERROR_500)
                    .entity(new ExceptionBean(e.getClass().getName(), e.getLocalizedMessage()))
                    .build();
        }
    }
    
    /**
     * Method manages the service for forecast publication with various filtering options
     * http://localhost:8080/senslog/rest/forecast/openmeteo/last
     * http://localhost:8080/senslog/rest/forecast/openmeteo/last?bbox=16.72822,48.80794,16.76688,48.83841
     * http://localhost:8080/senslog/rest/forecast/openmeteo/last?point=16.7430941,48.8325757
     * http://localhost:8080/senslog/rest/forecast/openmeteo/last?unit_id=1305167562273589
     * 
     * @param Bbox - String providing BBOX to filter in patter - {minx,miny,maxx,maxy}
     * @param unitId - ID of unit to find nearest forecast in max. 3 Km distance
     * @param point - Point with given coordinates in {x,y} to select corresponding grid of forecast
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/openmeteo/last")
    public Response getOpenMeteoLastForecast(
            @QueryParam(ParamsList.BBOX) String Bbox,
            @QueryParam(ParamsList.POINT) String point,
            @QueryParam(ParamsList.UNIT_ID) Long unitId) {
    	try {
            ArrayNode forecastJson = new ObjectMapper().createArrayNode();

            boolean hasBbox = Bbox != null && !Bbox.isEmpty();
            boolean hasPoint = point != null && !point.isEmpty();
            boolean hasUnitId = unitId != null;
            boolean hasSpatial = hasBbox || hasPoint || hasUnitId;
            int spatialCount = (hasBbox ? 1 : 0) + (hasPoint ? 1 : 0) + (hasUnitId ? 1 : 0);
            if (spatialCount > 1) {
                return Response.status(HttpStatus.BAD_REQUEST_400)
                        .entity(new ExceptionBean("BadRequest", "Only one spatial filter parameter should be defined (BBOX, Point or UnitID)!"))
                        .build();
            }

            if (hasSpatial) {
                if (hasBbox) {
                    forecastJson = ForecastUtil.getOpenMeteoLastByBbox(Bbox);
                } else if (hasPoint) {
                    forecastJson = ForecastUtil.getOpenMeteoLastByPoint(point);
                } else if (hasUnitId) {
                    forecastJson = ForecastUtil.getOpenMeteoLastByUnitId(unitId);
                } else {
                	return Response.status(HttpStatus.BAD_REQUEST_400)
                        .entity(new ExceptionBean("BadRequest","Wrong combination of parameters! Valid: bbox, point, unit_id"))
                        .build();
                }
            } else {
                return Response.status(HttpStatus.BAD_REQUEST_400)
                        .entity(new ExceptionBean("BadRequest","Wrong combination of parameters!"))
                        .build();
            }
            return Response.ok()
                    .entity(forecastJson)
                    .build();
        } catch (SQLException | IOException e) {
            return Response.status(HttpStatus.INTERNAL_SERVER_ERROR_500)
                    .entity(new ExceptionBean(e.getClass().getName(), e.getLocalizedMessage()))
                    .build();
        }
    }
    
    /**
     * Method manages the service for forecast publication with various filtering options
     * http://localhost:8080/senslog/rest/forecast/openmeteo/list
     * http://localhost:8080/senslog/rest/forecast/openmeteo/list?bbox=16.72822,48.80794,16.76688,48.83841
     * http://localhost:8080/senslog/rest/forecast/openmeteo/list?point=16.7430941,48.8325757
     * http://localhost:8080/senslog/rest/forecast/openmeteo/list?unit_id=1305167562273589
     * 
     * @param Bbox - String providing BBOX to filter in patter - {minx,miny,maxx,maxy}
     * @param unitId - ID of unit to find nearest forecast in max. 3 Km distance
     * @param point - Point with given coordinates in {x,y} to select corresponding grid of forecast
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/openmeteo/list")
    public Response getOpenMeteoListForecast(
            @QueryParam(ParamsList.BBOX) String Bbox,
            @QueryParam(ParamsList.POINT) String point,
            @QueryParam(ParamsList.UNIT_ID) Long unitId) {
    	try {
            ArrayNode forecastJson = new ObjectMapper().createArrayNode();

            boolean hasBbox = Bbox != null && !Bbox.isEmpty();
            boolean hasPoint = point != null && !point.isEmpty();
            boolean hasUnitId = unitId != null;
            boolean hasSpatial = hasBbox || hasPoint || hasUnitId;
            int spatialCount = (hasBbox ? 1 : 0) + (hasPoint ? 1 : 0) + (hasUnitId ? 1 : 0);
            if (spatialCount > 1) {
                return Response.status(HttpStatus.BAD_REQUEST_400)
                        .entity(new ExceptionBean("BadRequest", "Only one spatial filter parameter should be defined (BBOX, Point or UnitID)!"))
                        .build();
            }

            if (hasSpatial) {
                if (hasBbox) {
                    forecastJson = ForecastUtil.getOpenMeteoListByBbox(Bbox);
                } else if (hasPoint) {
                    forecastJson = ForecastUtil.getOpenMeteoListByPoint(point, 3000);
                } else if (hasUnitId) {
                    forecastJson = ForecastUtil.getOpenMeteoListByUnitId(unitId, 3000);
                } else {
                	ArrayNode forecastsList = ForecastUtil.getOpenMeteoForecastList();
                		return Response.ok()
                			.entity(forecastsList)
                			.build();
                }
            } else {
                return Response.status(HttpStatus.BAD_REQUEST_400)
                        .entity(new ExceptionBean("BadRequest","Wrong combination of parameters!"))
                        .build();
            }
            return Response.ok()
                    .entity(forecastJson)
                    .build();
        } catch (SQLException | IOException e) {
            return Response.status(HttpStatus.INTERNAL_SERVER_ERROR_500)
                    .entity(new ExceptionBean(e.getClass().getName(), e.getLocalizedMessage()))
                    .build();
        }
    }
    
    /**
     * 
     * @param fid
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/openmeteo/{fid}")
    public Response getOpenMeteoByID(@PathParam(ParamsList.FID) Integer fid){
    	try {
        	if(fid != null) {
        		return Response.ok()
        				.entity(ForecastUtil.getOpenMeteoByFid(fid))
        				.build();
        	} else {
                return Response.status(HttpStatus.BAD_REQUEST_400)
                		.entity(new ExceptionBean("BadRequest","Parameter FID can't be NULL!"))
                        .build();
        	}            	
		} catch (SQLException | IOException e) {
            return Response.status(HttpStatus.INTERNAL_SERVER_ERROR_500)
                    .entity(new ExceptionBean(e.getClass().getName(), e.getLocalizedMessage()))
                    .build();
		}
    }
    
    /*
     * ---------------------------------- ERA5Land -----------------------------------------------------
     */
    
    /**
     * http://localhost:8080/senslog/rest/forecast/era5land/plan
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/era5land/plan")
    public Response getEra5LandPlan() {
        try {
            ArrayNode forecastsList = ForecastUtil.getEra5LandPlan();
            return Response.ok()
                    .entity(forecastsList)
                    .build();
            
        } catch (SQLException e) {
            return Response.status(HttpStatus.INTERNAL_SERVER_ERROR_500)
                .entity(new ExceptionBean(e.getClass().getName(), e.getLocalizedMessage()))
                .build();
        } catch (IOException e) {
            return Response.status(HttpStatus.INTERNAL_SERVER_ERROR_500)
                    .entity(new ExceptionBean(e.getClass().getName(), e.getLocalizedMessage()))
                    .build();
        }
    }
    
    /**
     * http://localhost:8080/senslog/rest/forecast/era5land
     * @param forecast
     * @return
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/era5land")
    public Response insertEra5Land(String forecast) {
        boolean inserted;
        try {
            inserted = ForecastUtil.processEra5Land(forecast);
            if(inserted) {
                return Response.ok().build();
            }
            else {
                return Response.status(Status.NOT_MODIFIED).build();
            }
        } catch (IOException e) {
            return Response.serverError().entity(e.getMessage()).build();
        } catch (SQLException e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }
    
    /**
     * Method manages the service for forecast publication with various filtering options
     * http://localhost:8080/senslog/rest/forecast/era5land?from_time=2025-03-07&to_time=2025-03-20
     * http://localhost:8080/senslog/rest/forecast/era5land?from_time=2025-03-07&to_time=2025-03-20&bbox=13.823097834289939,50.07982827011179,13.913486546055736,50.117838916327315
     * http://localhost:8080/senslog/rest/forecast/era5land?start_from=2025-03-07&start_to=2025-03-10&bbox=13.823097834289939,50.07982827011179,13.913486546055736,50.117838916327315
     * http://localhost:8080/senslog/rest/forecast/era5land?start_from=2025-03-07&start_to=2025-03-10&point=13.823097834289939,50.07982827011179
     * http://localhost:8080/senslog/rest/forecast/era5land?start_from=2025-03-07&start_to=2025-03-10&unit_id=1305167562273589
     * 
     * @param fromTime - beginning of forecast (mandatory)
     * @param toTime - end of forecast (mandatory)
     * @param startFrom - min beginning of forecast (mandatory)
     * @param startTo - max beginning of forecast (mandatory)
     * @param Bbox - String providing BBOX to filter in patter - {minx,miny,maxx,maxy}
     * @param unitId - ID of unit to find nearest forecast in max. 3 Km distance
     * @param point - Point with given coordinates in {x,y} to select corresponding grid of forecast
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/era5land")
    public Response getEra5Land(
            @QueryParam(ParamsList.FROM_TIME) String fromTime,
            @QueryParam(ParamsList.TO_TIME) String toTime,
            @QueryParam(ParamsList.START_FROM) String startFrom,
            @QueryParam(ParamsList.START_TO) String startTo,
            @QueryParam(ParamsList.BBOX) String Bbox,
            @QueryParam(ParamsList.POINT) String point,
            @QueryParam(ParamsList.UNIT_ID) Long unitId) {

        try {
            ArrayNode forecastJson = new ObjectMapper().createArrayNode();

            boolean hasDate = (fromTime != null && !fromTime.isEmpty()) || (toTime != null && !toTime.isEmpty());
            boolean hasStart = (startFrom != null && !startFrom.isEmpty()) || (startTo != null && !startTo.isEmpty());
            boolean hasBbox = Bbox != null && !Bbox.isEmpty();
            boolean hasPoint = point != null && !point.isEmpty();
            boolean hasUnitId = unitId != null;
            boolean hasSpatial = hasBbox || hasPoint || hasUnitId;
            int spatialCount = (hasBbox ? 1 : 0) + (hasPoint ? 1 : 0) + (hasUnitId ? 1 : 0);
            if (spatialCount > 1) {
                return Response.status(HttpStatus.BAD_REQUEST_400)
                        .entity(new ExceptionBean("BadRequest", "Only one spatial parameter should be defined (BBOX, Point or UnitID)!"))
                        .build();
            }

            if ((hasDate || hasStart) && hasSpatial) {
                if (hasDate) {
                    if (hasBbox) {
                        forecastJson = ForecastUtil.getEra5LandByDatesByBbox(fromTime, toTime, Bbox);
                    } else if (hasPoint) {
                        forecastJson = ForecastUtil.getEra5LandByDatesByPoint(fromTime, toTime, point);
                    } else if (hasUnitId) {
                        forecastJson = ForecastUtil.getEra5LandByDatesByUnitId(fromTime, toTime, unitId);
                    } else {
                return Response.status(HttpStatus.BAD_REQUEST_400)
                        .entity(new ExceptionBean("BadRequest","Wrong combination of parameters! Valid: fromTime, toTime + (bbox,point,unit_id)"))
                        .build();
                    }

                } else if (hasStart) {
                    if (hasBbox) {
                        forecastJson = ForecastUtil.getEra5LandByStartDatesByBbox(startFrom, startTo, Bbox);
                    } else if (hasPoint) {
                        forecastJson = ForecastUtil.getEra5LandByStartDatesByPoint(startFrom, startTo, point);
                    } else if (hasUnitId) {
                        forecastJson = ForecastUtil.getEra5LandByStartDatesByUnitId(startFrom, startTo, unitId);
                    } else {
                return Response.status(HttpStatus.BAD_REQUEST_400)
                        .entity(new ExceptionBean("BadRequest","Wrong combination of parameters! Valid: startFrom, startTo + (bbox,point,unit_id)"))
                        .build();
                    }
                }

            } else if (!hasDate && !hasStart && hasSpatial) {
                if (hasBbox) {
                    forecastJson = ForecastUtil.getEra5LandByBbox(Bbox);
                } else if (hasPoint) {
                    forecastJson = ForecastUtil.getEra5LandByPoint(point);
                } else if (hasUnitId) {
                    forecastJson = ForecastUtil.getEra5LandByUnitId(unitId);
            } else {
                return Response.status(HttpStatus.BAD_REQUEST_400)
                        .entity(new ExceptionBean("BadRequest","Wrong combination of parameters! Valid: bbox, point, unit_id"))
                        .build();
                }

            } else {
                return Response.status(HttpStatus.BAD_REQUEST_400)
                        .entity(new ExceptionBean("BadRequest","Wrong combination of parameters!"))
                        .build();
            }

            return Response.ok()
                    .entity(forecastJson)
                    .build();

        } catch (SQLException | IOException e) {
            return Response.status(HttpStatus.INTERNAL_SERVER_ERROR_500)
                    .entity(new ExceptionBean(e.getClass().getName(), e.getLocalizedMessage()))
                    .build();
        }
    }
    
    /**
     * Method manages the service for forecast publication with various filtering options
     * http://localhost:8080/senslog/rest/forecast/era5land/last
     * http://localhost:8080/senslog/rest/forecast/era5land/last?bbox=16.72822,48.80794,16.76688,48.83841
     * http://localhost:8080/senslog/rest/forecast/era5land/last?point=16.7430941,48.8325757
     * http://localhost:8080/senslog/rest/forecast/era5land/last?unit_id=1305167562273589
     * 
     * @param Bbox - String providing BBOX to filter in patter - {minx,miny,maxx,maxy}
     * @param unitId - ID of unit to find nearest forecast in max. 3 Km distance
     * @param point - Point with given coordinates in {x,y} to select corresponding grid of forecast
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/era5land/last")
    public Response getEra5LandLastForecast(
            @QueryParam(ParamsList.BBOX) String Bbox,
            @QueryParam(ParamsList.POINT) String point,
            @QueryParam(ParamsList.UNIT_ID) Long unitId) {
    	try {
            ArrayNode forecastJson = new ObjectMapper().createArrayNode();

            boolean hasBbox = Bbox != null && !Bbox.isEmpty();
            boolean hasPoint = point != null && !point.isEmpty();
            boolean hasUnitId = unitId != null;
            boolean hasSpatial = hasBbox || hasPoint || hasUnitId;
            int spatialCount = (hasBbox ? 1 : 0) + (hasPoint ? 1 : 0) + (hasUnitId ? 1 : 0);
            if (spatialCount > 1) {
                return Response.status(HttpStatus.BAD_REQUEST_400)
                        .entity(new ExceptionBean("BadRequest", "Only one spatial filter parameter should be defined (BBOX, Point or UnitID)!"))
                        .build();
            }

            if (hasSpatial) {
                if (hasBbox) {
                    forecastJson = ForecastUtil.getEra5LandLastByBbox(Bbox);
                } else if (hasPoint) {
                    forecastJson = ForecastUtil.getEra5LandLastByPoint(point);
                } else if (hasUnitId) {
                    forecastJson = ForecastUtil.getEra5LandLastByUnitId(unitId);
                } else {
                	return Response.status(HttpStatus.BAD_REQUEST_400)
                        .entity(new ExceptionBean("BadRequest","Wrong combination of parameters! Valid: bbox, point, unit_id"))
                        .build();
                }
            } else {
                return Response.status(HttpStatus.BAD_REQUEST_400)
                        .entity(new ExceptionBean("BadRequest","Wrong combination of parameters!"))
                        .build();
            }
            return Response.ok()
                    .entity(forecastJson)
                    .build();
        } catch (SQLException | IOException e) {
            return Response.status(HttpStatus.INTERNAL_SERVER_ERROR_500)
                    .entity(new ExceptionBean(e.getClass().getName(), e.getLocalizedMessage()))
                    .build();
        }
    }
    
    /**
     * 
     * @param fid
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/era5land/{fid}")
    public Response getEra5LandByID(@PathParam(ParamsList.FID) Integer fid){
    	try {
        	if(fid != null) {
        		return Response.ok()
        				.entity(ForecastUtil.getEra5LandByFid(fid))
        				.build();
        	} else {
                return Response.status(HttpStatus.BAD_REQUEST_400)
                		.entity(new ExceptionBean("BadRequest","Parameter FID can't be NULL!"))
                        .build();
        	}            	
		} catch (SQLException | IOException e) {
            return Response.status(HttpStatus.INTERNAL_SERVER_ERROR_500)
                    .entity(new ExceptionBean(e.getClass().getName(), e.getLocalizedMessage()))
                    .build();
		}
    }
    
    /**
     * Method manages the service for forecast publication with various filtering options
     * http://localhost:8080/senslog/rest/forecast/era5land/list
     * http://localhost:8080/senslog/rest/forecast/era5land/list?bbox=16.72822,48.80794,16.76688,48.83841
     * http://localhost:8080/senslog/rest/forecast/era5land/list?point=16.7430941,48.8325757
     * http://localhost:8080/senslog/rest/forecast/era5land/list?unit_id=1305167562273589
     * 
     * @param Bbox - String providing BBOX to filter in patter - {minx,miny,maxx,maxy}
     * @param unitId - ID of unit to find nearest forecast in max. 3 Km distance
     * @param point - Point with given coordinates in {x,y} to select corresponding grid of forecast
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/era5land/list")
    public Response getEra5LandListForecast(
            @QueryParam(ParamsList.BBOX) String Bbox,
            @QueryParam(ParamsList.POINT) String point,
            @QueryParam(ParamsList.UNIT_ID) Long unitId) {
    	try {
            ArrayNode forecastJson = new ObjectMapper().createArrayNode();

            boolean hasBbox = Bbox != null && !Bbox.isEmpty();
            boolean hasPoint = point != null && !point.isEmpty();
            boolean hasUnitId = unitId != null;
            boolean hasSpatial = hasBbox || hasPoint || hasUnitId;
            int spatialCount = (hasBbox ? 1 : 0) + (hasPoint ? 1 : 0) + (hasUnitId ? 1 : 0);
            if (spatialCount > 1) {
                return Response.status(HttpStatus.BAD_REQUEST_400)
                        .entity(new ExceptionBean("BadRequest", "Only one spatial filter parameter should be defined (BBOX, Point or UnitID)!"))
                        .build();
            }

            if (hasSpatial) {
                if (hasBbox) {
                    forecastJson = ForecastUtil.getEra5LandListByBbox(Bbox);
                } else if (hasPoint) {
                    forecastJson = ForecastUtil.getEra5LandListByPoint(point, 3000);
                } else if (hasUnitId) {
                    forecastJson = ForecastUtil.getEra5LandListByUnitId(unitId, 3000);
                } else {
                	return Response.status(HttpStatus.BAD_REQUEST_400)
                            .entity(new ExceptionBean("BadRequest","Wrong combination of parameters! Valid: bbox, point, unit_id"))
                            .build();
                }
            } else {
            	ArrayNode forecastsList = ForecastUtil.getEra5LandList();
            		return Response.ok()
            			.entity(forecastsList)
            			.build();
            }
            return Response.ok()
                    .entity(forecastJson)
                    .build();
        } catch (SQLException | IOException e) {
            return Response.status(HttpStatus.INTERNAL_SERVER_ERROR_500)
                    .entity(new ExceptionBean(e.getClass().getName(), e.getLocalizedMessage()))
                    .build();
        }
    }
    
    /*
     * ---------------------------------- Alliance -----------------------------------------------------
     */
    
    /**
     * Endpoint for receiving AI processed forecast from the Aliance project
     * 
     * http://localhost:8080/senslog/rest/forecast/alianceforecast
     * @param forecast
     * @return
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/alianceforecast")
    public Response insertAlianceForecast(String forecast) {
        boolean hasForecast = forecast != null && !forecast.isEmpty();
        if(hasForecast) {
            boolean inserted;
            try {
            	inserted = ForecastUtil.processAlianceForecast(forecast);
                if(inserted) {
                    return Response.ok().build();
                }
                else {
                    return Response.status(Status.NOT_MODIFIED).build();
                }
            } catch(SQLException | IOException e) {
                return Response.status(HttpStatus.INTERNAL_SERVER_ERROR_500)
                        .entity(new ExceptionBean(e.getClass().getName(), e.getLocalizedMessage()))
                        .build();
            }            
        }
        else {
            return Response.status(HttpStatus.BAD_REQUEST_400)
                    .entity(new ExceptionBean("BadRequest","Forecast file wasn't provided!"))
                    .build();
        }
    }
    

    /**
     * Method manages the service for forecast publication with various filtering options
     * http://localhost:8080/senslog/rest/forecast/alianceforecast/list
     * http://localhost:8080/senslog/rest/forecast/alianceforecast/list?bbox=18.5858,18.6331,48.2048,48.2301
     * http://localhost:8080/senslog/rest/forecast/alianceforecast/list?point=18.61,48.22
     * http://localhost:8080/senslog/rest/forecast/alianceforecast/list?unit_id=1305167562273589
     * 
     * @param Bbox - String providing BBOX to filter in patter - {minx,miny,maxx,maxy}
     * @param unitId - ID of unit to find nearest forecast in max. 3 Km distance
     * @param point - Point with given coordinates in {x,y} to select corresponding grid of forecast
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/alianceforecast/list")
    public Response getAlianceForecastList(
            @QueryParam(ParamsList.BBOX) String Bbox,
            @QueryParam(ParamsList.POINT) String point,
            @QueryParam(ParamsList.UNIT_ID) Long unitId) {
    	try {
            ArrayNode forecastJson = new ObjectMapper().createArrayNode();

            boolean hasBbox = Bbox != null && !Bbox.isEmpty();
            boolean hasPoint = point != null && !point.isEmpty();
            boolean hasUnitId = unitId != null;
            boolean hasSpatial = hasBbox || hasPoint || hasUnitId;
            int spatialCount = (hasBbox ? 1 : 0) + (hasPoint ? 1 : 0) + (hasUnitId ? 1 : 0);
            if (spatialCount > 1) {
                return Response.status(HttpStatus.BAD_REQUEST_400)
                        .entity(new ExceptionBean("BadRequest", "Only one spatial filter parameter should be defined (BBOX, Point or UnitID)!"))
                        .build();
            }

            if (hasSpatial) {
                if (hasBbox) {
                    forecastJson = ForecastUtil.getAlianceForecastListByBbox(Bbox);
                } else if (hasPoint) {
                    forecastJson = ForecastUtil.getAlianceForecastListByPoint(point, 3000);
                } else if (hasUnitId) {
                    forecastJson = ForecastUtil.getAlianceForecastListByUnitId(unitId, 3000);
                } else {
                	return Response.status(HttpStatus.BAD_REQUEST_400)
                        .entity(new ExceptionBean("BadRequest","Wrong combination of parameters! Valid: bbox, point, unit_id"))
                        .build();
                }
            } else {
            ArrayNode forecastsList = ForecastUtil.getAlianceForecastList();
            return Response.ok()
                    .entity(forecastsList)
                    .build();
            }
            return Response.ok()
                    .entity(forecastJson)
                    .build();
        } catch (SQLException | IOException e) {
            return Response.status(HttpStatus.INTERNAL_SERVER_ERROR_500)
                    .entity(new ExceptionBean(e.getClass().getName(), e.getLocalizedMessage()))
                    .build();
        }
    }
    
    /**
     * http://localhost:8080/senslog/rest/forecast/alianceforecast/<id>
     * @param fid
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/alianceforecast/{fid}")
    public Response getAlianceForecastByID(@PathParam(ParamsList.FID) Integer fid){
    	try {
        	if(fid != null) {
        		return Response.ok()
        				.entity(ForecastUtil.getAlianceForecastByFid(fid))
        				.build();
        	} else {
                return Response.status(HttpStatus.BAD_REQUEST_400)
                		.entity(new ExceptionBean("BadRequest","Parameter FID can't be NULL!"))
                        .build();
        	}            	
		} catch (SQLException | IOException e) {
            return Response.status(HttpStatus.INTERNAL_SERVER_ERROR_500)
                    .entity(new ExceptionBean(e.getClass().getName(), e.getLocalizedMessage()))
                    .build();
		}
    }
    /**
     * Method manages the service for forecast publication with various filtering options
     * http://localhost:8080/senslog/rest/forecast/alianceforecast/last
     * http://localhost:8080/senslog/rest/forecast/alianceforecast/last?bbox=18.5858,18.6331,48.2048,48.2301
     * http://localhost:8080/senslog/rest/forecast/alianceforecast/last?point=18.61,48.22
     * http://localhost:8080/senslog/rest/forecast/alianceforecast/last?unit_id=13051
     * 
     * @param Bbox - String providing BBOX to filter in patter - {minx,miny,maxx,maxy}
     * @param unitId - ID of unit to find nearest forecast in max. 3 Km distance
     * @param point - Point with given coordinates in {x,y} to select corresponding grid of forecast
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/alianceforecast/last")
    public Response getAlianceForecastLast(
            @QueryParam(ParamsList.BBOX) String Bbox,
            @QueryParam(ParamsList.POINT) String point,
            @QueryParam(ParamsList.UNIT_ID) Long unitId) {
    	try {
            ArrayNode forecastJson = new ObjectMapper().createArrayNode();

            boolean hasBbox = Bbox != null && !Bbox.isEmpty();
            boolean hasPoint = point != null && !point.isEmpty();
            boolean hasUnitId = unitId != null;
            boolean hasSpatial = hasBbox || hasPoint || hasUnitId;
            int spatialCount = (hasBbox ? 1 : 0) + (hasPoint ? 1 : 0) + (hasUnitId ? 1 : 0);
            if (spatialCount > 1) {
                return Response.status(HttpStatus.BAD_REQUEST_400)
                        .entity(new ExceptionBean("BadRequest", "Only one spatial filter parameter should be defined (BBOX, Point or UnitID)!"))
                        .build();
            }

            if (hasSpatial) {
                if (hasBbox) {
                    forecastJson = ForecastUtil.getAlianceForecastLastByBbox(Bbox);
                } else if (hasPoint) {
                    forecastJson = ForecastUtil.getAlianceForecastLastByPoint(point);
                } else if (hasUnitId) {
                    forecastJson = ForecastUtil.getAlianceForecastLastByUnitId(unitId);
                } else {
                	return Response.status(HttpStatus.BAD_REQUEST_400)
                        .entity(new ExceptionBean("BadRequest","Wrong combination of parameters! Valid: bbox, point, unit_id"))
                        .build();
                }
            } else {
                return Response.status(HttpStatus.BAD_REQUEST_400)
                        .entity(new ExceptionBean("BadRequest","Wrong combination of parameters!"))
                        .build();
            }
            return Response.ok()
                    .entity(forecastJson)
                    .build();
        } catch (SQLException | IOException e) {
            return Response.status(HttpStatus.INTERNAL_SERVER_ERROR_500)
                    .entity(new ExceptionBean(e.getClass().getName(), e.getLocalizedMessage()))
                    .build();
        }
    }
    
    /**
     * Method manages the service for forecast publication with various filtering options
     * http://localhost:8080/senslog/rest/forecast/alianceforecast?from_time=2025-03-07&to_time=2025-03-20
     * http://localhost:8080/senslog/rest/forecast/alianceforecast?from_time=2025-03-07&to_time=2025-03-20&bbox=13.823097834289939,50.07982827011179,13.913486546055736,50.117838916327315
     * http://localhost:8080/senslog/rest/forecast/alianceforecast?start_from=2025-03-07&start_to=2025-03-10&bbox=13.823097834289939,50.07982827011179,13.913486546055736,50.117838916327315
     * http://localhost:8080/senslog/rest/forecast/alianceforecast?start_from=2025-03-07&start_to=2025-03-10&point=13.823097834289939,50.07982827011179
     * http://localhost:8080/senslog/rest/forecast/alianceforecast?start_from=2025-03-07&start_to=2025-03-10&unit_id=1305167562273589
     * 
     * @param fromTime - beginning of forecast (mandatory)
     * @param toTime - end of forecast (mandatory)
     * @param startFrom - min beginning of forecast (mandatory)
     * @param startTo - max beginning of forecast (mandatory)
     * @param Bbox - String providing BBOX to filter in patter - {minx,miny,maxx,maxy}
     * @param unitId - ID of unit to find nearest forecast in max. 3 Km distance
     * @param point - Point with given coordinates in {x,y} to select corresponding grid of forecast
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/alianceforecast")
    public Response getAlianceForecast(
            @QueryParam(ParamsList.FROM_TIME) String fromTime,
            @QueryParam(ParamsList.TO_TIME) String toTime,
            @QueryParam(ParamsList.START_FROM) String startFrom,
            @QueryParam(ParamsList.START_TO) String startTo,
            @QueryParam(ParamsList.BBOX) String Bbox,
            @QueryParam(ParamsList.POINT) String point,
            @QueryParam(ParamsList.UNIT_ID) Long unitId) {

        try {
            ArrayNode forecastJson = new ObjectMapper().createArrayNode();

            boolean hasDate = (fromTime != null && !fromTime.isEmpty()) || (toTime != null && !toTime.isEmpty());
            boolean hasStart = (startFrom != null && !startFrom.isEmpty()) || (startTo != null && !startTo.isEmpty());
            boolean hasBbox = Bbox != null && !Bbox.isEmpty();
            boolean hasPoint = point != null && !point.isEmpty();
            boolean hasUnitId = unitId != null;
            boolean hasSpatial = hasBbox || hasPoint || hasUnitId;
            int spatialCount = (hasBbox ? 1 : 0) + (hasPoint ? 1 : 0) + (hasUnitId ? 1 : 0);
            if (spatialCount > 1) {
                return Response.status(HttpStatus.BAD_REQUEST_400)
                        .entity(new ExceptionBean("BadRequest", "Only one spatial parameter should be defined (BBOX, Point or UnitID)!"))
                        .build();
            }

            if ((hasDate || hasStart) && hasSpatial) {
                if (hasDate) {
                    if (hasBbox) {
                        forecastJson = ForecastUtil.getAlianceForecastByDatesByBbox(fromTime, toTime, Bbox);
                    } else if (hasPoint) {
                        forecastJson = ForecastUtil.getAlianceForecastByDatesByPoint(fromTime, toTime, point);
                    } else if (hasUnitId) {
                        forecastJson = ForecastUtil.getAlianceForecastByDatesByUnitId(fromTime, toTime, unitId);
                    } else {
                return Response.status(HttpStatus.BAD_REQUEST_400)
                        .entity(new ExceptionBean("BadRequest","Wrong combination of parameters! Valid: fromTime, toTime + (bbox,point,unit_id)"))
                        .build();
                    }

                } else if (hasStart) {
                    if (hasBbox) {
                        forecastJson = ForecastUtil.getAlianceForecastByStartDatesByBbox(startFrom, startTo, Bbox);
                    } else if (hasPoint) {
                        forecastJson = ForecastUtil.getAlianceForecastByStartDatesByPoint(startFrom, startTo, point);
                    } else if (hasUnitId) {
                        forecastJson = ForecastUtil.getAlianceForecastByStartDatesByUnitId(startFrom, startTo, unitId);
                    } else {
                return Response.status(HttpStatus.BAD_REQUEST_400)
                        .entity(new ExceptionBean("BadRequest","Wrong combination of parameters! Valid: startFrom, startTo + (bbox,point,unit_id)"))
                        .build();
                    }
                }

            } else if (!hasDate && !hasStart && hasSpatial) {
                if (hasBbox) {
                    forecastJson = ForecastUtil.getAlianceForecastByBbox(Bbox);
                } else if (hasPoint) {
                    forecastJson = ForecastUtil.getAlianceForecastByPoint(point);
                } else if (hasUnitId) {
                    forecastJson = ForecastUtil.getAlianceForecastByUnitId(unitId);
            } else {
                return Response.status(HttpStatus.BAD_REQUEST_400)
                        .entity(new ExceptionBean("BadRequest","Wrong combination of parameters! Valid: bbox, point, unit_id"))
                        .build();
                }

            } else {
                return Response.status(HttpStatus.BAD_REQUEST_400)
                        .entity(new ExceptionBean("BadRequest","Wrong combination of parameters!"))
                        .build();
            }

            return Response.ok()
                    .entity(forecastJson)
                    .build();

        } catch (SQLException | IOException e) {
            return Response.status(HttpStatus.INTERNAL_SERVER_ERROR_500)
                    .entity(new ExceptionBean(e.getClass().getName(), e.getLocalizedMessage()))
                    .build();
        }
    }
}