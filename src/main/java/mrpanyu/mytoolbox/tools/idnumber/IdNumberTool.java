package mrpanyu.mytoolbox.tools.idnumber;

import mrpanyu.mytoolbox.framework.api.Action;
import mrpanyu.mytoolbox.framework.api.Parameter;
import mrpanyu.mytoolbox.framework.api.Tool;

public class IdNumberTool extends Tool {

	@Override
	public void initialize() {
		// 初始化配置
		setName("02_idNumber");
		setDisplayName("身份证号修正");
		setDescription("");
		setEnableProfile(false);
		// 初始化参数
		Parameter param = new Parameter("idNumber", "身份证号");
		param.setDescription("18位身份证号，后四位可任意输入");
		addParameter(param);
		// 初始化动作
		addAction(new Action("process", "修正身份证号"));
	}

	@Override
	public void performAction(String actionName) {
		if ("process".equals(actionName)) {
			String idNumber = getParameter("idNumber").getValue();
			if (idNumber.length() < 18) {
				getUserInterface().writeErrorMessage("请输入18位身份证号!");
				return;
			}
			String correctIdNumber = correctIdNumber(idNumber);
			getUserInterface().writeInfoMessage("正确的身份证号为: " + correctIdNumber);
		}
	}

	@Override
	public void onParameterValueChange(String name) {
	}

	public static String correctIdNumber(String idNumber) {
		String bit = "";
		// 根据算法规则计算
		int sum = Integer.parseInt(idNumber.substring(0, 1)) * 7 + Integer.parseInt(idNumber.substring(1, 2)) * 9
				+ Integer.parseInt(idNumber.substring(2, 3)) * 10 + Integer.parseInt(idNumber.substring(3, 4)) * 5
				+ Integer.parseInt(idNumber.substring(4, 5)) * 8 + Integer.parseInt(idNumber.substring(5, 6)) * 4
				+ Integer.parseInt(idNumber.substring(6, 7)) * 2 + Integer.parseInt(idNumber.substring(7, 8)) * 1
				+ Integer.parseInt(idNumber.substring(8, 9)) * 6 + Integer.parseInt(idNumber.substring(9, 10)) * 3
				+ Integer.parseInt(idNumber.substring(10, 11)) * 7 + Integer.parseInt(idNumber.substring(11, 12)) * 9
				+ Integer.parseInt(idNumber.substring(12, 13)) * 10 + Integer.parseInt(idNumber.substring(13, 14)) * 5
				+ Integer.parseInt(idNumber.substring(14, 15)) * 8 + Integer.parseInt(idNumber.substring(15, 16)) * 4
				+ Integer.parseInt(idNumber.substring(16, 17)) * 2;
		// 计算校验
		bit = "";
		if (sum % 11 == 0) {
			bit = "1";
		} else if (sum % 11 == 1) {
			bit = "0";
		} else if (sum % 11 == 2) {
			bit = "X";
		} else if (sum % 11 == 3) {
			bit = "9";
		} else if (sum % 11 == 4) {
			bit = "8";
		} else if (sum % 11 == 5) {
			bit = "7";
		} else if (sum % 11 == 6) {
			bit = "6";
		} else if (sum % 11 == 7) {
			bit = "5";
		} else if (sum % 11 == 8) {
			bit = "4";
		} else if (sum % 11 == 9) {
			bit = "3";
		} else if (sum % 11 == 10) {
			bit = "2";
		}
		return idNumber.substring(0, 17) + bit;
	}

}
