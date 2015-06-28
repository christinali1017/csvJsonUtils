package jsoncsv;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.json.JSONException;
import org.json.simple.parser.ParseException;

public interface JsonCsvUtils {
	public void jsonToCsv() throws FileNotFoundException, IOException, ParseException, JSONException;
	public void csvToJson() throws FileNotFoundException, IOException;
}
