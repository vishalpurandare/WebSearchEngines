package org.vip.bo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import snowballstemmer.PorterStemmer;

import com.bericotech.clavin.GeoParser;
import com.bericotech.clavin.GeoParserFactory;
import com.bericotech.clavin.resolver.ResolvedLocation;

public class SolrUtils {
	
	public static String[] stopWords = {"i", "me", "my", "myself", "we", "our", "ours", "ourselves", "you", "your", "yours",
			 "yourself", "yourselves", "he", "him", "his", "himself", "she", "her", "hers", "herself",
			 "it", "its", "itself", "they", "them", "their", "theirs", "themselves", "what", "which", "who",
			 "whom", "this", "that", "these", "those", "am", "is", "are", "was", "were", "be", "been", "being",
			 "have", "has", "had", "having", "do", "does", "did", "doing", "a", "an", "the", "and", "but", "if", "or",
			 "because", "as", "until", "while", "of", "at", "by", "for", "with", "about", "against", "between", "into",
			 "through", "during", "before", "after", "above", "below", "to", "from", "up", "down", "in", "out", "on",
			 "off", "over", "under", "again", "further", "then", "once", "here", "there", "when", "where", "why", "how",
			 "all", "any", "both", "each", "few", "more", "most", "other", "some", "such", "no", "nor", "not", "only", "own", "same", "so",
			 "than", "too", "very", "s", "t", "can", "will", "just", "don", "should", "now"};
	
	/* Solr Host to connect to */
	public static String host = "10.120.76.51"; //"192.168.1.13"; //"192.168.1.13"; //"10.120.67.209";
	/* Port to connect to */
	public static String port = "8983";
	/* How many rows from records returned to consider for visualization */
	public static int rows = 100;
	
	public static LinkedHashMap<String, Integer> sortHashMapByValues(HashMap<String, Integer> passedMap) {
		   List<String> mapKeys = new ArrayList<String>(passedMap.keySet());
		   List<Integer> mapValues = new ArrayList<Integer>(passedMap.values());
		   Collections.sort(mapValues);
		   Collections.reverse(mapValues);
		   Collections.sort(mapKeys);
		   Collections.reverse(mapKeys);
		   LinkedHashMap<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
		   Iterator<Integer> valueIt = mapValues.iterator();
		   while (valueIt.hasNext()) {
		       Object val = valueIt.next();
		       Iterator<String> keyIt = mapKeys.iterator();
		       while (keyIt.hasNext()) {
		           Object key = keyIt.next();
		           String comp1 = passedMap.get(key).toString();
		           String comp2 = val.toString();
		           if (comp1.equals(comp2)){
		               passedMap.remove(key);
		               mapKeys.remove(key);
		               sortedMap.put((String)key, (Integer)val);
		               break;
		           }
		       }
		   }
		   return sortedMap;
	}
	
	public static String filterQuery(String query) {
		String[] queryWords = query.split("\\s+");
		for (String word : queryWords) {
				if(Arrays.asList(stopWords).contains(word)) {
					query = query.replaceAll(word, "");
				}
		}
		return query;
	}

	public static String filterWord(String word) {
		if(Arrays.asList(stopWords).equals(word)) {
			return "";
		} else {
			//remove punchuation marks from the string
			word = word.replaceAll("[^a-zA-Z ]", "");
		}
		return word;
	}
	

	public static String getWordStemmed(String word) {
		PorterStemmer stemmer = new PorterStemmer();
		stemmer.setCurrent(word);
		if(stemmer.stem()) {
			return word;
		}
		return word;
	}
	
	public static String getStemmedQuery(String query) {
		String[] words = query.split("\\s+");
		for(String word : words) {
			String newWord = getWordStemmed(word);
			query = query.replaceAll(word, newWord);
		}
		return query;
	}
	

	public static Map<String, Set<String>> getDocLocationMap(String query) throws Exception {
		String url = "http://"+host+":"+port+"/solr/select?q="+query+"&wt=json&rows=" + rows;
		System.out.println(url);
		StringBuilder sb =  httpGet(url);
		JSONObject jsonObject = new JSONObject(sb.toString());
		JSONObject responseObj = (JSONObject) jsonObject.get("response");
		JSONArray docsArr = responseObj.getJSONArray("docs");
		int docsLength = docsArr.length();
		//Key: Document and Value: Set of locations
		Map<String, Set<String>> docLocMap = new HashMap<String, Set<String>>();
		Set<String> locationsSet = null;
		for(int i = 0; i < docsLength; i++) {
			locationsSet = new HashSet<String>();
			JSONObject docsObj = (JSONObject) docsArr.get(i);
			if(docsObj.has("attr_content")) {
				JSONArray contentArr = docsObj.getJSONArray("attr_content");
				//Using the set as not to duplicate the locations for this document
				String attrStreamName = (String) docsObj.getJSONArray("attr_stream_name").get(0);
				String content = (String) contentArr.get(0);
				// using CLAVIN to get the locations related to the document
				GeoParser geo = GeoParserFactory.getDefault("./IndexDirectory");
				String input = content;
				List<ResolvedLocation> resolvedLocations = geo.parse(input);
				for (ResolvedLocation resolvedLocation : resolvedLocations) {
					locationsSet.add(resolvedLocation.getMatchedName().toLowerCase());
				}
				docLocMap.put(attrStreamName, locationsSet);
			} else if (docsObj.has("content")) {
				JSONArray contentArr = docsObj.getJSONArray("content");
				//Using the set as not to duplicate the locations for this document
				String attrStreamName = (String) docsObj.getJSONArray("attr_stream_name").get(0);
				String content = (String) contentArr.get(0);
				//Using CLAVIN to get the locations related to the document
				GeoParser geo = GeoParserFactory.getDefault("./IndexDirectory");
				String input = content;
				List<ResolvedLocation> resolvedLocations = geo.parse(input);
				for (ResolvedLocation resolvedLocation : resolvedLocations) {
					locationsSet.add(resolvedLocation.getMatchedName());
				}
				docLocMap.put(attrStreamName, locationsSet);
			}
		}
		return docLocMap;
	}
	
	public static StringBuilder httpGet(String urlStr) {		
		BufferedReader rd = null;
		HttpURLConnection conn = null;
		StringBuilder sb = new StringBuilder();
		try {
			URL url = new URL(urlStr);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");
			if (conn.getResponseCode() != 200) {
				throw new IOException(conn.getResponseMessage());
			}
			// Buffer the result into a string
			rd = new BufferedReader(new InputStreamReader(
					conn.getInputStream()));
			String line;
			while ((line = rd.readLine()) != null) {
				sb.append(line);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				rd.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			conn.disconnect();
		}
		return sb;
	}
	
	
}
