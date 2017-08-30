package mrpanyu.mytoolbox.tools.qrcode;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.apache.commons.lang.StringUtils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import mrpanyu.mytoolbox.framework.api.Action;
import mrpanyu.mytoolbox.framework.api.Parameter;
import mrpanyu.mytoolbox.framework.api.ParameterType;
import mrpanyu.mytoolbox.framework.api.Tool;

public class QRCodeWriterTool extends Tool {

	@Override
	public void initialize() {
		// 初始化配置
		setName("08_qrcodewriter");
		setDisplayName("二维码生成");
		setDescription("二维码生成工具");
		setEnableProfile(false);
		// 初始化参数
		Parameter param = new Parameter("text", "文本内容");
		param.setDescription("生成二维码的原始文本内容");
		param.setType(ParameterType.MULTILINE_TEXT);
		addParameter(param);
		param = new Parameter("size", "图片尺寸");
		param.setDescription("图片尺寸");
		param.setValue("300");
		addParameter(param);
		param = new Parameter("file", "输出文件");
		param.setDescription("输出文件");
		param.setType(ParameterType.FILE);
		addParameter(param);
		// 初始化动作
		addAction(new Action("write", "生成"));
		addAction(new Action("preview", "预览"));
	}

	@Override
	public void performAction(String actionName) {
		getUserInterface().clearMessages();
		String text = getParameter("text").getValue();
		int size = 0;
		String file = getParameter("file").getValue();
		if ("write".equals(actionName) || "preview".equals(actionName)) {
			if (StringUtils.isBlank(text)) {
				getUserInterface().writeErrorMessage("请录入文本内容");
				return;
			}
			try {
				size = Integer.parseInt(getParameter("size").getValue());
				if (size <= 0) {
					getUserInterface().writeErrorMessage("图片尺寸请输入表示像素宽度的正整数");
					return;
				}
			} catch (Exception e) {
				getUserInterface().writeErrorMessage("图片尺寸请输入表示像素宽度的正整数");
				return;
			}
		}
		if ("write".equals(actionName)) {
			if (StringUtils.isBlank(file)) {
				getUserInterface().writeErrorMessage("请指定输出文件");
				return;
			}
			File f = new File(file);
			try {
				writeQRCode(text, size, f);
				getUserInterface().writeInfoMessage("已成功生成二维码图片");
			} catch (Exception e) {
				getUserInterface().writeErrorMessage("二维码生成失败：" + e.getMessage());
				return;
			}
		} else if ("preview".equals(actionName)) {
			try {
				File f = File.createTempFile("mytoolbox_qrcodewriter_", "");
				writeQRCode(text, size, f);
				String html = "<div><img src='file:///" + f.getAbsolutePath() + "'></img></div>";
				getUserInterface().writeHtmlMessage(html);
			} catch (Exception e) {
				getUserInterface().writeErrorMessage("二维码生成失败：" + e.getMessage());
				return;
			}
		}
	}

	private void writeQRCode(String text, int size, File file) throws Exception {
		QRCodeWriter writer = new QRCodeWriter();
		BitMatrix bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, size, size);
		BufferedImage img = MatrixToImageWriter.toBufferedImage(bitMatrix);
		ImageIO.write(img, "PNG", file);
	}

	@Override
	public void onParameterValueChange(String name) {
	}

}
