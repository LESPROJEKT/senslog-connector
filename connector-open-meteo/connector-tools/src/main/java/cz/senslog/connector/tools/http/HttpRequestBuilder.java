// Copyright (c) 2026 UWB & LESP.
// The UWB & LESP license this file to you under the BSD-3-Clause license.

package cz.senslog.connector.tools.http;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The class {@code HttpRequestBuilder} represents a builder for the {@link HttpRequest}.
 *
 * @author Lukas Cerny
 * @version 1.0
 * @since 1.0
 */
final class HttpRequestBuilder implements HttpRequest.Builder {

    private URL url;
    private Map<String, String> headers;
    private final List<HttpCookie> cookies;
    private String body;
    private HttpMethod method;
    private String contentType;

    HttpRequestBuilder() {
        this.headers = new HashMap<>();
        this.cookies = new ArrayList<>();
        this.method = HttpMethod.GET;
        this.body = "";
    }


    @Override
    public HttpRequest.Builder header(String name, String value) {
        this.headers.put(name, value);
        return this;
    }

    @Override
    public HttpRequest.Builder url(URL url) {
        this.url = url;
        return this;
    }

    @Override
    public HttpRequest.Builder POST() {
        this.method = HttpMethod.POST;
        return this;
    }

    @Override
    public HttpRequest.Builder GET() {
        this.method = HttpMethod.GET;
        return this;
    }

    @Override
    public HttpRequest.Builder contentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    @Override
    public HttpRequest.Builder body(String body) {
        this.body = body;
        return this;
    }

    @Override
    public HttpRequest.Builder addCookie(HttpCookie cookie) {
        this.cookies.add(cookie);
        return this;
    }

    @Override
    public HttpRequest build() {
        return new HttpRequest(url, headers, body, method, contentType, cookies.toArray(new HttpCookie[0]));
    }
}
