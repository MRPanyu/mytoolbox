package mrpanyu.mytoolbox.tools.escape;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;

import org.apache.commons.lang.StringEscapeUtils;

import mrpanyu.mytoolbox.framework.api.Action;
import mrpanyu.mytoolbox.framework.api.Parameter;
import mrpanyu.mytoolbox.framework.api.ParameterType;
import mrpanyu.mytoolbox.framework.api.Tool;

public class EscapeTool extends Tool {

	@Override
	public void initialize() {
		// 初始化配置
		setName("06_escape");
		setDisplayName("URL/XML/JS文本转义");
		setDescription("URL/XML/JS文本转义工具，支持Escape和Unescape");
		setEnableProfile(false);
		// 初始化参数
		Parameter param = new Parameter("type", "转义类型");
		param.setDescription("转义类型");
		param.setType(ParameterType.ENUMERATION);
		param.setEnumerationValues(Arrays.asList("URLEncode", "URLDecode", "EscapeXML", "UnescapeXML",
				"EscapeJavaScript", "UnescapeJavaScript"));
		param.setValue("URLEncode");
		addParameter(param);
		param = new Parameter("value", "原值");
		param.setDescription("要转义的原值");
		param.setType(ParameterType.MULTILINE_TEXT);
		addParameter(param);
		// 初始化动作
		addAction(new Action("escape", "转义"));
	}

	@Override
	public void performAction(String actionName) {
		getUserInterface().clearMessages();
		if ("escape".equals(actionName)) {
			String type = getParameter("type").getValue();
			String value = getParameter("value").getValue();
			String escapeValue = "";
			try {
				if ("URLEncode".equals(type)) {
					escapeValue = URLEncoder.encode(value, "UTF-8");
				} else if ("URLDecode".equals(type)) {
					escapeValue = URLDecoder.decode(value, "UTF-8");
				} else if ("EscapeXML".equals(type)) {
					escapeValue = StringEscapeUtils.escapeXml(value);
				} else if ("UnescapeXML".equals(type)) {
					escapeValue = StringEscapeUtils.unescapeXml(value);
				} else if ("EscapeJavaScript".equals(type)) {
					escapeValue = StringEscapeUtils.escapeJavaScript(value);
				} else if ("UnescapeJavaScript".equals(type)) {
					escapeValue = StringEscapeUtils.unescapeJavaScript(value);
				}
				getUserInterface().writeInfoMessage(escapeValue);
			} catch (Exception e) {
				e.printStackTrace();
				getUserInterface().writeErrorMessage(e.getClass() + ": " + e.getMessage());
			}
		}
	}

	@Override
	public void onParameterValueChange(String name) {
	}

}
