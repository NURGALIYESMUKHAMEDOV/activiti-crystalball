package org.activiti.crystalball.simulator.impl;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.activiti.crystalball.simulator.SimulationContext;
import org.activiti.crystalball.simulator.SimulationEvent;
import org.activiti.crystalball.simulator.SimulationEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CompleteEventHandler implements SimulationEventHandler {

	private static Logger log = LoggerFactory.getLogger(CompleteEventHandler.class);
	
	@Override
	public void handle(SimulationEvent event, SimulationContext context) {
		String taskId = (String) event.getProperty("task");
		context.getTaskService().complete( taskId );
		log.debug( SimpleDateFormat.getTimeInstance().format( new Date(event.getSimulationTime())) +": completed taskId "+ taskId);
	}

	@Override
	public void init(SimulationContext context) {
		
	}

}