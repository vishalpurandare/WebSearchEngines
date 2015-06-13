package org.vip.main;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.json.JSONException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.vip.bean.D3Five;
import org.vip.bean.D3Four;
import org.vip.bean.D3Three;
import org.vip.bean.D3Two;
import org.vip.bo.QuerySolrBO;

@RestController
public class RESTController {
    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();
    
    @RequestMapping("/greeting")
    public Greeting greeting(@RequestParam(value="name", defaultValue="World") String name, 
    		@RequestParam(value="address", defaultValue="1248 W Adams Blvd") String address) {
    	return new Greeting(counter.incrementAndGet(),
                            String.format(template, name), address);
    }
    
    @RequestMapping("/sayHello")
    public String helloWord(@RequestParam(value="name", defaultValue = "vishal") String name){
    	return new String("Hello World, from "+name);
    }
    
    @RequestMapping("/getdatad3one") //d3-1: D3 visualization using bar chart with document's content types frequency 
    public String getdatad3one(@RequestParam(value="query", defaultValue = "alaska") String query){
    	System.out.println("Request ------- 1 ##### START");
    	//call the solr service here and run algorithm
    	StringBuilder sb = null;
    	try {
    		Map<String, Float> dataMap = QuerySolrBO.getDataD3One(query);
    		sb = new StringBuilder();
        	sb.append("letter\tfrequency\n");
    		for (String element : dataMap.keySet()) {
    			sb.append(element+"\t"+dataMap.get(element)+"\n");
			}
    		
		} catch (IOException | JSONException e) {
			e.printStackTrace();
		}
    	System.out.println("Request ------- 1 ##### END");
    	return sb.toString();
    }
    
    @RequestMapping("/getdatad3two")
    public D3Two getdatad3two(@RequestParam(value="query", defaultValue = "alaska") String query){
    	System.out.println("Request ------- 2 ##### START");
    	D3Two obj = null;
    	try {
    		obj = QuerySolrBO.getDataD3Two(query);
		} catch (Exception e) {
			e.printStackTrace();
		}
    	System.out.println("Request ------- 2 ##### END");
    	return obj;
    }
    
    @RequestMapping("/getdatad3three")
    public String getdatad3three(@RequestParam(value="query", defaultValue = "snow") String query){
    	System.out.println("Request ------- 3 ##### START");
    	List<D3Three> list = null;
    	StringBuilder sb = null;
    	try {
    		list = QuerySolrBO.getDataD3Three(query);
    		sb = new StringBuilder();
        	sb.append("has,prefers,count\n");
    		for (D3Three d3Three : list) {
    			sb.append(d3Three.getDocument1()+","+d3Three.getDocument2()+","+d3Three.getCommonVal()+"\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
    	System.out.println("Request ------- 3 ##### END");
    	return sb.toString();
    }
    
    @RequestMapping("/getdatad3four")
    public D3Four getdatad3four(@RequestParam(value="query", defaultValue = "snow") String query){
    	System.out.println("Request ------- 4 ##### START");
    	D3Four obj = null;
    	try {
    		obj = QuerySolrBO.getDataD3Four(query);
		} catch (Exception e) {
			e.printStackTrace();
		}
    	System.out.println("Request ------- 4 ##### END");
    	return obj;
    }
    
    @RequestMapping("/getdatad3five")
    public D3Five getdatad3five(@RequestParam(value="query", defaultValue = "snow") String query){
    	System.out.println("Request ------- 5 ##### START");
    	D3Five obj = null;
    	try {
    		obj = QuerySolrBO.getDataD3Five(query);
		} catch (Exception e) {
			e.printStackTrace();
		}
    	System.out.println("Request ------- 5 ##### END");
    	return obj;
    }
    
    @RequestMapping("/getdatad3six")
    public D3Two getdatad3six(@RequestParam(value="query", defaultValue = "alaska") String query){
    	System.out.println("Request ------- 6 ##### START");
    	D3Two obj = null;
    	try {
    		obj = QuerySolrBO.getDataD3Six(query);
		} catch (Exception e) {
			e.printStackTrace();
		}
    	System.out.println("Request ------- 6 ##### END");
    	return obj;
    }
    
}