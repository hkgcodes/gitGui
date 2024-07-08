package com.gitReportGenerator.main;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Component
public class GitLabClient {
	 private static final String MOCK_PROJECTS_FILE = "/mock_projects.json";
	 private static final String MOCK_PROJECTS_FIL2 = "/mock_projects_2.json";

	    private static final String MOCK_COMMITS_FILE = "/mock_commits.json";
	    private static final String MOCK_COMMITS_FILE2 = "/mock_commits_2.json";

	    private final ObjectMapper objectMapper = new ObjectMapper();

	    public List<Map<String, Object>> getProjects(String groupId) throws IOException {
	        // Commenting out actual API call
	        // String url = GITLAB_API_URL + "/groups/" + groupId + "/projects";
	        // HttpHeaders headers = new HttpHeaders();
	        // headers.setBearerAuth(PERSONAL_ACCESS_TOKEN);
	        // RequestEntity<Void> request = new RequestEntity<>(headers, HttpMethod.GET, new URI(url));
	        // ResponseEntity<List> response = restTemplate.exchange(request, List.class);
	        // return response.getBody();

	        // Reading from mock JSON file instead
	    	if(groupId.equalsIgnoreCase("ABS11")) {
	        InputStream inputStream = getClass().getResourceAsStream(MOCK_PROJECTS_FILE);
	        return objectMapper.readValue(inputStream, new TypeReference<List<Map<String, Object>>>() {});
	    	}
	    	else {
	    		 InputStream inputStream = getClass().getResourceAsStream(MOCK_PROJECTS_FIL2);
	 	        return objectMapper.readValue(inputStream, new TypeReference<List<Map<String, Object>>>() {});
	    	}
	    }

	    public List<Map<String, Object>> getCommits(String projectId) throws IOException {
	        // Commenting out actual API call
	        // String url = GITLAB_API_URL + "/projects/" + projectId + "/repository/commits?since=" + since;
	        // HttpHeaders headers = new HttpHeaders();
	        // headers.setBearerAuth(PERSONAL_ACCESS_TOKEN);
	        // RequestEntity<Void> request = new RequestEntity<>(headers, HttpMethod.GET, new URI(url));
	        // ResponseEntity<List> response = restTemplate.exchange(request, List.class);
	        // return response.getBody();

	        // Reading from mock JSON file instead
	    	
	    	if(projectId.equalsIgnoreCase("14")){
	        InputStream inputStream = getClass().getResourceAsStream(MOCK_COMMITS_FILE2);
	        return objectMapper.readValue(inputStream, new TypeReference<List<Map<String, Object>>>() {});
	    	}
	    	else {
		        InputStream inputStream = getClass().getResourceAsStream(MOCK_COMMITS_FILE);
		        return objectMapper.readValue(inputStream, new TypeReference<List<Map<String, Object>>>() {});

	    	}
	    }

	    public String calculateSinceDate(int days) {
	        LocalDate date = LocalDate.now().minusDays(days);
	        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE;
	        return date.format(formatter) + "T00:00:00Z";
	    }
}
