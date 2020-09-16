package mrpanyu.mytoolbox.tools.dbexport.exporter;

import java.io.File;

import mrpanyu.mytoolbox.tools.dbexport.model.TableMetaData;

public abstract class DbExporter implements AutoCloseable {

	protected TableMetaData tableMetaData;
	protected File exportDir;

	public TableMetaData getTableMetaData() {
		return tableMetaData;
	}

	public void setTableMetaData(TableMetaData tableMetaData) {
		this.tableMetaData = tableMetaData;
	}

	public File getExportDir() {
		return exportDir;
	}

	public void setExportDir(File exportDir) {
		this.exportDir = exportDir;
	}

	public abstract void startExport() throws Exception;

	public abstract void exportRow(Object[] rowData) throws Exception;

	public abstract void finishExport() throws Exception;

	public abstract void close() throws Exception;

}
