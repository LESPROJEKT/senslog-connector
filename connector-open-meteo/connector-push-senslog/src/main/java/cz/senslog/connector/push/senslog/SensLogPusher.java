// Copyright (c) 2026 UWB & LESP.
// The UWB & LESP license this file to you under the BSD-3-Clause license.

package cz.senslog.connector.push.senslog;

import cz.senslog.connector.model.senslog.SensLogModel;
import cz.senslog.connector.push.api.ConnectorPusher;
import cz.senslog.connector.tools.http.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedTransferQueue;

import static cz.senslog.connector.tools.http.HttpContentType.APPLICATION_JSON;

public class SensLogPusher implements ConnectorPusher<SensLogModel> {

    private static final Logger logger = LogManager.getLogger(SensLogPusher.class);

    private final SensLogConfig config;
    private final HttpClient httpClient;

    private final List<HttpRequest> failedRequests;
    private final Queue<HttpRequest> requestQueue;

    public SensLogPusher(SensLogConfig config, HttpClient httpClient) {
        this.config = config;
        this.httpClient = httpClient;
        this.failedRequests = new ArrayList<>();
        this.requestQueue = new LinkedTransferQueue<>();
    }

    @Override
    public void init() {}

    @Override
    public void push(SensLogModel model) {
        if (model == null || model.getPassThroughData() == null || model.getPassThroughData().isEmpty()) {
            logger.warn("Model has no observations."); return;
        }

        for (SensLogModel.PassingData data : model.getPassThroughData()) {

            URLBuilder urlBuilder = URLBuilder.newBuilder(config.getBaseUrl());
            data.getParams().forEach(urlBuilder::addParam);

            HttpRequest.Builder req = HttpRequest.newBuilder().POST()
                    .url(urlBuilder.build())
                    .contentType(APPLICATION_JSON)
                    .body(data.getPayload());

            requestQueue.add(req.build());
        }

        if (!failedRequests.isEmpty()) {
            logger.info("Adding <{}> failed requests to the queue.", failedRequests.size());
            requestQueue.addAll(failedRequests);
            failedRequests.clear();
        }

        int totalToPush = requestQueue.size();
        int successfullyPushed = 0;
        while (!requestQueue.isEmpty()) {
            HttpRequest request = requestQueue.remove();
            HttpResponse res = httpClient.send(request);

            if (res.isError()) {
                logger.error("Request error <{}> with reason: {}", res.getStatus(), res.getBody());
                logger.error(request.getBody());
                failedRequests.add(request);
                continue;
            }

            if (res.isOk()) {
                logger.debug("Pushed <{}> successfully: {}", res.getStatus(), res.getBody());
                successfullyPushed++;
            }
        }

        logger.info("Pushed <{}/{}> payloads. For the time <{} - {}>", successfullyPushed, totalToPush, model.getFrom(), model.getTo());
    }
}
