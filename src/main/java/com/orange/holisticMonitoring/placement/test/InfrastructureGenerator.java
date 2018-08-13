package com.orange.holisticMonitoring.placement.test;

import java.io.FileWriter;
import java.io.IOException;

import com.esotericsoftware.yamlbeans.YamlWriter;
import com.orange.holisticMonitoring.placement.inputs.Link;
import com.orange.holisticMonitoring.placement.inputs.Server;

public class InfrastructureGenerator {
	
	
	public static void main(String[] args) throws IOException{
		
		//Input
		int centralPoPsNb = 9;
		
		int regionalPoPsNb = centralPoPsNb*2;int edgeDevicesNb = regionalPoPsNb*2;
		String infrastrctureId="C"+centralPoPsNb;
		String destinationFolder = "C:/Users/THLR0145/Desktop/mohamed/workspace/PlacementCalculator/src/main/resources";
		
		int firstCentralPoPId=0;
		int firstRegionalPoPId = centralPoPsNb;
		int firstedgeDeviceId = centralPoPsNb + regionalPoPsNb;
		
		int totalNb = centralPoPsNb+regionalPoPsNb+edgeDevicesNb;
		YamlWriter serversWriter = new YamlWriter(new FileWriter(destinationFolder+"/infrastructure_"+infrastrctureId+"/servers.yml"));
		YamlWriter linksWriter = new YamlWriter(new FileWriter(destinationFolder+"/infrastructure_"+infrastrctureId+"/links.yml"));
		
		//Central PoPs
		for(int centralPoPId=firstCentralPoPId; centralPoPId<firstRegionalPoPId; centralPoPId++){
			Server s = new Server();
			s.id = centralPoPId;
			s.capability = 10000; //10G
			serversWriter.write(s);
		}
		
		//Regional PoPs
		for(int regionalPoPId=firstRegionalPoPId; regionalPoPId<firstedgeDeviceId; regionalPoPId++){
			Server s = new Server();
			s.id = regionalPoPId;
			s.capability = 1000; //1G
			serversWriter.write(s);
		}

		//EDs
		for(int edgeDeviceId=firstedgeDeviceId; edgeDeviceId<totalNb; edgeDeviceId++){
			Server s = new Server();
			s.id = edgeDeviceId;
			s.capability = 100; //100 M
			serversWriter.write(s);
		}
/**/
		//Central PoPs <-> Central PoPs
		for(int centralPoPId=0; centralPoPId<centralPoPsNb; centralPoPId++){
			Link downLk = new Link();
			downLk.headId=centralPoPId+1;
			downLk.tailId=centralPoPId;
			downLk.id=downLk.tailId*totalNb+downLk.headId;
			downLk.capability=10000;//10G
			downLk.latency=22;
			linksWriter.write(downLk);
			
			Link upLk = new Link();
			upLk.headId=centralPoPId;
			upLk.tailId=centralPoPId+1;
			upLk.id=upLk.tailId*totalNb+upLk.headId;
			upLk.capability=10000;//10G
			upLk.latency=22;
			linksWriter.write(upLk);
		}
		
		//last central PoP <-> first central PoP
		{
			Link downLk = new Link();
			downLk.headId=firstRegionalPoPId-1;
			downLk.tailId=firstCentralPoPId;
			downLk.id=downLk.tailId*totalNb+downLk.headId;
			downLk.capability=10000;//10G
			downLk.latency=22;
			linksWriter.write(downLk);
			
			Link upLk = new Link();
			upLk.headId=firstCentralPoPId;
			upLk.tailId=firstRegionalPoPId-1;
			upLk.id=upLk.tailId*totalNb+upLk.headId;
			upLk.capability=10000;//10G
			upLk.latency=22;
			linksWriter.write(upLk);
		}
		
		//Central PoPs <-> Regional PoPs
		{
			int regionalPoPId=firstRegionalPoPId;
			for(int centralPoPId=firstCentralPoPId; centralPoPId<firstRegionalPoPId; centralPoPId++){
				for(int i=0; i<(regionalPoPsNb/centralPoPsNb); i++){
					Link downLk = new Link();
					downLk.headId=regionalPoPId;
					downLk.tailId=centralPoPId;
					downLk.id=downLk.tailId*totalNb+downLk.headId;
					downLk.capability=1000;
					downLk.latency=12;
					linksWriter.write(downLk);
					
					Link upLk = new Link();
					upLk.headId=centralPoPId;
					upLk.tailId=regionalPoPId;
					upLk.id=upLk.tailId*totalNb+upLk.headId;
					upLk.capability=1000;
					upLk.latency=12;
					linksWriter.write(upLk);
					regionalPoPId++;
				}
			}
		}
		//Regional PoPs <-> EDs
		int edgeDeviceId=firstedgeDeviceId;
		for(int regionalPoPId=firstRegionalPoPId; regionalPoPId<firstedgeDeviceId; regionalPoPId++){
			for(int i=0; i<(edgeDevicesNb/regionalPoPsNb); i++){
				Link downLk = new Link();
				downLk.headId=edgeDeviceId;
				downLk.tailId=regionalPoPId;
				downLk.id=downLk.tailId*totalNb+downLk.headId;
				downLk.capability=100;//100M
				downLk.latency=8;
				linksWriter.write(downLk);
				
				Link upLk = new Link();
				upLk.headId=regionalPoPId;
				upLk.tailId=edgeDeviceId;
				upLk.id=upLk.tailId*totalNb+upLk.headId;
				upLk.capability=100;//100M
				upLk.latency=8;
				linksWriter.write(upLk);
				edgeDeviceId++;
			}
		}
		
		serversWriter.close();
		linksWriter.close();
	    
		System.out.println("Test scenarios generated");
	}
}