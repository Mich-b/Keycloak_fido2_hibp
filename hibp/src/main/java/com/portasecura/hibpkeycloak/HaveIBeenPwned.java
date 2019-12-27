package com.portasecura.hibpkeycloak;

import java.io.IOException;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class HaveIBeenPwned {

    public static class ApiException extends Exception {
        private static final long serialVersionUID = 4310312365802997437L;

        public ApiException(Exception e) {
            super(e);
        }
    }

    private CloseableHttpClient httpclient;

    public HaveIBeenPwned() {
        httpclient = HttpClients.createDefault();
    }

    public int lookup(String password, String apiUrl) throws ApiException {
        try {
            String sha1 = getSha1(password);
            return getBreachCount(sha1, apiUrl);
        } catch (Exception e) {
            throw new ApiException(e);
        }
    }

    private int getBreachCount(String sha1, String apiUrl) throws ClientProtocolException, IOException {
        String prefix = sha1.substring(0, 5);
        String suffix = sha1.substring(5);

        String uri = apiUrl + prefix;
        HttpGet httpGet = new HttpGet(uri);

        String sha1Count = httpclient.execute(httpGet, createPrefixHandler(suffix));
        return Integer.parseInt(sha1Count.substring(suffix.length() + 1));
    }

    private ResponseHandler<String> createPrefixHandler(String prefix) {
        return new PrefixResponseHandler(prefix);
    }

    private String getSha1(String input) {
        return DigestUtils.sha1Hex(input);
    }
}
