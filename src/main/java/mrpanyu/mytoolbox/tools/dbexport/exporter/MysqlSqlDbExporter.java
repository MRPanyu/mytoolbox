package mrpanyu.mytoolbox.tools.dbexport.exporter;

import java.io.File;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import mrpanyu.mytoolbox.tools.dbexport.model.ColumnMetaData;

public class MysqlSqlDbExporter extends DbExporter {

	private static final int ROWS_PER_BATCH = 1000;

	private String insertHead;
	private String insertRowFormat;
	private PrintWriter writer;
	private int rowNum = 0;

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
		sb.append(") VALUES");
		insertHead = sb.toString();

		sb = new StringBuilder();
		sb.append("\t(");
		for (ColumnMetaData cm : tableMetaData.getColumnMeta()) {
			sb.append("%s,");
		}
		sb.deleteCharAt(sb.length() - 1);
		sb.append(")");
		insertRowFormat = sb.toString();
	}

	@Override
	public void exportRow(Object[] rowData) throws Exception {
		if (rowNum % ROWS_PER_BATCH == 0) {
			if (rowNum > 0) {
				writer.println(";");
				writer.println();
			}
			writer.println(insertHead);
		} else if (rowNum > 0) {
			writer.println(",");
		}

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
				param = "'" + dateStr + "'";
			} else if (type == Types.TIME) {
				String timeStr = new SimpleDateFormat("HH:mm:ss").format(value);
				param = "'" + timeStr + "'";
			} else if (type == Types.TIMESTAMP) {
				String dateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(value);
				param = "'" + dateStr + "'";
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
		String sql = String.format(insertRowFormat, params.toArray());
		writer.print(sql);

		rowNum++;
	}

	@Override
	public void finishExport() throws Exception {
		if (rowNum > 0) {
			writer.println(";");
			writer.println();
		}
	}

	@Override
	public void close() throws Exception {
		if (writer != null) {
			writer.close();
		}
	}

	private String textLiteral(String str) {
		return "'" + str.replace("'", "''").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t") + "'";
	}

}
