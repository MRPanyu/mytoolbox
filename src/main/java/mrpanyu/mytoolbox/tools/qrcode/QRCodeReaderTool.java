package mrpanyu.mytoolbox.tools.qrcode;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;

import javax.imageio.ImageIO;

import org.apache.commons.lang.StringUtils;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import mrpanyu.mytoolbox.framework.api.Action;
import mrpanyu.mytoolbox.framework.api.Parameter;
import mrpanyu.mytoolbox.framework.api.ParameterType;
import mrpanyu.mytoolbox.framework.api.Tool;

public class QRCodeReaderTool extends Tool {

	public static final String SOURCE_CLIPBOARD = "剪贴板";
	public static final String SOURCE_FILE = "文件";

	@Override
	public void initialize() {
		// 初始化配置
		setName("0501_qrcodereader");
		setDisplayName("二维码：二维码解析");
		setDescription("二维码解析工具");
		setEnableProfile(false);
		// 初始化参数
		Parameter param = new Parameter("source", "解析来源");
		param.setDescription("解析来源");
		param.setType(ParameterType.ENUMERATION);
		param.setEnumerationValues(Arrays.asList(SOURCE_CLIPBOARD, SOURCE_FILE));
		addParameter(param);
		param = new Parameter("file", "来源文件");
		param.setDescription("来源文件");
		param.setType(ParameterType.FILE);
		addParameter(param);
		// 初始化动作
		addAction(new Action("read", "解析"));
	}

	@Override
	public void performAction(String actionName) {
		if ("read".equals(actionName)) {
			getUserInterface().clearMessages();
			String source = getParameter("source").getValue();
			BufferedImage img = null;
			if (SOURCE_CLIPBOARD.equals(source)) {
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				Transferable trans = clipboard.getContents(null);
				if (trans == null) {
					getUserInterface().writeErrorMessage("剪贴板上无内容");
					return;
				}
				try {
					img = (BufferedImage) trans.getTransferData(DataFlavor.imageFlavor);
				} catch (Exception e) {
					e.printStackTrace();
					getUserInterface().writeErrorMessage("剪贴板上当前内容不是图片信息");
					return;
				}
			} else if (SOURCE_FILE.equals(source)) {
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
				} catch (Exception e) {
					e.printStackTrace();
					getUserInterface().writeErrorMessage("无法读取来源文件，请检查是否为一般格式的图片文件");
					return;
				}
			}
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
	}

	@Override
	public void onParameterValueChange(String name) {
	}

}
