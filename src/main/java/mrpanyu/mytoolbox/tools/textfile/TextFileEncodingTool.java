package mrpanyu.mytoolbox.tools.textfile;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;

import mrpanyu.mytoolbox.framework.api.Action;
import mrpanyu.mytoolbox.framework.api.Parameter;
import mrpanyu.mytoolbox.framework.api.ParameterType;

public class TextFileEncodingTool extends AbstractTextFileTool {

	private static final String ENCODING_AUTO = "自动识别 (仅支持GBK/UTF-8)";

	@Override
	public void initialize() {
		super.initialize();
		setName("0101_textFileEncoding");
		setDisplayName("文本处理：中文编码转换");
		// 参数
		Parameter param = new Parameter("srcEncoding", "源文件编码");
		param.setDescription("源文件的编码");
		param.setType(ParameterType.ENUMERATION);
		param.setEnumerationValues(Arrays.asList(ENCODING_AUTO, "UTF-8", "GBK"));
		addParameter(param);
		param = new Parameter("targetEncoding", "目标文件编码");
		param.setDescription("输出目标文件的编码");
		param.setType(ParameterType.ENUMERATION);
		param.setEnumerationValues(Arrays.asList("UTF-8", "GBK"));
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
			String srcEncoding = getParameter("srcEncoding").getValue();
			String targetEncoding = getParameter("targetEncoding").getValue();
			transformEncoding(inputFile, srcEncoding, outputFile, targetEncoding);
		}
	}

	private void transformEncoding(File srcFile, String srcEncoding, File targetFile, String targetEncoding)
			throws Exception {
		byte[] data = FileUtils.readFileToByteArray(srcFile);
		String srcRealEncoding = null;
		if (ENCODING_AUTO.equals(srcEncoding)) {
			if (isValidForEncoding(data, "UTF-8")) {
				srcRealEncoding = "UTF-8";
			} else if (isValidForEncoding(data, "GBK")) {
				srcRealEncoding = "GBK";
			}
		} else {
			if (isValidForEncoding(data, srcEncoding)) {
				srcRealEncoding = srcEncoding;
			}
		}
		if (srcRealEncoding == null) {
			getUserInterface().writeErrorMessage("无法识别文件编码，未处理: " + srcFile.getCanonicalPath());
			return;
		} else {
			getUserInterface()
					.writeInfoMessage("处理文件: " + srcFile.getCanonicalPath() + ", 编码(" + srcRealEncoding + ")");
			String text = new String(data, srcRealEncoding);
			FileUtils.writeStringToFile(targetFile, text, targetEncoding);
		}
	}

	private static boolean isValidForEncoding(byte[] data, String encoding) {
		try {
			Charset.availableCharsets().get(encoding).newDecoder().decode(ByteBuffer.wrap(data));
			return true;
		} catch (Exception t) {
			return false;
		}
	}

}
