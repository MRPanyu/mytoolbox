package mrpanyu.mytoolbox.tools.textfile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;

import mrpanyu.mytoolbox.framework.api.Action;
import mrpanyu.mytoolbox.framework.api.Parameter;
import mrpanyu.mytoolbox.framework.api.ParameterType;

public class TextFileLineEndingTool extends AbstractTextFileTool {

	public static final String OPTION_LINE_ENDING_UNIX = "Unix/Linux格式（LF）（\\n）";
	public static final String OPTION_LINE_ENDING_WINDOWS = "Windows格式（CRLF）（\\r\\n）";

	@Override
	public void initialize() {
		super.initialize();
		setName("0102_textFileLineEnding");
		setDisplayName("文本处理：换行符转换");
		// 参数
		Parameter param = new Parameter("targetLineEnding", "目标换行符格式");
		param.setDescription("输出目标的换行符格式");
		param.setType(ParameterType.ENUMERATION);
		param.setEnumerationValues(Arrays.asList(OPTION_LINE_ENDING_UNIX, OPTION_LINE_ENDING_WINDOWS));
		addParameter(param);
		// 操作
		addAction(new Action("transform", "转换"));
	}

	@Override
	protected boolean checkParameters() {
		return true;
	}

	@Override
	protected void performActionOnFile(String actionName, File inputFile, File outputFile) throws Exception {
		if ("transform".equals(actionName)) {
			getUserInterface().writeInfoMessage("处理文件：" + inputFile.getCanonicalPath());
			String targetLineEnding = getParameter("targetLineEnding").getValue();
			byte bcr = (byte) '\r';
			byte blf = (byte) '\n';
			byte[] bTarget = new byte[] { blf };
			if (OPTION_LINE_ENDING_WINDOWS.equals(targetLineEnding)) {
				bTarget = new byte[] { bcr, blf };
			}
			byte[] data = FileUtils.readFileToByteArray(inputFile);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			for (int i = 0; i < data.length; i++) {
				if (data[i] == bcr) {
					baos.write(bTarget);
					if (i < data.length - 1 && data[i + 1] == blf) {
						i++;
					}
				} else if (data[i] == blf) {
					baos.write(bTarget);
				} else {
					baos.write(data[i]);
				}
			}
			FileUtils.writeByteArrayToFile(outputFile, baos.toByteArray());
		}
	}

}
