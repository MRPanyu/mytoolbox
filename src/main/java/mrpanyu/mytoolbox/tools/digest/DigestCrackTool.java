package mrpanyu.mytoolbox.tools.digest;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import mrpanyu.mytoolbox.framework.api.Action;
import mrpanyu.mytoolbox.framework.api.Parameter;
import mrpanyu.mytoolbox.framework.api.ParameterType;
import mrpanyu.mytoolbox.framework.api.Tool;

public class DigestCrackTool extends Tool {

	private static String CHARS_NUM = "1234567890";
	private static String CHARS_LOWER = "abcdefghijklmnopqrstuvwxyz";
	private static String CHARS_UPPER = CHARS_LOWER.toUpperCase();

	private static int PROGRESS_REPORT_NUM = 1000000;

	private static ExecutorService executorService = Executors.newFixedThreadPool(32);

	private static class DigestTester implements Callable<Boolean> {
		String type;
		String value;
		String digest;

		DigestTester(String type, String value, String digest) {
			this.type = type;
			this.value = value;
			this.digest = digest;
		}

		@Override
		public Boolean call() throws Exception {
			String myDigest = "";
			if ("MD5".equals(type)) {
				myDigest = DigestUtils.md5Hex(value);
			} else if ("SHA-1".equals(type)) {
				myDigest = DigestUtils.sha1Hex(value);
			} else if ("SHA-256".equals(type)) {
				myDigest = DigestUtils.sha256Hex(value);
			} else if ("SHA-384".equals(type)) {
				myDigest = DigestUtils.sha384Hex(value);
			} else if ("SHA-512".equals(type)) {
				myDigest = DigestUtils.sha512Hex(value);
			}
			return myDigest.equalsIgnoreCase(digest);
		}
	}

	private static class SearchResult {
		boolean found = false;
		String value = null;
		BigInteger checked = BigInteger.ZERO;
	}

	private AtomicBoolean runningFlag = new AtomicBoolean(false);

	@Override
	public void initialize() {
		// 初始化配置
		setName("0402_digestCrack");
		setDisplayName("摘要：摘要密码暴力反算");
		setDescription("暴力反算MD5/SHA-1摘要后的密码，获取原密码");
		setEnableProfile(false);
		// 初始化参数
		Parameter param = new Parameter("type", "密码类型");
		param.setDescription("摘要计算类型");
		param.setType(ParameterType.ENUMERATION);
		param.setEnumerationValues(Arrays.asList("MD5", "SHA-1", "SHA-256", "SHA-384", "SHA-512"));
		param.setValue("MD5");
		addParameter(param);
		param = new Parameter("chars", "密码组成");
		param.setDescription("密码由哪些字符组成");
		param.setType(ParameterType.ENUMERATION);
		param.setEnumerationValues(Arrays.asList("纯数字", "小写字母+数字", "大写字母+数字", "大小写字母+数字"));
		param.setValue("大小写字母+数字");
		addParameter(param);
		param = new Parameter("otherChars", "其他字符");
		param.setDescription("密码中允许出现的其他字符，如_或#等，也包括空格，直接输入此文本框");
		param.setType(ParameterType.SIMPLE_TEXT);
		param.setValue(" !@#$%^&*()=+-_[]{}\\|;:'\",.<>/?");
		addParameter(param);
		param = new Parameter("maxLength", "最大长度");
		param.setDescription("尝试密码的最大长度");
		param.setType(ParameterType.SIMPLE_TEXT);
		param.setValue("8");
		addParameter(param);
		param = new Parameter("prefix", "加盐前缀");
		param.setDescription("密码的加盐前缀，可为空");
		param.setType(ParameterType.SIMPLE_TEXT);
		addParameter(param);
		param = new Parameter("suffix", "加盐后缀");
		param.setDescription("密码的加盐后缀，可为空");
		param.setType(ParameterType.SIMPLE_TEXT);
		addParameter(param);
		param = new Parameter("digest", "摘要值");
		param.setDescription("摘要后的密码Hex值");
		param.setType(ParameterType.SIMPLE_TEXT);
		addParameter(param);
		// 初始化动作
		addAction(new Action("crack", "破解"));
		addAction(new Action("stop", "停止"));
	}

	@Override
	public void performAction(String actionName) {
		if ("crack".equals(actionName)) {
			if (runningFlag.get()) {
				getUserInterface().writeErrorMessage("请等待之前处理完成，或点击\"停止\"按钮");
				return;
			}
			getUserInterface().clearMessages();
			runningFlag.set(true);
			try {
				String type = getParameter("type").getValue();
				String allChars = "";
				String chars = getParameter("chars").getValue();
				if ("纯数字".equals(chars)) {
					allChars = CHARS_NUM;
				} else if ("小写字母+数字".equals(chars)) {
					allChars = CHARS_LOWER + CHARS_NUM;
				} else if ("大写字母+数字".equals(chars)) {
					allChars = CHARS_UPPER + CHARS_NUM;
				} else if ("大小写字母+数字".equals(chars)) {
					allChars = CHARS_LOWER + CHARS_UPPER + CHARS_NUM;
				}
				String otherChars = getParameter("otherChars").getValue();
				allChars = allChars + otherChars;
				int maxLength = 0;
				try {
					maxLength = Integer.parseInt(getParameter("maxLength").getValue());
				} catch (Exception e) {
				}
				if (maxLength <= 0 || maxLength > 16) {
					getUserInterface().writeErrorMessage("最大长度请输入不大于16的正整数");
					return;
				}
				String prefix = getParameter("prefix").getValue();
				String suffix = getParameter("suffix").getValue();
				String digest = getParameter("digest").getValue();
				if (StringUtils.isBlank(digest)) {
					getUserInterface().writeErrorMessage("请填写摘要值");
					return;
				}
				crack(type, allChars, maxLength, prefix, suffix, digest);
			} catch (Exception e) {
				e.printStackTrace();
				getUserInterface().writeErrorMessage("发生异常：" + e.getMessage());
			} finally {
				runningFlag.set(false);
			}
		} else if ("stop".equals(actionName)) {
			runningFlag.set(false);
		}
	}

	private void crack(String type, String allChars, int maxLength, String prefix, String suffix, String digest)
			throws Exception {
		for (int length = 1; length <= maxLength; length++) {
			SearchResult result = crackLength(type, allChars, length, prefix, suffix, digest);
			if (result.found) {
				getUserInterface().writeInfoMessage("找到了匹配的密码值: " + result.value);
				return;
			}
			if (!runningFlag.get()) {
				break;
			}
		}
		if (runningFlag.get()) {
			getUserInterface().writeInfoMessage("未找到匹配的密码值，请检查摘要值是否正确");
		} else {
			getUserInterface().writeWarnMessage("已手工停止");
		}
	}

	private SearchResult crackLength(String type, String allChars, int length, String prefix, String suffix,
			String digest) throws Exception {
		BigInteger total = BigInteger.valueOf(allChars.length());
		total = total.pow(length);
		getUserInterface().writeInfoMessage("开始尝试" + length + "位密码，共" + total + "个");
		SearchResult result = crackLengthRec(type, allChars, length, prefix, suffix, digest, "", BigInteger.ZERO,
				total);
		return result;
	}

	private SearchResult crackLengthRec(String type, String allChars, int length, String prefix, String suffix,
			String digest, String currentValue, BigInteger checked, BigInteger total) throws Exception {
		SearchResult result = new SearchResult();
		result.checked = checked;
		if (currentValue.length() < length - 1) {
			for (int i = 0; i < allChars.length(); i++) {
				String newValue = currentValue + allChars.charAt(i);
				result = crackLengthRec(type, allChars, length, prefix, suffix, digest, newValue, result.checked,
						total);
				if (result.found) {
					break;
				}
				if (!runningFlag.get()) {
					break;
				}
			}
		} else {
			List<Future<Boolean>> futures = new ArrayList<Future<Boolean>>(allChars.length());
			for (int i = 0; i < allChars.length(); i++) {
				String newValue = currentValue + allChars.charAt(i);
				Future<Boolean> future = executorService
						.submit(new DigestTester(type, prefix + newValue + suffix, digest));
				futures.add(future);
			}
			for (int i = 0; i < allChars.length(); i++) {
				Future<Boolean> future = futures.get(i);
				boolean found = future.get();
				result.checked = result.checked.add(BigInteger.ONE);
				if (found) {
					result.found = true;
					result.value = currentValue + allChars.charAt(i);
					break;
				}
				if (result.checked.mod(BigInteger.valueOf(PROGRESS_REPORT_NUM)).intValue() == 0) {
					BigDecimal percent = new BigDecimal(result.checked.toString()).multiply(BigDecimal.valueOf(100))
							.divide(new BigDecimal(total.toString()), 4, RoundingMode.HALF_UP);
					getUserInterface().writeInfoMessage("已检查" + result.checked + "个值 (" + percent + "%)");
				}
			}
		}
		return result;
	}

	@Override
	public void onParameterValueChange(String name) {
	}

}
