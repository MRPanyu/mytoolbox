package mrpanyu.mytoolbox.tools.textfile;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import mrpanyu.mytoolbox.framework.api.Action;

public class TextFileLineCountTool extends AbstractTextFileTool {

	private long fileCount = 0L;
	private long lineCount = 0L;

	public TextFileLineCountTool() {
		this.setDoOutput(false);
	}

	@Override
	public void initialize() {
		super.initialize();
		setName("0104_textFileLineCount");
		setDisplayName("文本处理：统计行数");
		// 参数
		getParameter("includeFilters").setValue("*.java;*.jsp;*.html;*.js;*.css\n*.xml;*.properties;*.yaml\n*.sql");
		getParameter("excludeFilters").setValue("**/lib/**/*.js;**/lib/**/*.css");
		// 操作
		addAction(new Action("count", "统计行数"));
	}

	@Override
	protected void performActionBefore(String actionName) throws Exception {
		super.performActionBefore(actionName);
		this.fileCount = 0L;
		this.lineCount = 0L;
		getUserInterface().clearMessages();
	}

	@Override
	protected void performActionOnFile(String actionName, File inputFile, File outputFile) throws Exception {
		this.fileCount++;
		InputStreamReader reader = new InputStreamReader(new FileInputStream(inputFile), "UTF-8");
		try {
			char[] cbuf = new char[4096];
			int c = reader.read(cbuf);
			while (c > 0) {
				for (int i = 0; i < c; i++) {
					if (cbuf[i] == '\n') {
						this.lineCount++;
					}
				}
				c = reader.read(cbuf);
			}
		} finally {
			reader.close();
		}
	}

	@Override
	protected void performActionAfter(String actionName) throws Exception {
		super.performActionAfter(actionName);
		getUserInterface().writeInfoMessage("文件数：" + fileCount);
		getUserInterface().writeInfoMessage("总行数：" + lineCount);
	}

}
