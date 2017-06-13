package mrpanyu.mytoolbox.tools.randomcode;

import java.util.Arrays;
import java.util.Random;

import mrpanyu.mytoolbox.framework.api.Action;
import mrpanyu.mytoolbox.framework.api.Parameter;
import mrpanyu.mytoolbox.framework.api.ParameterType;
import mrpanyu.mytoolbox.framework.api.Tool;

public class RandomCodeTool extends Tool {

	@Override
	public void initialize() {
		// 初始化配置
		setName("03_randomCode");
		setDisplayName("随机号码生成");
		setDescription("生成一些随机号码，如社会信用证号，工商营业执照号等");
		setEnableProfile(false);
		// 初始化参数
		Parameter param = new Parameter("type", "号码类型");
		param.setDescription("号码类型");
		param.setType(ParameterType.ENUMERATION);
		param.setEnumerationValues(Arrays.asList("社会统一信用证号", "组织机构证号", "行政区划号码", "工商营业执照号"));
		addParameter(param);
		param = new Parameter("num", "数量");
		param.setDescription("生成个数");
		param.setValue("1");
		addParameter(param);
		// 初始化动作
		addAction(new Action("process", "生成随机号码"));
	}

	@Override
	public void performAction(String actionName) {
		if ("process".equals(actionName)) {
			getUserInterface().clearMessages();
			String type = getParameter("type").getValue();
			String num = getParameter("num").getValue();
			int n = 1;
			try {
				n = Integer.parseInt(num);
			} catch (Exception e) {
			}
			for (int i = 0; i < n; i++) {
				if ("社会统一信用证号".equals(type)) {
					getUserInterface().writeInfoMessage(generateUniscCode());
				} else if ("组织机构证号".equals(type)) {
					getUserInterface().writeInfoMessage(generateOrgCode());
				} else if ("行政区划号码".equals(type)) {
					getUserInterface().writeInfoMessage(generateLocationCode());
				} else if ("工商营业执照号".equals(type)) {
					getUserInterface().writeInfoMessage(generateIacLicenseCode());
				}
			}
		}
	}

	@Override
	public void onParameterValueChange(String name) {
	}

	/** 社会信用登记号（三证合一代码） */
	public String generateUniscCode() {
		StringBuilder sb = new StringBuilder();
		Random rand = new Random();
		// 第1-2位：登记管理部门代码-机构类别代码
		char[] c1Arr = new char[] { '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G',
				'Y' };
		char c1 = c1Arr[rand.nextInt(c1Arr.length)];
		char[] c2Arr = new char[] { '1', '2', '3', '9' };
		char c2 = '1';
		if (c1 == '1' || c1 == '5' || c1 == '9') {
			c2 = c2Arr[rand.nextInt(c2Arr.length)];
		}
		sb.append(c1).append(c2);
		// 第3-8位：行政区划码
		sb.append(generateLocationCode());
		// 第9-17位：组织机构号
		sb.append(generateOrgCode());
		// 第18位：校验码
		int total = 0;
		String cArr = "0123456789ABCDEFGHJKLMNPQRTUWXY";
		int[] weights = new int[] { 1, 3, 9, 27, 19, 26, 16, 17, 20, 29, 25, 13, 8, 24, 10, 30, 28 };
		for (int i = 0; i < sb.length(); i++) {
			char c = sb.charAt(i);
			int r = cArr.indexOf(c);
			total += r * weights[i];
		}
		int c18 = 31 - (total % 31);
		if (c18 == 31) {
			c18 = 0;
		}
		char c18s = cArr.charAt(c18);
		sb.append(c18s);
		return sb.toString();
	}

	/** 行政区划代码 */
	public String generateLocationCode() {
		StringBuilder sb = new StringBuilder();
		Random rand = new Random();
		// 前两位省级代码
		String[] arr = new String[] { "11", "12", "13", "14", "15", "21", "22", "23", "31", "32", "33", "34", "35",
				"36", "37", "41", "42", "43", "44", "45", "46", "50", "51", "52", "53", "54", "61", "62", "63", "64",
				"65" };
		sb.append(arr[rand.nextInt(arr.length)]);
		// 后四位随机（可能出现不存在的，但符合规则）
		for (int i = 0; i < 4; i++) {
			sb.append((char) ((int) '0' + rand.nextInt(10)));
		}
		return sb.toString();
	}

	/** 组织机构证 */
	public String generateOrgCode() {
		StringBuilder sb = new StringBuilder();
		Random rand = new Random();
		String cArr = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		int[] weights = new int[] { 3, 7, 9, 10, 5, 8, 4, 2 };
		int total = 0;
		for (int i = 0; i < 8; i++) {
			int r = rand.nextInt(cArr.length());
			char c = cArr.charAt(r);
			while ("IOZSV".indexOf(c) >= 0) {
				r = rand.nextInt(cArr.length());
				c = cArr.charAt(r);
			}
			total += r * weights[i];
			sb.append(c);
		}
		// 第9位校验码
		int c9 = 11 - (total % 11);
		String c9s = String.valueOf(c9);
		if (c9 == 11) {
			c9s = "0";
		} else if (c9 == 10) {
			c9s = "X";
		}
		sb.append(c9s);
		return sb.toString();
	}

	public String generateIacLicenseCode() {
		StringBuilder sb = new StringBuilder();
		Random rand = new Random();
		// 前6位行政区划代码
		sb.append(generateLocationCode());
		// 7-14位随机数值
		String cArr = "0123456789";
		for (int i = 0; i < 8; i++) {
			int r = rand.nextInt(cArr.length());
			char c = cArr.charAt(r);
			sb.append(c);
		}
		// 15位校验码
		int p = 10;
		for (int i = 0; i < 14; i++) {
			int a = Integer.parseInt(String.valueOf(sb.charAt(i)));
			p = (p + a) % 10;
			if (p == 0) {
				p = 10;
			}
			p = p * 2 % 11;
		}
		int c15 = 11 - p;
		if (c15 == 11) {
			c15 = 1;
		} else if (c15 == 10) {
			c15 = 0;
		}
		sb.append(c15);
		return sb.toString();
	}

}
