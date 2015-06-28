###csvJsonUtils

#####Json to csv:

	JsonCsvUtils j2c = new JsonCsvUtilsImpl();
	j2c.jsonToCsv(inputPath, outputPath);

	eg: j2c.jsonToCsv("jsonArray.json", "res.csv");
		


#####CSV to JSON:
	JsonCsvUtils j2c = new JsonCsvUtilsImpl();
	j2c.csvToJson(inputPath, outputPath);

	eg: j2c.csvToJson("demo.csv", "demo.json");
