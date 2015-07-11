package jsoncsv;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.text.ParseException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;

import au.com.bytecode.opencsv.CSVReader;

import com.google.common.collect.ImmutableMap;


/**
 * Implementation of JsonCsvUtils Interface.
 * @author jchrisli
 *
 */
public class JsonCsvUtilsImpl implements JsonCsvUtils {

    /**
     * Encoding type.
     */
    private static final Charset ENCODING = Charset.forName(System.getProperty("file.encoding"));

    /**
     * Column name of files in CSV file.
     */
    static final String COLUMN_NAME_OF_FILES = "ScenarioName";

    /**
     * Default file name for CSV.
     */
    static final String DEFAULT_FILE_NAME = "Default";

    @Override
    public void jsonToCsv(final String inputPath, final String outputPath)
            throws IOException, ParseException {
         JSONParser parser = new JSONParser();
         try (InputStreamReader inputReader = new InputStreamReader(new FileInputStream(inputPath), ENCODING)) {
             Object object = parser.parse(inputReader);
             ImmutableMap.Builder<String, String> jsonMap =  ImmutableMap.builder();
             jsonObjectToMap((JSONObject) object, jsonMap, "");
             saveJsonMapToCsvAddingFileNameColumn(jsonMap.build(), outputPath);
         } catch (org.json.simple.parser.ParseException e) {
             ParseException parseException = new ParseException("Json format is not valid", 0);
             parseException.initCause(e);
             throw parseException;
         }
    }


    /**
     * Convert JSONObject to Map.
     * @param obj JSONObject.
     * @param jsonMap Map format of JSON.
     * @param prefix prefix of key.
     */
    void jsonObjectToMap(final JSONObject obj, final ImmutableMap.Builder<String, String> jsonMap,
            final String prefix) {
        Set<Map.Entry<String, ?>> entrySet = obj.entrySet();
        for (Map.Entry<String, ?> pair : entrySet) {
            String key = pair.getKey();
            if (pair.getValue() instanceof JSONObject) {
                JSONObject jsonObject = (JSONObject) obj.get(key);
                jsonObjectToMap(jsonObject, jsonMap, prefix + key + ".");
            } else if (pair.getValue() instanceof JSONArray) {
                jsonMap.put(prefix + key, handleCommaForJsonArray((JSONArray) pair.getValue()));
            } else {
                String value = pair.getValue().toString();
                jsonMap.put(prefix + key, value);
            }
        }
    }

    /**
     * Handle comma for JSONArray, for each comma we surround it with ''.
     * @param jsonArray JSONArray.
     * @return String format of JSONArray, and surround comma with ''.
     */
    String handleCommaForJsonArray(final JSONArray jsonArray) {
        return StringUtils.join(jsonArray.toJSONString().split(","), "','");
    }

    /**
     * Save json map to csv file.
     * @param jsonMap map of JSON object.
     * @param outputPath path of CSV file, directory, does not contains file name.
     * @throws IOException "java.io.IOException"
     *         Exceptions produced by failed or interrupted I/O operations.
     */
    void saveJsonMapToCsv(final Map<String, String> jsonMap, final String outputPath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputPath),
                ENCODING))) {
            Set<String> headers = jsonMap.keySet();
            StringBuilder csvOutput = new StringBuilder(StringUtils.join(headers.toArray(), ",") + "\n");
            List<String> rowData = new ArrayList<String>();
            for (String header : headers) {
                String val = "";
                if (jsonMap.get(header) != null) {
                    val = jsonMap.get(header);
                }
                rowData.add(val);
            }
            csvOutput.append(StringUtils.join(rowData.toArray(), ",") + "\n");
            writer.write(csvOutput.toString());
        }
    }


    /**
     * Save JSON map to CSV file, add additional column for the CSV file.
     * @param jsonMap map of JSON object.
     * @param outputPath path of CSV file, directory, does not contains file name.
     * @throws IOException "java.io.IOException"
     *         Exceptions produced by failed or interrupted I/O operations.
     */
    void saveJsonMapToCsvAddingFileNameColumn(final Map<String, String> jsonMap, final String outputPath)
            throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputPath),
                ENCODING))) {
            Set<String> headers = jsonMap.keySet();
            StringBuilder csvOutput = new StringBuilder(StringUtils.join(headers.toArray(), ",") + "\n");
            csvOutput.insert(0, COLUMN_NAME_OF_FILES + ",");
            List<String> rowData = new ArrayList<String>();
            for (String header : headers) {
                rowData.add(jsonMap.get(header));
            }
            csvOutput.append(DEFAULT_FILE_NAME + ",");
            csvOutput.append(StringUtils.join(rowData.toArray(), ",") + "\n");
            writer.write(csvOutput.toString());
        }
    }

    @Override
    public void csvToJson(final String inputPath, final String outputPath) throws IOException {
        try (InputStreamReader inputReader = new InputStreamReader(new FileInputStream(inputPath), ENCODING)) {
               CSVReader reader = new CSVReader(inputReader, ',', '\'');
               String[] headers =  reader.readNext();
               String[] vals = reader.readNext();
               if (headers == null || vals == null) {
                   return;
               }
               while (vals != null) {
                   Optional<String> optional = getCsvFileName(headers, vals);
                   String fileName = optional.isPresent() && optional.get().length() != 0
                           ? optional.get() : getRandomNameForJsonFile();
                   try (OutputStreamWriter outputWriter = new OutputStreamWriter(
                           new FileOutputStream(outputPath + IOUtils.DIR_SEPARATOR + fileName + ".json"), ENCODING)) {
                       outputWriter.write(convertOneCsvLineToJsonObject(headers, vals).toJSONString());
                   }
                   vals = reader.readNext();
               }
           }
    }

    /**
     * Convert one csv line to JSON file.
     * @param headers csv file headers.
     * @param vals csv values.
     * @return return JSONObject.
     */
    JSONObject convertOneCsvLineToJsonObject(final String[] headers, final String[] vals) {
        JSONObject jsonObject = new JSONObject();
        JSONObject target = jsonObject;

        for (int i = 0; i < headers.length; i++) {
            if (headers[i].equals(COLUMN_NAME_OF_FILES)) {
                continue;
            }
            String[] keys = headers[i].split("\\.");
            target = jsonObject;
            for (int j = 0; j < keys.length - 1; j++) {
                if (!target.containsKey(keys[j])) {
                   target.put(keys[j], new JSONObject());
                }
                target = (JSONObject) target.get(keys[j]);
            }
            if (isArray(vals[i])) {
                target.put(keys[keys.length - 1], JSONValue.parse(vals[i]));
            } else {
                target.put(keys[keys.length - 1], vals[i]);
            }
        }
        return jsonObject;
    }

    /**
     * Check is string s is Array.
     * @param s String s
     * @return true if format is array.
     */
    boolean isArray(final String s) {
        return s.indexOf("[") != -1;
    }

    /**DEAULT_FILE_NAME
     * Get CSV file name from CSV columns.
     * @param headers CSV headers.
     * @param vals CSV values.
     * @return Optional<String> of file name if it exist.
     *      Otherwise return Optional<String> of a UUID string.
     */
    Optional<String> getCsvFileName(final String[] headers, final String[] vals) {
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].equals(COLUMN_NAME_OF_FILES)) {
                return Optional.of(vals[i]);
            }
        }
        return  Optional.empty();
    }

    /**
     * Get random name for JSON file.
     * @return random JSON file name.
     */
    String getRandomNameForJsonFile() {
        return UUID.randomUUID().toString();
    }
}

