package mrpanyu.mytoolbox.tools.textfile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import mrpanyu.mytoolbox.framework.api.Action;
import mrpanyu.mytoolbox.framework.api.Parameter;
import mrpanyu.mytoolbox.framework.api.ParameterType;
import mrpanyu.mytoolbox.framework.api.Tool;
import mrpanyu.mytoolbox.framework.api.UserInterface;

public class CombineTextFileTool extends Tool {

	private static final String ENCODING_AUTO = "自动识别 (仅支持GBK/UTF-8)";

	@Override
	public void initialize() {
		setName("0105_combineTextFile");
		setDisplayName("文件处理：文本文件合并");
		setDescription("将一个目录下所有文件内容合并，适用于如一堆sql脚本整合到一起之类的场景。");
		setEnableProfile(true);

		// 参数
		Parameter param = new Parameter("inputFolder", "输入目录");
		param.setDescription("要合并的文件所在目录");
		param.setType(ParameterType.DIRECTORY);
		addParameter(param);

		param = new Parameter("inputEncoding", "输入文件编码");
		param.setDescription("输入文件的编码");
		param.setType(ParameterType.ENUMERATION);
		param.setEnumerationValues(Arrays.asList(ENCODING_AUTO, "UTF-8", "GBK"));
		addParameter(param);

		param = new Parameter("outputFile", "输出文件");
		param.setDescription("要输出到的文件");
		param.setType(ParameterType.FILE);
		addParameter(param);

		param = new Parameter("outputEncoding", "输出文件编码");
		param.setDescription("输出目标文件的编码");
		param.setType(ParameterType.ENUMERATION);
		param.setEnumerationValues(Arrays.asList("UTF-8", "GBK"));
		addParameter(param);

		param = new Parameter("fileHeader", "附加头信息");
		param.setDescription("输出每个文件内容之前附加的信息，可以用$fileName表示输入文件名称");
		param.setValue("-- content of $fileName --");
		addParameter(param);

		// 操作
		addAction(new Action("combine", "合并"));
	}

	@Override
	public void performAction(String actionName) {
		UserInterface ui = getUserInterface();
		ui.clearMessages();
		String inputFolder = getParameter("inputFolder").getValue();
		String inputEncoding = getParameter("inputEncoding").getValue();
		String outputFile = getParameter("outputFile").getValue();
		String outputEncoding = getParameter("outputEncoding").getValue();
		String fileHeader = getParameter("fileHeader").getValue();
		if ("combine".equals(actionName)) {
			if (StringUtils.isBlank(inputFolder)) {
				ui.writeErrorMessage("输入目录不能为空");
				return;
			}
			File fInputFolder = new File(inputFolder);
			if (!fInputFolder.isDirectory()) {
				ui.writeErrorMessage("输入目录不存在");
				return;
			}
			if (StringUtils.isBlank(outputFile)) {
				ui.writeErrorMessage("输出文件不能为空");
				return;
			}
			File fOutputFile = new File(outputFile);
			if (fOutputFile.isDirectory()) {
				ui.writeErrorMessage("输出文件不能是一个目录");
				return;
			}
			try {
				combine(fInputFolder, inputEncoding, fOutputFile, outputEncoding, fileHeader, ui);
			} catch (Exception e) {
				ui.writeErrorMessage("发生异常：" + e.getClass().getName() + ": " + e.getMessage());
			}
		}
	}

	private void combine(File fInputFolder, String inputEncoding, File fOutputFile, String outputEncoding,
			String fileHeader, UserInterface ui) throws Exception {
		try (PrintWriter writer = new PrintWriter(
				new OutputStreamWriter(new FileOutputStream(fOutputFile), outputEncoding))) {
			File[] files = fInputFolder.listFiles();
			for (File f : files) {
				if (f.isFile()) {
					byte[] data = FileUtils.readFileToByteArray(f);
					String encoding = inputEncoding;
					if (ENCODING_AUTO.equals(encoding)) {
						for (String enc : new String[] { "UTF-8", "GBK" }) {
							if (isValidForEncoding(data, enc)) {
								encoding = enc;
								break;
							}
						}
						if (ENCODING_AUTO.equals(encoding)) {
							ui.writeWarnMessage("无法识别文件编码，忽略文件：" + f.getName());
							continue;
						}
					}
					ui.writeInfoMessage("开始合并文件内容：" + f.getName());
					String text = new String(data, encoding);
					if (StringUtils.isNotBlank(fileHeader)) {
						writer.println(fileHeader.replace("$fileName", f.getName()));
					}
					writer.println(text);
				}
			}
		}
		ui.writeInfoMessage("合并全部完成");
	}

	private boolean isValidForEncoding(byte[] data, String encoding) {
		try {
			Charset.availableCharsets().get(encoding).newDecoder().decode(ByteBuffer.wrap(data));
			return true;
		} catch (Exception t) {
			return false;
		}
	}

	@Override
	public void onParameterValueChange(String name) {
	}

}
