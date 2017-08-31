package mrpanyu.mytoolbox.framework;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.Element;
import javax.swing.text.html.HTMLDocument;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import mrpanyu.mytoolbox.framework.api.Action;
import mrpanyu.mytoolbox.framework.api.Parameter;
import mrpanyu.mytoolbox.framework.api.ParameterType;
import mrpanyu.mytoolbox.framework.api.Tool;
import mrpanyu.mytoolbox.framework.api.UserInterface;

@SuppressWarnings({ "serial", "rawtypes", "unchecked" })
public class ToolPanel extends JPanel implements UserInterface {

	private static int LINE_HEIGHT = 25;
	private static int LABEL_WIDTH = 100;

	private ImageIcon iconFolder = new ImageIcon(this.getClass().getResource("images/folder.png"));
	private ImageIcon iconFloppy = new ImageIcon(this.getClass().getResource("images/floppy.png"));
	private ImageIcon iconTrash = new ImageIcon(this.getClass().getResource("images/trash.png"));
	private ImageIcon iconHelp = new ImageIcon(this.getClass().getResource("images/help.png"));

	private Tool tool;
	private Map<String, JComponent> parameterComponentMap = new LinkedHashMap<String, JComponent>();
	private Profiles profiles;
	private JComboBox comboProfiles;
	private JTextPane textOutput;
	private StringBuilder outputMessages = new StringBuilder();
	private String helpContent = null;

	public ToolPanel(Tool tool) {
		this.tool = tool;
		this.profiles = new Profiles(tool.getName());
		initialize();
		tool.setUserInterface(this);
	}

	public void initialize() {
		// basic setting
		this.setLayout(new BorderLayout());
		this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		JPanel panelTop = new JPanel();
		panelTop.setLayout(new BoxLayout(panelTop, BoxLayout.Y_AXIS));
		this.add(panelTop, BorderLayout.NORTH);

		// initialize profile
		if (tool.isEnableProfile()) {
			profiles.load();
			JPanel panel = new JPanel();
			panelTop.add(panel);
			BoxLayout layout = new BoxLayout(panel, BoxLayout.X_AXIS);
			panel.setLayout(layout);
			JLabel label = new JLabel("预设方案: ");
			label.setHorizontalAlignment(SwingConstants.RIGHT);
			label.setPreferredSize(new Dimension(LABEL_WIDTH, LINE_HEIGHT));
			panel.add(label);
			comboProfiles = new JComboBox();
			comboProfiles.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						loadProfile();
					}
				}
			});
			panel.add(comboProfiles);
			JButton button = new JButton(iconFloppy);
			button.setPreferredSize(new Dimension(LINE_HEIGHT, LINE_HEIGHT));
			button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					saveProfile();
				}
			});
			panel.add(button);
			button = new JButton(iconTrash);
			button.setPreferredSize(new Dimension(LINE_HEIGHT, LINE_HEIGHT));
			button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					deleteProfile();
				}
			});
			panel.add(button);
			refreshComboProfiles();
		}

		// initialize parameter inputs
		List<Parameter> parameters = tool.getParameters();
		for (Parameter p : parameters) {
			final Parameter parameter = p;
			JPanel panel = new JPanel();
			panelTop.add(panel);
			BoxLayout layout = new BoxLayout(panel, BoxLayout.X_AXIS);
			panel.setLayout(layout);
			JLabel label = new JLabel(parameter.getDisplayName() + ": ");
			if (StringUtils.isNotBlank(parameter.getDescription())) {
				label.setToolTipText(parameter.getDescription());
			}
			label.setHorizontalAlignment(SwingConstants.RIGHT);
			label.setPreferredSize(new Dimension(LABEL_WIDTH, LINE_HEIGHT));
			panel.add(label);
			if (parameter.getType() == ParameterType.ENUMERATION) {
				final JComboBox combo = new JComboBox();
				for (String value : parameter.getEnumerationValues()) {
					combo.addItem(value);
				}
				if (StringUtils.isNotBlank(parameter.getValue())) {
					combo.setSelectedItem(parameter.getValue());
				}
				combo.addItemListener(new ItemListener() {
					public void itemStateChanged(ItemEvent e) {
						if (e.getStateChange() == ItemEvent.SELECTED) {
							parameter.setValue((String) combo.getSelectedItem());
							tool.onParameterValueChange(parameter.getName());
						}
					}
				});
				panel.add(combo);
				parameterComponentMap.put(parameter.getName(), combo);
			} else if (parameter.getType() == ParameterType.MULTILINE_TEXT) {
				final JTextArea text = new JTextArea();
				text.setPreferredSize(new Dimension(0, 100));
				text.setTabSize(4);
				JScrollPane scrollPane = new JScrollPane(text);
				scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
				scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
				if (StringUtils.isNotBlank(parameter.getValue())) {
					text.setText(parameter.getValue());
				}
				text.addFocusListener(new FocusAdapter() {
					public void focusLost(FocusEvent e) {
						if (!StringUtils.equals(parameter.getValue(), text.getText())) {
							parameter.setValue(text.getText());
							tool.onParameterValueChange(parameter.getName());
						}
					}
				});
				panel.add(scrollPane);
				parameterComponentMap.put(parameter.getName(), text);
			} else {
				final JTextField text = new JTextField();
				if (StringUtils.isNotBlank(parameter.getValue())) {
					text.setText(parameter.getValue());
				}
				text.addFocusListener(new FocusAdapter() {
					public void focusLost(FocusEvent e) {
						if (!StringUtils.equals(parameter.getValue(), text.getText())) {
							parameter.setValue(text.getText());
							tool.onParameterValueChange(parameter.getName());
						}
					}
				});
				panel.add(text);
				parameterComponentMap.put(parameter.getName(), text);
				if (parameter.getType() == ParameterType.DIRECTORY || parameter.getType() == ParameterType.FILE) {
					JButton button = new JButton(iconFolder);
					button.setPreferredSize(new Dimension(LINE_HEIGHT, LINE_HEIGHT));
					button.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							openFolderOrFile(parameter, text);
						}
					});
					panel.add(button);
				}
			}
		}

		// initialize action buttons
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
		panelTop.add(panel);
		BoxLayout layout = new BoxLayout(panel, BoxLayout.X_AXIS);
		panel.setLayout(layout);
		for (Action a : tool.getActions()) {
			final Action action = a;
			final JButton button = new JButton(action.getDisplayName());
			button.setToolTipText(action.getDescription());
			button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					new Thread(new Runnable() {
						public void run() {
							tool.performAction(action.getName());
						}
					}).start();
				}
			});
			panel.add(button);
		}

		// initialize help
		InputStream insHelp = tool.getClass().getResourceAsStream(tool.getClass().getSimpleName() + ".html");
		if (insHelp != null) {
			try {
				helpContent = IOUtils.toString(insHelp, "UTF-8");
				JButton buttonHelp = new JButton(iconHelp);
				buttonHelp.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						showHelp();
					}
				});
				panel.add(buttonHelp);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				IOUtils.closeQuietly(insHelp);
			}
		}

		// initialize output text
		panel = new JPanel();
		this.add(panel, BorderLayout.CENTER);
		layout = new BoxLayout(panel, BoxLayout.X_AXIS);
		panel.setLayout(layout);
		textOutput = new JTextPane();
		textOutput.setEditable(false);
		textOutput.setContentType("text/html");
		textOutput.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		textOutput.setText("<html><body id='body'></body></html>");
		JScrollPane scrollPane = new JScrollPane(textOutput);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		panel.add(scrollPane);

	}

	public void openFolderOrFile(Parameter parameter, JTextField text) {
		String currentPath = text.getText();
		JFileChooser jfc = new JFileChooser(new File(currentPath).getParentFile());
		if (parameter.getType() == ParameterType.DIRECTORY) {
			jfc.setAcceptAllFileFilterUsed(false);
			jfc.setFileFilter(new FileFilter() {
				public String getDescription() {
					return "文件夹";
				}

				public boolean accept(File f) {
					return f.isDirectory();
				}
			});
			jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		} else {
			jfc.setAcceptAllFileFilterUsed(true);
			jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		}
		int result = jfc.showOpenDialog(ToolPanel.this);
		if (result == JFileChooser.APPROVE_OPTION) {
			File f = jfc.getSelectedFile();
			String path = f.getAbsolutePath();
			text.setText(path);
			parameter.setValue((String) text.getText());
			tool.onParameterValueChange(parameter.getName());
		}
	}

	@Override
	public void writeInfoMessage(String message) {
		writeMessage(message, "blue");
	}

	@Override
	public void writeWarnMessage(String message) {
		writeMessage(message, "#606000");
	}

	@Override
	public void writeErrorMessage(String message) {
		writeMessage(message, "red");
	}

	@Override
	public void writeHtmlMessage(final String message) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					HTMLDocument doc = (HTMLDocument) textOutput.getDocument();
					Element body = doc.getElement("body");
					doc.insertBeforeStart(body.getElement(body.getElementCount() - 1), message);
				} catch (Exception e) {
					e.printStackTrace();
				}
				outputMessages.append(message);
			}
		});
	}

	public void clearMessages() {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				outputMessages = new StringBuilder();
				textOutput.setText("<html><body id='body'></body></html>");
			}
		});
	}

	@Override
	public void refreshParameterValue(final String name) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				JComponent component = parameterComponentMap.get(name);
				Parameter parameter = tool.getParameter(name);
				if (component instanceof JComboBox) {
					JComboBox combo = (JComboBox) component;
					combo.removeAllItems();
					for (String value : parameter.getEnumerationValues()) {
						combo.addItem(value);
					}
					combo.setSelectedItem(parameter.getValue());
				} else if (component instanceof JTextArea) {
					JTextArea text = (JTextArea) component;
					text.setText(parameter.getValue());
				} else if (component instanceof JTextField) {
					JTextField text = (JTextField) component;
					text.setText(parameter.getValue());
				}
			}
		});
	}

	@Override
	public void refreshParemeterValues() {
		for (Parameter p : tool.getParameters()) {
			refreshParameterValue(p.getName());
		}
	}

	private void writeMessage(final String message, final String color) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				String divStart = "<div style=\"color:" + color
						+ ";white-space:nowrap;font-family:Consolas,monospace;\">";
				String html = divStart + StringEscapeUtils.escapeHtml(message).replace("\r\n", "\n")
						.replace("\n", "</div>" + divStart).replace("\t", "    ").replace(" ", "&nbsp;") + "</div>";
				try {
					HTMLDocument doc = (HTMLDocument) textOutput.getDocument();
					Element body = doc.getElement("body");
					doc.insertBeforeStart(body.getElement(body.getElementCount() - 1), html);
				} catch (Exception e) {
					e.printStackTrace();
				}
				outputMessages.append(html);
			}
		});
	}

	private void refreshComboProfiles() {
		String oldName = (String) comboProfiles.getSelectedItem();
		String selectName = "";
		comboProfiles.removeAllItems();
		comboProfiles.addItem("");
		for (String name : profiles.getProfileNames()) {
			comboProfiles.addItem(name);
			if (StringUtils.equals(name, oldName)) {
				selectName = name;
			}
		}
		comboProfiles.setSelectedItem(selectName);
	}

	private void loadProfile() {
		String profileName = (String) comboProfiles.getSelectedItem();
		if (StringUtils.isNotBlank(profileName)) {
			Profile profile = profiles.getProfile(profileName);
			for (Map.Entry<String, String> entry : profile.getParameterValues().entrySet()) {
				String paramName = entry.getKey();
				String paramValue = entry.getValue();
				tool.getParameter(paramName).setValue(paramValue);
				tool.onParameterValueChange(paramName);
			}
			refreshParemeterValues();
		}
	}

	private void saveProfile() {
		String profileName = (String) comboProfiles.getSelectedItem();
		profileName = JOptionPane.showInputDialog(this, "保存预设方案：", profileName);
		if (StringUtils.isNotBlank(profileName)) {
			Profile profile = new Profile(profileName);
			for (Parameter parameter : tool.getParameters()) {
				profile.getParameterValues().put(parameter.getName(), parameter.getValue());
			}
			profiles.setProfile(profileName, profile);
			profiles.save();
			refreshComboProfiles();
			comboProfiles.setSelectedItem(profileName);
		}
	}

	private void deleteProfile() {
		String profileName = (String) comboProfiles.getSelectedItem();
		if (StringUtils.isNotBlank(profileName)) {
			if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(this, "确定删除预设方案？", "确认",
					JOptionPane.YES_NO_OPTION)) {
				profiles.removeProfile(profileName);
				profiles.save();
				refreshComboProfiles();
			}
		}
	}

	private void showHelp() {
		JFrame frameHelp = new JFrame("帮助");
		JTextPane textHelp = new JTextPane();
		textHelp.setEditable(false);
		textHelp.setContentType("text/html");
		textHelp.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		textHelp.setText(helpContent);
		JScrollPane scrollPane = new JScrollPane(textHelp);
		scrollPane.setPreferredSize(new Dimension(800, 600));
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		frameHelp.getContentPane().add(scrollPane, BorderLayout.CENTER);
		frameHelp.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frameHelp.setIconImage(iconHelp.getImage());
		frameHelp.pack();
		frameHelp.setLocationByPlatform(false);
		frameHelp.setLocationRelativeTo(null);
		frameHelp.setVisible(true);
	}

}
