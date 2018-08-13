package com.orange.holisticMonitoring.placement.calculator;

public class CalculationResult {
	
	public float firstSolution_time;
	public float firstSolution_computeFootprint;
	public float firstSolution_networkFootprint;
	public float firstSolution_nbMutualisations;
	
	public float lastSolution_time;
	public float lastSolution_computeFootprint;
	public float lastSolution_networkFootprint;
	public float lastSolution_nbMutualisations;
	
	public CalculationResult() {
				
		this.firstSolution_time = 0;
		this.firstSolution_computeFootprint = 0;
		this.firstSolution_networkFootprint = 0;
		this.firstSolution_nbMutualisations = 0;
		
		this.lastSolution_time = 0;
		this.lastSolution_computeFootprint = 0;
		this.lastSolution_networkFootprint = 0;
		this.lastSolution_nbMutualisations = 0;
	}
	
	public void add(CalculationResult c){
		this.firstSolution_time = this.firstSolution_time+c.firstSolution_time;
		this.firstSolution_computeFootprint = this.firstSolution_computeFootprint+c.firstSolution_computeFootprint;
		this.firstSolution_networkFootprint = this.firstSolution_networkFootprint+c.firstSolution_networkFootprint;
		this.firstSolution_nbMutualisations = this.firstSolution_nbMutualisations+c.firstSolution_nbMutualisations;
		
		this.lastSolution_time = this.lastSolution_time+c.lastSolution_time;
		this.lastSolution_computeFootprint = this.lastSolution_computeFootprint+c.lastSolution_computeFootprint;
		this.lastSolution_networkFootprint = this.lastSolution_networkFootprint+c.lastSolution_networkFootprint;
		this.lastSolution_nbMutualisations = this.lastSolution_nbMutualisations+c.lastSolution_nbMutualisations;
	}

	public void div(float d){
		
		this.firstSolution_time = this.firstSolution_time/d;
		this.firstSolution_computeFootprint = this.firstSolution_computeFootprint/d;
		this.firstSolution_networkFootprint = this.firstSolution_networkFootprint/d;
		this.firstSolution_nbMutualisations = this.firstSolution_nbMutualisations/d;
		
		this.lastSolution_time = this.lastSolution_time/d;
		this.lastSolution_computeFootprint = this.lastSolution_computeFootprint/d;
		this.lastSolution_networkFootprint = this.lastSolution_networkFootprint/d;
		this.lastSolution_nbMutualisations = this.lastSolution_nbMutualisations/d;
		
	}
		
}
