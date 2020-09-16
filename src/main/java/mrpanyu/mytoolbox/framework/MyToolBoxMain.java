package mrpanyu.mytoolbox.framework;

import java.util.List;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;

import mrpanyu.mytoolbox.framework.api.Tool;
import mrpanyu.mytoolbox.framework.utils.FindClassesUtils;

public class MyToolBoxMain {

	public static void main(String[] args) {
		List<Tool> tools = findTools();
		if (args == null || args.length == 0) {
			// start as window
			MyToolBoxFrame frame = new MyToolBoxFrame(tools);
			frame.initialize();
		} else {
			// start in cmd mode
			MyToolBoxCmd.execute(args, tools);
		}
	}

	private static List<Tool> findTools() {
		try {
			List<Tool> tools = new ArrayList<>();
			Set<Class<?>> toolClasses = FindClassesUtils.findClassesByBaseClass("mrpanyu.mytoolbox.tools", true,
					Tool.class);
			for (Class<?> cls : toolClasses) {
				if (Modifier.isAbstract(cls.getModifiers())) {
					continue;
				}
				Tool tool = (Tool) cls.newInstance();
				tool.initialize();
				tools.add(tool);
			}
			Collections.sort(tools, new Comparator<Tool>() {
				public int compare(Tool o1, Tool o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});
			return tools;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
