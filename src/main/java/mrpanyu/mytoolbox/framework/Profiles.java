package mrpanyu.mytoolbox.framework;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

public class Profiles {

	private String toolName;
	private File defaultSaveFile;
	private Map<String, Profile> profiles = new TreeMap<String, Profile>();

	public Profiles(String toolName) {
		this.toolName = toolName;
		this.defaultSaveFile = new File(".mtbpf_" + toolName);
	}

	public String getToolName() {
		return toolName;
	}

	public Set<String> getProfileNames() {
		return profiles.keySet();
	}

	public Profile getProfile(String name) {
		return profiles.get(name);
	}

	public void setProfile(String name, Profile profile) {
		profiles.put(name, profile);
	}

	public void removeProfile(String name) {
		profiles.remove(name);
	}

	public void load() {
		load(defaultSaveFile);
	}

	public void save() {
		save(defaultSaveFile);
	}

	public void load(File f) {
		try {
			if (!f.exists()) {
				return;
			}
			List<String> lines = FileUtils.readLines(f, "UTF-8");
			Profile profile = null;
			String lastParamName = null;
			for (String line : lines) {
				if (StringUtils.isNotBlank(line)) {
					if (line.startsWith("[")) {
						String name = StringUtils.substringBetween(line, "[", "]");
						profile = new Profile(name);
						profiles.put(name, profile);
					} else if (line.startsWith("\t")) {
						String value = profile.getParameterValues().get(lastParamName);
						value = value + "\n" + line.substring(1);
						profile.getParameterValues().put(lastParamName, value);
					} else {
						String[] arr = line.split("=", 2);
						profile.getParameterValues().put(arr[0], arr[1]);
						lastParamName = arr[0];
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public void save(File f) {
		List<String> lines = new ArrayList<String>();
		for (Map.Entry<String, Profile> entry : profiles.entrySet()) {
			String name = entry.getKey();
			Profile profile = entry.getValue();
			lines.add("[" + name + "]");
			for (Map.Entry<String, String> e : profile.getParameterValues().entrySet()) {
				lines.add(e.getKey() + "=" + e.getValue().replace("\n", "\n\t"));
			}
			lines.add("");
		}
		try {
			FileUtils.writeLines(f, "UTF-8", lines);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
}
