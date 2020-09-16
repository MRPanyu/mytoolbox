package mrpanyu.mytoolbox.tools.dbexport.utils;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;

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

}
