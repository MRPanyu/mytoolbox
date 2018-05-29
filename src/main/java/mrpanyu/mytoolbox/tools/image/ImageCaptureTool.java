package mrpanyu.mytoolbox.tools.image;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;

import javax.imageio.ImageIO;

import org.apache.commons.lang.StringUtils;

import mrpanyu.mytoolbox.framework.MyToolBox;
import mrpanyu.mytoolbox.framework.api.Action;
import mrpanyu.mytoolbox.framework.api.Parameter;
import mrpanyu.mytoolbox.framework.api.ParameterType;
import mrpanyu.mytoolbox.framework.api.Tool;
import mrpanyu.mytoolbox.framework.utils.ScreenCapture;
import mrpanyu.mytoolbox.framework.utils.ScreenCapture.ScreenCaptureCallback;
import mrpanyu.mytoolbox.framework.utils.TransferableImage;

public class ImageCaptureTool extends Tool implements ClipboardOwner {

	@Override
	public void initialize() {
		// 初始化配置
		setName("0601_imageCapture");
		setDisplayName("图像：截图工具");
		setDescription("简单的截图工具");
		setEnableProfile(false);
		// 初始化参数
		Parameter param = new Parameter("saveType", "保存方式");
		param.setDescription("截图保存方式");
		param.setType(ParameterType.ENUMERATION);
		param.setEnumerationValues(Arrays.asList("剪贴板", "文件"));
		param.setValue("剪贴板");
		addParameter(param);

		param = new Parameter("file", "输出文件");
		param.setDescription("输出到的文件");
		param.setType(ParameterType.FILE);
		addParameter(param);

		// 初始化动作
		addAction(new Action("capture", "截图", "截图至剪贴板或文件"));
	}

	@Override
	public void performAction(String actionName) {
		if ("capture".equals(actionName)) {
			getUserInterface().clearMessages();
			final String saveType = getParameter("saveType").getValue();
			final String file = getParameter("file").getValue();
			if ("文件".equals(saveType)) {
				if (StringUtils.isBlank(file)) {
					getUserInterface().writeErrorMessage("请指定保存文件名!");
					return;
				}
			}
			// 截图前隐藏主窗口
			MyToolBox.instance.setVisible(false);
			try {
				Thread.sleep(300);
			} catch (InterruptedException e1) {
			}
			ScreenCapture.capture(new ScreenCaptureCallback() {
				@Override
				public void onCaptured(BufferedImage img) {
					MyToolBox.instance.setVisible(true);
					try {
						if ("文件".equals(saveType)) {
							File f = new File(file);
							f.getParentFile().mkdirs();
							ImageIO.write(img, "PNG", f);
							getUserInterface().writeInfoMessage("已保存：" + file);
						} else {
							Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
							clipboard.setContents(new TransferableImage(img), ImageCaptureTool.this);
							getUserInterface().writeInfoMessage("已截图至剪贴板。");
						}
					} catch (Exception e) {
						e.printStackTrace();
						getUserInterface().writeErrorMessage(e.getClass().getName() + ": " + e.getMessage());
					}
				}

				@Override
				public void onCancel() {
					MyToolBox.instance.setVisible(true);
				}
			});
		}
	}

	@Override
	public void onParameterValueChange(String name) {
	}

	@Override
	public void lostOwnership(Clipboard clipboard, Transferable contents) {
	}

}
