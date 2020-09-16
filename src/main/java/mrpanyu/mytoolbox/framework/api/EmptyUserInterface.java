package mrpanyu.mytoolbox.framework.api;

import java.text.SimpleDateFormat;
import java.util.Date;

public class EmptyUserInterface implements UserInterface {

	@Override
	public void writeInfoMessage(String message) {
		System.out.println(timestamp() + "[INFO] " + message);
	}

	@Override
	public void writeWarnMessage(String message) {
		System.out.println(timestamp() + "[WARN] " + message);
	}

	@Override
	public void writeErrorMessage(String message) {
		System.out.println(timestamp() + "[ERROR] " + message);
	}

	@Override
	public void writeHtmlMessage(String message) {
		System.out.println(timestamp() + "[HTML] " + message);
	}

	@Override
	public void clearMessages() {
	}

	@Override
	public void refreshParameterValue(String name) {
	}

	@Override
	public void refreshParemeterValues() {
	}

	private String timestamp() {
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		return "[" + fmt.format(new Date()) + "]";
	}
}
