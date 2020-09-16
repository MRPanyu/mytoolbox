package mrpanyu.mytoolbox.tools.dbexport.model;

import java.util.ArrayList;
import java.util.List;

public class TableMetaData extends TableExportCondition {

	private static final long serialVersionUID = 1L;

	private List<ColumnMetaData> columnMeta = new ArrayList<ColumnMetaData>();

	public List<ColumnMetaData> getColumnMeta() {
		return columnMeta;
	}

	public void setColumnMeta(List<ColumnMetaData> columnMeta) {
		this.columnMeta = columnMeta;
	}

}
