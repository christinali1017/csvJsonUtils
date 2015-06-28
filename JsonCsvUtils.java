package jsoncsv;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.json.JSONException;
import org.json.simple.parser.ParseException;

public interface JsonCsvUtils {
	public void jsonToCsv(final String inputPath, final String outputPath) throws FileNotFoundException, IOException, ParseException, JSONException;
	public void csvToJson(final String inputPath, final String outputPath) throws FileNotFoundException, IOException;
}
