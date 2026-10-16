/*
 * Class that captures all the metadata around a mapping resulting from an Ontology Alignment process
 */

package uk.ac.ox.krr.logmap2.mediating_ontologies;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import uk.ac.ox.krr.logmap2.mappings.objects.MappingObjectStr;

public class MappingMetadata {
		   	private MappingObjectStr mapping;
	        private Double confidence;
	        private List<String> moList;
	        private Integer votingCount;
	        private boolean passThreshold;

	        public MappingMetadata() {
	        	this.confidence = 0.0;
	            this.votingCount=0;
	            this.moList = new ArrayList<>();
	            this.passThreshold = false;
	        }
	        
	        public MappingMetadata(MappingMetadata other) {
	        	this.votingCount = other.getVotingCount();
	        	this.moList = other.getMoList();
	        	this.confidence = other.getConfidence();
	        	this.passThreshold = other.hasPassedThreshold();
	        }

	        // Getters and Setters
	        public Double getConfidence() { 
	        	return confidence; 
	        	}
	        public void setConfidence(Double confidenceValue) {
	        	this.confidence = confidenceValue;
	        }
	        public List<String> getMoList() {
	        	return moList; 
	        	}
	        public void addToMOList(String MOLabel ) {
	        	this.moList.add(MOLabel);
	        }
	        
	        public Integer getVotingCount() {
	        	return votingCount; 
	        	}
	        
	        public void addToVotingCount() {
	        	this.votingCount++;
	        	}
	        
	        public boolean hasPassedThreshold() {
	        	return passThreshold; }
	        
	        public void setPassThreshold(boolean passThreshold) {
	        	this.passThreshold = passThreshold; 
	        	}
	        
	        @Override
	    	public boolean equals(Object obj){
	    		
	    		if  (obj == null || getClass() != obj.getClass())
	    			return false;
	    		if (obj == this)
	    			return true;
	    		if (!(obj instanceof MappingMetadata))
	    			return false;
	    		
	    		MappingMetadata other =  (MappingMetadata)obj;
	    		
	    		return this.votingCount == other.votingCount &&
	    				this.passThreshold == other.passThreshold &&
	    				this.confidence == other.confidence &&
	    				Objects.equals(this.mapping, other.mapping) &&
	    				Objects.equals(this.moList, other.moList);
	    		
	    	}


	        // Overriding hashCode (highly recommended when overriding equals)
	        @Override
	        public int hashCode() {
	            return Objects.hash(votingCount, passThreshold, confidence,mapping, moList);
	        }
	    

	    	
	    	public String toString(){
	    		return confidence + "\t" + votingCount + "\t" + moList;
	    	}
	    	
	    	public String toString(Boolean withThreshold) {
	    		if(withThreshold==true) {
	    		return confidence + "\t" + votingCount + "\t" + passThreshold + "\t" + moList;
	    	}else {
	    		return confidence + "\t" + votingCount + "\t" + moList;
	    	}
	    	
	    }
	    	
	    	
}