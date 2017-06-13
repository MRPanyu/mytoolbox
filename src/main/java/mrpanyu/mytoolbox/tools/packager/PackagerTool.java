package mrpanyu.mytoolbox.tools.packager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;

import mrpanyu.mytoolbox.framework.api.Action;
import mrpanyu.mytoolbox.framework.api.Parameter;
import mrpanyu.mytoolbox.framework.api.ParameterType;
import mrpanyu.mytoolbox.framework.api.Tool;

public class PackagerTool extends Tool {

	private static final String TYPE_WEBAPP = "WEB工程";
	private static final String TYPE_NORMAL = "普通Java工程";

	private Properties config;

	@Override
	public void initialize() {
		// 初始化配置
		setName("00_packager");
		setDisplayName("增量打包");
		setDescription("");
		setEnableProfile(true);
		// 初始化参数
		Parameter param = new Parameter("type", "打包类型");
		param.setDescription("设置打包类型，可打包WEB工程或普通Java工程");
		param.setType(ParameterType.ENUMERATION);
		param.setEnumerationValues(Arrays.asList(TYPE_WEBAPP, TYPE_NORMAL));
		addParameter(param);
		param = new Parameter("basePath", "工程路径");
		param.setType(ParameterType.DIRECTORY);
		addParameter(param);
		param = new Parameter("srcFolders", "源码文件夹");
		addParameter(param);
		param = new Parameter("targetFolder", "输出文件夹");
		param.setDescription("WEB工程该参数填写web根目录，Java普通工程该参数填写class文件输出根目录");
		addParameter(param);
		param = new Parameter("listFile", "文件清单");
		param.setType(ParameterType.FILE);
		addParameter(param);
		param = new Parameter("targetListFile", "输出文件清单");
		addParameter(param);
		param = new Parameter("targetZipFile", "输出压缩包");
		addParameter(param);
		// 初始化动作
		addAction(new Action("package", "打包"));
		// 初始化配置
		config = new Properties();
		try {
			config.load(this.getClass().getResourceAsStream("config.properties"));
		} catch (IOException e1) {
			throw new RuntimeException(e1);
		}
	}

	@Override
	public void performAction(String actionName) {
		getUserInterface().clearMessages();
		if ("package".equals(actionName)) {
			doPackage();
		}
	}

	@Override
	public void onParameterValueChange(String name) {
		if ("basePath".equals(name)) {
			onBasePathChange();
		} else if ("listFile".equals(name)) {
			onListFileChange();
		}
	}

	private void onBasePathChange() {
		String basePath = getParameter("basePath").getValue();
		File basePathFile = new File(basePath);
		if (basePathFile.isDirectory()) {
			File[] files = basePathFile.listFiles();
			for (File file : files) {
				if (file.isDirectory()) {
					File webxml = new File(file, "WEB-INF/web.xml");
					if (webxml.isFile()) {
						getParameter("targetFolder").setValue(file.getName());
						getUserInterface().refreshParameterValue("targetFolder");
						break;
					}
				}
			}

			String[] guessSrcFolders = config.getProperty("guessSrcFolders").split(",");
			List<String> srcFolders = new ArrayList<String>();
			for (String guessSrcFolder : guessSrcFolders) {
				guessSrcFolder = guessSrcFolder.trim();
				if (new File(basePathFile, guessSrcFolder).isDirectory()) {
					boolean subFolderIsSrc = false;
					for (String srcFolder : srcFolders) {
						if (srcFolder.startsWith(guessSrcFolder + "/")) {
							subFolderIsSrc = true;
							break;
						}
					}
					if (!subFolderIsSrc) {
						srcFolders.add(guessSrcFolder);
					}
				}
			}
			getParameter("srcFolders").setValue(StringUtils.join(srcFolders, ','));
			getUserInterface().refreshParameterValue("srcFolders");
		}
	}

	private void onListFileChange() {
		String listFile = getParameter("listFile").getValue();
		if (StringUtils.isNotBlank(listFile)) {
			String str = StringUtils.substringBeforeLast(listFile, ".");
			if (StringUtils.isNotBlank(str)) {
				getParameter("targetListFile").setValue(str + ".out.txt");
				getUserInterface().refreshParameterValue("targetListFile");
				getParameter("targetZipFile").setValue(str + ".zip");
				getUserInterface().refreshParameterValue("targetZipFile");
			}
		}
	}

	private void doPackage() {
		String type = getParameter("type").getValue();
		String basePath = getParameter("basePath").getValue();
		String srcFolders = getParameter("srcFolders").getValue();
		String targetFolder = getParameter("targetFolder").getValue();
		String listFile = getParameter("listFile").getValue();
		String targetListFile = getParameter("targetListFile").getValue();
		String targetZipFile = getParameter("targetZipFile").getValue();
		boolean check = true;
		if (StringUtils.isBlank(basePath)) {
			getUserInterface().writeErrorMessage("请填写工程路径！");
			check = false;
		}
		if (StringUtils.isBlank(srcFolders)) {
			getUserInterface().writeErrorMessage("请填写源码文件夹！");
			check = false;
		}
		if (StringUtils.isBlank(targetFolder)) {
			getUserInterface().writeErrorMessage("请填写输出文件夹！");
			check = false;
		}
		if (StringUtils.isBlank(listFile)) {
			getUserInterface().writeErrorMessage("请填写文件清单！");
			check = false;
		}
		if (StringUtils.isBlank(targetListFile)) {
			getUserInterface().writeErrorMessage("请填写输出文件清单！");
			check = false;
		}
		if (StringUtils.isBlank(targetZipFile)) {
			getUserInterface().writeErrorMessage("请填写输出压缩包！");
			check = false;
		}
		if (check) {
			String[] arrSrcFolders = srcFolders.split(",");
			final List<String> srcFoldersList = new ArrayList<String>();
			for (String srcFolder : arrSrcFolders) {
				if (StringUtils.isNotBlank(srcFolder)) {
					srcFoldersList.add(srcFolder.trim());
				}
			}
			if (TYPE_WEBAPP.equals(type)) {
				WebAppPackger packager = new WebAppPackger();
				packager.setBasePath(basePath);
				packager.setSrcFolders(srcFoldersList);
				packager.setWebContentFolder(targetFolder);
				packager.setListFile(new File(listFile));
				packager.setTargetListFile(new File(targetListFile));
				packager.setTargetZipFile(new File(targetZipFile));
				packager.setUserInterface(getUserInterface());
				packager.doPackage();
			} else if (TYPE_NORMAL.equals(type)) {
				NormalAppPackger packager = new NormalAppPackger();
				packager.setBasePath(basePath);
				packager.setSrcFolders(srcFoldersList);
				packager.setClassOutputFolder(targetFolder);
				packager.setListFile(new File(listFile));
				packager.setTargetListFile(new File(targetListFile));
				packager.setTargetZipFile(new File(targetZipFile));
				packager.setUserInterface(getUserInterface());
				packager.doPackage();
			}
		}
	}

}
