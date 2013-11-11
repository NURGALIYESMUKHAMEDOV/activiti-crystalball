package org.activiti.crystalball.simulator;

/*
 * #%L
 * simulator
 * %%
 * Copyright (C) 2012 - 2013 crystalball
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import org.activiti.crystalball.diagram.AuditTrailProcessDiagramGenerator;
import org.activiti.engine.*;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.util.ClockUtil;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * generate basic process engine state on which simulation is performed 
 *
 */
public class GenerateProcessEngineState {
	
	static String TEMP_DIR = "target";
	
	public static void main(String[] args) throws InterruptedException, IOException {
    if (args != null && args.length == 1)
		  TEMP_DIR = args[0];

		generateLiveDB();
		generateBasicEngine();
		generatePlaybackOriginal();
		
	}

	private static void generateBasicEngine()
			throws InterruptedException {
		String PROCESS_KEY = "threetasksprocess";
		RepositoryService repositoryService;
		RuntimeService runtimeService;
		TaskService taskService;
		IdentityService identityService;
		ProcessEngine processEngine;
		String liveDB = TEMP_DIR + "/BasicSimulation";
		
		// delete previous DB instalation and set property to point to the current file
		File prevDB = new File(liveDB+".h2.db");
		
		String previousLiveDB = System.getProperty("liveDB");
		
		System.setProperty("liveDB", liveDB);
		if ( prevDB.exists() )
			prevDB.delete();
		prevDB = null;

		ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext("org/activiti/crystalball/simulator/LiveEngine-context.xml");
		
		repositoryService = appContext.getBean( RepositoryService.class );
		runtimeService = appContext.getBean( RuntimeService.class );
		taskService = appContext.getBean( TaskService.class );
		identityService = appContext.getBean( IdentityService.class );
		processEngine = appContext.getBean( ProcessEngine.class);
		
		// deploy processes
		repositoryService.createDeployment()
	       .addClasspathResource("org/activiti/crystalball/simulator/ThreeTasksProcess.bpmn")
	       .deploy();

		// init identity service
		identityService.saveGroup( identityService.newGroup("Group1") );
		identityService.saveGroup( identityService.newGroup("Group2") );
		identityService.saveUser( identityService.newUser("user1") );
		identityService.saveUser( identityService.newUser("user2") );
		
		identityService.createMembership("user1", "Group1");
		identityService.createMembership("user2", "Group2");
		
		// start processes 
		Calendar calendar = Calendar.getInstance();
		calendar.set(2012, 11, 7, 18, 1, 00);
		Date dueDateFormal =   calendar.getTime();
		calendar.set(2012, 11, 7, 18, 1, 30);
		Date dueDateValue =   calendar.getTime();
		Map<String, Object> variables = new HashMap<String,Object>();
		variables.put("dueDateFormal", dueDateFormal);
		variables.put("dueDateValue", dueDateValue);
		
		for (int i = 0; i < 10; i++) {			
			runtimeService.startProcessInstanceByKey( PROCESS_KEY, "BUSINESS-KEY-" + i, variables);
		}

		
		// put first 5 tasks to the next node
		List<Task> taskList = taskService.createTaskQuery().taskCandidateUser("user1").list();
		for (int i = 0; i < 5; i++) {
				Task t = taskList.get(i);
				taskService.claim( t.getId(), "user1" );
		}
		
		// wait some time  to have some data in history 
		Thread.sleep(500);
		
		// complete these tasks
		for (int i = 0; i < 5; i++) {
			Task t = taskList.get(i);
			taskService.complete( t.getId() );
		}
		
		processEngine.close();
		
		appContext.close();
		
		if (previousLiveDB != null)
			System.setProperty("liveDB", previousLiveDB);
		else
			System.setProperty("liveDB", "");

	}

	private static void generateLiveDB() throws InterruptedException {
		
		RepositoryService repositoryService;
		IdentityService identityService;
		ProcessEngine processEngine;
		String LIVE_DB = TEMP_DIR + "/live-SimulateBottleneckTest";

		String previousLiveDB = System.getProperty("liveDB");
		System.setProperty("liveDB", LIVE_DB);
		
		// delete previous DB instalation and set property to point to the current file
		File prevDB = new File(LIVE_DB+".h2.db");
		
		if ( prevDB.exists() )
			prevDB.delete();
		prevDB = null;

		ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext("org/activiti/crystalball/simulator/LiveEngine-context.xml");
		
		repositoryService = appContext.getBean( RepositoryService.class );

		identityService = appContext.getBean( IdentityService.class );
		processEngine = appContext.getBean( ProcessEngine.class);
		
		// deploy processes
		repositoryService.createDeployment()
	       .addClasspathResource("org/activiti/crystalball/simulator/ParallelUserTasksProcess.bpmn")
	       .deploy();
		
		// init identity service
		identityService.saveGroup( identityService.newGroup("Group1") );
		identityService.saveGroup( identityService.newGroup("Group2") );
		identityService.saveGroup( identityService.newGroup("Group3") );
		identityService.saveGroup( identityService.newGroup("Group4") );
		identityService.saveUser( identityService.newUser("user1") );
		identityService.saveUser( identityService.newUser("user2") );
		identityService.saveUser( identityService.newUser("user3") );
		identityService.saveUser( identityService.newUser("user4") );
		
		identityService.createMembership("user1", "Group1");
		identityService.createMembership("user2", "Group2");
		identityService.createMembership("user3", "Group3");
		identityService.createMembership("user4", "Group4");
		identityService.createUserQuery().list();
		
		processEngine.close();
		
		appContext.close();		
		
		if (previousLiveDB != null)
			System.setProperty("liveDB", previousLiveDB);
		else
			System.setProperty("liveDB", "");
	}

	private static void generatePlaybackOriginal() throws InterruptedException, IOException {
		String PROCESS_KEY = "playback";
		RepositoryService repositoryService;
		RuntimeService runtimeService;
		TaskService taskService;
		IdentityService identityService;
		HistoryService historyService;
		ProcessEngine processEngine;
		String liveDB = TEMP_DIR + "/Playback";
				
		// delete previous DB instalation and set property to point to the current file
		File prevDB = new File(liveDB+".h2.db");
		
		String previousLiveDB = System.getProperty("liveDB");
		
		System.setProperty("liveDB", liveDB);
		if ( prevDB.exists() )
			prevDB.delete();
		prevDB = null;

		ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext("org/activiti/crystalball/simulator/LiveEngine-context.xml");
		Calendar calendar = Calendar.getInstance();
		calendar.set(2013, 01, 25, 14, 30, 00);
		ClockUtil.setCurrentTime( calendar.getTime());
		
		repositoryService = appContext.getBean( RepositoryService.class );
		runtimeService = appContext.getBean( RuntimeService.class );
		taskService = appContext.getBean( TaskService.class );
		identityService = appContext.getBean( IdentityService.class );		
		historyService = appContext.getBean( HistoryService.class );
		
		processEngine = appContext.getBean( ProcessEngine.class);
		
		// deploy processes
		repositoryService.createDeployment()
	       .addClasspathResource("org/activiti/crystalball/simulator/Playback.bpmn")
	       .deploy();

		// init identity service
		identityService.saveGroup( identityService.newGroup("Group1") );
		identityService.saveUser( identityService.newUser("user1") );
		
		identityService.createMembership("user1", "Group1");
		
		// start processes 
		Map<String, Object> variables2 = new HashMap<String,Object>();
		variables2.put("x", 3);
		Map<String, Object> variables1 = new HashMap<String,Object>();
		variables1.put("x", 1);
		Map<String, Object> variables0 = new HashMap<String,Object>();
		variables0.put("x", 0);

		ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey( PROCESS_KEY, "BUSINESS-KEY-1" , variables0);
		calendar.add( Calendar.SECOND, 1);
		ClockUtil.setCurrentTime( calendar.getTime());
		ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey( PROCESS_KEY, "BUSINESS-KEY-2" , variables1);
		calendar.add( Calendar.SECOND, 1);
		ClockUtil.setCurrentTime( calendar.getTime());
		runtimeService.startProcessInstanceByKey( PROCESS_KEY, "BUSINESS-KEY-3" , variables0);
		
		// put first 5 tasks to the next node
		List<Task> taskList = taskService.createTaskQuery().taskCandidateUser("user1").list();
		for ( Task t : taskList) {
				taskService.claim( t.getId(), "user1" );
		}
				
		// complete these tasks
		for (Task t : taskList) {
			// wait some time  to have some data in history 
			calendar.add( Calendar.SECOND, 1);
			ClockUtil.setCurrentTime( calendar.getTime());
			// complete task
			taskService.complete( t.getId(), variables2 );
		}
		
	    // audit trail images generator
		AuditTrailProcessDiagramGenerator generator = new AuditTrailProcessDiagramGenerator();
		generator.setHistoryService(historyService);
		generator.setRepositoryService((RepositoryServiceImpl) repositoryService);
		generator.setWriteUpdates(true);

		Map<String, Object> params = new HashMap<String,Object>();
		params.put( AuditTrailProcessDiagramGenerator.PROCESS_INSTANCE_ID, processInstance1.getId());
		
		ImageIO.write( ImageIO.read(generator.generateLayer("png", params))
				, "png"
				, new File( TEMP_DIR+"/playback-auditTrail1.png"));

		params.clear();
		params.put( AuditTrailProcessDiagramGenerator.PROCESS_INSTANCE_ID, processInstance2.getId());
		
		ImageIO.write( ImageIO.read(generator.generateLayer("png", params))
				, "png"
				, new File( TEMP_DIR+"/playback-auditTrail2.png"));

		processEngine.close();
		
		appContext.close();
		
		if (previousLiveDB != null)
			System.setProperty("liveDB", previousLiveDB);
		else
			System.setProperty("liveDB", "");

	}

}
