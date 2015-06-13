package org.vip.bo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.vip.bean.D3Five;
import org.vip.bean.D3Four;
import org.vip.bean.D3Three;
import org.vip.bean.D3Two;
import org.vip.helperbeans.D3FourLink;
import org.vip.helperbeans.D3FourNode;

import com.bericotech.clavin.GeoParser;
import com.bericotech.clavin.GeoParserFactory;
import com.bericotech.clavin.resolver.ResolvedLocation;

public class QuerySolrBO {
	
	public static void main(String[] arg) throws IOException, JSONException {
		List<D3Three> list;
		try {
			list = getDataD3Three("snow");
			for (D3Three d3Three : list) {
				System.out.println(d3Three.getDocument1() + " -- " + d3Three.getDocument2() + " -- " + d3Three.getCommonVal());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static Map<String, Float> getDataD3One(String query) throws IOException, JSONException {
		String url = "http://"+SolrUtils.host+":"+SolrUtils.port+"/solr/select?q="+query+"&wt=json";
		System.out.println(url);
		StringBuilder sb =  SolrUtils.httpGet(url);
		JSONObject jsonObject = new JSONObject(sb.toString());
		JSONObject responseObj = (JSONObject) jsonObject.get("response");
		JSONArray docsArr = responseObj.getJSONArray("docs");
		int docsLength = docsArr.length();
		Map<String, Integer> docTypeMap = new HashMap<String, Integer>();
		for (int i = 0; i < docsLength; i++) {
			JSONObject docsObj = (JSONObject) docsArr.get(i);
			JSONArray contentTypeArr = docsObj.getJSONArray("content_type");
			if(contentTypeArr != null) {
				String contentType = ((String) contentTypeArr.get(0)).split(";")[0];
				if(contentType != null && !contentType.equals("")) {
					int val = 0;
					if(docTypeMap.containsKey(contentType)) {
						val = docTypeMap.get(contentType).intValue();
					}
					docTypeMap.put(contentType, ++val);
				}
			}
		}
		//Create final float frequency map
		Map<String, Float> finalMap = new HashMap<String, Float>();
		int totalCount = 0;
		for (String element : docTypeMap.keySet()) {
			int val = docTypeMap.get(element);
			totalCount += val;
		}
		for (String element : docTypeMap.keySet()) {
			int val = docTypeMap.get(element);
			float freqVal = (float)val / totalCount;
			finalMap.put(element, freqVal);
		}
		return finalMap;
	}

	public static D3Two getDataD3Two(String query) throws Exception {
		String url = "http://"+SolrUtils.host+":"+SolrUtils.port+"/solr/select?q="+query+"&wt=json&rows=" + SolrUtils.rows;
		System.out.println(url);
		StringBuilder sb =  SolrUtils.httpGet(url);
		JSONObject jsonObject = new JSONObject(sb.toString());
		JSONObject responseObj = (JSONObject) jsonObject.get("response");
		JSONArray docsArr = responseObj.getJSONArray("docs");
		int docsLength = docsArr.length();
		D3Two parent = new D3Two();
    	parent.setName("flare");
    	List<D3Two> dChildren = new ArrayList<D3Two>();
		for(int i = 0; i < docsLength; i++) {
			JSONObject docsObj = (JSONObject) docsArr.get(i);
			if(docsObj.has("attr_content")) {
				JSONArray contentArr = docsObj.getJSONArray("attr_content");
				String attrStreamName = (String) docsObj.getJSONArray("attr_stream_name").get(0);
				String content = (String) contentArr.get(0);
				// using CLAVIN to get the locations related to the document
				GeoParser geo = GeoParserFactory.getDefault("./IndexDirectory");
				List<ResolvedLocation> resolvedLocations = geo.parse(content);
				D3Two child = new D3Two();
				child.setName(attrStreamName);
				List<D3Two> dGChildren = new ArrayList<D3Two>();
				Map<String, Integer> locationCountMap = new HashMap<String, Integer>();
				for (ResolvedLocation location : resolvedLocations) {
					String locName = location.getMatchedName();
					int cnt = 0;
					if(locationCountMap.containsKey(locName)) {
						cnt = locationCountMap.get(locName).intValue();
					}
					locationCountMap.put(locName, ++cnt);
				}
				for(String locName : locationCountMap.keySet()) {
					D3Two gChild = new D3Two();
					gChild.setName(locName);
					gChild.setSize(locationCountMap.get(locName));
					dGChildren.add(gChild);
				}
				child.setChildren(dGChildren);
				dChildren.add(child);
			}
		}
		parent.setChildren(dChildren);
		return parent;
	}
	
	public static List<D3Three> getDataD3Three(String query) throws Exception {
		List<D3Three> finalObj = new ArrayList<D3Three>();
		Map<String, Set<String>> docLocMap = SolrUtils.getDocLocationMap(query);
		TreeMap<String, Set<String>> treeMap = new TreeMap<String, Set<String>>(docLocMap);
		D3Three obj = null;
		for(Map.Entry<String, Set<String>> entry1: treeMap.entrySet()) {
			   String key1 = entry1.getKey();
			   int hash1 = System.identityHashCode(key1);
			   Set<String> locations1 = entry1.getValue();
			   for(Map.Entry<String, Set<String>> entry2: treeMap.entrySet()) {
				   Set<String> tempLocations = new HashSet<String>();
				   tempLocations.addAll(locations1);
				   obj = new D3Three();
			       String key2 = entry2.getKey();
			       Set<String> locations2  = entry2.getValue();
			       int hash2 = System.identityHashCode(key2);
			       if (hash1 >= hash2) continue;
			       tempLocations.retainAll(locations2);
			       obj.setDocument1(key1);
			       obj.setDocument2(key2);
			       obj.setCommonVal(tempLocations.size());
			       finalObj.add(obj);
			   }
		}
		return finalObj;
	}
	
	public static D3Four getDataD3Four(String query) throws Exception {
		List<D3Three> listDocRelation;
		D3Four obj = new D3Four();
		Map<String, Set<String>> docLocMap = SolrUtils.getDocLocationMap(query);
		listDocRelation = getDataD3Three(query);
		int index = 0;
		List<D3FourNode> listNodes = new ArrayList<D3FourNode>();
		Map<String, Integer> docIndexMap = new HashMap<String, Integer>();
		for (String docKey : docLocMap.keySet()) {
			D3FourNode node = new D3FourNode();
			node.setName(docKey);
			node.setIndex(index);
			node.setGroup(index/2);
			docIndexMap.put(docKey, index);
			listNodes.add(node);
			index++;
		}
		List<D3FourLink> listLinks = new ArrayList<D3FourLink>();
		for (D3Three d3Three : listDocRelation) {
			D3FourLink link = new D3FourLink();
			String doc1 = d3Three.getDocument1();
			String doc2 = d3Three.getDocument2();
			int val = d3Three.getCommonVal();
			link.setSource(docIndexMap.get(doc1).intValue());
			link.setTarget(docIndexMap.get(doc2).intValue());
			link.setValue(val);
			D3FourLink linkRev = new D3FourLink();
			linkRev.setSource(docIndexMap.get(doc2).intValue());
			linkRev.setTarget(docIndexMap.get(doc1).intValue());
			linkRev.setValue(val);
			listLinks.add(link);
			listLinks.add(linkRev);
		}
		obj.setLinks(listLinks);
		obj.setNodes(listNodes);
		return obj;
	}

	public static D3Five getDataD3Five(String query) throws Exception {
		Map<String, Set<String>> docLocMap = SolrUtils.getDocLocationMap(query);
		D3Five obj = new D3Five();
		obj.setName("Geolocations");
		obj.setUser_id(0);
		Map<Character, Set<String>> charLocationMap = new HashMap<Character, Set<String>>();
 		Map<String, Set<String>> locationDocMap = new HashMap<String, Set<String>>();
		for(String docName : docLocMap.keySet()) {
			Set<String> docLocations = docLocMap.get(docName);
			for (String location : docLocations) {
				char chAtStart = location.charAt(0);
				if(charLocationMap.containsKey(chAtStart)) {
					Set<String> locs = charLocationMap.get(chAtStart);
					locs.add(location);
					charLocationMap.put(chAtStart, locs);
				} else {
					Set<String> locs = new HashSet<String>();
					locs.add(location);
					charLocationMap.put(chAtStart, locs);
				}
				if(locationDocMap.containsKey(location)) {
					Set<String> docs = locationDocMap.get(location);
					docs.add(docName);
					locationDocMap.put(location, docs);
				} else {
					Set<String> docs = new HashSet<String>();
					docs.add(docName);
					locationDocMap.put(location, docs);
				}
			}
		}
		int childIndex = 0;
		List<D3Five> childObjList = new ArrayList<D3Five>();
		for(char locationChar : charLocationMap.keySet()) {
			D3Five objChild = new D3Five();
			objChild.setName(""+locationChar);
			objChild.setUser_id(childIndex);
			childObjList.add(objChild);
			childIndex++;
			Set<String> locations = charLocationMap.get(locationChar);
			List<D3Five> gChildObjList = new ArrayList<D3Five>();
			int gChildIndex = 0;
			for (String location : locations) {
				D3Five objGChild = new D3Five();
				objGChild.setName(location);
				objGChild.setUser_id(gChildIndex);
				gChildObjList.add(objGChild);
				gChildIndex++;
				Set<String> documents = locationDocMap.get(location);
				int ggChildIndex = 0;
				List<D3Five> ggChildObjList = new ArrayList<D3Five>();
				for (String doc : documents) {
					D3Five objGGChild = new D3Five();
					objGGChild.setName(doc);
					objGGChild.setUser_id(ggChildIndex);
					ggChildObjList.add(objGGChild);
					ggChildIndex++;
				}
				objGChild.setChildren(ggChildObjList);
			}
			objChild.setChildren(gChildObjList);
		}
		obj.setChildren(childObjList);
		return obj;
	}
	
	public static D3Two getDataD3Six(String query) throws Exception {
		String url = "http://"+SolrUtils.host+":"+SolrUtils.port+"/solr/select?q="+query+"&wt=json&rows=" + SolrUtils.rows;
		System.out.println(url);
		StringBuilder sb =  SolrUtils.httpGet(url);
		JSONObject jsonObject = new JSONObject(sb.toString());
		JSONObject responseObj = (JSONObject) jsonObject.get("response");
		JSONArray docsArr = responseObj.getJSONArray("docs");
		int docsLength = docsArr.length();
		Map<String, Map<String, Integer>> docWordCntMap = new HashMap<String, Map<String, Integer>>();
		for(int i = 0; i < docsLength; i++) {
			JSONObject docsObj = (JSONObject) docsArr.get(i);
			String attrStreamName = "";
			String content = "";
			if(docsObj.has("attr_content")) {
				JSONArray contentArr = docsObj.getJSONArray("attr_content");
				//Using the set as not to duplicate the locations for this document
				attrStreamName = (String) docsObj.getJSONArray("attr_stream_name").get(0);
				content = (String) contentArr.get(0);
			} else if (docsObj.has("content")) {
				JSONArray contentArr = docsObj.getJSONArray("content");
				//Using the set as not to duplicate the locations for this document
				attrStreamName = (String) docsObj.getJSONArray("attr_stream_name").get(0);
				content = (String) contentArr.get(0);
			}
			content = content.toLowerCase();
			
			String[] words = content.split("\\s+");
			Map<String, Integer> wordMap = new HashMap<String, Integer>();
			int wordCnt = 0;
			int wordsLength = words.length;
			for (; wordCnt < wordsLength; wordCnt++) {
				boolean wordFound = false;
				words[wordCnt] = words[wordCnt].toLowerCase();
				words[wordCnt] = SolrUtils.filterWord(words[wordCnt].trim());
				if(words[wordCnt].length() < 3) {
					continue;
				}
				if(words[wordCnt].trim().equals("")) {
					continue;
				}
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
			HashMap<String, Integer> sortedWordMap = SolrUtils.sortHashMapByValues((HashMap<String, Integer>) wordMap);
			docWordCntMap.put(attrStreamName, sortedWordMap);
		}
		
		D3Two parent = new D3Two();
    	parent.setName("flare");
    	List<D3Two> dChildren = new ArrayList<D3Two>();
    	for (String docName : docWordCntMap.keySet()) {
			D3Two child = new D3Two();
			child.setName(docName);
			List<D3Two> dGChildren = new ArrayList<D3Two>();
			Map<String, Integer> wordCntMap = docWordCntMap.get(docName);
			int count = 0;
			for (String word : wordCntMap.keySet()) {
				count++;
				if(count > 20) {
					break;
				}
				D3Two gChild = new D3Two();
				gChild.setName(word);
				gChild.setSize(wordCntMap.get(word));
				dGChildren.add(gChild);
			}
			child.setChildren(dGChildren);
			dChildren.add(child);
		}
    	parent.setChildren(dChildren);
		return parent;
	}
}