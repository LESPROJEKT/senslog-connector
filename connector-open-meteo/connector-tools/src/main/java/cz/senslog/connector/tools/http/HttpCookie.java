// Copyright (c) 2026 UWB & LESP.
// The UWB & LESP license this file to you under the BSD-3-Clause license.

package cz.senslog.connector.tools.http;


import org.apache.hc.client5.http.impl.cookie.BasicClientCookie;

public class HttpCookie {

    private final BasicClientCookie cookie;

    public static HttpCookie empty() {
        HttpCookie cookie = new HttpCookie("", "", "", "");
        cookie.cookie.setSecure(false);
        return cookie;
    }

    public HttpCookie(String name, String value, String domain, String path) {
        this.cookie = new BasicClientCookie(name, value);
        this.cookie.setDomain(domain);
        this.cookie.setPath(path);
        this.cookie.setSecure(true);
    }

    public String getName() {
        return this.cookie.getName();
    }

    public String getValue() {
        return this.cookie.getValue();
    }

    public String getDomain() {
        return this.cookie.getDomain();
    }

    public String getPath() {
        return this.cookie.getPath();
    }

    public boolean isSecure() {
        return this.cookie.isSecure();
    }

    public boolean isNotSecure() {
        return !this.cookie.isSecure();
    }

    public BasicClientCookie get() {
        return this.cookie;
    }
}
