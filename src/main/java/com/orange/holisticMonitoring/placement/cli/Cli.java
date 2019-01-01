package com.orange.holisticMonitoring.placement.cli;

import java.util.Scanner;
import java.io.IOException;

import com.orange.holisticMonitoring.placement.calculator.MutualisedCalculator;
import com.orange.holisticMonitoring.placement.calculator.NonMutualisedCalculator;

public class Cli {

	public static void main(String[] args) throws IOException {
		
		Scanner scanner = new Scanner(System.in);
				
		System.out.println("\n------------------------");
		System.out.println("| Placement Calculator |");
		System.out.println("------------------------");

		System.out.print("\nChoose a placement type : \n    [1] Non-mutualized \n    [2] Mutualized \nYour choice (1/2) : \n");
		String placementType = scanner.nextLine();
		while (!(placementType.equals("1")||placementType.equals("2"))){
			System.out.println("ERROR: Invalid input. Please enter \"1\" or \"2\" :");
			placementType = scanner.nextLine();
		}

		System.out.print("\nEnter the path for users requirements input (press enter key for default input) : \n");
		//scanner.nextLine();
		String usersRequirements=scanner.nextLine();
		if (usersRequirements.isEmpty()){
			DefaultPath defaultPath = new DefaultPath();
			usersRequirements = defaultPath.defaultPath+"usersRequirements_UR";
		}

		System.out.print("\nEnter the path for the infrastructure input (press enter key for default input) : \n");
		//scanner.nextLine();
		String infrastructure=scanner.nextLine();
		if (infrastructure.isEmpty()){
			DefaultPath defaultPath = new DefaultPath();
			infrastructure = defaultPath.defaultPath+"infrastructure_I";
		}

		System.out.print("\nChoose a resolution type : \n    [1] A single solution \n    [2] Many solutions \nYour choice (1/2) : \n");
		String resolutionType = scanner.nextLine();
		boolean singleSolution = true;
		while (!(resolutionType.equals("1")||resolutionType.equals("2"))){
			System.out.println("ERROR: Invalid input. Please enter \"1\" or \"2\" :");
			resolutionType = scanner.nextLine();
		}
		String calculationDuration = "0";
		if (resolutionType.equals("2")){
			singleSolution=false;
		}

		System.out.print("\nEnter the calculation time limit (in minutes): \n");
		calculationDuration = scanner.nextLine();
		while (!calculationDuration.matches("\\d+")||calculationDuration.equals("0")){
			System.out.println("ERROR: Invalid input. Please enter a non-zero number :");
			calculationDuration = scanner.nextLine();
		}

		scanner.close();

		System.out.print("\nCalculation resut :\n\n");

		if (placementType.equals("1"))
			NonMutualisedCalculator.calculate(infrastructure,usersRequirements,singleSolution,calculationDuration);
		else if (placementType.equals("2"))
			MutualisedCalculator.calculate(infrastructure,usersRequirements,singleSolution,calculationDuration);
		
		

	}

}
