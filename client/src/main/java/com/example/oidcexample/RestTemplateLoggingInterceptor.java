package com.example.oidcexample;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

// https://stackoverflow.com/a/47467572/4506703
@RequiredArgsConstructor
public class RestTemplateLoggingInterceptor implements ClientHttpRequestInterceptor {

    private final Logger logger;

    @Override
    public ClientHttpResponse intercept(final HttpRequest request, final byte[] body, final ClientHttpRequestExecution execution)
        throws IOException {
        traceRequest(request, body);
        final ClientHttpResponse response = execution.execute(request, body);
        return traceResponse(response);
    }

    private void traceRequest(final HttpRequest request, final byte[] body) throws IOException {
        if (!logger.isDebugEnabled()) {
            return;
        }
        logger.debug(
            "==========================request begin==============================================");
        logger.debug("URI                 : {}", request.getURI());
        logger.debug("Method            : {}", request.getMethod());
        logger.debug("Headers         : {}", request.getHeaders());
        logger.debug("Request body: {}", new String(body, "UTF-8"));
        logger.debug(
            "==========================request end================================================");
    }

    private ClientHttpResponse traceResponse(final ClientHttpResponse response) throws IOException {
        if (!logger.isDebugEnabled()) {
            return response;
        }
        final ClientHttpResponse responseWrapper = new BufferingClientHttpResponseWrapper(response);
        final StringBuilder inputStringBuilder = new StringBuilder();
        final BufferedReader bufferedReader = new BufferedReader(
            new InputStreamReader(responseWrapper.getBody(), "UTF-8"));
        String line = bufferedReader.readLine();
        while (line != null) {
            inputStringBuilder.append(line);
            inputStringBuilder.append('\n');
            line = bufferedReader.readLine();
        }
        logger.debug(
            "==========================response begin=============================================");
        logger.debug("Status code    : {}", responseWrapper.getStatusCode());
        logger.debug("Status text    : {}", responseWrapper.getStatusText());
        logger.debug("Headers            : {}", responseWrapper.getHeaders());
        logger.debug("Response body: {}", inputStringBuilder.toString());
        logger.debug(
            "==========================response end===============================================");
        return responseWrapper;
    }

}
