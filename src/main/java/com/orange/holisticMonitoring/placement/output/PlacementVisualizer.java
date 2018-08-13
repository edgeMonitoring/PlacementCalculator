package com.orange.holisticMonitoring.placement.output;

import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;

import com.orange.holisticMonitoring.placement.inputs.G_infrastructure;

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

public class PlacementVisualizer {
	
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
	
	public static void visualize(G_infrastructure g_infrastructure, int totalFunctionsNumber, SetVar x_serviceFunctions, SetVar x_serviceFlows, IntVar[] x_serverHostingF, String file) throws IOException{
		
		System.out.println();
		System.out.println("Beginning of visualization...");
		
		//*******************************************************************************
		// g_infrastructure
		//*******************************************************************************
		
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
		// g_monitoringService
		//*******************************************************************************
		
		int nbFunctions = x_serviceFunctions.getValue().toArray().length;
		int[] functions = x_serviceFunctions.getValue().toArray();
		
		Node[] GServiceNode = new Node[nbFunctions];
		for (int i = 0; i < nbFunctions; i++){
			GServiceNode[i]= node(String.valueOf(functions[i])).with("fontcolor","red").with(Color.RED);
		}
		
		for (int i = 0; i < nbFunctions; i++){
			int serverIndex = 0;
			while(g_infrastructure.getServers().get(serverIndex).id!=x_serverHostingF[functions[i]].getValue())
				serverIndex++;
			GInfrastructureNode[serverIndex] = GInfrastructureNode[serverIndex].with(GServiceNode[i]);
		}
		
		int nbFlows = x_serviceFlows.getValue().toArray().length;
		int[] flows = x_serviceFlows.getValue().toArray();
		
		for (int i = 0; i<nbFlows; i++){
			
			int flowId = flows[i];
			
			int tailId = tailId(flowId, totalFunctionsNumber);
			int tailServerIndex = 0;			
			while (g_infrastructure.getServers().get(tailServerIndex).id!=x_serverHostingF[tailId].getValue())
				tailServerIndex++;
			int tailFunctionIndex = 0;
			while (functions[tailFunctionIndex]!=tailId)
				tailFunctionIndex++;
			
			int headId = headId(flowId, totalFunctionsNumber);
			int headServerIndex = 0;			
			while (g_infrastructure.getServers().get(headServerIndex).id!=x_serverHostingF[headId].getValue())
				headServerIndex++;
			int headFunctionIndex = 0;
			while (functions[headFunctionIndex]!=headId)
				headFunctionIndex++;
					
			if(tailServerIndex<headServerIndex)
				GInfrastructureNode[headServerIndex]=GInfrastructureNode[headServerIndex].with(GServiceNode[tailFunctionIndex].link(to(GServiceNode[headFunctionIndex]).with(Label.of(String.valueOf(flowId))).with("fontcolor", "red").with(Color.RED)));
			else
				GInfrastructureNode[tailServerIndex]=GInfrastructureNode[tailServerIndex].with(GServiceNode[tailFunctionIndex].link(to(GServiceNode[headFunctionIndex]).with(Label.of(String.valueOf(flowId))).with("fontcolor", "red").with(Color.RED)));
	
		}
		
		//*******************************************************************************
		// global graph building
		//*******************************************************************************

		MutableGraph globalGraph = mutGraph("graph3").setStrict(false).setDirected(true).setCluster(true);
		for (int i = 0; i < nbServers; i++){
			globalGraph = globalGraph.add(GInfrastructureNode[i]);
		}	
		
		Graphviz.fromGraph(globalGraph).width(1000).render(Format.SVG).toFile(new File(file));
		
		System.out.println("End of visualization");
		
	}
}
