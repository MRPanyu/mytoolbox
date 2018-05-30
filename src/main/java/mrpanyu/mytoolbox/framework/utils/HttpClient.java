package mrpanyu.mytoolbox.framework.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.UUID;

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

	public static String postMultipart(String url, Map<String, String> headers, Map<String, Object> content)
			throws IOException {
		System.out.println("HTTP POST: url=" + url + ", headers=" + headers + ", content=" + content);
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
			String boundary = UUID.randomUUID().toString().replace('-', '0');
			conn.addRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.connect();
			OutputStream out = conn.getOutputStream();
			for (Map.Entry<String, Object> entry : content.entrySet()) {
				String key = entry.getKey();
				Object value = entry.getValue();
				if (value instanceof String) {
					StringBuilder sb = new StringBuilder();
					sb.append("--").append(boundary).append("\n");
					sb.append("Content-Disposition: form-data; name=\"").append(URLEncoder.encode(key, "UTF-8"))
							.append("\"\n");
					sb.append("\n");
					sb.append(value).append("\n");
					out.write(sb.toString().getBytes("UTF-8"));
				} else if (value instanceof MultipartFile) {
					MultipartFile f = (MultipartFile) value;
					StringBuilder sb = new StringBuilder();
					sb.append("--").append(boundary).append("\n");
					sb.append("Content-Disposition: form-data; name=\"").append(URLEncoder.encode(key, "UTF-8"))
							.append("\"; filename=\"").append(URLEncoder.encode(f.getFileName(), "UTF-8"))
							.append("\"\n");
					sb.append("Content-Type: ").append(f.getContentType()).append("\n");
					sb.append("Content-Transfer-Encoding: binary\n");
					sb.append("\n");
					out.write(sb.toString().getBytes("UTF-8"));
					out.write(f.getFileData());
				} else {
					throw new IllegalArgumentException("Unsupported content class: " + value.getClass().getName()
							+ ", only String and MultipartFile supported.");
				}
			}
			out.write(("--" + boundary + "--").getBytes("UTF-8"));

			out.close();
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

	public static class MultipartFile {

		private String fileName;
		private String contentType;
		private byte[] fileData;

		public MultipartFile(String fileName, String contentType, byte[] fileData) {
			super();
			this.fileName = fileName;
			this.contentType = contentType;
			this.fileData = fileData;
		}

		public String getFileName() {
			return fileName;
		}

		public void setFileName(String fileName) {
			this.fileName = fileName;
		}

		public String getContentType() {
			return contentType;
		}

		public void setContentType(String contentType) {
			this.contentType = contentType;
		}

		public byte[] getFileData() {
			return fileData;
		}

		public void setFileData(byte[] fileData) {
			this.fileData = fileData;
		}

		@Override
		public String toString() {
			return "MultipartFile [fileName=" + fileName + ", contentType=" + contentType + "]";
		}

	}

}
