package com.example.oidcexample;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

// https://stackoverflow.com/a/47467572/4506703
@RequiredArgsConstructor
public class RestTemplateLoggingInterceptor implements ClientHttpRequestInterceptor {

    @NonNull
    private final Logger logger;

    @Override
    public ClientHttpResponse intercept(final HttpRequest request, final byte[] body,
        final ClientHttpRequestExecution execution) throws IOException {

        if (logger.isDebugEnabled()) {

            final URI uri = request.getURI();
            final HttpMethod method = request.getMethod();
            final HttpHeaders headers = request.getHeaders();
            final String bodyStr = new String(body, StandardCharsets.UTF_8);

            logger.debug("Begin requesting. uri: {}, method: {}, headers: {}, body: {}", uri, method, headers,
                bodyStr);
        }

        final ClientHttpResponse response;
        try {
            response = execution.execute(request, body);
        } catch (final Exception e) {
            logger.error("Error requesting.", e);
            throw e;
        }

        try (
            final ClientHttpResponse respWrapper = new BufferingClientHttpResponseWrapper(response);
            final BufferedReader reader = new BufferedReader(
                new InputStreamReader(respWrapper.getBody(), "UTF-8"))) {

            if (logger.isDebugEnabled()) {
                final int statusCode = respWrapper.getRawStatusCode();
                final HttpHeaders headers = respWrapper.getHeaders();
                final String bodyStr = reader.lines().collect(Collectors.joining());

                logger.debug("End requesting. statusCode: {}, headers: {}, body: {}", statusCode, headers, bodyStr);
            }

            return respWrapper;
        }

    }

}
