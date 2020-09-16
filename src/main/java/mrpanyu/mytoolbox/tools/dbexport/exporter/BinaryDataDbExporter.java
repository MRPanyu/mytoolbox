package mrpanyu.mytoolbox.tools.dbexport.exporter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.Charset;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class BinaryDataDbExporter extends DbExporter {

	private ZipOutputStream zipOut;
	private ObjectOutputStream output;

	@Override
	public void startExport() throws Exception {
		File outputFile = new File(exportDir, tableMetaData.getTable() + ".data");
		zipOut = new ZipOutputStream(new FileOutputStream(outputFile), Charset.forName("UTF-8"));
		zipOut.putNextEntry(new ZipEntry(outputFile.getName()));
		output = new ObjectOutputStream(zipOut);
		output.writeObject(tableMetaData);
	}

	@Override
	public void exportRow(Object[] rowData) throws Exception {
		output.writeObject(rowData);
	}

	@Override
	public void finishExport() throws Exception {
		if (output != null) {
			output.flush();
			zipOut.closeEntry();
		}
	}

	@Override
	public void close() throws Exception {
		if (output != null) {
			output.close();
			zipOut.close();
		}
	}

}
