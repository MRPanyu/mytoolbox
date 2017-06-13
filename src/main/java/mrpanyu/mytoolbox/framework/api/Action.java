package mrpanyu.mytoolbox.framework.api;

public class Action {

	private String name;
	private String displayName;
	private String description;

	public Action() {
	}

	public Action(String name, String displayName) {
		this.name = name;
		this.displayName = displayName;
	}

	public Action(String name, String displayName, String description) {
		this.name = name;
		this.displayName = displayName;
		this.description = description;
	}

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

}
