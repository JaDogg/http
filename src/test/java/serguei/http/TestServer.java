package serguei.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.zip.GZIPOutputStream;

public class TestServer extends HttpServer {

    private static final int PORT = 8080;
    private static final String BODY_CODEPAGE = "UTF-8";

    private final TestRequestHandler requestHandler;

    public TestServer() throws IOException {
        super(new TestRequestHandler(), PORT);
        requestHandler = (TestRequestHandler)getRequestHandler();
        start();
        long startTime = System.currentTimeMillis();
        while (!isRunning() && System.currentTimeMillis() - startTime < 1000) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // nothing to do
            }
        }
        if (!isRunning()) {
            throw new IOException("Server did not start on " + PORT);
        }
    }

    public int getPort() {
        return PORT;
    }

    public void setResponse(HttpResponseHeaders headers, byte[] body) {
        setResponse(headers, body, BodyCompression.NONE);
    }

    public void setResponse(HttpResponseHeaders headers, byte[] body, BodyCompression compression) {
        byte[] responseBody;
        if (compression == BodyCompression.GZIP) {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            try {
                GZIPOutputStream gzipStream = new GZIPOutputStream(output);
                gzipStream.write(body);
                gzipStream.close();
            } catch (IOException e) {
                // this is used for tests only
                throw new RuntimeException("Error compressing body", e);
            }
            responseBody = output.toByteArray();
            headers.setHeader("Content-Length", Integer.toString(responseBody.length));
            headers.setHeader("Content-Encoding", "gzip");
        } else {
            responseBody = body;
            headers.setHeader("Content-Length", Integer.toString(body.length));
        }
        requestHandler.setResponse(new Response(headers, responseBody));
    }

    public void setChunkedResponse(HttpResponseHeaders headers, byte[] body) {
        setChunkedResponse(headers, body, BodyCompression.NONE);
    }

    public void setChunkedResponse(HttpResponseHeaders headers, byte[] body, BodyCompression compression) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        OutputStream stream = new ChunkedOutputStream(output, true);
        try {
            if (compression == BodyCompression.GZIP) {
                stream = new GZIPOutputStream(stream);
                headers.setHeader("Content-Encoding", "gzip");
            }
            stream.write(body);
            stream.close();
        } catch (IOException e) {
            // this is used for tests only
            throw new RuntimeException("Error compressing body", e);
        }
        headers.addHeader("Transfer-Encoding", "chunked");
        requestHandler.setResponse(new Response(headers, output.toByteArray()));
    }

    public HttpRequestHeaders getLatestRequestHeaders() {
        return requestHandler.getLatestRequestHeaders();
    }

    public byte[] getLatestRequestBody() {
        return requestHandler.getLatestRequestBody();
    }

    public String getLatestRequestBodyAsString() {
        try {
            return new String(getLatestRequestBody(), BODY_CODEPAGE);
        } catch (UnsupportedEncodingException e) {
            // should not happen
            e.printStackTrace();
            return null;
        }
    }

    private static class TestRequestHandler implements HttpServerRequestHandler {

        private Response response;
        private volatile HttpRequestHeaders latestRequestHeaders;
        private volatile byte[] latestRequestBody;

        @Override
        public void process(HttpRequest request, OutputStream outputStream) {
            latestRequestHeaders = request.getHeaders();
            try {
                latestRequestBody = request.readBodyAsBytes();
                response.getHeaders().write(outputStream);
                outputStream.write(response.getBody());
                outputStream.flush();
            } catch (IOException e) {
                throw new RuntimeException("Error", e);
            }
        }

        public HttpRequestHeaders getLatestRequestHeaders() {
            return latestRequestHeaders;
        }

        public byte[] getLatestRequestBody() {
            return latestRequestBody;
        }

        public void setResponse(Response response) {
            this.response = response;
        }
    }

    private static class Response {

        private final HttpResponseHeaders headers;
        private final byte[] body;

        public Response(HttpResponseHeaders headers, byte[] body) {
            this.headers = headers;
            this.body = body;
        }

        public HttpResponseHeaders getHeaders() {
            return headers;
        }

        public byte[] getBody() {
            return body;
        }

    }
}