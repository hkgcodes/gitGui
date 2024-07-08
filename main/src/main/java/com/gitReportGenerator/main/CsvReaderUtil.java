package com.gitReportGenerator.main;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class CsvReaderUtil {

	 public Map<String, String> readCsv(String filePath) throws IOException, CsvException {
	        Map<String, String> groupIdToNameMap = new HashMap<>();
	        try (CSVReader csvReader = new CSVReader(new FileReader(filePath))) {
	            List<String[]> rows = csvReader.readAll();
	            for (String[] row : rows) {
	                if (row.length >= 2) {
	                    String groupId = row[0].trim();
	                    String groupName = row[1].trim();
	                    groupIdToNameMap.put(groupId, groupName);
	                }
	            }
	        }
	        return groupIdToNameMap;
	    }
	 
}