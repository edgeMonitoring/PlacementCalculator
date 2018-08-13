package com.orange.holisticMonitoring.placement.inputs;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import com.orange.holisticMonitoring.placement.inputs.AllPathsFromASource.Graph;

public class G_infrastructure {

	private List<Server> servers = new ArrayList<Server>();
	private List<Link> links = new ArrayList<Link>();
	private List<PathsBetweenTwoVertices> allPaths = new ArrayList<PathsBetweenTwoVertices>();

	public List<Server> getServers() {
		return servers;
	}
	public List<Link> getLinks() {
		return links;
	}
	public List<PathsBetweenTwoVertices> getAllPaths() {
		return allPaths;
	}
	
	public G_infrastructure(String infrastrctureId) throws FileNotFoundException, YamlException {
	
		ClassLoader classLoader = getClass().getClassLoader();
		//String filesPath = classLoader.getResource("").getPath();
		String filesPath = "/home/mohamed/workspace/PlacementCalculator/src/main/resources/";
		
		YamlReader serversReader = new YamlReader(new FileReader(filesPath+"infrastructure_"+infrastrctureId+"/servers.yml"));
		while (true) {
	    	Server server = serversReader.read(Server.class);
	    	if (server == null) break;
	    	this.servers.add(server);
	    }
	    
		Graph g = new Graph(this.servers.size());
		
		YamlReader linksReader = new YamlReader(new FileReader(filesPath+"infrastructure_"+infrastrctureId+"/links.yml"));
		while (true) {
	    	Link link = linksReader.read(Link.class);
	    	if (link == null) break;
	    	link.id=link.tailId*servers.size()+link.headId;
	    	this.links.add(link);
	    	g.addEdge(link.tailId,link.headId);
	    }
		
		//Adding loops
		for (int i=0; i<this.servers.size(); i++){
			Link link = new Link();
			link.id=servers.get(i).id*servers.size()+servers.get(i).id;
			link.headId=servers.get(i).id;
			link.tailId=servers.get(i).id;
			link.capability=100;
			link.latency=0;
			this.links.add(link);
		}
		
	
		for (Server server1: servers){
			for (Server server2: servers){
				if(server1.id!=server2.id){
					PathsBetweenTwoVertices ps= new PathsBetweenTwoVertices();
					ps.sourceId = server1.id;
					ps.destinationId = server2.id;
					
					List<List<Integer>> vertexPaths = g.getAllPaths(server1.id,server2.id);
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
						ps.edgesIdsOfPath.add(epath);
					}
					allPaths.add(ps);
				}
			}
		
		}/**/
		
	}
	
}
