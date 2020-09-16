package mrpanyu.mytoolbox.tools.dbexport.importer;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.zip.ZipInputStream;

import mrpanyu.mytoolbox.tools.dbexport.model.ColumnMetaData;
import mrpanyu.mytoolbox.tools.dbexport.model.TableMetaData;

public class BinaryDataDbImporter extends DbImporter {

	@Override
	public void importData() throws Exception {
		for (File f : importDir.listFiles()) {
			if (f.isFile() && f.getName().endsWith(".data")) {
				importTableData(f);
			}
		}
	}

	private void importTableData(File dataFile) throws Exception {
		try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(dataFile), Charset.forName("UTF-8"));) {
			zipIn.getNextEntry();
			try (ObjectInputStream input = new ObjectInputStream(zipIn);) {
				TableMetaData tableMetaData = (TableMetaData) input.readObject();
				String table = tableMetaData.getTable();
				ui.writeInfoMessage("开始导入: " + table);
				String sql = null;
				PreparedStatement ps = null;
				if (truncateBeforeImport) {
					ui.writeInfoMessage("-- 导入前进行清空表数据");
					sql = "truncate table " + table;
					ps = connection.prepareStatement(sql);
					ps.executeUpdate();
					ps.close();
				}
				StringBuilder sqlb1 = new StringBuilder();
				StringBuilder sqlb2 = new StringBuilder();
				for (ColumnMetaData col : tableMetaData.getColumnMeta()) {
					sqlb1.append(col.getColumnName()).append(",");
					sqlb2.append("?,");
				}
				sqlb1.deleteCharAt(sqlb1.length() - 1);
				sqlb2.deleteCharAt(sqlb2.length() - 1);
				sql = "insert into " + table + " (" + sqlb1 + ") values (" + sqlb2 + ")";
				connection.setAutoCommit(false);
				ps = connection.prepareStatement(sql);
				int r = 0;
				while (true) {
					Object[] row = null;
					try {
						row = (Object[]) input.readObject();
					} catch (EOFException e) {
						break;
					}
					for (int i = 0; i < row.length; i++) {
						ColumnMetaData col = tableMetaData.getColumnMeta().get(i);
						Object value = row[i];
						int type = col.getColumnType();
						if (type == Types.DATE) {
							ps.setDate(i + 1, (java.sql.Date) value);
						} else if (type == Types.TIME || type == Types.TIME_WITH_TIMEZONE) {
							ps.setTime(i + 1, (java.sql.Time) value);
						} else if (type == Types.TIMESTAMP || type == Types.TIMESTAMP_WITH_TIMEZONE) {
							ps.setTimestamp(i + 1, (java.sql.Timestamp) value);
						} else if (type == Types.DECIMAL || type == Types.DOUBLE || type == Types.FLOAT
								|| type == Types.REAL || type == Types.NUMERIC) {
							ps.setBigDecimal(i + 1, (BigDecimal) value);
						} else if (type == Types.BIGINT || type == Types.INTEGER || type == Types.TINYINT) {
							ps.setLong(i + 1, (Long) value);
						} else {
							ps.setString(i + 1, (String) value);
						}
					}
					ps.executeUpdate();
					if (r > 0 && r % 1000 == 0) {
						ui.writeInfoMessage("-- 导入行数: " + r);
						connection.commit();
					}
					r++;
				}
				ui.writeInfoMessage("-- 导入" + table + "完成，共导入: " + r);
				ps.close();
				connection.commit();
			}
		}
	}

}
