package mrpanyu.mytoolbox.framework;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
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

	public static final MyToolBox instance = new MyToolBox();
	public static double VIEW_SCALE = 1.0;

	private JList listTools;
	private JPanel panelMain;
	private List<Tool> tools = new ArrayList<Tool>();

	public void initialize() {
		try {
			// 通过 -Dscale=1.0 参数可以调整整体缩放比例，如调整成1.5窗口和字体就是1.5倍大小
			String scale = System.getProperty("scale");
			if (scale != null) {
				VIEW_SCALE = Double.parseDouble(scale);
			}
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
		int width = (int) (200 * VIEW_SCALE);
		int height = (int) (600 * VIEW_SCALE);
		listTools.setPreferredSize(new Dimension(width, height));
		Font font = new Font("", Font.BOLD, (int) (13 * VIEW_SCALE));
		listTools.setFont(font);
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
		width = (int) (600 * VIEW_SCALE);
		height = (int) (600 * VIEW_SCALE);
		panelMain.setPreferredSize(new Dimension(width, height));
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
		instance.initialize();
	}

}
