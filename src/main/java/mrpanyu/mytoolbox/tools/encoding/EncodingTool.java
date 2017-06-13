package mrpanyu.mytoolbox.tools.encoding;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import mrpanyu.mytoolbox.framework.api.Action;
import mrpanyu.mytoolbox.framework.api.Parameter;
import mrpanyu.mytoolbox.framework.api.ParameterType;
import mrpanyu.mytoolbox.framework.api.Tool;
import mrpanyu.mytoolbox.framework.utils.GlobUtils;

public class EncodingTool extends Tool {

	private static final String ENCODING_AUTO = "自动识别 (仅支持GBK/UTF-8)";

	@Override
	public void initialize() {
		// 初始化配置
		setName("01_encoding");
		setDisplayName("中文编码转换");
		setDescription("");
		setEnableProfile(true);
		// 初始化参数
		Parameter param = new Parameter("srcFolder", "源文件夹");
		param.setDescription("文件来源文件夹");
		param.setType(ParameterType.DIRECTORY);
		addParameter(param);
		param = new Parameter("srcEncoding", "源文件编码");
		param.setDescription("源文件的编码");
		param.setType(ParameterType.ENUMERATION);
		param.setEnumerationValues(Arrays.asList(ENCODING_AUTO, "UTF-8", "GBK"));
		addParameter(param);
		param = new Parameter("targetFolder", "目标文件夹");
		param.setDescription("输出目标文件夹");
		param.setType(ParameterType.DIRECTORY);
		addParameter(param);
		param = new Parameter("targetEncoding", "目标文件编码");
		param.setDescription("输出目标文件的编码");
		param.setType(ParameterType.ENUMERATION);
		param.setEnumerationValues(Arrays.asList("UTF-8", "GBK"));
		addParameter(param);
		param = new Parameter("filter", "文件类型");
		param.setDescription("处理文件的过滤条件，可使用通配符，多个过滤条件用分号分隔，如“*.txt;*.java”");
		param.setValue("*");
		addParameter(param);
		// 初始化动作
		addAction(new Action("transform", "编码转换"));
	}

	@Override
	public void performAction(String actionName) {
		if ("transform".equals(actionName)) {
			transform();
		}
	}

	@Override
	public void onParameterValueChange(String name) {
		if ("srcFolder".equals(name)) {
			String srcFolder = getParameter("srcFolder").getValue();
			getParameter("targetFolder").setValue(srcFolder);
			getUserInterface().refreshParameterValue("targetFolder");
		}
	}

	private void transform() {
		getUserInterface().clearMessages();
		String srcFolder = getParameter("srcFolder").getValue();
		String srcEncoding = getParameter("srcEncoding").getValue();
		String targetFolder = getParameter("targetFolder").getValue();
		String targetEncoding = getParameter("targetEncoding").getValue();
		String filter = getParameter("filter").getValue();
		boolean check = true;
		if (StringUtils.isBlank(srcFolder)) {
			getUserInterface().writeErrorMessage("请填写源文件夹!");
			check = false;
		}
		File srcFolderFile = new File(srcFolder);
		if (!srcFolderFile.isDirectory()) {
			getUserInterface().writeErrorMessage("源文件夹不存在!");
			check = false;
		}
		if (StringUtils.isBlank(targetFolder)) {
			getUserInterface().writeErrorMessage("请填写目标文件夹!");
			check = false;
		}
		if (check) {
			try {
				getUserInterface().writeInfoMessage("开始转换编码...");
				String srcFolderPath = srcFolderFile.getCanonicalPath();
				File targetFolderFile = new File(targetFolder);
				String targetFolderPath = targetFolderFile.getCanonicalPath();
				targetFolderFile.mkdirs();
				String[] filters = new String[] { "*" };
				if (StringUtils.isNotBlank(filter)) {
					filters = filter.split(";");
				}
				Iterator<File> iterator = FileUtils.iterateFiles(srcFolderFile, null, true);
				while (iterator.hasNext()) {
					File srcFile = iterator.next();
					if (!isMatch(srcFile, filters)) {
						continue;
					}
					String srcPath = srcFile.getCanonicalPath();
					String targetPath = targetFolderPath + srcPath.substring(srcFolderPath.length());
					File targetFile = new File(targetPath);
					targetFile.getParentFile().mkdirs();
					transformEncoding(srcFile, srcEncoding, targetFile, targetEncoding);
				}
				getUserInterface().writeInfoMessage("转换编码已完成!");
			} catch (Exception e) {
				e.printStackTrace();
				getUserInterface().writeErrorMessage(e.getMessage());
			}
		}
	}

	private boolean isMatch(File file, String[] filters) {
		String name = file.getName();
		boolean match = false;
		for (String filter : filters) {
			if (StringUtils.isNotBlank(filter) && GlobUtils.globMatches(filter, name, true)) {
				match = true;
				break;
			}
		}
		return match;
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
					.writeInfoMessage("开始处理文件: " + srcFile.getCanonicalPath() + ", 编码(" + srcRealEncoding + ")");
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
