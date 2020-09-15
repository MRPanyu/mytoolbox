package mrpanyu.mytoolbox.framework.api;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public abstract class Tool {

	private String name;
	private String displayName;
	private String description;
	private boolean enableProfile = false;
	private List<Parameter> parameters = new ArrayList<Parameter>();
	private List<Action> actions = new ArrayList<Action>();
	private UserInterface userInterface = null;

	/** 初始化工具的基本信息，参数和动作 */
	public abstract void initialize();

	/** 执行具体动作 */
	public abstract void performAction(String actionName);

	/** 当参数值改变时触发 */
	public abstract void onParameterValueChange(String name);

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isEnableProfile() {
		return enableProfile;
	}

	public void setEnableProfile(boolean enableProfile) {
		this.enableProfile = enableProfile;
	}

	public List<Parameter> getParameters() {
		return parameters;
	}

	public Parameter getParameter(String name) {
		Parameter param = null;
		for (Parameter p : parameters) {
			if (StringUtils.equals(p.getName(), name)) {
				param = p;
				break;
			}
		}
		return param;
	}

	public void addParameter(Parameter param) {
		parameters.add(param);
	}

	public List<Action> getActions() {
		return actions;
	}

	public void addAction(Action action) {
		actions.add(action);
	}

	public UserInterface getUserInterface() {
		return userInterface;
	}

	public void setUserInterface(UserInterface userInterface) {
		this.userInterface = userInterface;
	}

}
