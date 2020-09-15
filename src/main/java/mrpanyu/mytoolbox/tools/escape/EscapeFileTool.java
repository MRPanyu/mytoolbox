package mrpanyu.mytoolbox.tools.escape;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tools.ant.util.Native2AsciiUtils;

import mrpanyu.mytoolbox.framework.api.Action;
import mrpanyu.mytoolbox.framework.api.Parameter;
import mrpanyu.mytoolbox.framework.api.ParameterType;
import mrpanyu.mytoolbox.framework.api.Tool;

public class EscapeFileTool extends Tool {

	@Override
	public void initialize() {
		// 初始化配置
		setName("0202_escapeFile");
		setDisplayName("转义文件：Base64/Properties");
		setDescription("Base64/Properties文本转义工具");
		setEnableProfile(false);
		// 初始化参数
		Parameter param = new Parameter("type", "转义类型");
		param.setDescription("转义类型");
		param.setType(ParameterType.ENUMERATION);
		param.setEnumerationValues(Arrays.asList("Base64Encode", "Base64Decode", "Native2Ascii", "Ascii2Native"));
		param.setValue("Base64Encode");
		addParameter(param);

		param = new Parameter("inputFile", "输入文件");
		param.setDescription("输入文件");
		param.setType(ParameterType.FILE);
		addParameter(param);

		param = new Parameter("outputFile", "输出文件");
		param.setDescription("输出文件");
		param.setType(ParameterType.FILE);
		addParameter(param);

		// 初始化动作
		addAction(new Action("escape", "转义"));
	}

	@Override
	public void performAction(String actionName) {
		getUserInterface().clearMessages();
		try {
			if ("escape".equals(actionName)) {
				String type = getParameter("type").getValue();
				String inputFile = getParameter("inputFile").getValue();
				String outputFile = getParameter("outputFile").getValue();
				File inputF = new File(inputFile);
				File outputF = new File(outputFile);
				if (!inputF.isFile()) {
					getUserInterface().writeErrorMessage("输入文件不存在！");
					return;
				}
				if (StringUtils.isBlank(outputFile)) {
					getUserInterface().writeErrorMessage("请选择输出文件！");
					return;
				}
				if ("Base64Encode".equals(type)) {
					byte[] data = FileUtils.readFileToByteArray(inputF);
					String base64 = Base64.encodeBase64String(data);
					FileUtils.writeStringToFile(outputF, base64, "ISO-8859-1");
				} else if ("Base64Decode".equals(type)) {
					String base64 = FileUtils.readFileToString(inputF, "ISO-8859-1");
					byte[] data = Base64.decodeBase64(base64);
					FileUtils.writeByteArrayToFile(outputF, data);
				} else if ("Native2Ascii".equals(type)) {
					List<String> lines = FileUtils.readLines(inputF, "UTF-8");
					List<String> outputLines = new ArrayList<>(lines.size());
					for (String line : lines) {
						String outputLine = Native2AsciiUtils.native2ascii(line);
						outputLines.add(outputLine);
					}
					FileUtils.writeLines(outputF, "UTF-8", outputLines);
				} else if ("Ascii2Native".equals(type)) {
					List<String> lines = FileUtils.readLines(inputF, "UTF-8");
					List<String> outputLines = new ArrayList<>(lines.size());
					for (String line : lines) {
						String outputLine = Native2AsciiUtils.ascii2native(line);
						outputLines.add(outputLine);
					}
					FileUtils.writeLines(outputF, "UTF-8", outputLines);
				}
				getUserInterface().writeInfoMessage("已写入输出文件。");
			}
		} catch (Exception e) {
			getUserInterface().writeErrorMessage(e.getClass() + ":" + e.getMessage());
		}
	}

	@Override
	public void onParameterValueChange(String name) {
		if ("inputFile".equals(name)) {
			if (StringUtils.isBlank(getParameter("outputFile").getValue())) {
				getParameter("outputFile").setValue(getParameter("inputFile").getValue() + ".out");
				getUserInterface().refreshParameterValue("outputFile");
			}
		}
	}

}
