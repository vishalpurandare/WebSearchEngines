package org.vip.bean;

import java.util.List;

import org.vip.helperbeans.D3FourLink;
import org.vip.helperbeans.D3FourNode;

public class D3Four {
	
	List<D3FourNode> nodes;
	List<D3FourLink> links;
	
	public List<D3FourNode> getNodes() {
		return nodes;
	}
	public void setNodes(List<D3FourNode> nodes) {
		this.nodes = nodes;
	}
	public List<D3FourLink> getLinks() {
		return links;
	}
	public void setLinks(List<D3FourLink> links) {
		this.links = links;
	}
	
}
