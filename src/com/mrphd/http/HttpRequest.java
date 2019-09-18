package com.mrphd.http;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.MissingFormatArgumentException;

import javax.net.ssl.HttpsURLConnection;

import com.mrphd.util.URLUtility;

public class HttpRequest {

	public static enum Method {
		GET(false), POST(true);
		private final boolean hasBody;
		private Method(final boolean hasBody) {
			this.hasBody = hasBody;
		}
		@Override
		public String toString() {
			return name().toUpperCase();
		}
		public boolean hasBody() {
			return this.hasBody;
		}
	}

	private final String urlText;
	private final Method method;
	private final Map<String, String> headers;
	private final Map<String, String> params;
	
	public HttpRequest(final String urlText, final Method method) {
		this.urlText = urlText;
		this.method = method;
		this.headers = new HashMap<String, String>();
		this.params = method.hasBody() ? new HashMap<String, String>() : Collections.emptyMap();
	}
	
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(String.format("%s %s HTTP/1.1", method, urlText)).append("\n");
		for(final Entry<String, String> header : headers.entrySet()) {
			sb.append(String.format("%s: %s", header.getKey(), header.getValue())).append("\n");
		}
		return sb.append("\n").append(getBody()).toString();
	}
	
	public HttpRequest withHeader(final String header, final String value) {
		headers.put(header, value); return this;
	}
	
	public HttpRequest withParam(final String key, final String value) {
		if(!method.hasBody) return this;
		params.put(key, value); return this;
	}
	
	public String getBody() {
		final StringBuilder body = new StringBuilder();
		int k = 0;
		for(final Entry<String, String> param : params.entrySet()) {
			if(k++ != 0) body.append("&");
			body.append(String.format(
					"%s=%s", 
					URLUtility.encodeUrlComponent(param.getKey()), 
					URLUtility.encodeUrlComponent(param.getValue())
			));
		}
		return body.toString();
	}
	
	public HttpResponse execute(final Proxy proxy) throws MalformedURLException, IOException {
		final URL url = new URL(urlText);
		final HttpsURLConnection conn = (HttpsURLConnection) (proxy == null ? url.openConnection() : url.openConnection(proxy));
		conn.setRequestMethod(method.toString());
		for(final Entry<String, String> header : headers.entrySet()) {
			conn.setRequestProperty(header.getKey(), header.getValue());
		}
		if(method.hasBody()) {
			conn.setDoOutput(true);
			final BufferedOutputStream bos = new BufferedOutputStream(conn.getOutputStream());
			final OutputStreamWriter os = new OutputStreamWriter(bos, "UTF-8");
			os.write(getBody());
		}
		
		conn.connect();
		if(conn.getResponseCode() != 404) {
			final BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
//			final String body = br.lines().collect(Collectors.joining());
			final StringBuilder body = new StringBuilder();
			String line;
			while((line = br.readLine()) != null) {
				body.append(line).append("\n");
			}
			return new HttpResponse(conn.getResponseCode(), body.toString(), conn.getHeaderFields());
		}
		return HttpResponse.NOT_FOUND;
	}
	
	public HttpResponse execute() throws MalformedURLException, IOException {
		return this.execute(null);
	}
	
	public static void main(final String[] args) throws MalformedURLException, IOException {
		final HttpRequest req1 = new HttpRequest("https://i-learn.uitm.edu.my/v3/users/loginForm/1", Method.POST)
				.withHeader("Host", "i-learn.uitm.edu.my")
				.withHeader("Connection", "keep-alive")
				.withHeader("Content-Length", "90")
				.withHeader("Cache-Control", "max-age=0")
				.withHeader("Origin", "https://i-learn.uitm.edu.my")
				.withHeader("Upgrade-Insecure-Requests", "1")
				.withHeader("Content-Type", "application/x-www-form-urlencoded")
				.withHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.75 Safari/537.36")
				.withHeader("Sec-Fetch-Mode", "navigate")
				.withHeader("Sec-Fetch-User", "?1")
				.withHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3")
				.withHeader("Sec-Fetch-Site", "same-origin")
				.withHeader("Referer", "https://i-learn.uitm.edu.my/v3/users/loginForm/1")
				.withHeader("Accept-Encoding", "gzip, deflate, br")
				.withHeader("Accept-Language", "en-US,en;q=0.9")
				.withHeader("Cookie", "")
				.withParam("_method", "POST")
				.withParam("data[User][username]", "2016692964")
				.withParam("data[User][password]", "Hb061097");
		final HttpResponse res1 = req1.execute();

		final String temp = String.join("; ", res1.getHeaders().get("Set-Cookie")).replace(";Path=/;HttpOnly", "").replace("; path=/; HttpOnly", "");
		String setCookie1 = "";
//		String CAKEPHP = "CAKEPHP="; //  = setCookie1.split("\\;\\ ")[0]
		int n=0;
		for(final String cookie : temp.split("; ")) {
			if(cookie.contains("cookiesession1") || cookie.contains("CAKEPHP")) {
				if(n++ != 0) setCookie1 += "; ";
				setCookie1 += cookie;
			}
		}
		
		System.out.println("Set-Cookie: " + setCookie1);
		
//		setCookie1 = String.join("; ", "_ga=GA1.3.224326680.1568689252; __tawkuuid=e::i-learn.uitm.edu.my::Q9rgmjbXAA1ryy9x4wWAyLX8Xr/35rI73kWoe4uzGhOAOTV7HenhI9PhEdPfvwYH::2; cookiesession1=47E99C00NBI0TAFGHWECVFYVBM4M4EE8; _gid=GA1.3.1851578809.1568793424; _gat_gtag_UA_117845915_1=1; TawkConnectionTime=0", CAKEPHP);
//		System.out.printf("Cookie: %s\n", setCookie1);
		setCookie1 = String.join(
			"; ",
			setCookie1,
			"_ga=GA1.3.224326680.1568689252",
			"__tawkuuid=e::i-learn.uitm.edu.my::Q9rgmjbXAA1ryy9x4wWAyLX8Xr/35rI73kWoe4uzGhOAOTV7HenhI9PhEdPfvwYH::2",
			"_gid=GA1.3.1851578809.1568793424",
			"_gat_gtag_UA_117845915_1=1",
			"TawkConnectionTime=0"
		);
		setCookie1 = String.join(
			"; ",
			"cookiesession1=47E99C00NBI0TAFGHWECVFYVBM4M4EE8",
			"CAKEPHP=39dqaser52t5j2dd338os1oge2"
//			"_ga=GA1.3.224326680.1568689252",
//			"__tawkuuid=e::i-learn.uitm.edu.my::Q9rgmjbXAA1ryy9x4wWAyLX8Xr/35rI73kWoe4uzGhOAOTV7HenhI9PhEdPfvwYH::2",
//			"_gid=GA1.3.1851578809.1568793424"
//			"_gat_gtag_UA_117845915_1=1"
//			"TawkConnectionTime=0"
		);
		System.out.println(setCookie1);
		
		final HttpRequest req2 = new HttpRequest("https://i-learn.uitm.edu.my/v3/users/profile", Method.GET)
				.withHeader("Host", "i-learn.uitm.edu.my")
				.withHeader("Connection", "keep-alive")
				.withHeader("Cache-Control", "max-age=0")
				.withHeader("Upgrade-Insecure-Requests", "1")
				.withHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.75 Safari/537.36")
				.withHeader("Sec-Fetch-Mode", "navigate")
				.withHeader("Sec-Fetch-User", "?1")
				.withHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3")
				.withHeader("Sec-Fetch-Site", "same-origin")
				.withHeader("Referer", "https://i-learn.uitm.edu.my/v3/users/loginForm/1")
				.withHeader("Accept-Encoding", "gzip, deflate, br")
				.withHeader("Accept-Language", "en-US,en;q=0.9")
				.withHeader("Cookie", setCookie1)
//				.withHeader("Cookie", String.join("", "_ga=GA1.3.224326680.1568689252; __tawkuuid=e::i-learn.uitm.edu.my::Q9rgmjbXAA1ryy9x4wWAyLX8Xr/35rI73kWoe4uzGhOAOTV7HenhI9PhEdPfvwYH::2; cookiesession1=47E99C00NBI0TAFGHWECVFYVBM4M4EE8; _gid=GA1.3.1851578809.1568793424; _gat_gtag_UA_117845915_1=1; TawkConnectionTime=0; ", setCookie1));
				;
		final HttpResponse res2 = req2.execute();

		final List<String> setCookie2 = res2.getHeaders().get("Set-Cookie");
		if(setCookie2 != null) {
			System.out.printf("Got Set-Cookie: %s\n", String.join("; ", setCookie2.toArray(new CharSequence[0])));
		}else {
			System.out.println("Got no set cookie header!");
		}
	}
	
}
