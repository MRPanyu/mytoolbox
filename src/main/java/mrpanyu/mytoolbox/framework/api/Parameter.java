package mrpanyu.mytoolbox.framework.api;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class Parameter {

	/** 参数名称 */
	private String name = "";
	/** 参数显示名称 */
	private String displayName = "";
	/** 参数具体描述 */
	private String description = "";
	/** 参数类型 */
	private ParameterType type = ParameterType.SIMPLE_TEXT;
	/** 参数值 */
	private String value = "";
	/** 枚举值 */
	private List<String> enumerationValues = new ArrayList<String>();

	public Parameter() {
	}

	public Parameter(String name, String displayName) {
		this.name = name;
		this.displayName = displayName;
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

	public ParameterType getType() {
		return type;
	}

	public void setType(ParameterType type) {
		this.type = type;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public List<String> getEnumerationValues() {
		return enumerationValues;
	}

	public void setEnumerationValues(List<String> enumerationValues) {
		this.enumerationValues = enumerationValues;
		if (StringUtils.isBlank(this.value) && enumerationValues != null && !enumerationValues.isEmpty()) {
			this.value = enumerationValues.get(0);
		}
	}

}
