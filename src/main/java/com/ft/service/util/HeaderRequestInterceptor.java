package com.ft.service.util;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.support.HttpRequestWrapper;
import org.springframework.util.StreamUtils;

public class HeaderRequestInterceptor implements ClientHttpRequestInterceptor {

	private final Logger log = LoggerFactory.getLogger(HeaderRequestInterceptor.class);

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
			throws IOException {
		HttpRequest wrapper = new HttpRequestWrapper(request);
		for (String k: headers.keySet())
			wrapper.getHeaders().set(k, headers.get(k));
		log.debug("=== {} URL: {}", request.getMethod(), request.getURI());
		log.debug(">>> REQ: {}", new String(body));
        ClientHttpResponse result = execution.execute(wrapper, body);
        log.debug("<<< RESP: {} - {} -- {}", result.getRawStatusCode(), result.getStatusText(), StreamUtils.copyToString(result.getBody(), Charset.defaultCharset()));
        return result;
	}

    private final Map<String, String> headers;


    public HeaderRequestInterceptor(Map<String, String> headers) {
    	this.headers = headers;
    }
}
