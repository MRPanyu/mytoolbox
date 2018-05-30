package mrpanyu.mytoolbox.tools.image;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import mrpanyu.mytoolbox.framework.utils.HttpClient;
import mrpanyu.mytoolbox.framework.utils.HttpClient.MultipartFile;

/**
 * 搜狗OCR服务 <a href=
 * "http://ai.sogou.com/ai-docs/api/ocr">http://ai.sogou.com/ai-docs/api/ocr</a>
 * 
 * @author mrpan
 *
 */
public class SougouOcrProvider implements OcrProvider {

	private String accessKey = "bTkALtTB9x6GAxmFi9wetAGH";
	private String secretKey = "PMROwlieALT36qfdGClVz2iH4Sv8xZxe";

	@Override
	public String callOcr(BufferedImage img) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(img, "JPG", baos);
		byte[] data = baos.toByteArray();

		Map<String, String> headers = new HashMap<>();
		headers.put("Authorization", authorization());

		Map<String, Object> content = new HashMap<>();
		String fileName = System.currentTimeMillis() + "_" + DigestUtils.md5Hex(data) + "_java.jpg";
		MultipartFile f = new MultipartFile(fileName, "image/jpg", data);
		content.put("filename", fileName);
		content.put("pic", f);

		String url = "http://api.ai.sogou.com/pub/ocr";
		String resp = HttpClient.postMultipart(url, headers, content);
		System.out.println("Resp: " + resp);

		StringBuilder sb = new StringBuilder();
		JSONObject respObj = JSON.parseObject(resp);
		JSONArray list = respObj.getJSONArray("list");
		if (list != null) {
			for (int i = 0; i < list.size(); i++) {
				JSONObject row = list.getJSONObject(i);
				sb.append(row.getString("content")).append("\n");
			}
		}
		if (sb.length() > 0) {
			sb.deleteCharAt(sb.length() - 1);
		}

		return sb.toString();
	}

	private String authorization() throws Exception {
		String authPrefix = "sac-auth-v1/" + accessKey + "/" + System.currentTimeMillis() + "/3600";
		String data = "POST\napi.ai.sogou.com\n/pub/ocr\n";
		Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
		SecretKeySpec secret_key = new SecretKeySpec(secretKey.getBytes("UTF-8"), "HmacSHA256");
		sha256_HMAC.init(secret_key);
		String signature = Base64.encodeBase64String(sha256_HMAC.doFinal((authPrefix + "\n" + data).getBytes("UTF-8")));
		return authPrefix + "/" + signature;
	}

}
