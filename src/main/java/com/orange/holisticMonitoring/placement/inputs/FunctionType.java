package com.orange.holisticMonitoring.placement.inputs;

public enum FunctionType {
	user(0),
	probe(1),
	monitoring_join(2),
	monitoring_split(3),
	monitoring_append(4),
	monitoring_filter(5),
	monitoring_aggregation(6),
	monitoring_notify(7),
	monitoring_persist(8);
	
	public final int value;

	private FunctionType(int value) {
		this.value = value;
	}
	  
};
