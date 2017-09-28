package rankingmodel.structuralMetrices;


import java.util.ArrayList;

/**
 * Created by Anila Sahar Butt.
 */
public class Node {
	
	private String nodeIRI;
	private Node parent;
	private ArrayList<Node> children;
	
	public Node(){
		this.nodeIRI="";
		this.children= new ArrayList<Node>();
	}
	public void setNode(String nodeIRI){
		this.nodeIRI = nodeIRI;
	}
	
	public void setNodeParent(Node node){
		this.parent = node;
	}
	
	public void setNodeChildren(ArrayList<Node> nodeList){
		this.children = nodeList;
	}
	
	public String getNode(){
		return this.nodeIRI;
	}
	
	public Node getNodeParent(){
		return this.parent;
	}
	
	public ArrayList<Node> getNodeChildren(){
		return this.children;
	}
	

}
