package serguei.http;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

public class HttpResponseTest {

    @Test
    public void shouldProcessResponse() throws IOException {
        String responseBody = "This is a response";
        int bodyLen = responseBody.getBytes().length;
        String responseData = "HTTP/1.1 200 OK\r\nContent-Length: " + bodyLen + "\r\n\r\n" + responseBody + "extra data";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(responseData.getBytes());

        HttpResponse response = new HttpResponse(inputStream);

        assertEquals(200, response.getStatusCode());
        assertEquals(responseBody, response.readBodyAsString());
        assertTrue(response.hasBody());
    }

    @Test
    public void shouldProcessResponseWithoutBody() throws IOException {
        String responseData = "HTTP/1.1 204 OK\r\nContent-Length: 0\r\n\r\n";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(responseData.getBytes());

        HttpResponse response = new HttpResponse(inputStream);

        assertEquals(204, response.getStatusCode());
        assertEquals("", response.readBodyAsString());
        assertFalse(response.hasBody());
    }

    @Test
    public void shouldReturnHeadersWhenPresent() throws IOException {
        String responseData = "HTTP/1.1 200 OK\r\nContent-Length: 0\r\nHeader1: test1\r\nHeader1: test2\r\n\r\n";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(responseData.getBytes());
        HttpResponse response = new HttpResponse(inputStream);

        List<String> headers = response.getHeaders("Header1");

        assertEquals(Arrays.asList("test1", "test2"), headers);
    }

    @Test
    public void shouldReturnEmptyHeaderListWhenMissing() throws IOException {
        String responseData = "HTTP/1.1 200 OK\r\nContent-Length: 0\r\nHeader1: test1\r\nHeader1: test2\r\n\r\n";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(responseData.getBytes());
        HttpResponse response = new HttpResponse(inputStream);

        List<String> headers = response.getHeaders("Not-Present");

        assertEquals(Collections.emptyList(), headers);
    }

}
