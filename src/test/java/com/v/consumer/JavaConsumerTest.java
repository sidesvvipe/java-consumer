package com.v.consumer;

import au.com.dius.pact.consumer.*;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit.PactProviderRule;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.model.MockProviderConfig;
import au.com.dius.pact.model.PactFragment;
import au.com.dius.pact.model.PactFragment$;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.junit.Rule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "AccountJSService", port = "8087")
public class JavaConsumerTest {

    ConsumerController c = new ConsumerController();
    private static final int WIREMOCK_PORT = 8089;
    private WireMockServer wireMockServer;

    @BeforeEach
    void setUp() {
        c.PROVIDER_PORT = "8087";
    }

    @AfterEach
    void shutDownMock2() {
        wireMockServer.stop();
    }


    void nonExistentProvider(String accNumToMock) {
        //other provider
        wireMockServer = new WireMockServer(WIREMOCK_PORT);
        wireMockServer.stubFor(get(
                urlEqualTo("/accounts/1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(String.format("{\"account_number\":\"%s\"}", accNumToMock))
                ));
        wireMockServer.start();
    }

    @Pact(provider = "AccountJSService", consumer = "JavaConsumer")
    public RequestResponsePact activePersonPact(PactDslWithProvider builder) {
        return builder
                .given("Request for Active person")
                .uponReceiving("Will respond with account where {..., active: true} ")
                .path("/accounts/num/ACT1V3")
                .method("GET")
                .willRespondWith()
                .status(200)
                .body("{\"name\":\"Active Person\",\"active\":true}")
                .toPact();
    }

    @Pact(provider = "AccountJSService", consumer = "JavaConsumer")
    public RequestResponsePact inactivePersonPact(PactDslWithProvider builder) {
        return builder
                .given("Java consumer requests JS provider inactive account")
                .uponReceiving("Request for inactive account")
                .path("/accounts/num/1N4CTV")
                .method("GET")
                .willRespondWith()
                .status(200)
                .body("{\"name\":\"Inactive Person\",\"active\":false}")
                .toPact();
    }
/*
    @Pact(provider = "AccountJSService", consumer = "JavaConsumer")
    public RequestResponsePact thirdRequestOption(PactDslWithProvider builder) {
        return builder
                .given("Never called contract")
                .uponReceiving("")
                .path("/accounts/num/")
                .method("GET")
                .willRespondWith()
                .status(200)
                .body("{}")
                .toPact();
    }*/
    // ^ uncomment during demo

    public RequestResponsePact nonExistentProvider(final PactDslWithProvider builder) {
        return builder
                .given("n")
                .uponReceiving("Request")
                .path("/accounts/1")
                .method("GET")
                .willRespondWith()
                .status(200)
                .body("{\"account_number\":\"1N4CTV\"}")
                .toPact();
    }

    @PactTestFor(providerName = "NonExistentProvider", port = "8089")
    public RequestResponsePact nonExistentProvider2(PactDslWithProvider builder) {
        return builder
                .given("n")
                .uponReceiving("Request")
                .path("/accounts/1")
                .method("GET")
                .willRespondWith()
                .status(200)
                .body("{\"account_number\":\"1N4CTV\"}")
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "activePersonPact")
    void testActivePersonAccount(MockServer mockServer) throws IOException {
//        HttpResponse httpResponse = Request.Get(mockServer.getUrl() + "/accounts/num/ACT1V3").execute().returnResponse();
//        will return provider mock directly
//        HttpResponse httpResponse2 = Request.Get("http://localhost:8085/check-input").execute().returnResponse();
//        will request running consumer as normal api request would, might be useful
        nonExistentProvider("ACT1V3");
        String s = c.checkInput();
        assertThat(s, is(equalTo("Active Person's account is active")));
    }

    @Test
    @PactTestFor(pactMethod = "inactivePersonPact")
    void testInactivePersonAccount(MockServer mockServer) throws IOException {
//        RequestResponsePact fragment = nonExistentProvider(ConsumerPactBuilder.consumer("JavaConsumer").hasPactWith("NonExistentProvider"));
//        au.com.dius.pact.model.MockProviderConfig config = new MockProviderConfig( "localhost", 8089);

        //
//        VerificationResult result = fragment.getInteractions(config, new TestRun() {
//            @Override
//            public void run(au.com.dius.pact.model.MockProviderConfig config) throws Throwable {
//                String s = c.checkInput();
//            }
//        });
        nonExistentProvider("1N4CTV");
        String s = c.checkInput();
        assertThat(s, is(equalTo("Inactive Person's account is not active")));
    }
}