// Copyright (c) 2026 UWB & LESP.
// The UWB & LESP license this file to you under the BSD-3-Clause license.

package cz.senslog.connector.tools.http;

import cz.senslog.connector.tools.util.StringUtils;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.cookie.CookieStore;
import org.apache.hc.client5.http.cookie.StandardCookieSpec;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpMessage;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.pool.PoolConcurrencyPolicy;
import org.apache.hc.core5.pool.PoolReusePolicy;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import static org.apache.hc.core5.http.HttpHeaders.*;


/**
 * The class {@code HttpClient} represents a wrapper for {@link org.apache.hc.client5.http.classic.HttpClient}.
 * Provides functionality of sending GET and POST request. Otherwise is returned response with {@see #BAD_REQUEST}.
 *
 * @author Lukas Cerny
 * @version 1.0
 * @since 1.0
 */
public class HttpClient {

    /**
     * Instance of http client.
     */
    private final org.apache.hc.client5.http.classic.HttpClient client;
    private final CookieStore cookieStore;

    private static HttpClient newClient() {

        PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
//                    .setSSLSocketFactory(SSLConnectionSocketFactoryBuilder.create()
//                            .setSslContext(SSLContexts.createSystemDefault())
////                            .setSslContext(sslContext)
//                            .setTlsVersions(TLS.V_1_3)
//                            .build())
                .setDefaultSocketConfig(SocketConfig.custom()
                        .setSoTimeout(Timeout.ofMinutes(1))
                        .build())
                .setPoolConcurrencyPolicy(PoolConcurrencyPolicy.STRICT)
                .setConnPoolPolicy(PoolReusePolicy.LIFO)
                .setDefaultConnectionConfig(ConnectionConfig.custom()
                        .setSocketTimeout(Timeout.ofMinutes(1))
                        .setConnectTimeout(Timeout.ofMinutes(1))
                        .setTimeToLive(TimeValue.ofMinutes(10))
                        .build())
                .build();

        CookieStore cookieStore = new BasicCookieStore();
        CloseableHttpClient client = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultCookieStore(cookieStore)
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec(StandardCookieSpec.STRICT)
                        .build())
                .build();

        return new HttpClient(client, cookieStore);
    }

    /**
     * Factory method to create a new instance of client.
     *
     * @return new instance of {@code HttpClient}.
     */
    public static HttpClient newHttpClient() {
        return newClient();
    }

    /**
     * Private constructors sets http client.
     */
    private HttpClient(org.apache.hc.client5.http.classic.HttpClient httpClient, CookieStore cookieStore) {
        this.client = httpClient;
        this.cookieStore = cookieStore;
    }

    /**
     * Sends http request.
     *
     * @param request - virtual request.
     * @return virtual response.
     */
    public HttpResponse send(HttpRequest request) {
        return send(request, Charset.defaultCharset());
    }

    public HttpResponse send(HttpRequest request, Charset charset) {
        try {
            switch (request.getMethod()) {
                case GET:
                    return sendGet(request, charset);
                case POST:
                    return sendPost(request, charset);
                default:
                    return HttpResponse.newBuilder()
                            .body("Request does not contain method definition.")
                            .status(HttpCode.METHOD_NOT_ALLOWED).build();
            }
        } catch (URISyntaxException e) {
            return HttpResponse.newBuilder()
                    .body(e.getMessage()).status(HttpCode.BAD_REQUEST)
                    .build();
        } catch (IOException e) {
            return HttpResponse.newBuilder()
                    .body(e.getMessage()).status(HttpCode.SERVER_ERROR)
                    .build();
        }
    }

    /**
     * Sends GET request.
     *
     * @param request - virtual request.
     * @return virtual response of the request.
     * @throws URISyntaxException throws if host url is not valid.
     * @throws IOException        throws if anything happen during sending.
     */
    private HttpResponse sendGet(HttpRequest request, Charset charset) throws IOException, URISyntaxException {

        URI uri = request.getUrl().toURI();
        HttpGet requestGet = new HttpGet(uri);
        setBasicHeaders(request, requestGet);

        cookieStore.clear();
        for (HttpCookie cookie : request.getCookies()) {
            cookieStore.addCookie(cookie.get());
        }

        return client.execute(requestGet, res -> HttpResponse.newBuilder()
                .status(res.getCode())
                .headers(getHeaders(res))
                .body(getBody(res.getEntity(), charset))
                .build());
    }

    /**
     * Sends POST request.
     *
     * @param request - virtual request.
     * @return virtual response of the request.
     * @throws URISyntaxException throws if host url is not valid.
     * @throws IOException        throws if anything happen during sending.
     */
    private HttpResponse sendPost(HttpRequest request, Charset charset) throws URISyntaxException, IOException {

        URI uri = request.getUrl().toURI();
        HttpPost requestPost = new HttpPost(uri);
        setBasicHeaders(request, requestPost);

        if (StringUtils.isNotBlank(request.getContentType())) {
            requestPost.setHeader(CONTENT_TYPE, request.getContentType());
        }

        requestPost.setEntity(new StringEntity(request.getBody()));

        return client.execute(requestPost, res -> HttpResponse.newBuilder()
                .status(res.getCode())
                .headers(getHeaders(res))
                .body(getBody(res.getEntity(), charset))
                .build());
    }

    /**
     * Sets basic headers to each request.
     *
     * @param userRequest - virtual request.
     * @param httpRequest - real request prepared to send.
     */
    private void setBasicHeaders(HttpRequest userRequest, HttpUriRequestBase httpRequest) {

        httpRequest.setHeader(USER_AGENT, "SenslogConnector/1.0");
        httpRequest.setHeader(CACHE_CONTROL, "no-cache");

        for (Map.Entry<String, String> headerEntry : userRequest.getHeaders().entrySet()) {
            httpRequest.setHeader(headerEntry.getKey(), headerEntry.getValue());
        }
    }

    /**
     * Returns map of headers from the response.
     *
     * @param response - response message.
     * @return map of headers.
     */
    private Map<String, String> getHeaders(HttpMessage response) {
        Map<String, String> headers = new HashMap<>();
        for (Header header : response.getHeaders()) {
            headers.put(header.getName(), header.getValue());
        }
        return headers;
    }

    /**
     * Returns body from the response.
     *
     * @param entity - response entity.
     * @return string body of the response.
     * @throws IOException can not get body from the response.
     */
    private String getBody(HttpEntity entity) throws IOException {
        return getBody(entity, Charset.defaultCharset());
    }

    private String getBody(HttpEntity entity, Charset charset) throws IOException {
        if (entity == null) return "";
        InputStream contentStream = entity.getContent();
        InputStreamReader bodyStream = new InputStreamReader(contentStream, charset);
        BufferedReader rd = new BufferedReader(bodyStream);
        StringBuilder bodyBuffer = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            bodyBuffer.append(line);
        }
        return bodyBuffer.toString();
    }
}
