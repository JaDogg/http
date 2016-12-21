package serguei.http;

import java.io.InputStream;
import java.io.OutputStream;

public interface HttpServerRequestHandler {

    public void process(HttpRequestHeaders request, InputStream inputStream, OutputStream outputStream);

}