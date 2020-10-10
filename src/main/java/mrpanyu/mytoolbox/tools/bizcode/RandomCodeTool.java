package mrpanyu.mytoolbox.tools.bizcode;

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
		setName("0302_randomCode");
		setDisplayName("证件号：随机证件号生成");
		setDescription("生成一些符合校验规则的随机证件号，如社会统一信用证号，组织机构证号，工商营业执照号等");
		setEnableProfile(false);
		// 初始化参数
		Parameter param = new Parameter("type", "号码类型");
		param.setDescription("号码类型");
		param.setType(ParameterType.ENUMERATION);
		param.setEnumerationValues(Arrays.asList("社会统一信用证号", "组织机构证号", "行政区划号码", "工商营业执照号", "VIN码"));
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
				} else if ("VIN码".equals(type)) {
					getUserInterface().writeInfoMessage(generateVinNo());
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

	public String generateVinNo() {
		StringBuilder sb = new StringBuilder();
		Random rand = new Random();

		// 所有字母数字，除IOQ以外
		char[] alphaNumArr = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E',
				'F', 'G', 'H', 'J', 'K', 'L', 'M', 'N', 'P', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };
		// 上述字母数字表示的校验值
		int[] alphaNumValueArr = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 1, 2, 3, 4, 5, 6, 7, 8, 1, 2, 3, 4, 5, 7, 9,
				2, 3, 4, 5, 6, 7, 8, 9 };
		// 17位各自的权重
		int[] weightArr = new int[] { 8, 7, 6, 5, 4, 3, 2, 10, 0, 9, 8, 7, 6, 5, 4, 3, 2 };
		// 仅数字
		char[] numArr = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };
		// 可表示国家的字符
		char[] nationArr = new char[] { '1', '2', '3', '4', '6', '9', 'J', 'K', 'L', 'R', 'S', 'T', 'V', 'W', 'Y',
				'Z' };
		// 可表示厂商的字符
		char[] makerArr = new char[] { '1', '2', '3', '4', '5', '6', '7', '8', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',
				'L', 'M', 'P', 'S', 'T' };
		// 可表示年份的字符
		char[] yearArr = new char[] { '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G',
				'H', 'J', 'K', 'L', 'M', 'N', 'P', 'R', 'S', 'T', 'V', 'W', 'X', 'Y' };

		// 第1位：国家
		int r = rand.nextInt(nationArr.length);
		sb.append(nationArr[r]);
		// 第2位：厂商
		r = rand.nextInt(makerArr.length);
		sb.append(makerArr[r]);
		// 第3位：厂商车系
		r = rand.nextInt(alphaNumArr.length);
		sb.append(alphaNumArr[r]);
		// 第4-8位：厂商车辆描述
		for (int i = 0; i < 5; i++) {
			r = rand.nextInt(alphaNumArr.length);
			sb.append(alphaNumArr[r]);
		}
		// 第9位：校验码，后面再计算
		sb.append("0");
		// 第10位：年份
		r = rand.nextInt(yearArr.length);
		sb.append(yearArr[r]);
		// 第11位：装配厂
		r = rand.nextInt(alphaNumArr.length);
		sb.append(alphaNumArr[r]);
		// 第12-17位：出厂序号，12-13位可以有字母，14-17位只能是数字
		for (int i = 0; i < 2; i++) {
			r = rand.nextInt(alphaNumArr.length);
			sb.append(alphaNumArr[r]);
		}
		for (int i = 0; i < 4; i++) {
			r = rand.nextInt(numArr.length);
			sb.append(numArr[r]);
		}

		// 计算第9位校验码
		int check = 0;
		for (int i = 0; i < 17; i++) {
			char c = sb.charAt(i);
			int value = 0;
			for (int j = 0; j < alphaNumArr.length; j++) {
				if (c == alphaNumArr[j]) {
					value = alphaNumValueArr[j];
					break;
				}
			}
			int weight = weightArr[i];
			check += value * weight;
		}
		check = check % 11;
		char checkChar = check == 10 ? 'X' : numArr[check];
		sb.setCharAt(8, checkChar);

		return sb.toString();
	}

}
