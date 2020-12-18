package mrpanyu.mytoolbox.tools.textfile;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import mrpanyu.mytoolbox.framework.api.Action;
import mrpanyu.mytoolbox.framework.api.Parameter;
import mrpanyu.mytoolbox.framework.api.ParameterType;
import mrpanyu.mytoolbox.framework.api.Tool;
import mrpanyu.mytoolbox.framework.api.UserInterface;

public class SplitJavaAndResourceFileTool extends Tool {

	@Override
	public void initialize() {
		setName("0106_splitJavaAndResource");
		setDisplayName("文件处理：分离java/resource文件");
		setDescription("用于将传统工程转化为Maven工程的过程中，将一个目录下混排的java和resource文件分拆开");
		// 参数
		Parameter param = new Parameter("projectFolder", "Maven工程目录");
		param.setDescription("Maven工程目录，即放pom.xml文件的目录");
		param.setType(ParameterType.DIRECTORY);
		addParameter(param);
		param = new Parameter("sourceFolder", "来源目录");
		param.setDescription("原始工程的源码目录，即所有java和resource文件的所在目录");
		param.setType(ParameterType.DIRECTORY);
		addParameter(param);
		// 操作
		addAction(new Action("split", "拆分"));
	}

	@Override
	public void performAction(String actionName) {
		UserInterface ui = getUserInterface();
		ui.clearMessages();
		if ("split".equals(actionName)) {
			String projectFolder = getParameter("projectFolder").getValue();
			String sourceFolder = getParameter("sourceFolder").getValue();
			if (StringUtils.isBlank(projectFolder)) {
				ui.writeErrorMessage("Maven工程目录不能为空");
				return;
			}
			if (StringUtils.isBlank(sourceFolder)) {
				ui.writeErrorMessage("来源目录不能为空");
				return;
			}
			File projectDir = new File(projectFolder);
			File sourceDir = new File(sourceFolder);
			if (!projectDir.isDirectory()) {
				ui.writeErrorMessage("Maven工程目录不存在");
				return;
			}
			if (!sourceDir.isDirectory()) {
				ui.writeErrorMessage("来源目录不存在");
				return;
			}
			File javaDir = new File(projectDir, "src/main/java");
			File resourceDir = new File(projectDir, "src/main/resources");
			try {
				split(sourceDir, javaDir, resourceDir);
				ui.writeInfoMessage("-- 已完成 --");
			} catch (Exception e) {
				ui.writeErrorMessage(e.getClass().getName() + ": " + e.getMessage());
			}
		}
	}

	private void split(File sourceDir, File javaDir, File resourceDir) throws Exception {
		for (File f : sourceDir.listFiles()) {
			if (f.isDirectory()) {
				String name = f.getName();
				File sourceDirSub = new File(sourceDir, name);
				File javaDirSub = new File(javaDir, name);
				File resourceDirSub = new File(resourceDir, name);
				split(sourceDirSub, javaDirSub, resourceDirSub);
			} else {
				String name = f.getName();
				File toDir = name.endsWith(".java") ? javaDir : resourceDir;
				toDir.mkdirs();
				getUserInterface().writeInfoMessage("Copy " + f.getAbsolutePath() + " to " + toDir);
				FileUtils.copyFileToDirectory(f, toDir);
			}
		}
	}

	@Override
	public void onParameterValueChange(String name) {
	}

}
