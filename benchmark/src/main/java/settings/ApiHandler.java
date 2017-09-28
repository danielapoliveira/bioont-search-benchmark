package settings;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class ApiHandler {
	
	//Attributes
	private String REST_URL;
	private String API_KEY;
	private final ObjectMapper mapper = new ObjectMapper();
	final ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
	
	//Constructors
	public ApiHandler(String url, String key)
	{
		REST_URL = url;
		API_KEY = key;
	}
	
	public ApiHandler(String url)
	{
		REST_URL = url;
		API_KEY = "";
	}
	
	public ApiHandler()
	{
		REST_URL = "";
		API_KEY = "";
	}
	
	//Public methods
	public JsonNode jsonToNode(String json) {
		JsonNode root = null;
		try {
			root = mapper.readTree(json);
		} catch (IOException ignored) {

		} 
		return root;
	}

	public String get(String urlToGet) {
		URL url;
		HttpURLConnection conn;
		BufferedReader rd;
		String line;
		String result = "";
		try {
			url = new URL(urlToGet);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Authorization", "apikey token=" + API_KEY);
			conn.setRequestProperty("Accept", "application/json");
			rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			while ((line = rd.readLine()) != null) {
				result += line;
			}
			rd.close();
		} catch (Exception e) {
			//e.printStackTrace();
		}
		return result;
	}
	
	public void setURL(String url) {
		REST_URL = url;
	}
	
	public void setKey(String key) {
		API_KEY = key;
	}
	
	public void setURLandKey(String url, String key) {
		REST_URL = url;
		API_KEY = key;
	}
	
	public String getUrl() {
		return REST_URL;
	}
	
	public String getApiKey() {
		return API_KEY;
	}

}
