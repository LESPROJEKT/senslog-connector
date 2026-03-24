// Copyright (c) 2026 UWB & LESP.
// The UWB & LESP license this file to you under the BSD-3-Clause license.

package cz.senslog.connector.tools.http;

import cz.senslog.connector.tools.json.BasicJson;

import java.util.Map;

/**
 * The class {@code HttpResponse} represents a wrapper for a http response.
 * Contains basic information like status, headers and body.
 *
 * @author Lukas Cerny
 * @version 1.0
 * @since 1.0
 */
public class HttpResponse {

    public interface Builder {
        Builder body(String body);
        Builder headers(Map<String, String> headers);
        Builder status(int status);
        HttpResponse build();
    }

    /**
     * Factory method to create a new builder for {@link HttpResponse}.
     * @return new instance of builder.
     */
    public static Builder newBuilder() {
        return new HttpResponseBuilder();
    }

    /** Response body. */
    private final String body;

    /** Response headers. */
    private final Map<String, String> headers;

    /** Response status. */
    private final int status;

    /**
     * Constructors sets all attributes.
     * @param body - body.
     * @param headers - headers.
     * @param status - status.
     */
    HttpResponse(String body, Map<String, String> headers, int status) {
        this.body = body;
        this.headers = headers;
        this.status = status;
    }

    public String getBody() {
        return body;
    }

    public String getHeader(String value) {
        return headers.get(value);
    }

    public int getStatus() {
        return status;
    }

    public boolean isOk() {
        return status == HttpCode.OK;
    }

    public boolean isError() {
        return !isOk();
    }

    @Override
    public String toString() {
        return BasicJson.objectToJson(this);
    }
}
