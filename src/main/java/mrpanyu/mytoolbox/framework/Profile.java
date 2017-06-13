package mrpanyu.mytoolbox.framework;

import java.util.LinkedHashMap;
import java.util.Map;

public class Profile {

	private String name;
	private Map<String, String> parameterValues = new LinkedHashMap<String, String>();

	public Profile(String name) {
		super();
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<String, String> getParameterValues() {
		return parameterValues;
	}

}
