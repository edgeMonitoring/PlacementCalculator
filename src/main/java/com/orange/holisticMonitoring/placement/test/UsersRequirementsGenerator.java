package com.orange.holisticMonitoring.placement.test;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlWriter;
import com.orange.holisticMonitoring.placement.inputs.Flow;
import com.orange.holisticMonitoring.placement.inputs.Function;
import com.orange.holisticMonitoring.placement.inputs.FunctionType;

public class UsersRequirementsGenerator {
	
	static String destinationFolder;
	static int usersRequirementsId;
	static int parameter=2;
	static YamlWriter functionsWriter;
	static YamlWriter flowsWriter;
	
	static int infrastructureId;
	static int centralPoPsNb = 0;static int regionalPoPsNb = 0;static int edgeDevicesNb = 0;
	static int firstRegionalPoPId = 0;
	static int firstedgeDeviceId = 0;
	
	static Random hostingServer = new Random(40);
	static Random latency = new Random(40);
	
	static int nextFunctionId=0;
	static int PredefinedHost=26;
	
	public static void blueGraph(boolean halfSimilarity) throws YamlException{
		
		Function f1 = new Function();
		f1.id=nextFunctionId++;
		f1.type=FunctionType.monitoring_aggregation;
		f1.parameter=1;
		f1.capabilityRequ=1;
		f1.maxLatency=-1;
		f1.predefinedHost=-1;
		functionsWriter.write(f1);
		
		Function f2 = new Function();
		f2.id=nextFunctionId++;
		f2.type=FunctionType.monitoring_append;
		if (halfSimilarity)
			f2.parameter=parameter++;
		else
			f2.parameter=1;
		f2.capabilityRequ=1;
		f2.maxLatency=-1;
		f2.predefinedHost=-1;
		functionsWriter.write(f2);
		
		Flow fl1 = new Flow();
		fl1.headId=f2.id;
		fl1.tailId=f1.id;
		fl1.id=fl1.tailId*16+fl1.headId;
		fl1.capabilityRequ=1;
		flowsWriter.write(fl1);
		
	}
	
	public static void blueGraphWithUsers(int usersNumber) throws YamlException{
		
		Function f0 = new Function();
		f0.id=nextFunctionId++;
		f0.type=FunctionType.probe;
		f0.parameter=1;
		f0.capabilityRequ=1;
		f0.maxLatency=-1;
		f0.predefinedHost=firstedgeDeviceId+hostingServer.nextInt(edgeDevicesNb);
		functionsWriter.write(f0);
		
		for (int i=0; i<usersNumber; i++){
			
			Flow fl0 = new Flow();
			fl0.headId=nextFunctionId;
			fl0.tailId=f0.id;
			fl0.id=fl0.tailId*16+fl0.headId;
			fl0.capabilityRequ=1;
			flowsWriter.write(fl0);
			
			blueGraph(true);
			
			Flow fl1 = new Flow();
			fl1.headId=nextFunctionId;
			fl1.tailId=nextFunctionId-1;
			fl1.id=fl1.tailId*16+fl1.headId;
			fl1.capabilityRequ=1;
			flowsWriter.write(fl1);
			
			Function f3 = new Function();
			f3.id=nextFunctionId++;
			f3.type=FunctionType.user;
			f3.parameter=1;
			f3.capabilityRequ=1;
			f3.predefinedHost=hostingServer.nextInt(centralPoPsNb+regionalPoPsNb+edgeDevicesNb);
			
			int l=minLatency(f0.predefinedHost, f3.predefinedHost);
			//System.out.println(f0.predefinedHost+" ->" + f3.predefinedHost+"="+l);
			//System.out.println(maxLatency());
			
			f3.maxLatency=l+latency.nextInt(maxLatency()-l);
			
			
			functionsWriter.write(f3);
		
		}
		
	}
	
	public static void greenGraph(boolean halfSimilarity) throws YamlException{
		
		blueGraph(false);
		
		Function f3 = new Function();
		f3.id=nextFunctionId++;
		f3.type=FunctionType.monitoring_filter;
		f3.parameter=1;
		f3.capabilityRequ=1;
		f3.maxLatency=-1;
		f3.predefinedHost=-1;
		functionsWriter.write(f3);
		
		Function f4 = new Function();
		f4.id=nextFunctionId++;
		f4.type=FunctionType.monitoring_join;
		f4.parameter=1;
		if (halfSimilarity)
			f4.parameter=parameter++;
		else
			f4.parameter=1;
		f4.capabilityRequ=1;
		f4.maxLatency=-1;
		f4.predefinedHost=-1;
		functionsWriter.write(f4);
		
		Function f5 = new Function();
		f5.id=nextFunctionId++;
		f5.type=FunctionType.monitoring_aggregation;
		f5.parameter=1;
		f5.capabilityRequ=1;
		f5.maxLatency=-1;
		f5.predefinedHost=-1;
		functionsWriter.write(f5);
		
		Flow fl1 = new Flow();
		fl1.headId=f3.id;
		fl1.tailId=f3.id-1;
		fl1.id=fl1.tailId*16+fl1.headId;
		fl1.capabilityRequ=1;
		flowsWriter.write(fl1);
		
		Flow fl2 = new Flow();
		fl2.headId=f4.id;
		fl2.tailId=f3.id;
		fl2.id=fl2.tailId*16+fl2.headId;
		fl2.capabilityRequ=1;
		flowsWriter.write(fl2);
		
		Flow fl4 = new Flow();
		fl4.headId=f5.id;
		fl4.tailId=f4.id;
		fl4.id=fl4.tailId*16+fl4.headId;
		fl4.capabilityRequ=1;
		flowsWriter.write(fl4);
		
	}
	
	public static void greenGraphWithUsers(int usersNumber) throws YamlException{
		
		Function f0 = new Function();
		f0.id=nextFunctionId++;
		f0.type=FunctionType.probe;
		f0.parameter=1;
		f0.capabilityRequ=1;
		f0.maxLatency=-1;
		f0.predefinedHost=firstedgeDeviceId+hostingServer.nextInt(edgeDevicesNb);
		functionsWriter.write(f0);
		
		Function f1 = new Function();
		f1.id=nextFunctionId++;
		f1.type=FunctionType.probe;
		f1.parameter=1;
		f1.capabilityRequ=1;
		f1.maxLatency=-1;
		f1.predefinedHost=firstedgeDeviceId+hostingServer.nextInt(edgeDevicesNb);
		functionsWriter.write(f1);
		
		for (int i=0; i<usersNumber; i++){
			
			Flow fl0 = new Flow();
			fl0.headId=nextFunctionId;
			fl0.tailId=f0.id;
			fl0.id=fl0.tailId*16+fl0.headId;
			fl0.capabilityRequ=1;
			flowsWriter.write(fl0);
			
			greenGraph(true);
			
			Flow fl1 = new Flow();
			fl1.headId=nextFunctionId-2;
			fl1.tailId=f1.id;
			fl1.id=fl1.tailId*16+fl1.headId;
			fl1.capabilityRequ=1;
			flowsWriter.write(fl1);
			
			Flow fl2 = new Flow();
			fl2.headId=nextFunctionId;
			fl2.tailId=nextFunctionId-1;
			fl2.id=fl2.tailId*16+fl2.headId;
			fl2.capabilityRequ=1;
			flowsWriter.write(fl2);
			
			Function f3 = new Function();
			f3.id=nextFunctionId++;
			f3.type=FunctionType.user;
			f3.parameter=1;
			f3.capabilityRequ=1;
			f3.predefinedHost=hostingServer.nextInt(centralPoPsNb+regionalPoPsNb+edgeDevicesNb);

			int l=minLatency(f0.predefinedHost, f3.predefinedHost);
			if (minLatency(f1.predefinedHost, f3.predefinedHost)>l)
				l=minLatency(f1.predefinedHost, f3.predefinedHost);
			f3.maxLatency=l+latency.nextInt(maxLatency()-l);
			
			
			functionsWriter.write(f3);
			
		}
		
	}
	
	public static void redGraph() throws YamlException{
		
		greenGraph(false);
		
		Flow fl1 = new Flow();
		fl1.headId=nextFunctionId;
		fl1.tailId=nextFunctionId-1;
		fl1.id=fl1.tailId*16+fl1.headId;
		fl1.capabilityRequ=1;
		flowsWriter.write(fl1);
		
		Function f1 = new Function();
		f1.id=nextFunctionId++;
		f1.type=FunctionType.monitoring_append;
		f1.parameter=1;
		f1.capabilityRequ=1;
		f1.maxLatency=10;
		f1.predefinedHost=-1;
		functionsWriter.write(f1);
		
		Flow fl2 = new Flow();
		fl2.headId=nextFunctionId;
		fl2.tailId=nextFunctionId-1;
		fl2.id=fl2.tailId*16+fl2.headId;
		fl2.capabilityRequ=1;
		flowsWriter.write(fl2);
		
		Function f2 = new Function();
		f2.id=nextFunctionId++;
		f2.type=FunctionType.monitoring_filter;
		f2.parameter=parameter++;
		f2.capabilityRequ=1;
		f2.maxLatency=10;
		f2.predefinedHost=-1;
		functionsWriter.write(f2);

		Flow fl3 = new Flow();
		fl3.headId=nextFunctionId;
		fl3.tailId=nextFunctionId-1;
		fl3.id=fl3.tailId*16+fl3.headId;
		fl3.capabilityRequ=1;
		flowsWriter.write(fl3);
		
		Function f3 = new Function();
		f3.id=nextFunctionId++;
		f3.type=FunctionType.monitoring_join;
		f3.parameter=parameter++;
		f3.capabilityRequ=1;
		f3.maxLatency=10;
		f3.predefinedHost=-1;
		functionsWriter.write(f3);
		
		Flow fl_ = new Flow();
		fl_.headId=f3.id;
		fl_.tailId=f2.id;
		fl_.id=fl_.tailId*16+fl_.headId;
		fl_.capabilityRequ=1;
		flowsWriter.write(fl_);

		Flow fl4 = new Flow();
		fl4.headId=nextFunctionId;
		fl4.tailId=nextFunctionId-1;
		fl4.id=fl4.tailId*16+fl4.headId;
		fl4.capabilityRequ=1;
		flowsWriter.write(fl4);
		
		Function f4 = new Function();
		f4.id=nextFunctionId++;
		f4.type=FunctionType.monitoring_aggregation;
		f4.parameter=parameter++;
		f4.capabilityRequ=1;
		f4.maxLatency=10;
		f4.predefinedHost=-1;
		functionsWriter.write(f4);
		
		Flow fl5 = new Flow();
		fl5.headId=nextFunctionId;
		fl5.tailId=nextFunctionId-1;
		fl5.id=fl5.tailId*16+fl5.headId;
		fl5.capabilityRequ=1;
		flowsWriter.write(fl5);
		
		Function f5 = new Function();
		f5.id=nextFunctionId++;
		f5.type=FunctionType.monitoring_append;
		f5.parameter=parameter++;
		f5.capabilityRequ=1;
		f5.maxLatency=10;
		f5.predefinedHost=-1;
		functionsWriter.write(f5);
		
		Flow fl6 = new Flow();
		fl6.headId=nextFunctionId;
		fl6.tailId=nextFunctionId-1;
		fl6.id=fl6.tailId*16+fl6.headId;
		fl6.capabilityRequ=1;
		flowsWriter.write(fl6);
		
		Function f6 = new Function();
		f6.id=nextFunctionId++;
		f6.type=FunctionType.monitoring_filter;
		f6.parameter=parameter++;
		f6.capabilityRequ=1;
		f6.maxLatency=10;
		f6.predefinedHost=-1;
		functionsWriter.write(f6);
		
	}
	
	public static void redGraphWithUsers(int usersNumber) throws YamlException{
		
		Function p0 = new Function();
		p0.id=nextFunctionId++;
		p0.type=FunctionType.probe;
		p0.parameter=1;
		p0.capabilityRequ=1;
		p0.maxLatency=-1;
		p0.predefinedHost=firstedgeDeviceId+hostingServer.nextInt(edgeDevicesNb);
		functionsWriter.write(p0);
		
		Function p1 = new Function();
		p1.id=nextFunctionId++;
		p1.type=FunctionType.probe;
		p1.parameter=1;
		p1.capabilityRequ=1;
		p1.maxLatency=-1;
		p1.predefinedHost=firstedgeDeviceId+hostingServer.nextInt(edgeDevicesNb);
		functionsWriter.write(p1);
		
		Function p2 = new Function();
		p2.id=nextFunctionId++;
		p2.type=FunctionType.probe;
		p2.parameter=1;
		p2.capabilityRequ=1;
		p2.maxLatency=-1;
		p2.predefinedHost=firstedgeDeviceId+hostingServer.nextInt(edgeDevicesNb);
		functionsWriter.write(p2);
		
		Function p3 = new Function();
		p3.id=nextFunctionId++;
		p3.type=FunctionType.probe;
		p3.parameter=1;
		p3.capabilityRequ=1;
		p3.maxLatency=-1;
		p3.predefinedHost=firstedgeDeviceId+hostingServer.nextInt(edgeDevicesNb);
		functionsWriter.write(p3);
		
		for (int i=0; i<usersNumber; i++){
			
			Flow fl0 = new Flow();
			fl0.headId=nextFunctionId;
			fl0.tailId=p0.id;
			fl0.id=fl0.tailId*16+fl0.headId;
			fl0.capabilityRequ=1;
			flowsWriter.write(fl0);
			
			redGraph();
			
			//probe 1 <-> join 1
			Flow fl1 = new Flow();
			fl1.headId=nextFunctionId-8;
			fl1.tailId=p1.id;
			fl1.id=fl1.tailId*16+fl1.headId;
			fl1.capabilityRequ=1;
			flowsWriter.write(fl1);
			
			// probe 2 <-> join 2
			Flow fl2 = new Flow();
			fl2.headId=nextFunctionId-4;
			fl2.tailId=p2.id;
			fl2.id=fl2.tailId*16+fl2.headId;
			fl2.capabilityRequ=1;
			flowsWriter.write(fl2);

			// probe 3 <-> join 2
			Flow fl3 = new Flow();
			fl3.headId=nextFunctionId-4;
			fl3.tailId=p3.id;
			fl3.id=fl3.tailId*16+fl3.headId;
			fl3.capabilityRequ=1;
			flowsWriter.write(fl3);
			
			// user <-> filter 3
			Flow fl4 = new Flow();
			fl4.headId=nextFunctionId;
			fl4.tailId=nextFunctionId-1;
			fl4.id=fl4.tailId*16+fl4.headId;
			fl4.capabilityRequ=1;
			flowsWriter.write(fl4);
			
			Function u0 = new Function();
			u0.id=nextFunctionId++;
			u0.type=FunctionType.user;
			u0.parameter=1;
			u0.capabilityRequ=1;
			u0.predefinedHost=hostingServer.nextInt(centralPoPsNb+regionalPoPsNb+edgeDevicesNb);
			
			int l=minLatency(p0.predefinedHost, u0.predefinedHost);
			if (minLatency(p1.predefinedHost, u0.predefinedHost)>l)
				l=minLatency(p1.predefinedHost, u0.predefinedHost);
			if (minLatency(p2.predefinedHost, u0.predefinedHost)>l)
				l=minLatency(p2.predefinedHost, u0.predefinedHost);
			if (minLatency(p3.predefinedHost, u0.predefinedHost)>l)
				l=minLatency(p3.predefinedHost, u0.predefinedHost);
			u0.maxLatency=l+latency.nextInt(maxLatency()-l);
			
			functionsWriter.write(u0);
			

		}
		
	}
	
	public static int minLatency(int point1Id, int point2Id){
		
		int result = 0;
	
		if (point2Id<point1Id){
			int tmp = point1Id;
			point1Id = point2Id;
			point2Id = tmp;
		}
		
		if(point1Id==point2Id){
			result=0;
		}
		//point2Id is edgeDevice
		else if (point2Id>=firstedgeDeviceId){
			int regionalPoPOfPoint2Id = ((point2Id-(centralPoPsNb+regionalPoPsNb)) / (edgeDevicesNb/regionalPoPsNb))+centralPoPsNb;
			result = 8 + minLatency(point1Id,regionalPoPOfPoint2Id);
		}
		//point1Id is edgeDevice
		else if (point1Id>=firstedgeDeviceId){
			int regionalPoPOfPoint1Id = ((point1Id-(centralPoPsNb+regionalPoPsNb)) / (edgeDevicesNb/regionalPoPsNb))+centralPoPsNb;
			result = 8 + minLatency(regionalPoPOfPoint1Id,point2Id);
		}
		//point2Id is RegionalPoP
		else if (point2Id>=firstRegionalPoPId && point2Id<firstedgeDeviceId){
			int centralPoPOfPoint2Id = (point2Id-centralPoPsNb) / (regionalPoPsNb/centralPoPsNb);
			result = 12 + minLatency(point1Id,centralPoPOfPoint2Id);
		}
		//point1Id is RegionalPoP
		else if (point1Id>=firstRegionalPoPId && point1Id<firstedgeDeviceId){
			int centralPoPOfPoint1Id = (point1Id-centralPoPsNb) / (regionalPoPsNb/centralPoPsNb);
			result = 12 + minLatency(centralPoPOfPoint1Id,point2Id);
		}
		//point1Id and point2Id are Central PoPs
		else if(point1Id<firstRegionalPoPId && point2Id<firstRegionalPoPId){
			//choose the shortest path between them
			if ((point2Id - point1Id) < (centralPoPsNb/2))
				result = (point2Id - point1Id) * 22;
			else
				result = (centralPoPsNb - (point2Id - point1Id)) * 22;
		}
		return result;
	}
	
	public static int maxLatency(){
		
		int centralPoPsNb=0;
		
		if(infrastructureId==2){
			centralPoPsNb=4;
		}else if (infrastructureId==3){
			centralPoPsNb=256;
		}
		
		return (centralPoPsNb/2)*22+2*12+2*8+50; //50: marge supplementaire
	}
	
	static void infrastructureParameters(int infrastructure){
		if(infrastructure==2){
			infrastructureId=2;
			centralPoPsNb = 4; regionalPoPsNb = 8; edgeDevicesNb = 16;
			firstRegionalPoPId = centralPoPsNb;
			firstedgeDeviceId = centralPoPsNb + regionalPoPsNb;
		}else if (infrastructure==3){
			infrastructureId=3;
			centralPoPsNb = 256; regionalPoPsNb = 512; edgeDevicesNb = 1024;
			firstRegionalPoPId = centralPoPsNb;
			firstedgeDeviceId = centralPoPsNb + regionalPoPsNb;
		}
	}
	
	public static void main(String[] args) throws IOException{

		//parameter=2;
		
		/*
		//usersRequirementsId = 22; infrastructureParameters(2);
		usersRequirementsId = 23; infrastructureParameters(3);
		destinationFolder = "C:/Users/THLR0145/Desktop/mohamed/workspace/PlacementCalculator/src/main/resources";
		functionsWriter = new YamlWriter(new FileWriter(destinationFolder+"/usersRequirements_"+usersRequirementsId+"/functions.yml"));
		flowsWriter = new YamlWriter(new FileWriter(destinationFolder+"/usersRequirements_"+usersRequirementsId+"/flows.yml"));
		blueGraphWithUsers(4);*/
		
		/*
		usersRequirementsId = 32; infrastructureParameters(2);
		//usersRequirementsId = 33; infrastructureParameters(3);
		destinationFolder = "C:/Users/THLR0145/Desktop/mohamed/workspace/PlacementCalculator/src/main/resources";
		functionsWriter = new YamlWriter(new FileWriter(destinationFolder+"/usersRequirements_"+usersRequirementsId+"/functions.yml"));
		flowsWriter = new YamlWriter(new FileWriter(destinationFolder+"/usersRequirements_"+usersRequirementsId+"/flows.yml"));
		greenGraphWithUsers(4);*/

		/*
		usersRequirementsId = 42; infrastructureParameters(2);
		//usersRequirementsId = 43; infrastructureParameters(3);
		destinationFolder = "C:/Users/THLR0145/Desktop/mohamed/workspace/PlacementCalculator/src/main/resources";
		functionsWriter = new YamlWriter(new FileWriter(destinationFolder+"/usersRequirements_"+usersRequirementsId+"/functions.yml"));
		flowsWriter = new YamlWriter(new FileWriter(destinationFolder+"/usersRequirements_"+usersRequirementsId+"/flows.yml"));
		redGraphWithUsers(4);*/
		
		/*
		usersRequirementsId = 52; infrastructureParameters(2);
		//usersRequirementsId = 53; infrastructureParameters(3);
		destinationFolder = "C:/Users/THLR0145/Desktop/mohamed/workspace/PlacementCalculator/src/main/resources";
		functionsWriter = new YamlWriter(new FileWriter(destinationFolder+"/usersRequirements_"+usersRequirementsId+"/functions.yml"));
		flowsWriter = new YamlWriter(new FileWriter(destinationFolder+"/usersRequirements_"+usersRequirementsId+"/flows.yml"));
		blueGraphWithUsers(4);
		blueGraphWithUsers(16);
		blueGraphWithUsers(32);*/
		
		/*
		//usersRequirementsId = 62; infrastructureParameters(2);
		usersRequirementsId = 63; infrastructureParameters(3);
		destinationFolder = "C:/Users/THLR0145/Desktop/mohamed/workspace/PlacementCalculator/src/main/resources";
		functionsWriter = new YamlWriter(new FileWriter(destinationFolder+"/usersRequirements_"+usersRequirementsId+"/functions.yml"));
		flowsWriter = new YamlWriter(new FileWriter(destinationFolder+"/usersRequirements_"+usersRequirementsId+"/flows.yml"));
		blueGraphWithUsers(16);
		blueGraphWithUsers(64);
		blueGraphWithUsers(128);*/

		functionsWriter.close();
		flowsWriter.close();
		
		System.out.println("Test scenarios generated");
	}
}