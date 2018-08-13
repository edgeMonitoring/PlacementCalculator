package com.orange.holisticMonitoring.placement.inputs;

import java.io.FileNotFoundException;
import com.esotericsoftware.yamlbeans.YamlException;

public class InputBuilder {
	
	public static void main(String[] args) throws YamlException, FileNotFoundException {
		/*
		int infrastrctureId=0;
		G_infrastructure g_infrastructure=new G_infrastructure(infrastrctureId);
		for(Server server : g_infrastructure.getServers() )
			System.out.println(server.id);
		*/
		String usersRequirementId="0";
		G_usersRequirements g_usersRequirements=new G_usersRequirements(usersRequirementId);
		for(Function function: g_usersRequirements.getFunctions() )
			System.out.println(function.parameter);
	    
	  }


}
