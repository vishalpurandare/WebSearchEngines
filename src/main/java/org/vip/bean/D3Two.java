package org.vip.bean;

import java.util.List;

public class D3Two {
	private String name;
	private List<D3Two> children;
	private long size;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<D3Two> getChildren() {
		return children;
	}

	public void setChildren(List<D3Two> children) {
		this.children = children;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}
}
