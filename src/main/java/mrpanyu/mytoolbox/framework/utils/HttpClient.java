package mrpanyu.mytoolbox.framework.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import org.apache.commons.io.IOUtils;

public class HttpClient {

	public static String get(String url, Map<String, String> headers) throws IOException {
		System.out.println("HTTP GET: url=" + url + ", headers=" + headers);
		HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
		try {
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(3000);
			conn.setReadTimeout(30000);
			if (headers != null) {
				for (Map.Entry<String, String> entry : headers.entrySet()) {
					conn.addRequestProperty(entry.getKey(), entry.getValue());
				}
			}
			conn.setDoInput(true);
			conn.setDoOutput(false);
			conn.connect();
			if (HttpURLConnection.HTTP_OK != conn.getResponseCode()) {
				throw new IOException("HTTP Response Code " + conn.getResponseCode());
			}
			InputStream in = conn.getInputStream();
			String resp = new String(IOUtils.toByteArray(in), "UTF-8");
			return resp;
		} finally {
			conn.disconnect();
		}
	}

	public static String get(String url) throws IOException {
		return get(url, null);
	}

	public static String post(String url, Map<String, String> headers, String data) throws IOException {
		System.out.println("HTTP POST: url=" + url + ", headers=" + headers + ", data=" + data);
		HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
		try {
			conn.setRequestMethod("POST");
			conn.setConnectTimeout(3000);
			conn.setReadTimeout(30000);
			if (headers != null) {
				for (Map.Entry<String, String> entry : headers.entrySet()) {
					conn.addRequestProperty(entry.getKey(), entry.getValue());
				}
			}
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.connect();
			conn.getOutputStream().write(data.getBytes("UTF-8"));
			conn.getOutputStream().close();
			if (HttpURLConnection.HTTP_OK != conn.getResponseCode()) {
				throw new IOException("HTTP Response Code " + conn.getResponseCode());
			}
			InputStream in = conn.getInputStream();
			String resp = new String(IOUtils.toByteArray(in), "UTF-8");
			return resp;
		} finally {
			conn.disconnect();
		}
	}

	public static String post(String url, String data) throws IOException {
		return post(url, null, data);
	}

}
