// Copyright (c) 2026 UWB & LESP.
// The UWB & LESP license this file to you under the BSD-3-Clause license.

package cz.senslog.connector.tools.http;

import java.net.URL;
import java.util.Map;

/**
 * The class {@code HttpRequest} represents a wrapper for a http request.
 *
 * @author Lukas Cerny
 * @version 1.0
 * @since 1.0
 */
public class HttpRequest {

    public interface Builder {
        Builder header(String name, String value);
        Builder url(URL url);
        Builder POST();
        Builder GET();
        Builder contentType(String contentType);
        Builder body(String body);
        Builder addCookie(HttpCookie cookie);
        HttpRequest build();
    }

    /**
     * Factory method to create a new builder for {@link HttpRequest}.
     * @return new instance of builder.
     */
    public static Builder newBuilder() {
        return new HttpRequestBuilder();
    }

    /**
     * Factory method to create a new builder for {@link HttpRequest}.
     * @param url - host url.
     * @return new instance of builder.
     */
    public static Builder newBuilder(URL url) {
        HttpRequestBuilder builder = new HttpRequestBuilder();
        builder.url(url);
        return builder;
    }

    /** Request url. */
    private final URL url;

    /** Request headers. */
    private final Map<String, String> headers;

    /** Request body. */
    private final String body;

    /** Request method. */
    private final HttpMethod method;

    /** Request content type. */
    private final String contentType;

    private final HttpCookie [] cookies;

    /**
     * Constructors sets all attributes.
     * @param url - url.
     * @param headers - headers.
     * @param body - body.
     * @param method - method.
     * @param contentType - content type.
     */
    HttpRequest(URL url, Map<String, String> headers, String body, HttpMethod method, String contentType, HttpCookie [] cookies) {
        this.url = url;
        this.headers = headers;
        this.body = body;
        this.method = method;
        this.contentType = contentType;
        this.cookies = cookies;
    }

    public URL getUrl() {
        return url;
    }

    public String getBody() {
        return body;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getContentType() {
        return contentType;
    }

    public HttpCookie[] getCookies() {
        return cookies;
    }
}
