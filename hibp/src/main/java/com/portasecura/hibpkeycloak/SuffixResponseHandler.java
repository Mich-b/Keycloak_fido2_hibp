package com.portasecura.hibpkeycloak;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;

public class SuffixResponseHandler implements ResponseHandler<String> {

    private String suffix;

    public SuffixResponseHandler(String suffix) {
        this.suffix = suffix.toUpperCase();
    }

    public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
        int status = response.getStatusLine().getStatusCode();
        if (status == 200) {
            HttpEntity entity = response.getEntity();
            BufferedReader br = new BufferedReader(new InputStreamReader(entity.getContent()));
            try {
                String occurrences = "0";
                String inputLine;
                // the range API returns a set of suffixes (the hash without the prefix). If the suffix of our password hash is present, it is compromised
                while ((inputLine = br.readLine()) != null) {
                    if (inputLine.startsWith(suffix)) {
                        //password compromised, so return amount
                        br.close();
                        occurrences = inputLine.split(":", 2)[1];
                        return occurrences;
                    }
                }
                br.close();
                //password was not found in the list, so assuming it is not compromised, returning 0
                return occurrences;
            } catch (IOException e) {
                throw new ClientProtocolException("Exception thrown reading response", e);
            }
        } else {
            throw new ClientProtocolException("Unexpected response status: " + status);
        }

    }


}
