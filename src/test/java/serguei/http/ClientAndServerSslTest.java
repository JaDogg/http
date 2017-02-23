package serguei.http;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ClientAndServerSslTest {

    private static final String HOST = "localhost";
    private static final String BODY_CHARSET = "UTF-8";
    private static final String PATH = "/test/file.txt";
    private static final String REQUEST_LINE = "POST " + PATH + " HTTP/1.1";

    private String requestBody = makeBody("client");
    private String responseBody = makeBody("server");

    private TestServer server;
    private HttpClientConnection clientConnection;

    @Before
    public void setup() throws Exception {
        server = new TestServer();
        clientConnection = new HttpClientConnection(HOST, server.getSslPort());
    }

    @After
    public void clear() {
        clientConnection.close();
        server.stop();
    }

    @Test
    public void shouldSendAndReceiveFromServer() throws Exception {
        server.setResponse(HttpResponseHeaders.ok(), responseBody.getBytes(BODY_CHARSET), BodyCompression.NONE);
        HttpRequestHeaders headers = new HttpRequestHeaders(REQUEST_LINE, "Host: localhost");

        clientConnection.startHandshake();
        HttpResponse response = clientConnection.send(headers, requestBody);

        assertEquals("http://localhost" + PATH, server.getLatestRequestHeaders().getUrl().toString());
        assertTrue(server.getLatestConnectionContext().isSsl());
        assertNotNull(server.getLatestConnectionContext().getNegotiatedTlsProtocol());
        assertTrue(server.getLatestConnectionContext().getNegotiatedCipher().length() > 0);
        assertEquals(requestBody, server.getLatestRequestBodyAsString());
        assertEquals(200, response.getStatusCode());
        assertNull(response.getHeader("Content-Encoding"));
        assertEquals(responseBody.getBytes(BODY_CHARSET).length, response.getContentLength());
        assertFalse(response.isContentChunked());
        assertEquals(responseBody, response.readBodyAsString());
    }

    private static String makeBody(String msg) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 200; i++) {
            builder.append("This is line " + i + " for " + msg);
            builder.append(System.lineSeparator());
        }
        return builder.toString();
    }

}
