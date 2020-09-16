package mrpanyu.mytoolbox.tools.dbexport.model;

import java.io.Serializable;

public class ColumnMetaData implements Serializable {

	private static final long serialVersionUID = 1L;

	private String columnName;
	private int columnType;

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public int getColumnType() {
		return columnType;
	}

	public void setColumnType(int columnType) {
		this.columnType = columnType;
	}

}
