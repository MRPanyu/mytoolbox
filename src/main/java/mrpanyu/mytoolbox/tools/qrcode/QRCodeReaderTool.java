package mrpanyu.mytoolbox.tools.qrcode;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.StringUtils;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import mrpanyu.mytoolbox.framework.MyToolBox;
import mrpanyu.mytoolbox.framework.api.Action;
import mrpanyu.mytoolbox.framework.api.Parameter;
import mrpanyu.mytoolbox.framework.api.ParameterType;
import mrpanyu.mytoolbox.framework.api.Tool;
import mrpanyu.mytoolbox.framework.utils.ScreenCapture;
import mrpanyu.mytoolbox.framework.utils.ScreenCapture.ScreenCaptureCallback;

public class QRCodeReaderTool extends Tool {

	@Override
	public void initialize() {
		// 初始化配置
		setName("0501_qrcodereader");
		setDisplayName("二维码：二维码解析");
		setDescription("二维码解析工具");
		setEnableProfile(false);
		// 初始化参数
		Parameter param = new Parameter("file", "图片文件");
		param.setDescription("来源文件");
		param.setType(ParameterType.FILE);
		addParameter(param);
		// 初始化动作
		addAction(new Action("capture", "截屏并解析", "截取屏幕并解析"));
		addAction(new Action("clipboard", "解析剪贴板图片", "解析剪贴板上的图片"));
		addAction(new Action("read", "解析文件", "解析图片文件"));
	}

	@Override
	public void performAction(String actionName) {
		getUserInterface().clearMessages();
		BufferedImage img = null;
		if ("clipboard".equals(actionName)) {
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			Transferable trans = clipboard.getContents(null);
			if (trans == null) {
				getUserInterface().writeErrorMessage("剪贴板上无内容");
				return;
			}
			try {
				img = (BufferedImage) trans.getTransferData(DataFlavor.imageFlavor);
				readQRCode(img);
			} catch (Exception e) {
				e.printStackTrace();
				getUserInterface().writeErrorMessage("剪贴板上当前内容不是图片信息");
				return;
			}
		} else if ("read".equals(actionName)) {
			String file = getParameter("file").getValue();
			if (StringUtils.isBlank(file)) {
				getUserInterface().writeErrorMessage("请指定来源文件");
				return;
			}
			File f = new File(file);
			if (!f.isFile()) {
				getUserInterface().writeErrorMessage("来源文件不正确");
				return;
			}
			try {
				img = ImageIO.read(f);
				readQRCode(img);
			} catch (Exception e) {
				e.printStackTrace();
				getUserInterface().writeErrorMessage("无法读取来源文件，请检查是否为一般格式的图片文件");
				return;
			}
		} else if ("capture".equals(actionName)) {
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
					readQRCode(img);
				}

				@Override
				public void onCancel() {
					MyToolBox.instance.setVisible(true);
				}
			});
		}
	}

	private void readQRCode(BufferedImage img) {
		try {
			BufferedImageLuminanceSource luminateSource = new BufferedImageLuminanceSource(img);
			QRCodeReader reader = new QRCodeReader();
			String text = reader.decode(new BinaryBitmap(new HybridBinarizer(luminateSource))).getText();
			getUserInterface().writeInfoMessage(text);
		} catch (Exception e) {
			e.printStackTrace();
			getUserInterface().writeErrorMessage("解析图片文件失败");
		}
	}

	@Override
	public void onParameterValueChange(String name) {
	}

}
