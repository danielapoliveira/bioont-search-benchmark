package rankingmodel.booleanModel;

import java.util.ArrayList;

import com.hp.hpl.jena.rdf.model.*;
import query.ResultFormatter;

public class BooleanModel {

	
  	public ArrayList<ResultFormatter> getRankedProperties(Model model) {

   		ArrayList<ResultFormatter> rf = new ArrayList<ResultFormatter>();
   		
   		System.out.println("In boolean ");
   		
        Property props = model.getProperty("http://www.biomeanalytics.com/model/orthoOntology.owl#hasProperty");
        Property label = model.getProperty("http://www.w3.org/2000/01/rdf-schema#label");
        
        NodeIterator propertyIterator = model.listObjectsOfProperty(props);
        
        while(propertyIterator.hasNext()){
        	System.out.println("In while ");
        	ResultFormatter rsf = new ResultFormatter();
        	String propertyURI = propertyIterator.next().toString();
        	        	
        	//getLabel of property
        	Resource property = model.getResource(propertyURI);
        	NodeIterator labelIterator = model.listObjectsOfProperty(property,label);
        	
        	String propLabel="";
        	
        	if (labelIterator.hasNext()){
        	RDFNode pLabel = labelIterator.nextNode();
        	propLabel = pLabel.toString();
        		if(propLabel.contains("@")) {
        			propLabel=propLabel.split("@")[0];
        		} 
        		if (propLabel.contains("^")){
        			propLabel= propLabel.split("\\^")[0];
        		}
        	} else {
        		propLabel = propertyURI;
        	}

        	rsf.setGraphIRI("");
   		  	rsf.setTermIRI(propertyURI);
   		  	rsf.setTermLabel(propLabel);
   		  	rsf.setScore("");
   		  	rf.add(rsf);
        }
        
      return rf;
 	}
  	
  	
  	public ArrayList<ResultFormatter> getRankedClasses(Model model) {

   		ArrayList<ResultFormatter> rf = new ArrayList<ResultFormatter>();
   		
        Property props = model.getProperty("http://www.biomeanalytics.com/model/orthoOntology.owl#hasProperty");
        Property label = model.getProperty("http://www.w3.org/2000/01/rdf-schema#label");
        Property graphProperty = model.getProperty("http://www.w3.org/2000/01/rdf-schema#graph");

        ResIterator propertyIterator = model.listSubjectsWithProperty(graphProperty);
        while(propertyIterator.hasNext()){
        	ResultFormatter rsf = new ResultFormatter();
        	String propertyURI = propertyIterator.next().toString();
        	        	
        	//getLabel of property
        	Resource property = model.getResource(propertyURI);
        	NodeIterator labelIterator = model.listObjectsOfProperty(property,label);
        	
        	String propLabel="";
        	
        	if (labelIterator.hasNext()){
        	RDFNode pLabel = labelIterator.nextNode();
        	propLabel = pLabel.toString();
        		if(propLabel.contains("@")) {
        			propLabel=propLabel.split("@")[0];
        		} 
        		if (propLabel.contains("^")){
        			propLabel= propLabel.split("\\^")[0];
        		}
        	} else {
        		propLabel = propertyURI;
        	}
        	 
        	rsf.setGraphIRI("");
   		  	rsf.setTermIRI(propertyURI);
   		  	rsf.setTermLabel(propLabel);
   		  	rsf.setScore("");
   		  	rf.add(rsf);
        }
        
      return rf;
 	}
  	
  	private String getLabel(NodeIterator labelIterator, String propertyURI){   	
    	
  		String propLabel="";
    	
    	if (labelIterator.hasNext()){
    	RDFNode pLabel = labelIterator.nextNode();
    	propLabel = pLabel.toString();
    	//System.out.println(propLabel + "is property Label");
    		if(propLabel.contains("@")) {
    			propLabel=propLabel.split("@")[0];
    		} 
    		if (propLabel.contains("^")){
    			propLabel= propLabel.split("\\^")[0];
    		}
    	} else {
    		propLabel = propertyURI;
    	}
    	return propLabel;
  	}
}
