package com.mrphd.http;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class HttpResponse {
	
	public static final HttpResponse NOT_FOUND = new HttpResponse(404, "", Collections.emptyMap());

	private final int status;
	private final String body;
	private final Map<String, List<String>> headers;
	
	public HttpResponse(final int status, final String body, final Map<String, List<String>> headers) {
		this.status = status;
		this.body = body;
		this.headers = Collections.unmodifiableMap(headers);
	}
	
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(headers.get(null)).append("\n");
		for(final Entry<String, List<String>> header : headers.entrySet()) {
			if(header.getKey() == null) continue;
			sb.append(String.format("%s: %s", header.getKey(), header.getValue().parallelStream().collect(Collectors.joining()))).append("\n");
		}
		sb.append("\n").append(body);
		return sb.toString();
	}
	
	public int getStatusCode() {
		return this.status;
	}
	
	public String getBody() {
		return this.body;
	}
	
	public Map<String, List<String>> getHeaders() {
		return this.headers;
	}
	
}
