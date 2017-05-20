package com.theses.analyzer;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
    	System.setProperty("org.graphstream.ui.renderer",
		        "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
        new GraphExplore();
    }
    
}
