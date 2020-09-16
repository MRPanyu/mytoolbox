package mrpanyu.mytoolbox.tools.dbexport;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import mrpanyu.mytoolbox.framework.api.Action;
import mrpanyu.mytoolbox.framework.api.Parameter;
import mrpanyu.mytoolbox.framework.api.ParameterType;
import mrpanyu.mytoolbox.framework.api.Tool;
import mrpanyu.mytoolbox.framework.api.UserInterface;
import mrpanyu.mytoolbox.tools.dbexport.importer.BinaryDataDbImporter;
import mrpanyu.mytoolbox.tools.dbexport.importer.DbImporter;
import mrpanyu.mytoolbox.tools.dbexport.utils.DbExportUtils;

public class DbImportTool extends Tool {

	private Map<String, Class<? extends DbImporter>> importerMap = new LinkedHashMap<>();

	public DbImportTool() {
		importerMap.put("序列化数据文件", BinaryDataDbImporter.class);
	}

	@Override
	public void initialize() {
		// 初始化配置
		setName("0702_dbImport");
		setDisplayName("数据库：数据导入");
		setDescription("");
		setEnableProfile(true);

		// 初始化参数
		Parameter param = new Parameter("type", "数据库类型");
		param.setDescription("数据库类型");
		param.setType(ParameterType.ENUMERATION);
		param.setEnumerationValues(Arrays.asList(DbExportUtils.TYPE_ORACLE, DbExportUtils.TYPE_MYSQL));
		addParameter(param);

		param = new Parameter("importer", "导入格式");
		param.setDescription("导入文件格式");
		param.setType(ParameterType.ENUMERATION);
		param.setEnumerationValues(new ArrayList<String>(importerMap.keySet()));
		addParameter(param);

		param = new Parameter("url", "JDBC URL");
		param.setDescription("JDBC连接URL");
		addParameter(param);

		param = new Parameter("user", "USER");
		param.setDescription("JDBC连接用户");
		addParameter(param);

		param = new Parameter("password", "PASSWORD");
		param.setDescription("JDBC连接密码");
		addParameter(param);

		param = new Parameter("importDir", "导入目录");
		param.setDescription("导入文件的目录");
		param.setType(ParameterType.DIRECTORY);
		addParameter(param);

		param = new Parameter("truncate", "导入前清空");
		param.setDescription("是否导入前先执行truncate table清空数据");
		param.setType(ParameterType.ENUMERATION);
		param.setEnumerationValues(Arrays.asList("否", "是"));
		addParameter(param);

		// 初始化动作
		addAction(new Action("import", "导入", "进行数据导入"));
	}

	@Override
	public void performAction(String actionName) {
		UserInterface ui = getUserInterface();
		ui.clearMessages();
		String type = getParameter("type").getValue();
		Class<? extends DbImporter> importerClass = importerMap.get(getParameter("importer").getValue());
		String url = getParameter("url").getValue();
		String user = getParameter("user").getValue();
		String password = getParameter("password").getValue();
		String strImportDir = getParameter("importDir").getValue();
		File importDir = new File(strImportDir);
		String truncate = getParameter("truncate").getValue();
		if (StringUtils.isBlank(url)) {
			ui.writeErrorMessage("未指定JDBC URL");
			return;
		}
		if (!importDir.isDirectory()) {
			ui.writeErrorMessage("导入目录错误");
			return;
		}
		try (Connection conn = DbExportUtils.createConnection(type, url, user, password);) {
			DbImporter importer = importerClass.newInstance();
			importer.setConnection(conn);
			importer.setImportDir(importDir);
			importer.setTruncateBeforeImport("是".equals(truncate));
			importer.setUi(getUserInterface());
			importer.importData();
		} catch (Exception e) {
			e.printStackTrace();
			ui.writeErrorMessage(e.getClass().getName() + ": " + e.getMessage());
		}
	}

	@Override
	public void onParameterValueChange(String name) {
	}

}
