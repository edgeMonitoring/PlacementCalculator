
package com.orange.holisticMonitoring.placement.cli;

public class DefaultPath {

	public String defaultPath;

	public DefaultPath(){

		defaultPath = getClass().getClassLoader().getResource("").getPath();

	}

}
