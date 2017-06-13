package mrpanyu.mytoolbox.tools.packager;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;

import mrpanyu.mytoolbox.framework.api.EmptyUserInterface;
import mrpanyu.mytoolbox.framework.api.UserInterface;

public class WebAppPackger {

	private UserInterface userInterface = new EmptyUserInterface();

	private String basePath;
	private List<String> srcFolders;
	private String webContentFolder;

	private File listFile;
	private File targetListFile;
	private File targetZipFile;

	private class JavaOrResourceFile implements Comparable<JavaOrResourceFile> {
		String path;
		String pathWithoutSrcFolder;

		public int compareTo(JavaOrResourceFile o) {
			return path.compareTo(o.path);
		}
	}

	public void doPackage() {
		// java源码文件集合
		Set<JavaOrResourceFile> javaFiles = new TreeSet<JavaOrResourceFile>();
		// 源码目录下的资源文件集合
		Set<JavaOrResourceFile> resourceFiles = new TreeSet<JavaOrResourceFile>();
		// web工程目录下的其他打包文件
		Set<String> otherFiles = new TreeSet<String>();
		// web工程目录/WEB-INF/classes下的class文件
		Set<String> classFiles = new TreeSet<String>();
		// web工程目录/WEB-INF/classes下的非class文件
		Set<String> resourceInClassesFiles = new TreeSet<String>();

		try {
			checkParams();
			userInterface.writeInfoMessage("开始打包操作...");
			/* 1. 读取list文件 */
			List<String> data = readList();
			/*
			 * 2. 解析list数据，区分出java源码，源码目录下的资源文件和其他文件 ， 检查文件是否存在
			 */
			analyzeData(data, javaFiles, resourceFiles, otherFiles, classFiles, resourceInClassesFiles);
			/* 3. 根据java源码和资源文件，获取对应的WEB-INF/classes下的文件，检查文件是否存在 */
			findClassFiles(javaFiles, classFiles);
			findResourceInClassesFiles(resourceFiles, resourceInClassesFiles);
			/* 4. 扫描WEB-INF/classes文件，查找可能的内部类文件，增加到列表中 */
			findInnerClassFiles(classFiles);
			/* 5. 校验class文件中是否有编译异常 */
			checkCompilationError(classFiles);
			/* 6. 生成目标列表文件 */
			generateTargetList(javaFiles, resourceFiles, otherFiles, classFiles, resourceInClassesFiles);
			/* 7. 生成目标jar文件 */
			generatePackage(javaFiles, resourceFiles, otherFiles, classFiles, resourceInClassesFiles);
			userInterface.writeInfoMessage("打包操作完成！");
		} catch (Exception e) {
			e.printStackTrace();
			userInterface.writeErrorMessage(e.getMessage());
		}
	}

	private void checkParams() throws Exception {
		if (StringUtils.isBlank(basePath)) {
			throw new Exception("项目路径basePath不能为空");
		}
		if (StringUtils.isBlank(webContentFolder)) {
			throw new Exception("web目录不能为空");
		}
		if (listFile == null || !listFile.isFile()) {
			throw new RuntimeException("找不到文件清单文件：" + listFile);
		}
		if (targetListFile == null) {
			throw new RuntimeException("输出文件清单文件不能为空");
		}
		if (targetZipFile == null) {
			throw new RuntimeException("输出压缩包文件不能为空");
		}
		// 规范源码目录名称
		List<String> tempSrcFolders = this.srcFolders;
		if (tempSrcFolders == null) {
			tempSrcFolders = Collections.emptyList();
		}
		srcFolders = new ArrayList<String>(tempSrcFolders.size());
		for (String srcFolder : tempSrcFolders) {
			srcFolder = StringUtils.strip(srcFolder.trim().replace('\\', '/'), "/");
			srcFolders.add(srcFolder);
		}
		// 规范web目录名称
		webContentFolder = StringUtils.strip(webContentFolder.trim().replace('\\', '/'), "/");
	}

	/**
	 * 读取list。读取时去除空行，每行进行trim，所有路径分割用"/"，去掉最前面的斜杠和可能有的工程目录名称
	 */
	private List<String> readList() throws Exception {
		List<String> rawData = FileUtils.readLines(listFile);
		List<String> data = new ArrayList<String>();
		// 工程目录名称
		String projectFolderName = StringUtils
				.substringAfterLast(StringUtils.stripEnd(basePath.replace('\\', '/'), "/"), "/");
		for (String line : rawData) {
			line = line.trim();
			if (StringUtils.isNotBlank(line) && !StringUtils.startsWith(line, "#")) {
				// 统一目录分割为"/"
				line = line.replace('\\', '/');
				// 去掉最前面的"/"
				line = StringUtils.stripStart(line, "/");
				// 如果最前面有工程名称，也去掉
				if (line.startsWith(projectFolderName + "/")) {
					line = line.substring(projectFolderName.length() + 1);
				}
				// 去掉工程名称后再去掉"/"
				line = StringUtils.stripStart(line, "/");
				// 中间可能存在的多个"/"统一替换成单个
				line = line.replaceAll("[/]+", "/");
				data.add(line);
			}
		}
		return data;
	}

	/**
	 * 分析数据，区分出java，资源文件和web目录下文件
	 */
	private void analyzeData(List<String> data, Set<JavaOrResourceFile> javaFiles,
			Set<JavaOrResourceFile> resourceFiles, Set<String> otherFiles, Set<String> classFiles,
			Set<String> resourceInClassesFiles) throws Exception {
		for (String line : data) {
			// 判断是否为java或资源文件
			boolean isSrc = false;
			for (String srcFolder : srcFolders) {
				if (line.startsWith(srcFolder + "/")) {
					JavaOrResourceFile jrf = new JavaOrResourceFile();
					jrf.path = line;
					jrf.pathWithoutSrcFolder = line.substring(srcFolder.length() + 1);
					if (line.endsWith(".java")) {
						javaFiles.add(jrf);
					} else {
						resourceFiles.add(jrf);
					}
					isSrc = true;
					break;
				}
			}
			// 判断是否为web目录文件
			if (!isSrc) {
				if (line.startsWith(webContentFolder + "/WEB-INF/classes/")) {
					if (line.endsWith(".class")) {
						classFiles.add(line);
					} else {
						resourceInClassesFiles.add(line);
					}
				} else {
					otherFiles.add(line);
				}
			}
		}
		// 校验文件是否存在
		for (JavaOrResourceFile jrf : new TreeSet<JavaOrResourceFile>(javaFiles)) {
			File file = new File(basePath + "/" + jrf.path);
			if (!file.isFile()) {
				userInterface.writeErrorMessage("源码文件不存在或不是文件：" + jrf.path);
				javaFiles.remove(jrf);
			}
		}
		for (JavaOrResourceFile jrf : new TreeSet<JavaOrResourceFile>(resourceFiles)) {
			File file = new File(basePath + "/" + jrf.path);
			if (!file.exists()) {
				userInterface.writeErrorMessage("资源文件不存在：" + jrf.path);
				resourceFiles.remove(jrf);
			} else if (!file.isFile()) {
				userInterface.writeWarnMessage("指定的资源文件是一个目录，已被忽略：" + jrf.path);
				resourceFiles.remove(jrf);
			}
		}
		for (String line : new TreeSet<String>(otherFiles)) {
			File file = new File(basePath + "/" + line);
			if (!file.exists()) {
				userInterface.writeErrorMessage("文件不存在：" + line);
				otherFiles.remove(line);
			} else if (!file.isFile()) {
				userInterface.writeWarnMessage("指定的文件是一个目录，已被忽略：" + line);
				otherFiles.remove(line);
			}
		}
	}

	/**
	 * 根据java文件找class文件
	 */
	private void findClassFiles(Set<JavaOrResourceFile> javaFiles, Set<String> classFiles) throws Exception {
		for (JavaOrResourceFile jrf : javaFiles) {
			String classFile = webContentFolder + "/WEB-INF/classes/"
					+ jrf.pathWithoutSrcFolder.substring(0, jrf.pathWithoutSrcFolder.length() - ".java".length())
					+ ".class";
			classFiles.add(classFile);
		}
		for (String classFile : classFiles) {
			File file = new File(basePath + "/" + classFile);
			if (!file.isFile()) {
				userInterface.writeErrorMessage("class文件不存在：" + classFile);
			}
		}
	}

	/**
	 * 根据资源文件找WEB-INF/classes下的资源文件
	 */
	private void findResourceInClassesFiles(Set<JavaOrResourceFile> resourceFiles, Set<String> resourceInClassesFiles)
			throws Exception {
		for (JavaOrResourceFile jrf : resourceFiles) {
			String resFile = webContentFolder + "/WEB-INF/classes/" + jrf.pathWithoutSrcFolder;
			resourceInClassesFiles.add(resFile);
		}
		for (String resFile : resourceInClassesFiles) {
			File file = new File(basePath + "/" + resFile);
			if (!file.isFile()) {
				userInterface.writeErrorMessage("生成的资源文件不存在：" + resFile);
			}
		}
	}

	/**
	 * 根据class文件找内部类
	 */
	private void findInnerClassFiles(Set<String> classFiles) throws Exception {
		Set<String> rawClassFiles = new TreeSet<String>(classFiles);
		for (String classFile : rawClassFiles) {
			File classFileFile = new File(basePath + "/" + classFile);
			if (classFileFile.isFile()) {
				String classShortName = classFileFile.getName();
				classShortName = classShortName.substring(0, classShortName.length() - ".class".length());
				File folder = classFileFile.getParentFile();
				File[] allFilesInFolder = folder.listFiles();
				for (File file : allFilesInFolder) {
					String fileName = file.getName();
					if (fileName.startsWith(classShortName + "$") && fileName.endsWith(".class")) {
						String innerClass = StringUtils.substringBeforeLast(classFile, "/") + "/" + fileName;
						classFiles.add(innerClass);
						userInterface.writeInfoMessage("发现内部类文件：" + innerClass);
					}
				}
			}
		}
	}

	/**
	 * 检查class文件中是否存在编译问题
	 */
	private void checkCompilationError(Set<String> classFiles) throws Exception {
		byte[] mark = "Unresolved compilation problem".getBytes();
		for (String classFile : classFiles) {
			File file = new File(basePath + "/" + classFile);
			if (file.isFile()) {
				byte[] data = FileUtils.readFileToByteArray(file);
				for (int i = 0; i < data.length - mark.length; i++) {
					boolean found = true;
					for (int j = 0; j < mark.length; j++) {
						if (data[i + j] != mark[j]) {
							found = false;
							break;
						}
					}
					if (found) {
						userInterface.writeErrorMessage("class文件有编译问题：" + classFile);
						break;
					}
				}
			}
		}
	}

	/**
	 * 生成文件列表
	 */
	private void generateTargetList(Set<JavaOrResourceFile> javaFiles, Set<JavaOrResourceFile> resourceFiles,
			Set<String> otherFiles, Set<String> classFiles, Set<String> resourceInClassesFiles) throws Exception {
		List<String> lines = new ArrayList<String>();
		for (JavaOrResourceFile jrf : javaFiles) {
			lines.add(jrf.path);
		}
		lines.add("");
		lines.addAll(classFiles);
		lines.add("");
		for (JavaOrResourceFile jrf : resourceFiles) {
			lines.add(jrf.path);
		}
		lines.add("");
		lines.addAll(resourceInClassesFiles);
		lines.add("");
		lines.addAll(otherFiles);
		targetListFile.delete();
		targetListFile.getParentFile().mkdirs();
		FileUtils.writeLines(targetListFile, lines);
	}

	/**
	 * 文件打包
	 */
	private void generatePackage(Set<JavaOrResourceFile> javaFiles, Set<JavaOrResourceFile> resourceFiles,
			Set<String> otherFiles, Set<String> classFiles, Set<String> resourceInClassesFiles) throws Exception {
		targetZipFile.delete();
		targetZipFile.getParentFile().mkdirs();
		ZipOutputStream zipOut = new ZipOutputStream(targetZipFile);
		try {
			// 获取所有文件
			List<String> allFiles = new ArrayList<String>();
			for (JavaOrResourceFile jrf : javaFiles) {
				allFiles.add(jrf.path);
			}
			for (JavaOrResourceFile jrf : resourceFiles) {
				allFiles.add(jrf.path);
			}
			allFiles.addAll(classFiles);
			allFiles.addAll(resourceInClassesFiles);
			allFiles.addAll(otherFiles);
			// 依次加入包
			for (String file : allFiles) {
				File f = new File(basePath + "/" + file);
				if (f.isFile()) {
					zipOut.putNextEntry(new ZipEntry(file));
					IOUtils.copy(new FileInputStream(f), zipOut);
					zipOut.closeEntry();
				}
			}
		} finally {
			zipOut.close();
		}
	}

	public String getBasePath() {
		return basePath;
	}

	public void setBasePath(String basePath) {
		this.basePath = basePath == null ? null : basePath.trim();
	}

	public List<String> getSrcFolders() {
		return srcFolders;
	}

	public void setSrcFolders(List<String> srcFolders) {
		this.srcFolders = srcFolders;
	}

	public String getWebContentFolder() {
		return webContentFolder;
	}

	public void setWebContentFolder(String webContentFolder) {
		this.webContentFolder = webContentFolder;
	}

	public File getListFile() {
		return listFile;
	}

	public void setListFile(File listFile) {
		this.listFile = listFile;
	}

	public File getTargetListFile() {
		return targetListFile;
	}

	public void setTargetListFile(File targetListFile) {
		this.targetListFile = targetListFile;
	}

	public File getTargetZipFile() {
		return targetZipFile;
	}

	public void setTargetZipFile(File targetZipFile) {
		this.targetZipFile = targetZipFile;
	}

	public UserInterface getUserInterface() {
		return userInterface;
	}

	public void setUserInterface(UserInterface userInterface) {
		this.userInterface = userInterface;
	}

}
