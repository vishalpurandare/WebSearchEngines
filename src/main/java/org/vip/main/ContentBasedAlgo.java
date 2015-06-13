package org.vip.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import snowballstemmer.PorterStemmer;

/**
 * @author CSCI 572: team-33
 * This algorithm is designed extract the query references from all the document corpus
 * Input: query string
 * Output: list of the pages in decreasing order of relevancy
 * Libraries required to build this java file: SnowballStemmer.jar, org.json-20120521.jar
 * 
 */
public class ContentBasedAlgo {
	
	/*
	 * Global data structures holding intermediate results during program execution
	 * 
	 */
	
	//doc->map(word->count), key-document and map of word to its count in that document
	private Map<String, Map<String,Integer>> termFreqMap;
	
	//word->map(doc->wordWeight), key-word and map of doc to wordWeight = tfInDoc * idfVal
	private Map<String, Map<String,Float>> termTfIdfMap;
	
	//word->count, how many docs the word present in
	private Map<String, Integer> wordDocCountMap; 
	
	//word->idf (word and Inverse document Frequency)
	private Map<String, Float> inverseDocFreqMap;
	
	//Total number of documents
	private int countDocs;
	
	//Constructor initialized all global data structures (HashMaps) used for algorithm
	public ContentBasedAlgo() {
		termFreqMap = new HashMap<String, Map<String, Integer>>();
		termTfIdfMap = new HashMap<String, Map<String, Float>>();
		inverseDocFreqMap = new HashMap<String, Float>();
		wordDocCountMap = new HashMap<String, Integer>();
		countDocs = 0;
	}
	
	//Program starts execution here
	public static void main(String[] args) {
		//input the query from command line
		String query = "alaska";
		ContentBasedAlgo testObj = new ContentBasedAlgo();
		final File folder = new File("./post");
		String fillerFile = "";
		
		//return value for word->count for each document
		Map<String, Integer> docWordHash;
		
		/*  
		 * Read all files present in document corpus present in ./PostFiles
		 * PreRequisite: All documents must be posted on the Apache Solr instance running on same machine using CURL
		 */
		for (final File fileEntry : folder.listFiles()) {
			testObj.countDocs++;
			fillerFile = fileEntry.getName();
			String url = "http://192.168.1.16:8983/solr/select?q=attr_stream_name:"
					+ fillerFile + "&wt=json";
			trace(url);
			try {
				//using HTTP get getting the content of the document, returns words and its counts
				docWordHash = httpGet(url);
				
				//Generating hashmap for word and the documents
				for (String keyDocWord : docWordHash.keySet()) {
					if(testObj.wordDocCountMap.get(keyDocWord) != null) {
						int oldCnt = testObj.wordDocCountMap.get(keyDocWord);
						testObj.wordDocCountMap.put(keyDocWord, ++oldCnt);
					} else {
						testObj.wordDocCountMap.put(keyDocWord, 1);
					}
				}
				testObj.termFreqMap.put(fillerFile, docWordHash);
			} catch (IOException e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
			} catch (JSONException e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
		}
		
		trace("-------------------------------------------------------------------------------------------------------------------");
		
		for (String keyWord : testObj.wordDocCountMap.keySet()) {
			int wordCntGlobal = testObj.wordDocCountMap.get(keyWord);
			float idfVal = (float) (Math.log((float)testObj.countDocs/wordCntGlobal) /  Math.log(2));
			testObj.inverseDocFreqMap.put(keyWord, new Float(idfVal));
		}
		Map<String,Float> documentWeightMap = null;
		
		for (String wordKey : testObj.inverseDocFreqMap.keySet()) {
			float idfVal = testObj.inverseDocFreqMap.get(wordKey);
			documentWeightMap = new HashMap<String, Float>();
			
			for(String docKey : testObj.termFreqMap.keySet()) {
				Map<String,Integer> wordInDocMap = testObj.termFreqMap.get(docKey);
				Integer countInDocObj = wordInDocMap.get(wordKey);
				int tfInDoc = 0;
				if(countInDocObj != null) {
					 tfInDoc = countInDocObj;
				}
				float wordWeight = tfInDoc * idfVal;
				documentWeightMap.put(docKey, new Float(wordWeight));
			}
			testObj.termTfIdfMap.put(wordKey, documentWeightMap);
		}
		
		System.out.println("---------------------------------------------------------------------------------------------------------------");
		
		for (String key : testObj.termTfIdfMap.keySet()) {
			System.out.println(key + " --------->> ");
			Map<String,Float> map = testObj.termTfIdfMap.get(key);
			System.out.println(map);
			for (String s : map.keySet()) {
				System.out.println(s + " --> " + map.get(s).floatValue());
			}
		}
		System.out.println("The length of the dochash: " + testObj.termFreqMap.size());
		System.out.println("word doc cnt " + testObj.wordDocCountMap.size());
		
		String queryFiltered = filterQuery(query);
		String queryStemmed = getStemmedQuery(queryFiltered);
		answerQuery(queryStemmed, testObj);
	}
	
	private static String filterQuery(String query) {
		String[] stopWords = {"i", "me", "my", "myself", "we", "our", "ours", "ourselves", "you", "your", "yours",
				 "yourself", "yourselves", "he", "him", "his", "himself", "she", "her", "hers", "herself",
				 "it", "its", "itself", "they", "them", "their", "theirs", "themselves", "what", "which", "who",
				 "whom", "this", "that", "these", "those", "am", "is", "are", "was", "were", "be", "been", "being",
				 "have", "has", "had", "having", "do", "does", "did", "doing", "a", "an", "the", "and", "but", "if", "or",
				 "because", "as", "until", "while", "of", "at", "by", "for", "with", "about", "against", "between", "into",
				 "through", "during", "before", "after", "above", "below", "to", "from", "up", "down", "in", "out", "on",
				 "off", "over", "under", "again", "further", "then", "once", "here", "there", "when", "where", "why", "how",
				 "all", "any", "both", "each", "few", "more", "most", "other", "some", "such", "no", "nor", "not", "only", "own", "same", "so",
				 "than", "too", "very", "s", "t", "can", "will", "just", "don", "should", "now"};
		
		String[] queryWords = query.split("\\s+");
		for (String word : queryWords) {
				if(Arrays.asList(stopWords).contains(word)) {
					query = query.replaceAll(word, "");
				}
		}
		return query;
	}

	public static void answerQuery(String queryString, ContentBasedAlgo testObj){
		String[] queryWords = queryString.split("\\s+");
		
		List<Float> hitTfIdfList = null;
		TreeMap<String, List<Float>> resultDocsMap = null;
		Map<String, List<Float>> tempResultDocsMap = null;
		ListValueComparator bvc = null;
		
		tempResultDocsMap = new HashMap<String, List<Float>>();
		bvc =  new ListValueComparator(tempResultDocsMap);
		resultDocsMap = new TreeMap<String, List<Float>>(bvc);
		for (String queryWord : queryWords) {
			Map<String, Float> sortedDocsMap = testObj.termTfIdfMap.get(queryWord);
			for(String docKey : sortedDocsMap.keySet()) {
				Float val = sortedDocsMap.get(docKey);
				if(val != null && val != 0.0) {
					List<Float> valList = tempResultDocsMap.get(docKey);
					if(valList == null) {
						hitTfIdfList = new ArrayList<Float>(2);
						hitTfIdfList.add(new Float(1));
						hitTfIdfList.add(new Float(val));
					}
					else {
						float prevDocCount = valList.get(0);
						float prevDocTfIdf = valList.get(1);
						prevDocCount++;
						prevDocTfIdf+=val;
						valList.set(0, new Float(prevDocCount));
						valList.set(1, new Float(prevDocTfIdf));
						hitTfIdfList = valList;
					}
					tempResultDocsMap.put(docKey, hitTfIdfList);
				}
			}
		}
		resultDocsMap.putAll(tempResultDocsMap);
		
		System.out.println("#############################################################################");
		for (String docWord : resultDocsMap.keySet()) {
			List<Float> fVal = resultDocsMap.get(docWord);
			System.out.println(docWord + " --->> " + fVal.get(0) + " -- " + fVal.get(1));
		}
	}
	
	public static Map<String, Integer> httpGet(String urlStr) throws IOException,
			JSONException {
		URL url = new URL(urlStr);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Accept", "application/json");

		if (conn.getResponseCode() != 200) {
			throw new IOException(conn.getResponseMessage());
		}
		// Buffer the result into a string
		BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = rd.readLine()) != null) {
			sb.append(line);
		}
		JSONObject jsonObject = new JSONObject(sb.toString());
		JSONObject responseObj = (JSONObject) jsonObject.get("response");
		JSONArray docsArr = responseObj.getJSONArray("docs");
		JSONObject docsObj = (JSONObject) docsArr.get(0);
		JSONArray contentArr = docsObj.getJSONArray("attr_content");
		String content = (String) contentArr.get(0);
		content = filterQuery(content);
		String[] words = content.split("\\s+");
		System.out.println(words.length);
		Map<String, Integer> wordMap = new HashMap<String, Integer>();
		int wordCnt = 0;
		int wordsLength = words.length;
		for (; wordCnt < wordsLength; wordCnt++) {
			boolean wordFound = false;
			words[wordCnt] = getWordStemmed(words[wordCnt]);
			for (String hashWord : wordMap.keySet()) {
				if (hashWord.equals(words[wordCnt].toLowerCase())) {
					int oldVal = wordMap.get(hashWord);
					wordFound = true;
					wordMap.put(hashWord, ++oldVal);
					break;
				}

			}
			if (!wordFound) {
				wordMap.put(words[wordCnt].toLowerCase(), 1);
			}
		}
		for (String hashWord : wordMap.keySet()) {
			System.out.println(hashWord + " : " + wordMap.get(hashWord));
		}
		rd.close();
		conn.disconnect();
		return wordMap;
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
	
	public static void trace(String output) {
		System.out.println(output);
	}
}

class ListValueComparator implements Comparator<String> {
	Map<String, List<Float>> base;
	public ListValueComparator(Map<String, List<Float>> base) {
	    this.base = base;
	}
	public int compare(String a, String b) {
		List<Float> listA = base.get(a);
		List<Float> listB = base.get(b);
		if(listA.get(0).floatValue() > listB.get(0).floatValue()) {
			return -1;
		} else if(listA.get(0).floatValue() == listB.get(0).floatValue()) {
			if(listA.get(1).floatValue() > listB.get(1).floatValue()) {
				return -1;
			} else if(listA.get(1).floatValue() == listB.get(1).floatValue()) {
				return 1;
			}
			return 1;
		}
		return 1;
	}
}