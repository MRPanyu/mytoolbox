package mrpanyu.mytoolbox.tools.image;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import mrpanyu.mytoolbox.framework.MyToolBoxFrame;
import mrpanyu.mytoolbox.framework.api.Action;
import mrpanyu.mytoolbox.framework.api.Parameter;
import mrpanyu.mytoolbox.framework.api.ParameterType;
import mrpanyu.mytoolbox.framework.api.Tool;
import mrpanyu.mytoolbox.framework.utils.ScreenCapture;
import mrpanyu.mytoolbox.framework.utils.ScreenCapture.ScreenCaptureCallback;

public class OcrTool extends Tool {

	protected Map<String, OcrProvider> providerMap = new LinkedHashMap<>();

	@Override
	public void initialize() {
		providerMap.put("百度", new BaiduOcrProvider());
		providerMap.put("百度通用票据(JSON)", new BaiduReceiptOcrProvider());
		providerMap.put("搜狗", new SougouOcrProvider());

		// 初始化配置
		setName("0602_ocr");
		setDisplayName("图像：在线OCR识别");
		setDescription("使用在线服务进行OCR识别");
		setEnableProfile(false);
		// 初始化参数
		Parameter param = new Parameter("provider", "OCR服务商");
		param.setDescription("OCR服务网络提供商");
		param.setType(ParameterType.ENUMERATION);
		param.setEnumerationValues(new ArrayList<>(providerMap.keySet()));
		param.setValue(param.getEnumerationValues().get(0));
		addParameter(param);

		param = new Parameter("inputFile", "输入图片");
		param.setDescription("输入的图片文件");
		param.setType(ParameterType.FILE);
		addParameter(param);

		param = new Parameter("outputFile", "输出文件");
		param.setDescription("输出到的文本文件");
		param.setType(ParameterType.FILE);
		addParameter(param);

		// 初始化动作
		addAction(new Action("read", "识别输入图片", "OCR识别输入图片"));
		addAction(new Action("capture", "截屏并识别", "截取屏幕并进行OCR识别"));
	}

	@Override
	public void performAction(String actionName) {
		getUserInterface().clearMessages();
		if ("read".equals(actionName)) {
			String inputFile = getParameter("inputFile").getValue();
			if (StringUtils.isBlank(inputFile)) {
				getUserInterface().writeErrorMessage("请指定输入图片文件。");
				return;
			}
			File inputF = new File(inputFile);
			if (!inputF.isFile()) {
				getUserInterface().writeErrorMessage("指定输入图片文件不存在。");
				return;
			}
			try {
				BufferedImage img = ImageIO.read(inputF);
				callOcr(img);
			} catch (Exception e) {
				e.printStackTrace();
				getUserInterface().writeErrorMessage(e.getClass().getName() + ": " + e.getMessage());
			}
		} else if ("capture".equals(actionName)) {
			// 截图前隐藏主窗口
			MyToolBoxFrame.instance.setVisible(false);
			try {
				Thread.sleep(300);
			} catch (InterruptedException e1) {
			}
			ScreenCapture.capture(new ScreenCaptureCallback() {
				@Override
				public void onCaptured(BufferedImage img) {
					MyToolBoxFrame.instance.setVisible(true);
					callOcr(img);
				}

				@Override
				public void onCancel() {
					MyToolBoxFrame.instance.setVisible(true);
				}
			});
		}
	}

	private void callOcr(BufferedImage img) {
		try {
			String provider = getParameter("provider").getValue();
			OcrProvider ocrProvider = providerMap.get(provider);
			String text = ocrProvider.callOcr(img);
			if (text.startsWith("<html>")) {
				getUserInterface().writeHtmlMessage(text.substring(6));
			} else {
				getUserInterface().writeInfoMessage(text);
			}
			String outputFile = getParameter("outputFile").getValue();
			if (StringUtils.isNotBlank(outputFile)) {
				File out = new File(outputFile);
				out.getParentFile().mkdirs();
				FileUtils.writeStringToFile(out, text, "UTF-8");
			}
		} catch (Exception e) {
			e.printStackTrace();
			getUserInterface().writeErrorMessage(e.getClass().getName() + ": " + e.getMessage());
		}
	}

	@Override
	public void onParameterValueChange(String name) {
	}

}
