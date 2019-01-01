package com.orange.holisticMonitoring.placement.test;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import com.orange.holisticMonitoring.placement.calculator.CalculationResult;
import com.orange.holisticMonitoring.placement.calculator.MutualisedCalculator;
import com.orange.holisticMonitoring.placement.calculator.NonMutualisedCalculator;
import com.orange.holisticMonitoring.placement.cli.DefaultPath;

public class Tester {
	
	public static void main(String[] args) throws IOException {

		DefaultPath defaultPath = new DefaultPath();

		System.out.println("Non-mutualised placement calculation");
		NonMutualisedCalculator.calculate(defaultPath.defaultPath+"infrastructure_I",defaultPath.defaultPath+"usersRequirements_UR",true,"10");
		
		System.out.println();
		
		System.out.println("Mutualised placement calculation");
	MutualisedCalculator.calculate(defaultPath.defaultPath+"infrastructure_I",defaultPath.defaultPath+"usersRequirements_UR",true,"10");
		
    }

}
