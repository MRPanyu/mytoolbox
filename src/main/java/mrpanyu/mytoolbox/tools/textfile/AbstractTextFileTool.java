package mrpanyu.mytoolbox.tools.textfile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import mrpanyu.mytoolbox.framework.api.Parameter;
import mrpanyu.mytoolbox.framework.api.ParameterType;
import mrpanyu.mytoolbox.framework.api.Tool;
import mrpanyu.mytoolbox.framework.utils.AntPathMatcher;

/**
 * 批量文本文件处理工具的基类，实现基本的目录迭代搜索功能，由具体工具执行每个文件的处理操作。
 * 
 * @author Panyu
 *
 */
public abstract class AbstractTextFileTool extends Tool {

	@Override
	public void initialize() {
		// 批量文本处理工具基本都需要预设方案功能
		setEnableProfile(true);
		// 初始化参数
		Parameter param = new Parameter("srcFolder", "源文件夹");
		param.setDescription("文件来源文件夹");
		param.setType(ParameterType.DIRECTORY);
		addParameter(param);
		param = new Parameter("targetFolder", "目标文件夹");
		param.setDescription("输出目标文件夹，如果同源文件夹，则转换后内容直接覆盖");
		param.setType(ParameterType.DIRECTORY);
		addParameter(param);
		param = new Parameter("includeFilters", "过滤条件-包含");
		param.setDescription("包含哪些文件的过滤条件，每行一个或分号分隔，支持通配符如*.java，以斜杠开头的支持ant式路径表达式，如/dir1/**，留空表示包含所有文件");
		param.setType(ParameterType.MULTILINE_TEXT);
		addParameter(param);
		param = new Parameter("excludeFilters", "过滤条件-排除");
		param.setDescription("包含哪些文件的过滤条件，每行一个或分号分隔，支持通配符如*.java，以斜杠开头的支持ant式路径表达式，如/dir1/**");
		param.setType(ParameterType.MULTILINE_TEXT);
		param.setValue("*.jpg;*.png;*.gif\n*.zip;*.jar;*.class\n.svn;.git");
		addParameter(param);
	}

	/**
	 * 进行参数校验后根据过滤条件迭代搜索文件，之后调用工具本身的{@link #performActionOnFile(String, File)}方法进行实际操作
	 */
	@Override
	public void performAction(String actionName) {
		String srcFolder = getParameter("srcFolder").getValue();
		String targetFolder = getParameter("targetFolder").getValue();
		if (StringUtils.isBlank(srcFolder)) {
			getUserInterface().writeErrorMessage("请选择源文件夹");
			return;
		}
		File fSrcFolder = new File(srcFolder);
		if (!fSrcFolder.isDirectory()) {
			getUserInterface().writeErrorMessage("指定的源文件夹不存在或不是目录");
			return;
		}
		if (StringUtils.isBlank(targetFolder)) {
			getUserInterface().writeErrorMessage("请选择目标文件夹");
			return;
		}
		File fTargetFolder = new File(targetFolder);
		if (!fTargetFolder.isDirectory()) {
			if (!fTargetFolder.mkdirs()) {
				getUserInterface().writeErrorMessage("指定的目标文件夹不存在且创建失败");
				return;
			}
		}
		String includeFilters = getParameter("includeFilters").getValue();
		String excludeFilters = getParameter("excludeFilters").getValue();
		List<String> lIncludeFilters = getFiltersAsList(includeFilters);
		List<String> lExcludeFilters = getFiltersAsList(excludeFilters);
		try {
			searchFile(actionName, fSrcFolder, fTargetFolder, lIncludeFilters, lExcludeFilters);
		} catch (Exception e) {
			e.printStackTrace();
			getUserInterface().writeErrorMessage("发生异常：" + e.getClass() + ": " + e.getMessage());
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

	/** 工具本身提供的参数校验方法，如果返回false则不再执行操作 */
	protected abstract boolean checkParameters();

	/** 工具本身对单个文件处理的方法 */
	protected abstract void performActionOnFile(String actionName, File inputFile, File outputFile) throws Exception;

	protected List<String> getFiltersAsList(String filters) {
		String[] arr = filters.replace("\r\n", "\n").split("\n|;");
		List<String> lFilters = new ArrayList<>(arr.length);
		for (String str : arr) {
			if (StringUtils.isNotBlank(str)) {
				lFilters.add(str.trim());
			}
		}
		return lFilters;
	}

	protected void searchFile(String actionName, File fSrcFolder, File fTargetFolder, List<String> lIncludeFilters,
			List<String> lExcludeFilters) throws Exception {
		searchFileRec(actionName, fSrcFolder, fSrcFolder, fTargetFolder, lIncludeFilters, lExcludeFilters);
	}

	private void searchFileRec(String actionName, File f, File fSrcFolder, File fTargetFolder,
			List<String> lIncludeFilters, List<String> lExcludeFilters) throws Exception {
		// 过滤
		String relativePath = getRelativePath(f, fSrcFolder);
		boolean isDir = f.isDirectory();
		boolean isMatch = filter(relativePath, isDir, lIncludeFilters, lExcludeFilters);
		if (!isMatch) {
			getUserInterface().writeWarnMessage("已排除" + (isDir ? "目录" : "文件") + "：" + relativePath);
			return;
		}
		// 处理
		if (isDir) {
			File[] subList = f.listFiles();
			for (File sub : subList) {
				searchFileRec(actionName, sub, fSrcFolder, fTargetFolder, lIncludeFilters, lExcludeFilters);
			}
		} else {
			File outputFile = new File(fTargetFolder, relativePath);
			performActionOnFile(actionName, f, outputFile);
		}
	}

	private String getRelativePath(File f, File fSrcFolder) throws Exception {
		String srcPath = fSrcFolder.getCanonicalPath().replace('\\', '/');
		String path = f.getCanonicalPath().replace('\\', '/');
		return StringUtils.stripStart(path.substring(srcPath.length()), "/");
	}

	private boolean filter(String relativePath, boolean isDir, List<String> lIncludeFilters,
			List<String> lExcludeFilters) throws Exception {
		if (StringUtils.isBlank(relativePath)) {
			return true;
		}
		boolean isMatch = true;
		AntPathMatcher matcher = new AntPathMatcher();
		// 目录下面可能含符合的文件，因此目录不做包含校验，只做排除校验（排除目录与排除目录下所有文件等效）
		if (!lIncludeFilters.isEmpty() && !isDir) {
			isMatch = false;
			for (String s : lIncludeFilters) {
				if (s.startsWith("/")) {
					s = StringUtils.stripStart(s, "/");
				} else {
					s = "**/" + s;
				}
				if (matcher.match(s, relativePath)) {
					isMatch = true;
					break;
				}
			}
		}
		if (isMatch) {
			for (String s : lExcludeFilters) {
				if (s.startsWith("/")) {
					s = StringUtils.stripStart(s, "/");
				} else {
					s = "**/" + s;
				}
				if (matcher.match(s, relativePath)) {
					isMatch = false;
					break;
				}
			}
		}
		return isMatch;
	}

}
