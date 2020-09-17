package mrpanyu.mytoolbox.tools.dbexport.exporter;

import java.io.File;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import mrpanyu.mytoolbox.tools.dbexport.model.ColumnMetaData;

public class OracleSqlDbExporter extends DbExporter {

	private String format;
	private PrintWriter writer;

	@Override
	public void startExport() throws Exception {
		String table = tableMetaData.getTable();
		File f = new File(exportDir, table + ".sql");
		writer = new PrintWriter(f, "UTF-8");

		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO ").append(table).append(" (");
		for (ColumnMetaData cm : tableMetaData.getColumnMeta()) {
			sb.append(cm.getColumnName()).append(",");
		}
		sb.deleteCharAt(sb.length() - 1);
		sb.append(") VALUES (");
		for (ColumnMetaData cm : tableMetaData.getColumnMeta()) {
			sb.append("%s,");
		}
		sb.deleteCharAt(sb.length() - 1);
		sb.append(");");
		format = sb.toString();
	}

	@Override
	public void exportRow(Object[] rowData) throws Exception {
		List<String> params = new ArrayList<>();
		for (int i = 0; i < rowData.length; i++) {
			ColumnMetaData cm = tableMetaData.getColumnMeta().get(i);
			int type = cm.getColumnType();
			Object value = rowData[i];
			String param = null;
			if (value == null) {
				param = "NULL";
			} else if (type == Types.DATE) {
				String dateStr = new SimpleDateFormat("yyyy-MM-dd").format(value);
				param = "to_date('" + dateStr + "', 'YYYY-MM-DD')";
			} else if (type == Types.TIME) {
				String timeStr = new SimpleDateFormat("HH:mm:ss").format(value);
				param = "to_time('" + timeStr + "', 'HH24:MI:SS')";
			} else if (type == Types.TIMESTAMP) {
				String dateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(value);
				param = "to_date('" + dateStr + "', 'YYYY-MM-DD HH24:MI:SS')";
			} else if (type == Types.DECIMAL || type == Types.DOUBLE || type == Types.FLOAT || type == Types.REAL
					|| type == Types.NUMERIC) {
				BigDecimal num = (BigDecimal) value;
				param = num.toPlainString();
			} else if (type == Types.BIGINT || type == Types.INTEGER || type == Types.SMALLINT
					|| type == Types.TINYINT) {
				param = value.toString();
			} else {
				String str = value.toString();
				param = textLiteral(str);
			}
			params.add(param);
		}
		String sql = String.format(format, params.toArray());
		writer.println(sql);
	}

	@Override
	public void finishExport() throws Exception {
	}

	@Override
	public void close() throws Exception {
		if (writer != null) {
			writer.close();
		}
	}

	private String textLiteral(String str) {
		StringBuilder sb = new StringBuilder();
		int beginIndex = 0;
		while (beginIndex < str.length()) {
			int endIndex = Math.min(beginIndex + 1000, str.length());
			String part = str.substring(beginIndex, endIndex);
			part = part.replace("'", "''");
			part = part.replace("&", "'||'&'||'");
			sb.append("'").append(part).append("'");
			if (endIndex < str.length()) {
				sb.append("||");
			}
			beginIndex = endIndex;
		}
		if (str.length() > 1000) {
			sb.insert(0, "to_clob('')||");
		}
		return sb.toString();
	}

}
