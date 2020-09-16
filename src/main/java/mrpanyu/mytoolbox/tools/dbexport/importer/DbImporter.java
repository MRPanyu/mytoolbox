package mrpanyu.mytoolbox.tools.dbexport.importer;

import java.io.File;
import java.sql.Connection;

import mrpanyu.mytoolbox.framework.api.UserInterface;

public abstract class DbImporter {

	protected Connection connection;
	protected boolean truncateBeforeImport = false;
	protected File importDir;
	protected UserInterface ui;

	public Connection getConnection() {
		return connection;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	public boolean isTruncateBeforeImport() {
		return truncateBeforeImport;
	}

	public void setTruncateBeforeImport(boolean truncateBeforeImport) {
		this.truncateBeforeImport = truncateBeforeImport;
	}

	public File getImportDir() {
		return importDir;
	}

	public void setImportDir(File importDir) {
		this.importDir = importDir;
	}

	public UserInterface getUi() {
		return ui;
	}

	public void setUi(UserInterface ui) {
		this.ui = ui;
	}

	public abstract void importData() throws Exception;

}
