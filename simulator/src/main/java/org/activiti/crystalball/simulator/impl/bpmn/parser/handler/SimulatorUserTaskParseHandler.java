package org.activiti.crystalball.simulator.impl.bpmn.parser.handler;

import org.activiti.bpmn.model.UserTask;
import org.activiti.crystalball.simulator.SimulationEvent;
import org.activiti.crystalball.simulator.delegate.UserTaskExecutionListener;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.bpmn.parser.handler.UserTaskParseHandler;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.activiti.engine.impl.pvm.process.ScopeImpl;

/**
 * This class changes UserTaskBehavior for simulation purposes.
 */
public class SimulatorUserTaskParseHandler extends UserTaskParseHandler {

  protected void executeParse(BpmnParse bpmnParse, UserTask userTask) {
    super.executeParse(bpmnParse, userTask);

    ScopeImpl scope = bpmnParse.getCurrentScope();
    ProcessDefinitionImpl processDefinition = scope.getProcessDefinition();
    ActivityImpl activity = processDefinition.findActivity(userTask.getId());

    SimulatorParserUtils.setSimulationBehavior(scope, userTask);

    UserTaskActivityBehavior userTaskActivity = (UserTaskActivityBehavior) activity.getActivityBehavior();
    userTaskActivity.getTaskDefinition().addTaskListener(TaskListener.EVENTNAME_CREATE, new UserTaskExecutionListener(SimulationEvent.TYPE_TASK_CREATE));

  }
}
