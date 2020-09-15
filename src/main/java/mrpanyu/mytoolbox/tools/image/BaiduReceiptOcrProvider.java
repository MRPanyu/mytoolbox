package mrpanyu.mytoolbox.tools.image;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import mrpanyu.mytoolbox.framework.utils.HttpClient;

public class BaiduReceiptOcrProvider implements OcrProvider {

	private static final String CLIENT_ID = "fTy19sF4oorhLrfrSzYuWuzA";
	private static final String CLIENT_SECRET = "dTnD7ZKzXjQIRTBBidT0dHAkoNMXBdet";

	private String token;

	@Override
	public String callOcr(BufferedImage img) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(img, "PNG", baos);
		if (token == null) {
			token = token();
		}

		String url = "https://aip.baidubce.com/rest/2.0/ocr/v1/receipt?access_token=" + token;
		Map<String, String> headers = new HashMap<>();
		headers.put("Content-Type", "application/x-www-form-urlencoded");
		String imgBase64 = Base64.encodeBase64String(baos.toByteArray());
		String data = "image=" + URLEncoder.encode(imgBase64, "UTF-8");
		String resp = HttpClient.post(url, headers, data);
		JSONObject respObj = JSON.parseObject(resp);
		return JSONObject.toJSONString(respObj, true);
	}

	private String token() throws Exception {
		String url = "https://aip.baidubce.com/oauth/2.0/token";
		Map<String, String> headers = new HashMap<>();
		headers.put("Content-Type", "application/x-www-form-urlencoded");
		String data = "grant_type=client_credentials&client_id=" + URLEncoder.encode(CLIENT_ID, "UTF-8")
				+ "&client_secret=" + URLEncoder.encode(CLIENT_SECRET, "UTF-8");
		String resp = HttpClient.post(url, headers, data);
		JSONObject respObj = JSON.parseObject(resp);
		String token = respObj.getString("access_token");
		if (StringUtils.isBlank(token)) {
			String error = respObj.getString("error");
			String description = respObj.getString("error_description");
			throw new RuntimeException(error + ": " + description);
		}
		return token;
	}

}
