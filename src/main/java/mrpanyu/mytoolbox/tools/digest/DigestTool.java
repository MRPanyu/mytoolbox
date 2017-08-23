package mrpanyu.mytoolbox.tools.digest;

import java.io.File;
import java.util.Arrays;
import java.util.zip.CRC32;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import mrpanyu.mytoolbox.framework.api.Action;
import mrpanyu.mytoolbox.framework.api.Parameter;
import mrpanyu.mytoolbox.framework.api.ParameterType;
import mrpanyu.mytoolbox.framework.api.Tool;

public class DigestTool extends Tool {

	@Override
	public void initialize() {
		// 初始化配置
		setName("04_digest");
		setDisplayName("摘要值计算");
		setDescription("计算MD5，SHA-1等摘要值");
		setEnableProfile(false);
		// 初始化参数
		Parameter param = new Parameter("type", "计算类型");
		param.setDescription("摘要计算类型");
		param.setType(ParameterType.ENUMERATION);
		param.setEnumerationValues(Arrays.asList("MD5", "SHA-1", "SHA-256", "SHA-384", "SHA-512", "CRC32"));
		param.setValue("MD5");
		addParameter(param);
		param = new Parameter("value", "字符串值");
		param.setDescription("要计算的字符串值");
		param.setType(ParameterType.NORMAL);
		addParameter(param);
		param = new Parameter("file", "文件");
		param.setDescription("要计算的文件");
		param.setType(ParameterType.FILE);
		addParameter(param);
		// 初始化动作
		addAction(new Action("calc", "计算"));
	}

	@Override
	public void performAction(String actionName) {
		getUserInterface().clearMessages();
		if ("calc".equals(actionName)) {
			String type = getParameter("type").getValue();
			String value = getParameter("value").getValue();
			String file = getParameter("file").getValue();
			if (StringUtils.isBlank(value) && StringUtils.isBlank(file)) {
				getUserInterface().writeErrorMessage("字符串值或文件请填写一项");
				return;
			}
			if (StringUtils.isNotBlank(value) && StringUtils.isNotBlank(file)) {
				getUserInterface().writeErrorMessage("字符串值和文件只能填写一项，当前计算的为字符串值");
			}
			if (StringUtils.isBlank(value) && !(new File(file)).isFile()) {
				getUserInterface().writeErrorMessage("选择的文件不存在");
				return;
			}
			try {
				byte[] data = value.getBytes("UTF-8");
				if (StringUtils.isBlank(value)) {
					data = FileUtils.readFileToByteArray(new File(file));
				}
				String digest = "";
				String digestDigital = "";
				if ("MD5".equals(type)) {
					digest = DigestUtils.md5Hex(data);
				} else if ("SHA-1".equals(type)) {
					digest = DigestUtils.sha1Hex(data);
				} else if ("SHA-256".equals(type)) {
					digest = DigestUtils.sha256Hex(data);
				} else if ("SHA-384".equals(type)) {
					digest = DigestUtils.sha384Hex(data);
				} else if ("SHA-512".equals(type)) {
					digest = DigestUtils.sha512Hex(data);
				} else if ("CRC32".equals(type)) {
					CRC32 crc32 = new CRC32();
					crc32.update(data);
					digest = Long.toHexString(crc32.getValue());
					digestDigital = Long.toString(crc32.getValue());
				}
				getUserInterface().writeInfoMessage("摘要值(大写): " + digest.toUpperCase());
				getUserInterface().writeInfoMessage("摘要值(小写): " + digest);
				if (StringUtils.isNotBlank(digestDigital)) {
					getUserInterface().writeInfoMessage("摘要值(十进制): " + digestDigital);
				}
			} catch (Exception e) {
				getUserInterface().writeErrorMessage(e.getMessage());
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onParameterValueChange(String name) {
	}

}
