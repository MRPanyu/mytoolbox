package mrpanyu.mytoolbox.framework;

import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;

import mrpanyu.mytoolbox.framework.api.EmptyUserInterface;
import mrpanyu.mytoolbox.framework.api.Tool;

public class MyToolBoxCmd {

	public static void execute(String[] args, List<Tool> tools) {
		try {
			CommandLineParser parser = new DefaultParser();
			Options options = new Options();
			options.addRequiredOption("t", "tool", true, "Tool name to use.");
			options.addRequiredOption("p", "profile", true, "Profile to load.");
			options.addRequiredOption("a", "action", true, "Action to execute.");
			CommandLine cmd = parser.parse(options, args);
			String toolName = cmd.getOptionValue("tool");
			String profileName = cmd.getOptionValue("profile");
			String actionName = cmd.getOptionValue("action");
			Tool tool = null;
			for (Tool t : tools) {
				if (StringUtils.equals(t.getName(), toolName)) {
					tool = t;
					break;
				}
			}
			if (tool == null) {
				System.out.println("Cannot find tool: " + toolName);
				return;
			}
			Profiles profiles = new Profiles(toolName);
			profiles.load();
			Profile profile = profiles.getProfile(profileName);
			if (profile == null) {
				System.out.println("Cannot find profile for tool: " + profileName);
				return;
			}
			for (Map.Entry<String, String> entry : profile.getParameterValues().entrySet()) {
				String paramName = entry.getKey();
				String paramValue = entry.getValue();
				tool.getParameter(paramName).setValue(paramValue);
			}
			tool.setUserInterface(new EmptyUserInterface());
			tool.performAction(actionName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
