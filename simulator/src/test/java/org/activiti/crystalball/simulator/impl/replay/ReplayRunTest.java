package org.activiti.crystalball.simulator.impl.replay;

import org.activiti.crystalball.simulator.ReplaySimulationRun;
import org.activiti.crystalball.simulator.SimulationDebugger;
import org.activiti.crystalball.simulator.SimulationEventHandler;
import org.activiti.crystalball.simulator.delegate.UserTaskExecutionListener;
import org.activiti.crystalball.simulator.delegate.event.ActivitiEventToSimulationEventTransformer;
import org.activiti.crystalball.simulator.delegate.event.impl.InMemoryRecordActivitiEventListener;
import org.activiti.crystalball.simulator.delegate.event.impl.ProcessInstanceCreateTransformer;
import org.activiti.crystalball.simulator.delegate.event.impl.UserTaskCompleteTransformer;
import org.activiti.crystalball.simulator.impl.StartReplayProcessEventHandler;
import org.activiti.crystalball.simulator.impl.bpmn.parser.handler.AddListenerUserTaskParseHandler;
import org.activiti.crystalball.simulator.impl.playback.PlaybackUserTaskCompleteEventHandler;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.parse.BpmnParseHandler;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class ReplayRunTest {

  // Process instance start event
  private static final String PROCESS_INSTANCE_START_EVENT_TYPE = "PROCESS_INSTANCE_START";
  private static final String PROCESS_DEFINITION_ID_KEY = "processDefinitionId";
  private static final String VARIABLES_KEY = "variables";
  // User task completed event
  private static final String USER_TASK_COMPLETED_EVENT_TYPE = "USER_TASK_COMPLETED";

  private static final String USERTASK_PROCESS = "oneTaskProcess";
  private static final String BUSINESS_KEY = "testBusinessKey";
  private static final String TEST_VALUE = "TestValue";
  private static final String TEST_VARIABLE = "testVariable";

  protected static InMemoryRecordActivitiEventListener listener = new InMemoryRecordActivitiEventListener(getTransformers());

  private static final String THE_USERTASK_PROCESS = "org/activiti/crystalball/simulator/impl/playback/PlaybackProcessStartTest.testUserTask.bpmn20.xml";

  @Test
  public void testProcessInstanceStartEvents() throws Exception {
    ProcessEngineImpl processEngine = initProcessEngine();

    TaskService taskService = processEngine.getTaskService();
    RuntimeService runtimeService = processEngine.getRuntimeService();

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put(TEST_VARIABLE, TEST_VALUE);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(USERTASK_PROCESS, BUSINESS_KEY, variables);

    Task task = taskService.createTaskQuery().taskDefinitionKey("userTask").singleResult();
    TimeUnit.MILLISECONDS.sleep(50);
    taskService.complete(task.getId());

    final SimulationDebugger simRun = new ReplaySimulationRun(processEngine, getReplayHandlers(processInstance.getId()));

    simRun.init();

    // original process is finished - there should not be any running process instance/task
    assertEquals(0, runtimeService.createProcessInstanceQuery().processDefinitionKey(USERTASK_PROCESS).count());
    assertEquals(0, taskService.createTaskQuery().taskDefinitionKey("userTask").count());

    simRun.step();

    // replay process was started
    assertEquals(1, runtimeService.createProcessInstanceQuery().processDefinitionKey(USERTASK_PROCESS).count());
    // there should be one task
    assertEquals(1, taskService.createTaskQuery().taskDefinitionKey("userTask").count());

    simRun.step();

    // userTask was completed - replay process was finished
    assertEquals(0, runtimeService.createProcessInstanceQuery().processDefinitionKey(USERTASK_PROCESS).count());
    assertEquals(0, taskService.createTaskQuery().taskDefinitionKey("userTask").count());

    simRun.close();
    processEngine.close();
    ProcessEngines.destroy();
  }

  private ProcessEngineImpl initProcessEngine() {
    ProcessEngineConfigurationImpl configuration = getProcessEngineConfiguration();
    ProcessEngineImpl processEngine = (ProcessEngineImpl) configuration.buildProcessEngine();

    processEngine.getRepositoryService().
        createDeployment().
        addClasspathResource(THE_USERTASK_PROCESS).
        deploy();
    return processEngine;
  }

  private ProcessEngineConfigurationImpl getProcessEngineConfiguration() {
    ProcessEngineConfigurationImpl configuration = new org.activiti.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration();
    configuration.setHistory("full");
    configuration.setJobExecutorActivate(false);
    configuration.setCustomDefaultBpmnParseHandlers(Arrays.<BpmnParseHandler>asList(new AddListenerUserTaskParseHandler(TaskListener.EVENTNAME_CREATE, new UserTaskExecutionListener(USER_TASK_COMPLETED_EVENT_TYPE, USER_TASK_COMPLETED_EVENT_TYPE, listener.getSimulationEvents()))));
    configuration.setEventListeners(Arrays.<ActivitiEventListener>asList(listener));
    return configuration;
  }

  private static List<ActivitiEventToSimulationEventTransformer> getTransformers() {
    List<ActivitiEventToSimulationEventTransformer> transformers = new ArrayList<ActivitiEventToSimulationEventTransformer>();
    transformers.add(new ProcessInstanceCreateTransformer(PROCESS_INSTANCE_START_EVENT_TYPE, PROCESS_DEFINITION_ID_KEY, BUSINESS_KEY, VARIABLES_KEY));
    transformers.add(new UserTaskCompleteTransformer(USER_TASK_COMPLETED_EVENT_TYPE));
    return transformers;
  }

  public static Map<String, SimulationEventHandler> getReplayHandlers(String processInstanceId) {
    Map<String, SimulationEventHandler> handlers = new HashMap<String, SimulationEventHandler>();
    handlers.put(PROCESS_INSTANCE_START_EVENT_TYPE, new StartReplayProcessEventHandler(processInstanceId, PROCESS_INSTANCE_START_EVENT_TYPE, PROCESS_INSTANCE_START_EVENT_TYPE, listener.getSimulationEvents(), PROCESS_DEFINITION_ID_KEY, BUSINESS_KEY, VARIABLES_KEY));
    handlers.put(USER_TASK_COMPLETED_EVENT_TYPE, new PlaybackUserTaskCompleteEventHandler());
    return handlers;
  }
}