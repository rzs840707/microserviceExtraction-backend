package ch.uzh.ifi.seal.monolith2microservices.services.decomposition.logicalcoupling.graph;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import ch.uzh.ifi.seal.monolith2microservices.main.utils.Comparators;
import ch.uzh.ifi.seal.monolith2microservices.models.LogicalCoupling;

@Service
public class GraphMapper {

	private Map<String, ClassNode> nodeMap;
	
	public List<ClassNode> mapToGraph(List<LogicalCoupling> couplings){
		nodeMap = new HashMap<>();
		int lowerQuartile = computeLowerQuartile(couplings);
		
		for(LogicalCoupling coupling: couplings){
			if((coupling.getClassFiles().size() == 2) && (coupling.getScore() > lowerQuartile)){
				String firstClassName = coupling.getClassFiles().get(0);
				String secondClassName = coupling.getClassFiles().get(1);
				
				ClassNode firstNode = nodeMap.get(firstClassName); 
				ClassNode secondNode = nodeMap.get(secondClassName);

				if((firstNode == null) && (secondNode == null)){
					
					firstNode = new ClassNode(firstClassName);
					secondNode = new ClassNode(secondClassName);
					nodeMap.put(firstClassName, firstNode);
					nodeMap.put(secondClassName, secondNode);
					
				}else if((firstNode != null) && (secondNode == null)){
					
					secondNode = new ClassNode(secondClassName);
					nodeMap.put(secondClassName, secondNode);
					
				}else if((firstNode == null) && (secondNode != null)){
					
					firstNode = new ClassNode(firstClassName);
					nodeMap.put(firstClassName, firstNode);
					
				}
				//TODO: Caution, this will only work if the value of firstNode already in the map will also get updated, which it actually should according to passing references by-value
				firstNode.addNeighborWithWeight(secondNode, coupling.getScore());
				secondNode.addNeighborWithWeight(firstNode, coupling.getScore());
			}
		}
		
		return nodeMap.values().stream().collect(Collectors.toList());
	}
	
	
	private int computeLowerQuartile(List<LogicalCoupling> couplings){
		Collections.sort(couplings, Comparators.LOGICAL_COUPLING_SCORE);
		int q1Index = (int) Math.round(couplings.size() * 0.75f);
		return couplings.get(q1Index).getScore();
	}
}
