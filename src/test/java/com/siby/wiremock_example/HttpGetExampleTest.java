package com.siby.wiremock_example;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpResponseException;
import org.apache.http.entity.ContentType;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.apache.http.client.fluent.Request.Get;
import static org.apache.http.client.fluent.Request.Post;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class HttpGetExampleTest {

    String baseUri = "http://localhost:8089";

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8089); // No-args constructor defaults to port 8080

    @Before
    public void init() {
        stubFor(get(urlEqualTo("/500.txt")).willReturn(aResponse().withStatus(500).withHeader("Content-Type", "text/plain").withBody("hoge")));
        stubFor(get(urlEqualTo("/503.txt")).willReturn(aResponse().withStatus(503).withHeader("Content-Type", "text/plain").withBody("hoge")));
    }

    @Test
    public void shouldReturnOk() throws Exception {
        // given
        stubFor(get(urlEqualTo("/products")).willReturn(
                aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/plain")
                        .withBody("{type:ok}")));

        // when
        String actual = Get(baseUri + "/products").execute().returnContent().asString();

        // then
        assertThat(actual, is("{type:ok}"));
    }


    @Test
    public void shouldMatchUrl() throws Exception {
        // given
        stubFor(get(urlMatching("/products/matching/[0-9]+")).willReturn(
                aResponse()
                        .withStatus(200)
                        .withBody("{type:urlmatching}")
        ));

        // when
        String actual = Get(baseUri + "/products/matching/123").execute().returnContent().asString();

        // then
        assertThat(actual, is("{type:urlmatching}"));
    }


    @Test
    public void shouldMatchRequestHeaders() throws IOException {
        // given
        stubFor(post(urlEqualTo("/with/headers"))
                .withHeader("Content-Type", equalTo("text/xml"))
                .withHeader("Accept", matching("text/.*"))
                .withHeader("etag", notMatching("abcd.*"))
                .withHeader("X-Custom-Header", containing("2134"))
                .willReturn(aResponse().withStatus(200)));

        // when
        HttpResponse response = Post(baseUri + "/with/headers")
                .addHeader("Content-Type", "text/xml")
                .addHeader("Accept", "text/html")
                .addHeader("etag", "gdfgs")
                .addHeader("X-Custom-Header", "3452134").execute().returnResponse();

        // then
        assertThat(response.getStatusLine().getStatusCode(), is(200));
    }

    @Test
    public void shouldMatchQueryParameters() throws IOException {
        // given
        stubFor(get(urlPathEqualTo("/with/query"))
                .withQueryParam("search", containing("text"))
                .willReturn(aResponse().withBody("matchedQueryParameter").withStatus(200)));

        // when
        String actual = Get(baseUri + "/with/query?search=Checktext").execute().returnContent().asString();

        // then
        assertThat(actual, is("matchedQueryParameter"));
    }

    @Test
    public void shouldMatchRequestBody() throws IOException {
        // given
        stubFor(post(urlEqualTo("/with/body"))
                .withRequestBody(matching("<status>OK</status>"))
                .withRequestBody(notMatching(".*ERROR.*"))
                .willReturn(aResponse().withStatus(200)));

        // when
        HttpResponse response = Post(baseUri + "/with/body").bodyString("<status>OK</status>", ContentType.APPLICATION_ATOM_XML).execute().returnResponse();

        // then
        assertThat(response.getStatusLine().getStatusCode(), is(200));
    }

    @Test
    public void shouldMatchJsonBody() throws IOException {
        // given
        stubFor(post(urlEqualTo("/with/json/body"))
                .withRequestBody(equalToJson("{ \"houseNumber\": 4, \"postcode\": \"N1 1ZZ\" }"))
                .willReturn(aResponse().withStatus(200)));

        // when
        HttpResponse response = Post(baseUri + "/with/json/body").bodyString("{ \"houseNumber\": 4, \"postcode\": \"N1 1ZZ\" }", ContentType.APPLICATION_JSON).execute().returnResponse();

        // then
        assertThat(response.getStatusLine().getStatusCode(), is(200));
    }

    @Test
    public void shouldMatchXMLBody() throws IOException {
        // given
        stubFor(post(urlEqualTo("/with/xml/body"))
                .withRequestBody(equalToXml("<thing>value</thing>"))
                .willReturn(aResponse().withStatus(200)));

        // when
        HttpResponse response = Post(baseUri + "/with/xml/body").bodyString("<thing>value</thing>", ContentType.TEXT_XML).execute().returnResponse();

        // then
        assertThat(response.getStatusLine().getStatusCode(), is(200));
    }

    @Test(expected = HttpResponseException.class)
    public void notFound() throws Exception {
        Get("http://localhost:8089/NOT_FOUND").execute().returnContent().asString();
    }

    @Test(expected = HttpResponseException.class)
    public void internalServerError() throws Exception {
        Get("http://localhost:8089/500.txt").execute().returnContent().asString();
    }

    @Test(expected = HttpResponseException.class)
    public void serviceUnavailable() throws Exception {
        Get("http://localhost:8089/503.txt").execute().returnContent().asString();
    }

}