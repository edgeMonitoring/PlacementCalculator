package com.orange.holisticMonitoring.placement.output;

import com.orange.holisticMonitoring.placement.inputs.G_infrastructure;
import com.orange.holisticMonitoring.placement.inputs.G_usersRequirements;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.Graph;
import guru.nidi.graphviz.model.Label;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.Node;
import guru.nidi.graphviz.attribute.Color;
import guru.nidi.graphviz.attribute.Shape;

import static guru.nidi.graphviz.model.Factory.*;

import java.io.File;
import java.io.IOException;

public class GraphVisualizer {
	
	static int headId(int edgeID, int vertexNumber)
	{
		if (edgeID!=0)
			return edgeID%vertexNumber;
		else
			return 0;
	}
	
	static int tailId(int edgeID, int vertexNumber)
	{
		if (edgeID!=0)
			return edgeID/vertexNumber;
		else
			return 0;
	}
	
	static void vizualizeInfrastructure(String infrastructureId,String file) throws IOException{
		
		G_infrastructure g_infrastructure=new G_infrastructure(infrastructureId); 
		System.out.println("graph read (nb links="+g_infrastructure.getLinks().size()+")");
		System.out.println();
				
		int nbServers =	g_infrastructure.getServers().size();
		int nbLinks =	g_infrastructure.getLinks().size();
		
		Node[] GInfrastructureMainNode = new Node[nbServers];  
		for (int i = 0; i < nbServers; i++){
			GInfrastructureMainNode[i]= node(g_infrastructure.getServers().get(i).id.toString()+" ").with(Shape.RECTANGLE);
		}
		
		Graph[] GInfrastructureNode = new Graph[nbServers];
		for (int i = 0; i < nbServers; i++){
			GInfrastructureNode[i] = graph("g"+g_infrastructure.getServers().get(i).id).directed().cluster()
					.with(GInfrastructureMainNode[i]);
		}
		
		for (int i = 0; i < nbLinks; i++){
			
			int linkId = g_infrastructure.getLinks().get(i).id;
			
			int tailId = tailId(linkId, nbServers);
			int tailIndex = 0;
			while (g_infrastructure.getServers().get(tailIndex).id!=tailId)
				tailIndex++;
			
			int headId = headId(linkId, nbServers);
			int headIndex = 0;
			while (g_infrastructure.getServers().get(headIndex).id!=headId)
				headIndex++;
			
			if(tailIndex<headIndex)
				GInfrastructureNode[headIndex]=GInfrastructureNode[headIndex].with(GInfrastructureMainNode[tailIndex].link(to(GInfrastructureMainNode[headIndex]).with(Label.of(String.valueOf(linkId)+" "))));
			else if (tailIndex>headIndex)
				GInfrastructureNode[tailIndex]=GInfrastructureNode[tailIndex].with(GInfrastructureMainNode[tailIndex].link(to(GInfrastructureMainNode[headIndex]).with(Label.of(String.valueOf(linkId)+" "))));
	
			//System.out.println(tailIndex+"->"+headIndex+"  "+linkId);
		}
		
		//*******************************************************************************
		// global graph building
		//*******************************************************************************

		MutableGraph globalGraph = mutGraph("graph3").setStrict(false).setDirected(true).setCluster(true);
		for (int i = 0; i < nbServers; i++){
			globalGraph = globalGraph.add(GInfrastructureNode[i]);
		}	
		
		Graphviz.fromGraph(globalGraph).width(10000).render(Format.SVG).toFile(new File(file));
		
	}
	
	static void vizualizeUsersRequirements(String usersRequirementId, String file) throws IOException {
		
		G_usersRequirements g_usersRequirements=new G_usersRequirements(usersRequirementId);

		Graph g = graph("g_usersRequirements").directed();
		
		int nbFunctions = g_usersRequirements.getFunctions().size();
		
		Node[] GServiceNode = new Node[nbFunctions];
		for (int i = 0; i < nbFunctions; i++){
			GServiceNode[i]= node(String.valueOf(g_usersRequirements.getFunctions().get(i).id)).with("fontcolor","red").with(Color.RED);
		}
		
		int nbFlows = g_usersRequirements.getFlows().size();
		
		for (int i = 0; i<nbFlows; i++){
			
			int flowId = g_usersRequirements.getFlows().get(i).id;
			
			int tailId = tailId(flowId, nbFunctions);
			int tailFunctionIndex = 0;
			while (g_usersRequirements.getFunctions().get(tailFunctionIndex).id!=tailId)
				tailFunctionIndex++;
				
			
			int headId = headId(flowId, nbFunctions);
			int headFunctionIndex = 0;
			while (g_usersRequirements.getFunctions().get(headFunctionIndex).id!=headId)
				headFunctionIndex++;
					
				g=g.with(GServiceNode[tailFunctionIndex].link(to(GServiceNode[headFunctionIndex]).with(Label.of(String.valueOf(flowId))).with("fontcolor", "red").with(Color.RED)));
		}
				
		Graphviz.fromGraph(g).width(10000).render(Format.SVG).toFile(new File(file));
		
	}
	
	
	public static void main(String[] args) throws IOException{
		

		System.out.println("Beginning of visualization...");
		
		String file1 = "C:/Users/THLR0145/Desktop/infrastructure.svg";
		//vizualizeInfrastructure("2",file1);

		String file2 = "C:/Users/THLR0145/Desktop/usersRequirements.svg";
		vizualizeUsersRequirements("108",file2);
		
		
		System.out.println("End of visualization");
		
	}
}
