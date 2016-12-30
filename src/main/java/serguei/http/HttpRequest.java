package serguei.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.List;

public class HttpRequest {

    private final HttpRequestHeaders headers;

    private final HttpBody body;
    private final long contentLength;
    private final boolean chunked;
    private final URL url;

    private MultipartBodyParser multipartBodyParser;

    HttpRequest(InputStream inputStream) throws IOException {
        this.headers = new HttpRequestHeaders(inputStream);
        this.url = headers.getUrl();
        contentLength = headers.getContentLength();
        chunked = contentLength < 0 && headers.hasChunkedBody();
        String encoding = headers.getHeader("content-encoding");
        body = new HttpBody(inputStream, contentLength, chunked, encoding);
    }

    public String getMethod() {
        return headers.getMethod();
    }

    public URL getUrl() {
        return url;
    }

    public String getVersion() {
        return headers.getVersion();
    }

    public String getHost() {
        String host = headers.getHeader("Host");
        if (host != null) {
            return host;
        } else {
            return url.getHost();
        }
    }

    public long getContentLength() {
        return contentLength;
    }

    public boolean isResponseChunked() {
        return chunked;
    }

    public boolean hasMultipartBody() {
        String contentType = headers.getHeader("Content-Type");
        if (contentType != null) {
            return contentType.startsWith("multipart/");
        } else {
            return false;
        }
    }

    public String getHeader(String headerName) {
        return headers.getHeader(headerName);
    }

    public List<String> getHeaders(String headerName) {
        return Collections.unmodifiableList(headers.getHeaders(headerName));
    }

    public String readBodyAsString() throws IOException {
        return body.readAsString();
    }

    public byte[] readBodyAsBytes() throws IOException {
        return body.readAsBytes();
    }

    public String readBodyAndUnzip() throws IOException {
        return body.readAndUnzipAsString();
    }

    public BodyPart readNextBodyPart() throws IOException {
        if (multipartBodyParser == null) {
            if (!hasMultipartBody()) {
                return null;
            }
            String boundary = headers.getHeaderValue("Content-Type", "boundary");
            if (boundary == null) {
                return null;
            }
            multipartBodyParser = new MultipartBodyParser(body.getBodyInputStream(), boundary);
        }
        return multipartBodyParser.readNextBodyPart();
    }

    HttpRequestHeaders getHeaders() {
        return headers;
    }

    @Override
    public String toString() {
        return headers.toString();
    }

}
