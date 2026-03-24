// Copyright (c) 2026 UWB & LESP.
// The UWB & LESP license this file to you under the BSD-3-Clause license.

package cz.senslog.connector.tools.http;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;

import static java.net.URLEncoder.encode;

/**
 * The class {@code URLBuilder} represents a builder to create a new instance of {@link URL}.
 * Provides a creating a url from domain and path and adding a parameter.
 *
 * @author Lukas Cerny
 * @version 1.0
 * @since 1.0
 */
public final class URLBuilder {

    /**
     * Factory method to create a new instance of {@code URLBuilder} from base url.
     * @param baseURL - host url.
     * @return new instance of {@code URLBuilder}.
     */
    public static URLBuilder newBuilder(String baseURL) {
        return new URLBuilder(baseURL);
    }

    /**
     * Factory method to create a new instance of {@code URLBuilder} from domain and path.
     * Normalizes domain and path to the form:
     * domain: http://domain.com/
     * path: /host
     * -> url: http://domain.com/host
     * domain: http://domain.com
     * path: host
     * -> url: http://domain.com/host
     *
     * @param domain - domain of host.
     * @param path - path of host.
     * @return new instance of {@code URLBuilder}.
     */
    public static URLBuilder newBuilder(String domain, String path) {
        boolean domainSlash = domain.endsWith("/");
        boolean pathSlash = path.startsWith("/");

        if ((domainSlash && !pathSlash) || (!domainSlash && pathSlash)) {
            return new URLBuilder(domain + path);
        } else if (domainSlash) {
            return new URLBuilder(domain + path.substring(1));
        } else {
            return new URLBuilder(domain + "/" + path);
        }
    }

    /** String builder for url. */
    private StringBuilder urlBuilder;

    /** String builder for parameters. */
    private StringBuilder paramsBuilder;

    /**
     * Private constructor initializes builders and normalizes url.
     * If the url ends with slash '/', it is removed.
     * @param baseURL - host url.
     */
    private URLBuilder(String baseURL) {
        String url = baseURL.endsWith("/") ? baseURL.substring(0, baseURL.length() - 1) : baseURL;
        this.urlBuilder = new StringBuilder(url);
        this.paramsBuilder = new StringBuilder();
    }

    /**
     * Adds a new parameter to the url.
     * @param name - name of parameter.
     * @param value - value of parameter.
     * @return instance of {@code URLBuilder}.
     */
    public URLBuilder addParam(String name, String value) {
        try {
            paramsBuilder.append("&").append(name).append("=").append(encode(value, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e.getMessage());
        }
        return this;
    }

    /**
     * Adds a new parameter to the url.
     * @param name - name of parameter.
     * @param value - value of parameter.
     * @return instance of {@code URLBuilder}.
     */
    public URLBuilder addParam(String name, Object value) {
        if (value == null) return this;
        return addParam(name, value.toString());
    }

    /**
     * Creates a new instance of {@link URL}.
     * @return new instance of {@link URL}.
     */
    public URL build() {
        try {
            String params = paramsBuilder.replace(0, 1, "").toString();
            return new URL(urlBuilder.append(params.isEmpty() ? "" : ("?" + params)).toString());
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }
}

