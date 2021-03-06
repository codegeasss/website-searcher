package com.company.seacher.utils;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;

/**
 * Helper class to create URL connection
 */
public class HttpHelper {

    /**
     * Creates a connection based on protocol(http vs https)
     * Both protocols are tried if the url doesn't specify one
     *
     * @param   url Url to connect
     * @throws IOException if url is not reachable
     * @return HttpURLConnection
     */
    public static HttpURLConnection getConnection(String url) throws IOException {
        HttpURLConnection httpConnection = null;
        // is https?
        if(url.startsWith("https:")) {
            httpConnection = (HttpsURLConnection) getURLConnection(url);
        } else if(url.startsWith("http:")) { // is http?
            httpConnection = (HttpURLConnection) getURLConnection( url);
        } else { // no protocol? try both
            // try http first
            httpConnection = (HttpURLConnection) getURLConnection("http://" + url);
            if(httpConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                // try https
                httpConnection = (HttpsURLConnection) getURLConnection("https://" + url);
                // throw exception if the response is still not 200
                if(httpConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    throw new UnknownHostException("Unknown host");
                }
            }
        }

       return httpConnection;
    }

    private static URLConnection getURLConnection(String url) throws IOException {
        URLConnection conn = new URL(url).openConnection();;
        conn.setReadTimeout(2000);
        return conn;
    }
}
