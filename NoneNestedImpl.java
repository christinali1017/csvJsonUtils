package jsoncsv;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.json.CDL;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.simple.parser.JSONParser;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Json csv utils implementation for none nested json and csv.
 * @author jchrisli
 *
 */
public class NoneNestedImpl implements JsonCsvUtils {

    /**
     * Encoding type.
     */
    private static final Charset ENCODING = Charset.defaultCharset();

    @Override
    public void jsonToCsv(final String inputPath, final String outputPath)
            throws FileNotFoundException, IOException {
        try (InputStreamReader inputReader = new InputStreamReader(new FileInputStream(inputPath), ENCODING);
             OutputStreamWriter outputWriter = new OutputStreamWriter(new FileOutputStream(outputPath), ENCODING)) {
            JSONParser jsonParser = new JSONParser();
            JSONArray jsonArray = new JSONArray(jsonParser.parse(inputReader).toString());
            outputWriter.write(CDL.toString(jsonArray));
        } catch (org.json.simple.parser.ParseException e) {
        	//TODO
        } catch (JSONException e) {
            //TODO
            e.printStackTrace();
        }
    }

    @Override
    public void csvToJson(final String inputPath, final String outputPath)
            throws FileNotFoundException, IOException {
        try (InputStreamReader inputReader = new InputStreamReader(new FileInputStream(inputPath), ENCODING);
                OutputStreamWriter outputWriter = new OutputStreamWriter(new FileOutputStream(outputPath), ENCODING)) {
            CSVReader reader = new CSVReader(inputReader);
            List<String[]> csv = reader.readAll();
            StringBuilder csvString = new StringBuilder();
            for (String[] arr : csv) {
                csvString.append(StringUtils.join(arr, ",") + "\n");
            }
            JSONArray array = CDL.toJSONArray(csvString.toString());
            outputWriter.write(array.toString(2));
        } catch (JSONException e) {
            //TODO
            e.printStackTrace();
        }
    }
    
	public static void main(String[] args) throws FileNotFoundException, IOException, ParseException, org.json.simple.parser.ParseException {
		JsonCsvUtils j2c = new NoneNestedImpl();
		j2c.jsonToCsv("jsonArray.json", "res.csv");
		j2c.csvToJson("demo.csv", "demo.json");
	}

}

