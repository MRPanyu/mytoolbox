package mrpanyu.mytoolbox.framework.api;

/** 工具进行用户界面反向操作的接口 */
public interface UserInterface {

	public void writeInfoMessage(String message);

	public void writeWarnMessage(String message);

	public void writeErrorMessage(String message);

	/** 如果支持，以HTML格式输出富文本内容 */
	public void writeHtmlMessage(String message);

	public void clearMessages();

	public void refreshParameterValue(String name);

	public void refreshParemeterValues();

}
