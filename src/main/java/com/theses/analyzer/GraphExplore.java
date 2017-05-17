package com.theses.analyzer;

import java.util.Iterator;
import java.util.List;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;

import org.graphstream.*;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.*;


// json parsing imports
import org.json.simple.parser.JSONParser;
import com.jsoniter.*;
import com.jsoniter.any.Any;

public class GraphExplore {
	
	GraphExplore() {
//		Graph graph = new SingleGraph("testing");
		JSONParser parser = new JSONParser();
		File file = new File("C:/data/issues.json");
				
		try {
			byte[] bytesArray = new byte[ (int) file.length()];
			FileInputStream fis = new FileInputStream(file);
			fis.read(bytesArray);
			fis.close();
			Any obj = JsonIterator.deserialize(bytesArray);
			String payload = obj.asList().get(0).toString("payload");
			String val = JsonIterator.deserialize(payload).toString("action");
			System.out.print(val);
			
//			for( Any ob : obj ) {
//				List payload = ob.asList();
//				System.out.println(payload);
//			}
			
			
			
		} catch ( Exception e ) {
			e.printStackTrace();
		}
		
		
//		graph.setStrict( false );
//		graph.setAutoCreate( true );
//		graph.display();
//		
//		graph.addEdge("AB", "A", "B");
//        graph.addEdge("BC", "B", "C");
//        graph.addEdge("CA", "C", "A");
//        graph.addEdge("AD", "A", "D");
//        graph.addEdge("DE", "D", "E");
//        graph.addEdge("DF", "D", "F");
//        graph.addEdge("EF", "E", "F");
//        
//        for( Node node: graph ) {
//        	node.addAttribute("ui.label", node.getId());
//        }
        
	}
}
