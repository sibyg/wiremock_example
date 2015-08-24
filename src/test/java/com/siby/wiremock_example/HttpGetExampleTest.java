package com.siby.wiremock_example;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.http.client.HttpResponseException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class HttpGetExampleTest {
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8089); // No-args constructor defaults to port 8080

    HttpGetExample instance = new HttpGetExample();

    @Before
    public void init() {
        stubFor(get(urlEqualTo("/hoge.txt")).willReturn(
                aResponse().withStatus(200).withHeader("Content-Type", "text/plain").withBody("hoge")));
        stubFor(get(urlEqualTo("/500.txt")).willReturn(
                aResponse().withStatus(500).withHeader("Content-Type", "text/plain").withBody("hoge")));
        stubFor(get(urlEqualTo("/503.txt")).willReturn(
                aResponse().withStatus(503).withHeader("Content-Type", "text/plain").withBody("hoge")));
    }

    @Test
    public void ok() throws Exception {
        String actual = instance.fetchAsString("http://localhost:8089/hoge.txt");
        String expected = "hoge";
        assertThat(actual, is(expected));
    }

    @Test(expected = HttpResponseException.class)
    public void notFound() throws Exception {
        instance.fetchAsString("http://localhost:8089/NOT_FOUND");
    }

    @Test(expected = HttpResponseException.class)
    public void internalServerError() throws Exception {
        instance.fetchAsString("http://localhost:8089/500.txt");
    }

    @Test(expected = HttpResponseException.class)
    public void serviceUnavailable() throws Exception {
        instance.fetchAsString("http://localhost:8089/503.txt");
    }
}