package org.activiti.crystalball.simulator.executor.impl;

import java.util.List;
import java.util.Map;
import java.util.Random;

import org.activiti.crystalball.simulator.executor.UserTaskExecutor;
import org.activiti.engine.HistoryService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.persistence.entity.TaskEntity;

/**
 * Simulate user work based on taskinstance history, in the case when there is no task history 
 * use back up executor to generate task complete simulation time  
 *
 */
public class TaskInstanceHistoryExecutor implements UserTaskExecutor {
	
	private HistoryService historyService;
	
	/** executor used in the case when there is no user task history which can be used*/
	private UserTaskExecutor backUpExecutor;	
	
	private Random randomGenerator;
	
	public TaskInstanceHistoryExecutor() {
		randomGenerator = new Random();
	}

	public TaskInstanceHistoryExecutor(HistoryService historyService) {
		randomGenerator = new Random();
		this.setHistoryService(historyService);
	}

	
	/**
	 * simulate task execution
	 * take randomly one task from the history and use its duration for simulation  
	 * 
	 */
	public long simulateTaskExecution(TaskEntity execTask, Map<String, Object> variables) {
		List<HistoricTaskInstance> historicInstances = historyService.createHistoricTaskInstanceQuery()
			.taskDefinitionKey( execTask.getTaskDefinitionKey() ).finished().list();
		
		if ( historicInstances.isEmpty() )
			//use backupExecutor
			return backUpExecutor.simulateTaskExecution(execTask, variables);
		
		// get random historic instance 
		HistoricTaskInstance historicTask = historicInstances.get( randomGenerator.nextInt( historicInstances.size() ));
		//@TODO in version 5.12 use time spend on task 
		return historicTask.getDurationInMillis();
	}

	public HistoryService getHistoryService() {
		return historyService;
	}

	public void setHistoryService(HistoryService historyService) {
		this.historyService = historyService;
	}

	public UserTaskExecutor getBackUpExecutor() {
		return backUpExecutor;
	}

	public void setBackUpExecutor(UserTaskExecutor backUpExecutor) {
		this.backUpExecutor = backUpExecutor;
	}

}
