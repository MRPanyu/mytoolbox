package mrpanyu.mytoolbox.tools.textfile;

import java.io.File;

import org.apache.commons.io.FileUtils;

import mrpanyu.mytoolbox.framework.api.Action;

public class TextFileRemoveBomTool extends AbstractTextFileTool {

	@Override
	public void initialize() {
		super.initialize();
		setName("0103_textFileRemoveBom");
		setDisplayName("文本处理：去除BOM");
		// 操作
		addAction(new Action("removeBom", "去除BOM"));
	}

	@Override
	protected boolean checkParameters() {
		return true;
	}

	@Override
	protected void performActionOnFile(String actionName, File inputFile, File outputFile) throws Exception {
		if ("removeBom".equals(actionName)) {
			byte[] data = FileUtils.readFileToByteArray(inputFile);
			if (data.length >= 3) {
				if (data[0] == (byte) 0xEF && data[1] == (byte) 0xBB && data[2] == (byte) 0xBF) {
					getUserInterface().writeInfoMessage("处理文件：" + inputFile.getCanonicalPath());
					byte[] newData = new byte[data.length - 3];
					System.arraycopy(data, 3, newData, 0, newData.length);
					FileUtils.writeByteArrayToFile(outputFile, newData);
				}
			}
		}
	}

}
