package com.theses.analyzer;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.graphstream.algorithm.*;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.*;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.Viewer;

import com.jsoniter.*;
import com.jsoniter.any.Any;

public class GraphExplore {
	
	double MAX_POINT = .50;
	int EVENT_ISSUES = 0, EVENT_PR = 1, ALL = 0, SCORES_ONLY = 1, COUNTER_ONLY = 2, PR_ONLY = 3, ISSUES_ONLY = 4;
	Graph graph = new MultiGraph("Collaboration Graph");
	List<GitNode> gitList = new ArrayList<GitNode>();
	Map<String, Double> authorityScores = new HashMap<String, Double>();
	Map<String, Double> hubScores = new HashMap<String, Double>();
	
	GraphExplore() {
		graph.setStrict( false );
		graph.setAutoCreate( true );
		parseAndGraph( loadFromFile("C:/data/small_issues.json"), EVENT_ISSUES);
		setGraph();
		calculateScores();
		calculateWeightedScores();
		
		// optional prints
//		getTopRepositories( false ); // before weighted
//		getTopRepositories( true ); // after weighted
		printResult(SCORES_ONLY); // for debug, print after weighted along with other data.
	}
	
	public void calculateWeightedScores(){
		for( GitNode n : gitList ) {
			double open = 0.0 , close = 0.0 , merged = 0.0, pr = 0.0; // edit pr=0 this, temporary to avoid NaN
			if( !n.isUserNode()) {
				for( GitNode ng : n.getNeighbors()) {
					Edge edge = graph.getEdge(""+ng.getName()+n.getName());
					String status = edge.getAttribute("status").toString();
					if (status.equals("opened")){
						open++;
					} else if( status.equals("closed")) {
						close++;
					} else if( status.equals("merged")) {
						merged++;
					} else {
						pr++;
					}
				}
				double hubScore = hubScores.get(n.getName());
				double boostIssues = MAX_POINT  * ( close / (open + close)); 
				double boostPR = MAX_POINT * ( merged / pr );
				double totalBoost = (Double.isNaN(boostIssues)? 0 : boostIssues) + (Double.isNaN(boostPR)? 0 : boostPR);
				n.setBoost( totalBoost );
				n.setStatusCount(open, close);
				n.setPRCount( merged, pr );
				n.setWeightedScore(hubScore + (totalBoost * hubScore));
			}
		}
	}
	
	public void calculateScores(){
		for( int i = 0; i < 10000; i++ ) {
			double norm = 0;
			for( GitNode g : gitList ) {
				if( g.isUserNode() ) {
					authorityScores.replace(g.getName(), 0.0);
					for( GitNode n : g.getNeighbors() ) {
						double newAuth = authorityScores.get(g.getName()) + hubScores.get(n.getName());
						authorityScores.replace(g.getName(), newAuth );
					}
					norm = norm + ( authorityScores.get(g.getName()) * authorityScores.get(g.getName()) );
				}
			}
			norm = Math.sqrt(norm);
			for( GitNode g : gitList ) {
				if ( g.isUserNode() ){
					double newAuth = authorityScores.get(g.getName()) / norm;
					authorityScores.replace(g.getName(), newAuth);
				}
			}
			norm = 0;
			for( GitNode g : gitList ) {
				if ( !g.isUserNode() ) {
					hubScores.replace(g.getName(), 0.0);
					for( GitNode n : g.getNeighbors() ){
						double newHub = hubScores.get(n.getName()) + authorityScores.get(n.getName());
						hubScores.replace(g.getName(), newHub);
					}
					norm = norm + ( hubScores.get(g.getName()) * hubScores.get(g.getName()));
				}
			}
			norm = Math.sqrt(norm);
			for( GitNode g : gitList ){
				if ( !g.isUserNode() ) {
					double newHub = hubScores.get(g.getName()) / norm;
					hubScores.replace(g.getName(), newHub);
				}
			}
		}
	}
	
	public void setGraph() {
		for( Node n : graph ){
			GitNode gn = new GitNode(n);
			Iterator<Node> it = n.getNeighborNodeIterator();
			
			while( it.hasNext()) {
				Node gnb = it.next();
				GitNode neig = new GitNode(gnb);
				if( gnb.getAttribute("ui.label").toString().contains("/")){
					neig.notUserNode();
				}
				gn.addNeighbor(neig);	
			}
			
			if( n.getAttribute("ui.label").toString().contains("/")){
				gn.notUserNode();
			}
			
			authorityScores.put(gn.getName(),  1.0);
			hubScores.put(gn.getName(), 1.0);
			gitList.add(gn);
		}
	}
	
	public byte[] loadFromFile(String addr) {
		File file = new File(addr);
		byte[] bytesArray;
		try{
			bytesArray = new byte[ (int) file.length()];
			FileInputStream fis = new FileInputStream(file);
			fis.read(bytesArray);
			fis.close();
			return bytesArray;
		} catch( Exception e) {
			bytesArray = null;
			e.printStackTrace();
		} 
		
		return bytesArray;
	}
	
	public void parseAndGraph( byte[] bytesArray , int eventType ){
		
		Any obj = JsonIterator.deserialize(bytesArray);
		
		if( eventType == 0) {
			String payload, issueObj, userObj, status, repo_name, username;
			Node usr, repo;
			for( Any ob : obj ) {
				payload = ob.toString("payload"); // get payload object from response object
				issueObj = JsonIterator.deserialize(payload).toString("issue"); // get issue object from payload object
				userObj = JsonIterator.deserialize(issueObj).toString("user"); // get user object from issue object
				
				repo_name = ob.toString("repo_name"); // get repo name
				username = JsonIterator.deserialize(userObj).toString("login"); // get username of issue sender 
				status = JsonIterator.deserialize(payload).toString("action"); // get issue status (open or closed)
				
				usr = graph.addNode(username);
				usr.addAttribute("ui.label", username);
				repo = graph.addNode(repo_name);
				repo.addAttribute("ui.label", repo_name);
//				Thread.sleep(100);
				Edge edge = graph.addEdge( ""+usr.getId()+repo.getId() , usr, repo);
				edge.addAttribute("status", status);
				edge.addAttribute("ui.label", status);
			}
		} else if ( eventType == 1) { // parsing for pull request
			
		} else {
			System.out.println("Unhandled parsing event type");
		}
	}
	
	// sort top 10
	public void getTopRepositories( boolean afterWeighted ){
		if( afterWeighted ){
			//TODO
		} else {
			
		}
	}
	
	// not yet finished
	// no pr scores
	public void printResult(int type){
		for( GitNode n : gitList ){
			if( !n.isUserNode()) {
				if( type == ALL ){
					System.out.println( n.getName() 
							+ " -> Hub: " + hubScores.get(n.getName()) + " | Auth: " + authorityScores.get(n.getName())
							+ " | " + "Open: " + n.getOpenCount() + " | Close: " + n.getCloseCount()
							+ " | Boost: " + n.getBoost()
							+ " | Merged: " + n.getMerged() + " | Total PR: " + n.getPr()
							+ n.getBoost() + " | weighted score: " + n.getWeightedScore());
				} else if( type == COUNTER_ONLY) {
					System.out.println( n.getName() 
							+" -> Open: " + n.getOpenCount() + " | Close: " + n.getCloseCount()
							+" | Merged: " + n.getMerged() + " | Total PR: " + n.getPr() );
				} else if( type == SCORES_ONLY ){
					System.out.println( n.getName() 
							+ " -> Hub: " + hubScores.get(n.getName()) + " | Auth: " + authorityScores.get(n.getName())
							+ " | Boost: " + n.getBoost()
							+ " | weighted score: " + n.getWeightedScore());
				} else if( type == PR_ONLY ){
					System.out.println( n.getName() 
							+" | Merged: " + n.getMerged() + " | Total PR: " + n.getPr() );
				} else if( type == ISSUES_ONLY ){
					System.out.println( n.getName() 
							+" -> Open: " + n.getOpenCount() + " | Close: " + n.getCloseCount());
				} else {
					System.out.println("Unhandled print type, fallback print all!");
					printResult(ALL);
				}
			}	
		}
	}
	
}
