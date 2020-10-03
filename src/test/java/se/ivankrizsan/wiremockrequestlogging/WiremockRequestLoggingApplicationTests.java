package se.ivankrizsan.wiremockrequestlogging;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.ClasspathFileSource;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestListener;
import com.github.tomakehurst.wiremock.standalone.JsonFileMappingsSource;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import wiremock.org.apache.http.HttpStatus;

/**
 * Tests using WireMock as a mock server and shows how to log information
 * concerning the requests received by the WireMock server.
 *
 * @author Ivan Krizsan
 */
@SpringBootTest
class WiremockRequestLoggingApplicationTests {
    /* Constant(s): */
    private static final Logger LOGGER = LoggerFactory.getLogger(WiremockRequestLoggingApplicationTests.class);
    protected final static int HTTP_ENDPOINT_PORT = 8123;
    protected final static String WIREMOCK_MAPPINGS_PATH = "mappings/";
    protected final static String URL = "http://localhost:" + HTTP_ENDPOINT_PORT;

    /* Instance variable(s): */
    protected WireMockServer mWireMockServer;

    /**
     * Tests sending a request to the WireMock server.
     *
     */
    @Test
    void sendRequestToWireMockServerTest() {
        final Response theResponse = RestAssured
            .given()
            .contentType("application/xml")
            .accept("application/xml")
            .when()
            .get(URL);
        theResponse
            .then()
            .statusCode(HttpStatus.SC_OK);
    }

    /**
     * Sets up RestAssured, the test-client, before every test.
     */
    @BeforeEach
    public void setupRestAsssured() {
        RestAssured.reset();
        RestAssured.port = HTTP_ENDPOINT_PORT;
    }

    /**
     * Sets up and configures the WireMock mock server.
     * Responses served by the mock server are selected from a number of mapping
     * files on the classpath.
     * WireMock is also configured to log information about the request and
     * any matching response.
     */
    @BeforeEach
    public void setupWireMock() {
        mWireMockServer = new WireMockServer(HTTP_ENDPOINT_PORT);
        /* Load WireMock mappings from files in directory on classpath. */
        mWireMockServer.loadMappingsUsing(
            new JsonFileMappingsSource(
                new ClasspathFileSource(WIREMOCK_MAPPINGS_PATH)
            ));
        /* Add logging of request and any matched response. */
        mWireMockServer.addMockServiceRequestListener(
            WiremockRequestLoggingApplicationTests::requestReceived);
        mWireMockServer.start();
    }

    /**
     * Stops the WireMock server after each test.
     */
    @AfterEach
    public void stopWireMock() {
        mWireMockServer.stop();
    }

    /**
     * Logs information from supplied WireMock request and response objects.
     *
     * @param inRequest Object containing information from received request.
     * @param inResponse Response object containing data from the selected response.
     * If no response was matched, payload will be null and there will be no response
     * headers.
     */
    protected static void requestReceived(Request inRequest,
        com.github.tomakehurst.wiremock.http.Response inResponse) {
        LOGGER.info("WireMock request at URL: {}", inRequest.getAbsoluteUrl());
        LOGGER.info("WireMock request headers: \n{}", inRequest.getHeaders());
        LOGGER.info("WireMock response body: \n{}", inResponse.getBodyAsString());
        LOGGER.info("WireMock response headers: \n{}", inResponse.getHeaders());
    }
}
