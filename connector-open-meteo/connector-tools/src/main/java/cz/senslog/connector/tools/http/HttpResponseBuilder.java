// Copyright (c) 2026 UWB & LESP.
// The UWB & LESP license this file to you under the BSD-3-Clause license.

package cz.senslog.connector.tools.http;

import java.util.Map;

/**
 * The class {@code HttpResponseBuilder} represents a builder for the {@link HttpResponse}.
 *
 * @author Lukas Cerny
 * @version 1.0
 * @since 1.0
 */
class HttpResponseBuilder implements HttpResponse.Builder {

    private String body;
    private Map<String, String> headers;
    private int status;

    HttpResponseBuilder(){}

    @Override
    public HttpResponse.Builder body(String body) {
        this.body = body;
        return this;
    }

    @Override
    public HttpResponse.Builder headers(Map<String, String> headers) {
        this.headers = headers;
        return this;
    }

    @Override
    public HttpResponse.Builder status(int status) {
        this.status = status;
        return this;
    }

    @Override
    public HttpResponse build() {
        return new HttpResponse(body, headers, status);
    }
}