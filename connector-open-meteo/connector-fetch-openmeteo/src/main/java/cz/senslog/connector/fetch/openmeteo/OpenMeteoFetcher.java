// Copyright (c) 2026 UWB & LESP.
// The UWB & LESP license this file to you under the BSD-3-Clause license.

package cz.senslog.connector.fetch.openmeteo;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cz.senslog.connector.fetch.api.ConnectorFetcher;
import cz.senslog.connector.model.api.VoidSession;
import cz.senslog.connector.model.openmeteo.OpenMeteoModel;
import cz.senslog.connector.tools.exception.ModuleInterruptedException;
import cz.senslog.connector.tools.exception.SyntaxException;
import cz.senslog.connector.tools.http.HttpClient;
import cz.senslog.connector.tools.http.HttpRequest;
import cz.senslog.connector.tools.http.HttpResponse;
import cz.senslog.connector.tools.http.URLBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.*;
import java.util.*;
import java.util.function.Supplier;

import static cz.senslog.connector.tools.json.BasicJson.jsonToObject;
import static java.time.format.DateTimeFormatter.ofPattern;

public class OpenMeteoFetcher implements ConnectorFetcher<VoidSession, OpenMeteoModel> {

    private static final Logger logger = LogManager.getLogger(OpenMeteoFetcher.class);

    private final HttpClient httpClient;
    private final OpenMeteoConfig config;

    private final Set<Station> stations;
    private LocalDateTime currentDateTime;


    public OpenMeteoFetcher(OpenMeteoConfig config, HttpClient httpClient) {
        this.httpClient = httpClient;
        this.config = config;
        this.stations = new HashSet<>();
    }

    @Override
    public void init() {}


    @Override
    public OpenMeteoModel fetch(Optional<VoidSession> session) {

        currentDateTime = currentDateTime != null ? currentDateTime : LocalDateTime.of(config.getStartDate(), LocalTime.now());
        stations.clear();


        if (config.getEndDate() != null && currentDateTime.toLocalDate().isAfter(config.getEndDate())) {
            throw new ModuleInterruptedException(String.format("The connector reached the end: %s", config.getEndDate()));
        }

        LocalDateTime now = LocalDateTime.now();
        if (currentDateTime.isAfter(now)) {
            logger.warn("The current execution can't be in the future: {} > {}", currentDateTime, now);
            LocalDateTime futureExecution = currentDateTime.plusHours(config.getPeriod());
            logger.info("Waiting for current date. Execution in {} minutes", Duration.between(LocalDateTime.now(), futureExecution).toMinutes());
            return new OpenMeteoModel(null, null, Collections.emptyList());
        }

        {
            OpenMeteoConfig.AllowedStation allowedStation = config.getAllowedStation();
            HttpRequest.Builder req = HttpRequest.newBuilder().GET()
                    .url(URLBuilder.newBuilder(allowedStation.getUrl()).build());

            HttpResponse res = httpClient.send(req.build());

            if (res.isError()) {
                logger.error("Request error <{}> with reason: {}", res.getStatus(), res.getBody());
                logger.error("Error while getting the stations from <{}>.", allowedStation.getUrl());
                return new OpenMeteoModel(null, null, Collections.emptyList());
            }

            JsonArray listJSON = jsonToObject(res.getBody(), JsonArray.class);
            for (JsonElement element : listJSON) {
                int id = element.getAsJsonObject().get("id").getAsInt();
                JsonObject stationJSON = element.getAsJsonObject();
                JsonObject geometryJSON = stationJSON.get("geometry").getAsJsonObject();
                String geometryType = geometryJSON.get("type").getAsString();
                if (id >= allowedStation.getMinId() && id <= allowedStation.getMaxId()) {
                    if (geometryType.equals("Point")) {
                        JsonArray coordinatesJSON = geometryJSON.get("coordinates").getAsJsonArray();
                        stations.add(new Station(coordinatesJSON.get(0).getAsDouble(), coordinatesJSON.get(1).getAsDouble()));
                    }
                }
            }
        }

        OpenMeteoConfig.AllowedStation allowedStation = config.getAllowedStation();
        List<JsonObject> results = new ArrayList<>(stations.size());
        for (Station station : stations) {

            Map<String, Supplier<String>> dynamicParamMapping = new HashMap<>();
            dynamicParamMapping.put("latitude", () -> Double.toString(station.getLatitude()));
            dynamicParamMapping.put("longitude", () -> Double.toString(station.getLongitude()));
            dynamicParamMapping.put("currentDate", () -> currentDateTime.format(ofPattern("yyyy-MM-dd")));

            URLBuilder urlBuilder = URLBuilder.newBuilder(config.getUrl());

            for (Map.Entry<String, OpenMeteoConfig.Param> paramEntry : allowedStation.getParams().entrySet()) {
                String paramName = paramEntry.getKey();
                OpenMeteoConfig.Param param = paramEntry.getValue();

                if (param.isDynamic()) {
                    Supplier<String> valueSupplier = dynamicParamMapping.get(param.getValue());
                    if (valueSupplier != null) {
                        urlBuilder.addParam(paramName, valueSupplier.get());
                    } else {
                        logger.warn("Dynamic param {} not found.", paramName);
                        return new OpenMeteoModel(null, null, Collections.emptyList());
                    }
                } else {
                    urlBuilder.addParam(paramName, param.getValue());
                }
            }

            HttpRequest.Builder req = HttpRequest.newBuilder().GET().url(urlBuilder.build());
            HttpResponse res = httpClient.send(req.build());

            if (res.isError()) {
                logger.warn("Open Meteo station lat: {}, lon: {} failed to fetch. Error: {}", station.getLatitude(), station.getLongitude(), res.getBody());
                continue;
            }

            String jsonBody = res.getBody();
            try {
                JsonObject predictionJSON = jsonToObject(jsonBody, JsonObject.class);
                JsonObject filteredJSON = removeNullPredictions(predictionJSON);
                if (filteredJSON != null) {
                    results.add(filteredJSON);
                }
            } catch (SyntaxException e) {
                logger.error("Error while parsing JSON: {}", e.getLocalizedMessage());
                logger.error(jsonBody);
            }
        }

        OffsetDateTime currentDateTime = ZonedDateTime.of(this.currentDateTime, config.getTimeZone().toZoneId()).toOffsetDateTime();

        if (results.isEmpty()) {
            logger.warn("No results found for the date: {}", currentDateTime);
        }

        // do not move by period step if period < 0
        this.currentDateTime = config.getPeriod() < 0 ? LocalDateTime.now() : this.currentDateTime.plusHours(config.getPeriod());

        return new OpenMeteoModel(currentDateTime, currentDateTime, results);
    }

    private static JsonObject removeNullPredictions(JsonObject json) {
        if (json == null) {
            return null;
        }

        final String propertyTag = "hourly_units";
        final String valueTag = "hourly";

        JsonObject resultJSON = json.deepCopy();
        if (json.has(propertyTag) && json.has(valueTag)) {
            JsonObject hourlyUnitsJSON = json.get(propertyTag).getAsJsonObject();
            JsonObject hourlyJSON = json.get(valueTag).getAsJsonObject();
            for (String paramName : hourlyUnitsJSON.keySet()) {
                if (hourlyJSON.has(paramName)) {
                    JsonElement element = hourlyJSON.get(paramName);
                    if (element.isJsonArray() && isAllNull(element.getAsJsonArray())) {
                        resultJSON.getAsJsonObject(propertyTag).remove(paramName);
                        resultJSON.getAsJsonObject(valueTag).remove(paramName);
                    }
                }
            }
        }

        if (resultJSON.getAsJsonObject(propertyTag).isEmpty()) {
            return null;
        }

        return resultJSON;
    }

    private static boolean isAllNull(JsonArray array) {
        if (array == null || array.isEmpty()) {
            return true;
        }
        boolean allNull = false;
        for (JsonElement el : array) {
            if (el.isJsonNull()) {
                allNull = true;
            }
        }
        return allNull;
    }
}