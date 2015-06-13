package org.vip.main;

public class Greeting {

    private final long id;
    private final String content;
    private final String address;
    
    public Greeting(long id, String content, String address) {
        this.id = id;
        this.content = content;
        this.address = address;
    }

    public long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }
    
    public String getAddress() {
    	return address;
    }
	
}