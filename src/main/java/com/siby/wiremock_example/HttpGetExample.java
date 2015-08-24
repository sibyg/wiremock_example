package com.siby.wiremock_example;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Request;

import java.io.IOException;

public class HttpGetExample {
    public String fetchAsString(String url) throws ClientProtocolException, IOException {
        return Request.Get(url).execute().returnContent().asString();
    }
}
