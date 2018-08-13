package com.orange.holisticMonitoring.placement.test;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import com.orange.holisticMonitoring.placement.calculator.CalculationResult;
import com.orange.holisticMonitoring.placement.calculator.MutualisedCalculator;
import com.orange.holisticMonitoring.placement.calculator.NonMutualisedCalculator;

public class Tester {
	
	public static void main(String[] args) throws IOException {
    	
		System.out.println("Non-mutualised placement calculation");
		NonMutualisedCalculator.calculate("I","UR");
		
		System.out.println();
		
		System.out.println("Mutualised placement calculation");
		NonMutualisedCalculator.calculate("I","UR");
		
    }

}
