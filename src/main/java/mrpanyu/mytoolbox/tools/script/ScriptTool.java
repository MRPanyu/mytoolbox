package mrpanyu.mytoolbox.tools.script;

import java.util.Arrays;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import mrpanyu.mytoolbox.framework.api.Action;
import mrpanyu.mytoolbox.framework.api.Parameter;
import mrpanyu.mytoolbox.framework.api.ParameterType;
import mrpanyu.mytoolbox.framework.api.Tool;
import mrpanyu.mytoolbox.framework.api.UserInterface;

public class ScriptTool extends Tool {

	@Override
	public void initialize() {
		// 初始化配置
		setName("0000_script");
		setDisplayName("自定义脚本执行");
		setDescription("自定义脚本执行工具");
		setEnableProfile(true);
		// 初始化参数
		Parameter param = new Parameter("engine", "脚本引擎");
		param.setDescription("脚本类型");
		param.setType(ParameterType.ENUMERATION);
		param.setEnumerationValues(Arrays.asList("Groovy", "Nashorn"));
		addParameter(param);
		param = new Parameter("script", "脚本内容");
		param.setDescription("脚本内容");
		param.setType(ParameterType.MULTILINE_TEXT);
		addParameter(param);

		// 初始化动作
		addAction(new Action("execute", "执行", "执行脚本"));
		addAction(new Action("clear", "清空", "清空输出结果"));
	}

	@Override
	public void performAction(String actionName) {
		UserInterface ui = getUserInterface();
		if ("execute".equals(actionName)) {
			String engine = getParameter("engine").getValue();
			String script = getParameter("script").getValue();
			try {
				ScriptEngine se = new ScriptEngineManager().getEngineByName(engine);
				Object returnValue = se.eval(script);
				ui.writeInfoMessage("" + returnValue);
			} catch (Exception e) {
				e.printStackTrace();
				ui.writeErrorMessage(e.getClass().getName() + ": " + e.getMessage());
			}
		} else if ("clear".equals(actionName)) {
			ui.clearMessages();
		}
	}

	@Override
	public void onParameterValueChange(String name) {
	}

}
