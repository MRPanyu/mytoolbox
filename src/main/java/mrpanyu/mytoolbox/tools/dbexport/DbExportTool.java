package mrpanyu.mytoolbox.tools.dbexport;

import java.io.File;
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
import mrpanyu.mytoolbox.tools.dbexport.exporter.BinaryDataDbExporter;
import mrpanyu.mytoolbox.tools.dbexport.exporter.DbExporter;
import mrpanyu.mytoolbox.tools.dbexport.model.TableExportCondition;
import mrpanyu.mytoolbox.tools.dbexport.utils.DbExportUtils;

public class DbExportTool extends Tool {

	private Map<String, Class<? extends DbExporter>> exporterMap = new LinkedHashMap<>();

	public DbExportTool() {
		exporterMap.put("序列化数据文件", BinaryDataDbExporter.class);
	}

	@Override
	public void initialize() {
		// 初始化配置
		setName("0701_dbExport");
		setDisplayName("数据库：数据导出");
		setDescription("");
		setEnableProfile(true);

		// 初始化参数
		Parameter param = new Parameter("type", "数据库类型");
		param.setDescription("数据库类型");
		param.setType(ParameterType.ENUMERATION);
		param.setEnumerationValues(Arrays.asList(DbExportUtils.TYPE_ORACLE, DbExportUtils.TYPE_MYSQL));
		addParameter(param);

		param = new Parameter("exporter", "导出格式");
		param.setDescription("导出文件格式<ul><li>序列化数据文件：导出为二进制文件，需结合数据导入功能使用</li></ul>");
		param.setType(ParameterType.ENUMERATION);
		param.setEnumerationValues(new ArrayList<String>(exporterMap.keySet()));
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

		param = new Parameter("exportDir", "导出目录");
		param.setDescription("导出文件的目录");
		param.setType(ParameterType.DIRECTORY);
		addParameter(param);

		param = new Parameter("cond", "导出表及sql");
		param.setDescription(
				"导出的表名及对应的sql，按“表名:sql”格式。多个表之间分号隔开。<br/>例如：“SYS_USER:select * from SYS_USER;SYS_COMPANY:select * from SYS_COMPANY;” <br/>也可以不加sql部分，等同于“select * from 表名”，如 “SYS_USER;SYS_COMPANY”");
		param.setType(ParameterType.MULTILINE_TEXT);
		param.setValue("SYS_USER:select * from SYS_USER;\nSYS_COMPANY:select * from SYS_COMPANY;");
		addParameter(param);

		// 初始化动作
		addAction(new Action("export", "导出", "进行数据导出"));
	}

	@Override
	public void performAction(String actionName) {
		UserInterface ui = getUserInterface();
		ui.clearMessages();
		String type = getParameter("type").getValue();
		Class<? extends DbExporter> exporterClass = exporterMap.get(getParameter("exporter").getValue());
		String url = getParameter("url").getValue();
		String user = getParameter("user").getValue();
		String password = getParameter("password").getValue();
		String strExportDir = getParameter("exportDir").getValue();
		File exportDir = new File(strExportDir);
		String strCond = getParameter("cond").getValue();
		if (StringUtils.isBlank(url)) {
			ui.writeErrorMessage("未指定JDBC URL");
			return;
		}
		if (!exportDir.exists()) {
			exportDir.mkdirs();
		}
		if (!exportDir.isDirectory()) {
			ui.writeErrorMessage("导出目录错误");
			return;
		}
		if (StringUtils.isBlank(strCond)) {
			ui.writeErrorMessage("未指定导出表及sql");
			return;
		}
		String[] arrCond = strCond.split("[;；]");
		for (String ts : arrCond) {
			if (StringUtils.isBlank(ts)) {
				continue;
			}
			String[] arrTs = ts.split("[:：]", 2);
			TableExportCondition cond = new TableExportCondition();
			cond.setTable(StringUtils.trim(arrTs[0]));
			if (StringUtils.isBlank(cond.getTable())) {
				ui.writeErrorMessage("导出表及sql格式错误：" + ts);
				return;
			}
			if (arrTs.length > 1) {
				cond.setSql(StringUtils.trim(arrTs[1]));
			}
			try {
				DbExportUtils.export(type, url, user, password, exportDir, cond, exporterClass, ui);
			} catch (Exception e) {
				ui.writeErrorMessage("导出表" + cond.getTable() + "错误：");
				ui.writeErrorMessage(e.getClass().getName() + ": " + e.getMessage());
			}
		}
	}

	@Override
	public void onParameterValueChange(String name) {
	}

}
