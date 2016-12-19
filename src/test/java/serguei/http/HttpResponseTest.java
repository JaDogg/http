package serguei.http;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.Test;

public class HttpResponseTest {

    private static final String LINE_BREAK = "\r\n";

    @Test
    public void shouldCreateResponse() throws Exception {
        HttpResponse response = new HttpResponse("HTTP/1.1 200 OK", "Content-Length: 100");

        assertEquals("HTTP/1.1", response.getVersion());
        assertEquals(200, response.getStatusCode());
        assertEquals("OK", response.getReason());
        assertEquals("100", response.getHeader("Content-Length"));
        assertNull(response.getHeader("random"));
    }

    @Test(expected = HttpException.class)
    public void shouldThrowExceptionWhenWrongStatus() throws Exception {
        new HttpResponse("HTTP/1.1 WRONG OK", "Content-Length: 100");
    }

    @Test
    public void shouldReadResponse() throws Exception {
        String data = "HTTP/1.1 200 OK" + LINE_BREAK + "Content-Length: 100" + LINE_BREAK + LINE_BREAK;
        InputStream inputStream = new ByteArrayInputStream(data.getBytes("UTF-8"));

        HttpResponse response = new HttpResponse(inputStream);

        assertEquals("HTTP/1.1", response.getVersion());
        assertEquals(200, response.getStatusCode());
        assertEquals("OK", response.getReason());
        assertEquals("100", response.getHeader("Content-Length"));
        assertNull(response.getHeader("random"));
    }

    @Test
    public void shouldReturnOkResponse() {
        HttpResponse response = HttpResponse.ok();

        assertEquals("HTTP/1.1", response.getVersion());
        assertEquals(200, response.getStatusCode());
        assertEquals("OK", response.getReason());
    }

    @Test
    public void shouldReturnRedirectResponse() {
        String url = "http://www.google.com/";
        HttpResponse response = HttpResponse.redirect(url);

        assertEquals("HTTP/1.1", response.getVersion());
        assertEquals(302, response.getStatusCode());
        assertEquals("Found", response.getReason());
        assertEquals(url, response.getHeader("location"));
    }

    @Test
    public void shouldReturnServerError() {
        HttpResponse response = HttpResponse.serverError();

        assertEquals("HTTP/1.1", response.getVersion());
        assertEquals(500, response.getStatusCode());
        assertEquals("Server Error", response.getReason());
    }

}
