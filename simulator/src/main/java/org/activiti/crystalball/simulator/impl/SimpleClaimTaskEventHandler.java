package org.activiti.crystalball.simulator.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.activiti.crystalball.simulator.SimulationContext;
import org.activiti.crystalball.simulator.SimulationEvent;
import org.activiti.crystalball.simulator.SimulationEventHandler;
import org.activiti.crystalball.simulator.executor.UserTaskExecutor;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.util.ClockUtil;
import org.activiti.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * claim task - we have unlimited resources to process a task. 
 *
 */
public class SimpleClaimTaskEventHandler implements SimulationEventHandler {
	private static final String DEFAULT_USER = "default_user";

	private static Logger log = LoggerFactory.getLogger(SimpleClaimTaskEventHandler.class);
	
	protected UserTaskExecutor userTaskExecutor;
	
	/**
	 * everybody should start to work on something - we have unlimited resources 
	 * :-)
	 * 
	 */
	@Override
	public void init(SimulationContext context) {
		TaskService taskService = context.getTaskService();
		long simulationTime = ClockUtil.getCurrentTime().getTime();
		
		for ( Task execTask : taskService.createTaskQuery().list()) {
			if (execTask != null ) {
				// claim task and update assignee
				log.debug( SimpleDateFormat.getTimeInstance().format( new Date(simulationTime)) + 
						": claiming task  [" + execTask.getName() + "][" +execTask.getId() +"] for user ["+DEFAULT_USER+"]");
				taskService.claim( execTask.getId(), DEFAULT_USER);
				
				// create complete task event
				Map<String, Object> props = new HashMap<String, Object>();
				props.put( "task", execTask.getId());
				Map<String, Object> variables = new HashMap<String, Object>();
				long userTaskDelta = userTaskExecutor.simulateTaskExecution((TaskEntity) execTask, variables);
				props.put( "variables", variables);

				SimulationEvent completeEvent = new SimulationEvent( simulationTime + userTaskDelta, SimulationEvent.TYPE_TASK_COMPLETE, props);
				// schedule complete task event
				context.getEventCalendar().addEvent( completeEvent);
			}
		}
	}

	@Override
	public void handle(SimulationEvent event, SimulationContext context) {
		TaskEntity task = (TaskEntity) event.getProperty();
		
		context.getTaskService().claim(task.getId(), DEFAULT_USER);		
		task.setAssignee( DEFAULT_USER );
		
		// create complete task event
		Map<String, Object> props = new HashMap<String, Object>();
		props.put( "task", task.getId());
		Map<String, Object> variables = new HashMap<String, Object>();
		long userTaskDelta = userTaskExecutor.simulateTaskExecution(task, variables);
		props.put( "variables", variables);
	
		SimulationEvent completeEvent = new SimulationEvent( ClockUtil.getCurrentTime().getTime() + userTaskDelta, SimulationEvent.TYPE_TASK_COMPLETE, props);
		// schedule complete task event
		context.getEventCalendar().addEvent( completeEvent);
			
		log.debug( SimpleDateFormat.getTimeInstance().format( new Date(event.getSimulationTime())) +": claimed {}, name: {}, assignee: {}", task, task.getName(), task.getAssignee());

	}

	public UserTaskExecutor getUserTaskExecutor() {
		return userTaskExecutor;
	}

	public void setUserTaskExecutor(UserTaskExecutor userTaskExecutor) {
		this.userTaskExecutor = userTaskExecutor;
	}


}
