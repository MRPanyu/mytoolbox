package mrpanyu.mytoolbox.framework.api;

public class EmptyUserInterface implements UserInterface {

	@Override
	public void writeInfoMessage(String message) {
		System.out.println("[INFO] " + message);
	}

	@Override
	public void writeWarnMessage(String message) {
		System.out.println("[WARN] " + message);
	}

	@Override
	public void writeErrorMessage(String message) {
		System.out.println("[ERROR] " + message);
	}

	@Override
	public void writeHtmlMessage(String message) {
		System.out.println("[HTML] " + message);
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

}
