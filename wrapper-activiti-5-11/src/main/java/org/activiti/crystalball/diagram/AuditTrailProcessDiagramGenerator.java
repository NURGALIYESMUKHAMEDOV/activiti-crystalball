package org.activiti.crystalball.diagram;

/*
 * #%L
 * image-builder
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


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.activiti.crystalball.processengine.wrapper.HistoryServiceWrapper;
import org.activiti.crystalball.processengine.wrapper.ProcessDiagramService;
import org.activiti.crystalball.processengine.wrapper.activiti.ProcessDiagramCanvasImpl;
import org.activiti.crystalball.processengine.wrapper.queries.HistoricActivityInstanceWrapper;
import org.activiti.crystalball.processengine.wrapper.queries.HistoricDetailWrapper;
import org.activiti.crystalball.processengine.wrapper.queries.HistoricProcessInstanceWrapper;
import org.activiti.engine.history.HistoricVariableUpdate;
import org.activiti.engine.impl.bpmn.behavior.BoundaryEventActivityBehavior;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;

/**
 *	generate audit trail diagram layer based on audit trail data in historyService 
 *	TODO: Parallel activities
 */
public class AuditTrailProcessDiagramGenerator extends AbstractProcessDiagramLayerGenerator {
	
	protected static Logger log = Logger.getLogger(AuditTrailProcessDiagramGenerator.class.getName());

	protected HistoryServiceWrapper historyService;
	
	public static final String PROCESS_INSTANCE_ID = "process_instance_id";

	/** transition line hight */
	private static final int TRANSITION_HEIGHT = 20;
	private int maxX = 0;
	private int maxY = 0;
	
	protected boolean writeUpdates = false;
	
	//
	// Copied from ProcessDiagramGenerator
	//
	  protected static final Map<String, ActivityDrawXYInstruction> activityDrawInstructions = new HashMap<String, ActivityDrawXYInstruction>();

	  // The instructions on how to draw a certain construct is
	  // created statically and stored in a map for performance.
	  static {
	    // start event
	    activityDrawInstructions.put("startEvent", new ActivityDrawXYInstruction() {

	      public void draw(ProcessDiagramCanvasImpl processDiagramCreator, ActivityImpl activityImpl, int x, int y) {
	        processDiagramCreator.drawNoneStartEvent(x, y, activityImpl.getWidth(), activityImpl.getHeight());
	      }
	    });

	    // start timer event
	    activityDrawInstructions.put("startTimerEvent", new ActivityDrawXYInstruction() {

	      public void draw(ProcessDiagramCanvasImpl processDiagramCreator, ActivityImpl activityImpl, int x, int y) {
	        processDiagramCreator.drawTimerStartEvent(x, y, activityImpl.getWidth(), activityImpl.getHeight());
	      }
	    });
	    
	    // signal catch
	    activityDrawInstructions.put("intermediateSignalCatch", new ActivityDrawXYInstruction() {
	      
	      public void draw(ProcessDiagramCanvasImpl processDiagramCreator, ActivityImpl activityImpl, int x, int y) {
	    	BoundaryEventActivityBehavior behavior = (BoundaryEventActivityBehavior)activityImpl.getActivityBehavior();
	        processDiagramCreator.drawCatchingSignalEvent(x, y, activityImpl.getWidth(), activityImpl.getHeight(), behavior.isInterrupting());
	      }
	    });
	    
	    // signal throw
	    activityDrawInstructions.put("intermediateSignalThrow", new ActivityDrawXYInstruction() {
	      
	      public void draw(ProcessDiagramCanvasImpl processDiagramCreator, ActivityImpl activityImpl, int x, int y) {
	        processDiagramCreator.drawThrowingSignalEvent(x, y, activityImpl.getWidth(), activityImpl.getHeight());
	      }
	    });
	    
	    // end event
	    activityDrawInstructions.put("endEvent", new ActivityDrawXYInstruction() {

	      public void draw(ProcessDiagramCanvasImpl processDiagramCreator, ActivityImpl activityImpl, int x, int y) {
	        processDiagramCreator.drawNoneEndEvent(x, y, activityImpl.getWidth(), activityImpl.getHeight());
	      }
	    });

	    // error end event
	    activityDrawInstructions.put("errorEndEvent", new ActivityDrawXYInstruction() {

	      public void draw(ProcessDiagramCanvasImpl processDiagramCreator, ActivityImpl activityImpl, int x, int y) {
	        processDiagramCreator.drawErrorEndEvent(x, y, activityImpl.getWidth(), activityImpl.getHeight());
	      }
	    });
	    
	    // error start event
	    activityDrawInstructions.put("errorStartEvent", new ActivityDrawXYInstruction() {

	      public void draw(ProcessDiagramCanvasImpl processDiagramCreator, ActivityImpl activityImpl, int x, int y) {
	        processDiagramCreator.drawErrorStartEvent(x, y, activityImpl.getWidth(), activityImpl.getHeight());
	      }
	    });
	    
	    // task
	    activityDrawInstructions.put("task", new ActivityDrawXYInstruction() {

	      public void draw(ProcessDiagramCanvasImpl processDiagramCreator, ActivityImpl activityImpl, int x, int y) {
	        processDiagramCreator.drawTask((String) activityImpl.getProperty("name"), x, y, activityImpl.getWidth(),
	                activityImpl.getHeight());
	      }
	    });

	    // user task
	    activityDrawInstructions.put("userTask", new ActivityDrawXYInstruction() {

	      public void draw(ProcessDiagramCanvasImpl processDiagramCreator, ActivityImpl activityImpl, int x, int y) {
	        processDiagramCreator.drawUserTask((String) activityImpl.getProperty("name"), x, y, activityImpl.getWidth(),
	                activityImpl.getHeight());
	      }
	    });

	    // script task
	    activityDrawInstructions.put("scriptTask", new ActivityDrawXYInstruction() {

	      public void draw(ProcessDiagramCanvasImpl processDiagramCreator, ActivityImpl activityImpl, int x, int y) {
	        processDiagramCreator.drawScriptTask((String) activityImpl.getProperty("name"), x, y, activityImpl.getWidth(),
	                activityImpl.getHeight());
	      }
	    });

	    // service task
	    activityDrawInstructions.put("serviceTask", new ActivityDrawXYInstruction() {

	      public void draw(ProcessDiagramCanvasImpl processDiagramCreator, ActivityImpl activityImpl, int x, int y) {
	        processDiagramCreator.drawServiceTask((String) activityImpl.getProperty("name"), x, y, activityImpl.getWidth(),
	                activityImpl.getHeight());
	      }
	    });

	    // receive task
	    activityDrawInstructions.put("receiveTask", new ActivityDrawXYInstruction() {

	      public void draw(ProcessDiagramCanvasImpl processDiagramCreator, ActivityImpl activityImpl, int x, int y) {
	        processDiagramCreator.drawReceiveTask((String) activityImpl.getProperty("name"), x, y, activityImpl.getWidth(),
	                activityImpl.getHeight());
	      }
	    });

	    // send task
	    activityDrawInstructions.put("sendTask", new ActivityDrawXYInstruction() {

	      public void draw(ProcessDiagramCanvasImpl processDiagramCreator, ActivityImpl activityImpl, int x, int y) {
	        processDiagramCreator.drawSendTask((String) activityImpl.getProperty("name"), x, y, activityImpl.getWidth(),
	                activityImpl.getHeight());
	      }
	    });

	    // manual task
	    activityDrawInstructions.put("manualTask", new ActivityDrawXYInstruction() {

	      public void draw(ProcessDiagramCanvasImpl processDiagramCreator, ActivityImpl activityImpl, int x, int y) {
	        processDiagramCreator.drawManualTask((String) activityImpl.getProperty("name"), x, y, activityImpl.getWidth(),
	                activityImpl.getHeight());
	      }
	    });
	    
	    // businessRuleTask task
	    activityDrawInstructions.put("businessRuleTask", new ActivityDrawXYInstruction() {

	      public void draw(ProcessDiagramCanvasImpl processDiagramCreator, ActivityImpl activityImpl, int x, int y) {
	        processDiagramCreator.drawBusinessRuleTask((String) activityImpl.getProperty("name"), x, y, activityImpl.getWidth(),
	                activityImpl.getHeight());
	      }
	    });

	    // exclusive gateway
	    activityDrawInstructions.put("exclusiveGateway", new ActivityDrawXYInstruction() {

	      public void draw(ProcessDiagramCanvasImpl processDiagramCreator, ActivityImpl activityImpl, int x, int y) {
	        processDiagramCreator.drawExclusiveGateway(x, y, activityImpl.getWidth(), activityImpl.getHeight());
	      }
	    });

	    // inclusive gateway
	    activityDrawInstructions.put("inclusiveGateway", new ActivityDrawXYInstruction() {

	      public void draw(ProcessDiagramCanvasImpl processDiagramCreator, ActivityImpl activityImpl, int x, int y) {
	        processDiagramCreator.drawInclusiveGateway(x, y, activityImpl.getWidth(), activityImpl.getHeight());
	      }
	    });

	    // parallel gateway
	    activityDrawInstructions.put("parallelGateway", new ActivityDrawXYInstruction() {

	      public void draw(ProcessDiagramCanvasImpl processDiagramCreator, ActivityImpl activityImpl, int x, int y) {
	        processDiagramCreator.drawParallelGateway(x, y, activityImpl.getWidth(), activityImpl.getHeight());
	      }
	    });

	    // Boundary timer
	    activityDrawInstructions.put("boundaryTimer", new ActivityDrawXYInstruction() {

	      public void draw(ProcessDiagramCanvasImpl processDiagramCreator, ActivityImpl activityImpl, int x, int y) {
	    	BoundaryEventActivityBehavior behavior = (BoundaryEventActivityBehavior)activityImpl.getActivityBehavior();
	        processDiagramCreator.drawCatchingTimerEvent(x, y, activityImpl.getWidth(), activityImpl.getHeight(), behavior.isInterrupting());
	      }
	    });

	    // Boundary catch error
	    activityDrawInstructions.put("boundaryError", new ActivityDrawXYInstruction() {

	      public void draw(ProcessDiagramCanvasImpl processDiagramCreator, ActivityImpl activityImpl, int x, int y) {
	    	BoundaryEventActivityBehavior behavior = (BoundaryEventActivityBehavior)activityImpl.getActivityBehavior();
	        processDiagramCreator.drawCatchingErrorEvent(x, y, activityImpl.getWidth(), activityImpl.getHeight(), behavior.isInterrupting());
	      }
	    });
	    
	    // Boundary signal event
	    activityDrawInstructions.put("boundarySignal", new ActivityDrawXYInstruction() {

	      public void draw(ProcessDiagramCanvasImpl processDiagramCreator, ActivityImpl activityImpl, int x, int y) {
	    	BoundaryEventActivityBehavior behavior = (BoundaryEventActivityBehavior)activityImpl.getActivityBehavior();
	        processDiagramCreator.drawCatchingSignalEvent(x, y, activityImpl.getWidth(), activityImpl.getHeight(), behavior.isInterrupting());
	      }
	    });

	    // timer catch event
	    activityDrawInstructions.put("intermediateTimer", new ActivityDrawXYInstruction() {

	      public void draw(ProcessDiagramCanvasImpl processDiagramCreator, ActivityImpl activityImpl, int x, int y) {
	    	BoundaryEventActivityBehavior behavior = (BoundaryEventActivityBehavior)activityImpl.getActivityBehavior();
	        processDiagramCreator.drawCatchingTimerEvent(x, y, activityImpl.getWidth(), activityImpl.getHeight(), behavior.isInterrupting());
	      }
	    });

	    // subprocess
	    activityDrawInstructions.put("subProcess", new ActivityDrawXYInstruction() {

	      public void draw(ProcessDiagramCanvasImpl processDiagramCreator, ActivityImpl activityImpl, int x, int y) {
	        Boolean isExpanded = (Boolean) activityImpl.getProperty(BpmnParse.PROPERTYNAME_ISEXPANDED);
	        Boolean isTriggeredByEvent = (Boolean) activityImpl.getProperty("triggeredByEvent");
	        if(isTriggeredByEvent == null) {
	          isTriggeredByEvent = Boolean.TRUE;
	        }
	        if (isExpanded != null && isExpanded == false) {
	          processDiagramCreator.drawCollapsedSubProcess((String) activityImpl.getProperty("name"), x, y,
	                  activityImpl.getWidth(), activityImpl.getHeight(), isTriggeredByEvent);
	        } else {
	          processDiagramCreator.drawExpandedSubProcess((String) activityImpl.getProperty("name"), x, y,
	                  activityImpl.getWidth(), activityImpl.getHeight(), isTriggeredByEvent);
	        }
	      }
	    });

	    // call activity
	    activityDrawInstructions.put("callActivity", new ActivityDrawXYInstruction() {

	      public void draw(ProcessDiagramCanvasImpl processDiagramCreator, ActivityImpl activityImpl, int x, int y) {
	        processDiagramCreator.drawCollapsedCallActivity((String) activityImpl.getProperty("name"), x, y,
	                activityImpl.getWidth(), activityImpl.getHeight());
	      }
	    });

	  }

	  protected static void drawActivity(ProcessDiagramCanvasImpl processDiagramCanvas, ActivityImpl activity, Map<String,Object> varUpdates, Integer x, Integer y, boolean writeUpdates) {
	    String type = (String) activity.getProperty("type");
	    ActivityDrawXYInstruction drawInstruction = activityDrawInstructions.get(type);
	    if (drawInstruction != null) {

	      drawInstruction.draw(processDiagramCanvas, activity, x, y);

	      // Gather info on the multi instance marker
	      boolean multiInstanceSequential = false, multiInstanceParallel = false, collapsed = false;
	      String multiInstance = (String) activity.getProperty("multiInstance");
	      if (multiInstance != null) {
	        if ("sequential".equals(multiInstance)) {
	          multiInstanceSequential = true;
	        } else {
	          multiInstanceParallel = true;
	        }
	      }

	      // Gather info on the collapsed marker
	      Boolean expanded = (Boolean) activity.getProperty(BpmnParse.PROPERTYNAME_ISEXPANDED);
	      if (expanded != null) {
	        collapsed = !expanded;
	      }

	      // Actually draw the markers
	      processDiagramCanvas.drawActivityMarkers(x, y, activity.getWidth(), activity.getHeight(), multiInstanceSequential,
	              multiInstanceParallel, collapsed);
	      
	      if (writeUpdates && !varUpdates.isEmpty())
	    	  processDiagramCanvas.drawStringToNode( varUpdates.toString(), x, y, activity.getWidth()+10, activity.getHeight());
	    }

	    // TODO: Nested activities (boundary events)
//	    
//	    for (ActivityImpl nestedActivity : activity.getActivities()) {
//	      drawActivity(processDiagramCanvas, nestedActivity, highLightedActivities);
//	    }
	  }

	  protected interface ActivityDrawXYInstruction {

		  /**
		   * draw activity on the given position.
		   * 
		   * @#see ActivityDrawInstruction
		   * @param processDiagramCreator
		   * @param activityImpl
		   * @param x
		   * @param y
		   */
	    void draw(ProcessDiagramCanvasImpl processDiagramCreator, ActivityImpl activityImpl, int x, int y);

	  }

	
	public byte[] generateLayer( String imageType, Map<String, Object> params) {
		
		// get process instance from which we will get audit trail layer
		final String processInstanceId = (String) params.get( PROCESS_INSTANCE_ID );
	    ProcessDiagramCanvasImpl canvas = generateDiagram( processInstanceId);
	    
		return convertToByteArray(imageType, canvas.generateImage(imageType));
	}

	protected ProcessDiagramCanvasImpl generateDiagram(	String processInstanceId) {
		if ( historyService.createHistoricProcessInstanceQuery().processInstanceId( processInstanceId).count() == 0) {
			log.info("Process instanceId["+ processInstanceId +"] does not have any history");
			return null;
		}
		
		HistoricProcessInstanceWrapper historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
	
		final String processDefinitionId  = historicProcessInstance.getProcessDefinitionId();	    
	    ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) ( repositoryService.getDeployedProcessDefinition( processDefinitionId ));

		ProcessDiagramCanvasImpl processDiagramCanvas = initProcessDiagramCanvas(processInstanceId, processDefinition);

		// TODO: Draw pool shape, if process is participant in collaboration
		// TODO: Draw lanes

		// Draw activities
		List<HistoricActivityInstanceWrapper> historicActivities = historyService.createHistoricActivityInstanceQuery()
	    		.processInstanceId( processInstanceId)
	    		.orderByHistoricActivityInstanceStartTime()
	    		.asc()
	    		.orderByHistoricActivityInstanceEndTime()
	    		.asc()
	    		.list();
	    int y = 0;
	    
	    if ( !historicActivities.isEmpty()) {
	    	ActivityImpl activity = findActivity(processDefinition, historicActivities.get(0).getActivityId());
	   
	    	drawActivity( processDiagramCanvas, activity, getVarUpdates(historicActivities.get(0)), (maxX - activity.getWidth())/2, y, writeUpdates);
		    y += activity.getHeight();
		    
	    	for ( int i=1; i < historicActivities.size(); i++ ) {
	    		// draw transition
	            processDiagramCanvas.drawSequenceflow( maxX/2, y, maxX/2, y+TRANSITION_HEIGHT, false);
			    y += TRANSITION_HEIGHT;
			    
	    		// draw activity
	    		activity = findActivity(processDefinition, historicActivities.get(i).getActivityId());
	    		drawActivity( processDiagramCanvas, activity, getVarUpdates(historicActivities.get(i)), (maxX - activity.getWidth())/2, y, writeUpdates);
			    y += activity.getHeight();
	    	}
	    } 	

		return processDiagramCanvas;
	}
	
	/** 
	 * get var updates in the node
	 * @param historicActivityInstance
	 * @return
	 */
	private Map<String, Object> getVarUpdates(HistoricActivityInstanceWrapper historicActivityInstance) {
		Map<String, Object> updates = new HashMap<String,Object>();
		
		List<HistoricDetailWrapper> historicUpdates = historyService.createHistoricDetailQuery()
				.processInstanceId( historicActivityInstance.getProcessInstanceId())
				.activityInstanceId( historicActivityInstance.getId())
				.variableUpdates()
				.list();
		for (HistoricDetailWrapper historicDetail : historicUpdates)
			updates.put( ((HistoricVariableUpdate) historicDetail).getVariableName(), ((HistoricVariableUpdate) historicDetail).getValue());
		return updates;
	}

	protected ProcessDiagramCanvasImpl initProcessDiagramCanvas(String processInstanceId,
			ProcessDefinitionEntity processDefinition) {
		int minX = 0;
		int minY = 0;
		maxX = 0;
		maxY = 0;

		//TODO: participant processes are not taken into account

		List<HistoricActivityInstanceWrapper> historicActivityList = historyService.createHistoricActivityInstanceQuery().processInstanceId(processInstanceId).orderByHistoricActivityInstanceStartTime().asc().list();
		for (HistoricActivityInstanceWrapper historicActivity : historicActivityList) {
			ActivityImpl activity = findActivity( processDefinition, historicActivity.getActivityId());
			
			if (activity != null) {
				// width
				if (activity.getWidth() > maxX) {					
					maxX = activity.getWidth();
				}

				// height
				maxY += activity.getHeight();
	
				// + transition size
				maxY += TRANSITION_HEIGHT;
			}
		}
		
		//TODO:  lanes are not taken into consideration
		maxX += 10;
		// correct image height
		maxY += 10 - TRANSITION_HEIGHT;
		return new ProcessDiagramCanvasImpl(maxX, maxY, minX, minY);
	}
	
	private static ActivityImpl findActivity(ProcessDefinitionEntity processDefinition, String activityId) {
		if (activityId == null)
			return null;
		
		for (ActivityImpl activity : processDefinition.getActivities()) {
			if ( activityId.equals( activity.getId() ))
				return activity;
		}
		return null;
	}
	
	public HistoryServiceWrapper getHistoryService() {
		return historyService;
	}

	public void setHistoryService(HistoryServiceWrapper historyService) {
		this.historyService = historyService;
	}

	public boolean isWriteUpdates() {
		return writeUpdates;
	}

	public void setWriteUpdates(boolean writeUpdates) {
		this.writeUpdates = writeUpdates;
	}

}
