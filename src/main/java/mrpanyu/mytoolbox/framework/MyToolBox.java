package mrpanyu.mytoolbox.framework;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import mrpanyu.mytoolbox.framework.api.Tool;
import mrpanyu.mytoolbox.framework.utils.FindClassesUtils;

@SuppressWarnings("serial")
public class MyToolBox extends JFrame {

	private JList listTools;
	private JPanel panelMain;
	private List<Tool> tools = new ArrayList<Tool>();

	public void initialize() {
		try {
			findTools();
			initializeComponents();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void findTools() throws Exception {
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
	}

	private void initializeComponents() throws Exception {
		JSplitPane splitPane = new JSplitPane();
		this.getContentPane().add(splitPane, BorderLayout.CENTER);
		List<String> displayNames = new ArrayList<String>();
		for (Tool tool : tools) {
			displayNames.add(tool.getDisplayName());
		}
		listTools = new JList(displayNames.toArray());
		listTools.setPreferredSize(new Dimension(200, 600));
		listTools.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					int index = listTools.getSelectedIndex();
					panelMain.removeAll();
					ToolPanel toolPanel = new ToolPanel(tools.get(index));
					panelMain.add(toolPanel, BorderLayout.CENTER);
					MyToolBox.this.pack();
				}
			}
		});
		splitPane.setLeftComponent(listTools);
		panelMain = new JPanel();
		panelMain.setLayout(new BorderLayout());
		panelMain.setPreferredSize(new Dimension(600, 600));
		splitPane.setRightComponent(panelMain);
		this.pack();
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setTitle("我的工具盒");
		this.setIconImage(new ImageIcon(this.getClass().getResource("images/toolbox.png")).getImage());
		this.setLocationByPlatform(false);
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}

	public static void main(String[] args) {
		MyToolBox mtb = new MyToolBox();
		mtb.initialize();
	}

}
