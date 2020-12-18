package mrpanyu.mytoolbox.tools.dbexport.utils;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import mrpanyu.mytoolbox.framework.api.UserInterface;
import mrpanyu.mytoolbox.tools.dbexport.exporter.DbExporter;
import mrpanyu.mytoolbox.tools.dbexport.model.ColumnMetaData;
import mrpanyu.mytoolbox.tools.dbexport.model.TableExportCondition;
import mrpanyu.mytoolbox.tools.dbexport.model.TableMetaData;

public class DbExportUtils {

	public static final String TYPE_MYSQL = "mysql";
	public static final String TYPE_ORACLE = "oracle";

	public static Connection createConnection(String type, String url, String user, String password)
			throws SQLException {
		try {
			if (TYPE_ORACLE.equals(type)) {
				Class.forName("oracle.jdbc.OracleDriver");
			} else if (TYPE_MYSQL.equals(type)) {
				Class.forName("com.mysql.cj.jdbc.Driver");
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		if (StringUtils.isNotBlank(user)) {
			return DriverManager.getConnection(url, user, password);
		} else {
			return DriverManager.getConnection(url);
		}
	}

	public static void export(String type, String url, String user, String password, File exportDir,
			TableExportCondition cond, Class<? extends DbExporter> exporterClass, UserInterface ui) throws Exception {
		try (Connection conn = createConnection(type, url, user, password);
				DbExporter exporter = exporterClass.newInstance();) {
			String table = cond.getTable();
			ui.writeInfoMessage("开始导出：" + table);
			String sql = cond.getSql();
			if (StringUtils.isBlank(sql)) {
				sql = "select * from " + table;
			}
			PreparedStatement ps = conn.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();

			TableMetaData tableMetaData = new TableMetaData();
			tableMetaData.setTable(table);
			tableMetaData.setSql(sql);
			ResultSetMetaData meta = rs.getMetaData();
			for (int i = 0; i < meta.getColumnCount(); i++) {
				ColumnMetaData col = new ColumnMetaData();
				col.setColumnName(meta.getColumnName(i + 1));
				col.setColumnType(meta.getColumnType(i + 1));
				tableMetaData.getColumnMeta().add(col);
			}
			exporter.setTableMetaData(tableMetaData);
			exporter.setExportDir(exportDir);
			exporter.startExport();
			int r = 0;
			while (rs.next()) {
				Object[] row = new Object[tableMetaData.getColumnMeta().size()];
				for (int i = 0; i < row.length; i++) {
					ColumnMetaData col = tableMetaData.getColumnMeta().get(i);
					Object value = null;
					int colType = col.getColumnType();
					if (colType == Types.DATE) {
						value = rs.getDate(i + 1);
					} else if (colType == Types.TIME || colType == Types.TIME_WITH_TIMEZONE) {
						value = rs.getTime(i + 1);
					} else if (colType == Types.TIMESTAMP || colType == Types.TIMESTAMP_WITH_TIMEZONE) {
						value = rs.getTimestamp(i + 1);
					} else if (colType == Types.DECIMAL || colType == Types.DOUBLE || colType == Types.FLOAT
							|| colType == Types.REAL || colType == Types.NUMERIC) {
						value = rs.getBigDecimal(i + 1);
					} else if (colType == Types.BIGINT || colType == Types.INTEGER || colType == Types.TINYINT) {
						value = rs.getLong(i + 1);
					} else {
						value = rs.getString(i + 1);
					}
					row[i] = value;
				}
				exporter.exportRow(row);
				if (r > 0 && r % 1000 == 0) {
					ui.writeInfoMessage("-- 已导出: " + r);
				}
				r++;
			}
			exporter.finishExport();
			ui.writeInfoMessage("-- 导出" + table + "完成，共导出: " + r);

			rs.close();
			ps.close();
		}
	}

	/** 根据通配符查询表名 */
	public static List<TableExportCondition> generateConditionsByTableNameLike(String type, String url, String user,
			String password, String tableNameLike, UserInterface ui) {
		try {
			if (TYPE_ORACLE.equals(type)) {
				return generateConditionsByTableNameLike_oracle(type, url, user, password, tableNameLike, ui);
			} else if (TYPE_MYSQL.equals(type)) {
				return generateConditionsByTableNameLike_mysql(type, url, user, password, tableNameLike, ui);
			} else {
				ui.writeErrorMessage("不支持该数据库类型");
				return new ArrayList<>();
			}
		} catch (Exception e) {
			ui.writeErrorMessage("查询导出表名" + tableNameLike + "错误：");
			ui.writeErrorMessage(e.getClass().getName() + ": " + e.getMessage());
			return new ArrayList<>();
		}
	}

	private static List<TableExportCondition> generateConditionsByTableNameLike_oracle(String type, String url,
			String user, String password, String tableNameLike, UserInterface ui) throws Exception {
		List<TableExportCondition> list = new ArrayList<>();
		try (Connection conn = createConnection(type, url, user, password)) {
			String sql = "select table_name from user_tables where table_name like ?";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, tableNameLike);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				String tableName = rs.getString(1);
				TableExportCondition cond = new TableExportCondition();
				cond.setTable(tableName);
				list.add(cond);
			}
			rs.close();
			ps.close();
		}
		return list;
	}

	private static List<TableExportCondition> generateConditionsByTableNameLike_mysql(String type, String url,
			String user, String password, String tableNameLike, UserInterface ui) throws Exception {
		List<TableExportCondition> list = new ArrayList<>();
		String schema = url;
		if (url.indexOf('?') > 0) {
			schema = StringUtils.substringBefore(url, "?");
		}
		schema = StringUtils.substringAfterLast(schema, "/");
		try (Connection conn = createConnection(type, url, user, password)) {
			String sql = "select table_name from information_schema.tables where table_schema=? and table_name like ?";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, schema);
			ps.setString(2, tableNameLike);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				String tableName = rs.getString(1);
				TableExportCondition cond = new TableExportCondition();
				cond.setTable(tableName);
				list.add(cond);
			}
			rs.close();
			ps.close();
		}
		return list;
	}

}
