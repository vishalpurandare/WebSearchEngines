package org.vip.bean;

import java.util.List;

public class D3Five {

	private int user_id;
	private String name;
	private List<D3Five> children;
	
	public int getUser_id() {
		return user_id;
	}
	public void setUser_id(int user_id) {
		this.user_id = user_id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<D3Five> getChildren() {
		return children;
	}
	public void setChildren(List<D3Five> children) {
		this.children = children;
	}
	
}
