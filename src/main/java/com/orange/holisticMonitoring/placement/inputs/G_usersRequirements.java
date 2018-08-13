package com.orange.holisticMonitoring.placement.inputs;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import com.orange.holisticMonitoring.placement.inputs.AllPathsFromASource.Graph;

public class G_usersRequirements {

	private List<Function> functions = new ArrayList<Function>();
	private List<Flow> flows = new ArrayList<Flow>();
	private List<PathsBetweenTwoVertices> allPaths = new ArrayList<PathsBetweenTwoVertices>();
	
	public List<Function> getFunctions() {
		return functions;
	}
	public List<Flow> getFlows() {
		return flows;
	}
	public List<PathsBetweenTwoVertices> getAllPaths() {
		return allPaths;
	}
	
	public G_usersRequirements(String usersRequirementsId) throws FileNotFoundException, YamlException {
		
		super();
		ClassLoader classLoader = getClass().getClassLoader();
		//String filesPath = classLoader.getResource("").getPath();
		String filesPath = "/home/mohamed/workspace/PlacementCalculator/src/main/resources/";
		
		YamlReader functionsReader = new YamlReader(new FileReader(filesPath+"usersRequirements_"+usersRequirementsId+"/functions.yml"));
		while (true) {
	    	Function function = functionsReader.read(Function.class);
	    	if (function == null) break;
	    	this.functions.add(function);
	    }
	    
		Graph g = new Graph(this.functions.size());
		
		YamlReader flowsReader = new YamlReader(new FileReader(filesPath+"usersRequirements_"+usersRequirementsId+"/flows.yml"));
		while (true) {
	    	Flow flow = flowsReader.read(Flow.class);
	    	if (flow == null) break;
	    	flow.id=flow.tailId*functions.size()+flow.headId;
	    	this.flows.add(flow);
	    	g.addEdge(flow.tailId,flow.headId);
	    }
		
		for (Function function1: functions){
			if (function1.type==FunctionType.probe){
				for (Function function2: functions){
					if (function2.type==FunctionType.user){
						List<List<Integer>> vertexPaths = g.getAllPaths(function1.id,function2.id);
						for(List<Integer> vpath : vertexPaths){
							List<Integer> epath = new ArrayList<Integer>();
							int v1 = -1;
							for (int v2 : vpath)
							{
								 if (v1!=-1)
								 {
									 int e=v1*g.V+v2;
									 epath.add(e);
								 } 
								 v1=v2;
							}
							
							PathsBetweenTwoVertices ps= new PathsBetweenTwoVertices();
							ps.edgesIdsOfPath.add(epath);
							ps.sourceId = function1.id;
							ps.destinationId = function2.id;
							allPaths.add(ps);
						}
					}
				}
			}
		
		}
		
		
	}
	
	
	
}
