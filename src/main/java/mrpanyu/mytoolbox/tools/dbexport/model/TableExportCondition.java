package mrpanyu.mytoolbox.tools.dbexport.model;

import java.io.Serializable;

public class TableExportCondition implements Serializable {

	private static final long serialVersionUID = 1L;

	private String table;
	private String sql;

	public String getTable() {
		return table;
	}

	public void setTable(String table) {
		this.table = table;
	}

	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

}
