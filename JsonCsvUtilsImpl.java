package jsoncsv;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import au.com.bytecode.opencsv.CSVReader;

public class JsonCsvUtilsImpl implements JsonCsvUtils {
	public static void main(String[] args) throws FileNotFoundException, IOException, ParseException {
		JsonCsvUtils j2c = new JsonCsvUtilsImpl();
		j2c.jsonToCsv("jsonArray.json", "res.csv");
		j2c.csvToJson("demo.csv", "demo.json");
	}

	@Override
	public void jsonToCsv(final String inputPath, final String outputPath) throws FileNotFoundException, IOException, ParseException {
		 JSONParser parser = new JSONParser();
	     Object object = parser.parse(new FileReader(inputPath));
		 List<Map<String, String>> jsonList = new ArrayList<Map<String, String>>();
		 //Check if input JSON file is JSONArray or JSONObject
	     if (object.getClass() == JSONObject.class) {
	    	 jsonList = parseJsonObject((JSONObject) object);
	     } else if (object.getClass() == JSONArray.class) {
	    	 jsonList = parseJsonArray((JSONArray) object);
	     }
	     saveToCsv(jsonList, outputPath);	 
	}
	
	@SuppressWarnings({ "unchecked", "resource" })
	@Override
	public void csvToJson(final String inputPath, final String outputPath) throws IOException {
		CSVReader reader = new CSVReader(new FileReader(inputPath));
		FileWriter file = new FileWriter(outputPath);
		
		List<String[]> csv = reader.readAll();
		if (csv.size() != 0) {
			JSONArray jsonArray = new JSONArray();
			List<String> headers = new ArrayList<String>();
			for (String s : csv.get(0)) {
				headers.add(s);
			}
			for (int i = 1; i < csv.size(); i++) {
				JSONObject jsonObject = new JSONObject();
				String[] currentLine = csv.get(i);
				for (int j = 0; j < currentLine.length; j++) {
					jsonObject.put(headers.get(j), currentLine[j]);
				}
				jsonArray.add(jsonObject);
			}
			file.write(jsonArray.toJSONString());
		}
		file.flush();
		file.close();
	}
	
	//Json to csv help functions
	private List<Map<String, String>> parseJsonObject(JSONObject jsonObject) {
		List<Map<String, String>> jsonList =  new ArrayList<Map<String, String>>();
		Map<String, String> jsonMap = new LinkedHashMap<String, String>();
		flattenToMap(jsonObject, jsonMap, "");
		jsonList.add(jsonMap);
		return jsonList;
	}
	
	private List<Map<String, String>> parseJsonArray(JSONArray jsonArray) {
		List<Map<String, String>> jsonList =  new ArrayList<Map<String, String>>();
		for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = (JSONObject) jsonArray.get(i);
            Map<String, String> jsonMap = new LinkedHashMap<String, String>();
            flattenToMap(jsonObject, jsonMap, "");            
    	    jsonList.add(jsonMap);
        }
		return jsonList;
	}
	
    @SuppressWarnings("unchecked")
	private void flattenToMap(JSONObject obj, Map<String, String> jsonMap, String prefix) {
        Set<String> set = obj.keySet();
        for (String key : set) {
            if (obj.get(key).getClass() == JSONObject.class) {
                JSONObject jsonObject = (JSONObject) obj.get(key);
                flattenToMap(jsonObject, jsonMap, key + prefix + ".");
            } else if (obj.get(key).getClass() == JSONArray.class) {
                JSONArray jsonArray = (JSONArray) obj.get(key);
                if (jsonArray.size() < 1) continue;
                flattenToMap(jsonArray, jsonMap, key);
            } else {
                String value = (String) obj.get(key);
                if (value != null && !value.equals("null")) {
                	jsonMap.put(prefix + key, value);
                }
            } 
        }
    }
    
    private void saveToCsv(List<Map<String, String>> jsonList,String fileName) throws IOException {
    	//To have order, change to treeset. 
    	Set<String> headers = new LinkedHashSet<String>();
    	for (Map<String, String> map : jsonList) {
    		headers.addAll(map.keySet());
    	}
    	StringBuilder csvOutput = new StringBuilder(StringUtils.join(headers.toArray(), ",") + "\n");
        for (Map<String, String> map : jsonList) {
        	//get row
        	List<String> rowData = new ArrayList<String>();
        	for (String header : headers) {
        		rowData.add(map.get(header) == null ? "" : map.get(header));
        	}
        	csvOutput.append(StringUtils.join(rowData.toArray(), ",") + "\n");
        }
        
        //write to csv file
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));;
    	writer.write(csvOutput.toString());
    	writer.flush();
    	writer.close();
    }
    
    private void flattenToMap(JSONArray obj, Map<String, String> jsonMap, String prefix) {
        int length = obj.size();
        for (int i = 0; i < length; i++) {
            if (obj.get(i).getClass() == JSONArray.class) {
                JSONArray jsonArray = (JSONArray) obj.get(i);
                if (jsonArray.size() < 1) continue;
                flattenToMap(jsonArray, jsonMap, prefix + i);
            } else if (obj.get(i).getClass() == JSONObject.class) {
                JSONObject jsonObject = (JSONObject) obj.get(i);
                flattenToMap(jsonObject, jsonMap, prefix + ".");
            } else {
                String value = (String) obj.get(i);
                if (value != null) {
                	jsonMap.put(prefix + ".", value);
                }
            }
        }
    }
}
