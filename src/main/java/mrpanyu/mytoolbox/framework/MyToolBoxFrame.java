package mrpanyu.mytoolbox.framework;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import mrpanyu.mytoolbox.framework.api.Tool;

@SuppressWarnings("serial")
public class MyToolBoxFrame extends JFrame {

	public static MyToolBoxFrame instance;
	public static double VIEW_SCALE = 1.0;

	private JList listTools;
	private JPanel panelMain;
	private List<Tool> tools = new ArrayList<Tool>();

	public MyToolBoxFrame(List<Tool> tools) {
		this.tools = tools;
	}

	public void initialize() {
		try {
			instance = this;
			// 通过 -Dscale=1.0 参数可以调整整体缩放比例，如调整成1.5窗口和字体就是1.5倍大小
			String scale = System.getProperty("scale");
			if (scale != null) {
				VIEW_SCALE = Double.parseDouble(scale);
			}
			initializeComponents();
		} catch (Exception e) {
			e.printStackTrace();
		}
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
					MyToolBoxFrame.this.pack();
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

}
