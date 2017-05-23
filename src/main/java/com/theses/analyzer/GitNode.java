package com.theses.analyzer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.graphstream.graph.Node;

public class GitNode {
	
	Node node;
	List<GitNode> neighbor = new ArrayList<GitNode>();
	Iterator<Node> it ;
	double weighted_score = 0;
	double open = 0;
	double close = 0;
	double boost = 0;
	private double merged = 0;
	private double pr = 0;
	boolean isUser = true;
	
	GitNode(Node node) {
		this.node = node;
		it = node.getNeighborNodeIterator();
	}
	
	String getName() {
		return node.getId();
	}
		
	List<GitNode> getNeighbors() {
		return neighbor;
	}
	
	
	boolean isUserNode() {
		return isUser;
	}
	
	double getWeightedScore(){
		return weighted_score;
	}
	
	double getBoost() {
		return boost;
	}
	
	double getOpenCount(){
		return open;
	}

	double getCloseCount(){
		return close;
	}
	
	void setBoost( double p ){
		boost = p;
	}
	
	void setStatusCount( double o, double c ) {
		open = o;
		close = c;
	}
	
	void setPRCount( double m, double p) {
		merged = m;
		pr = p;
	}
	
	void setWeightedScore( double x) {
		weighted_score = x;
	}
	
	void notUserNode() {
		isUser = false;
	}
	
	void addNeighbor( GitNode n ){
		neighbor.add(n);
	}

	double getMerged() {
		return merged;
	}

	void setMerged(double merged) {
		this.merged = merged;
	}

	double getPr() {
		return pr;
	}

	void setPr(double pr) {
		this.pr = pr;
	}
}
