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


import java.util.List;
import java.util.Map;
import java.util.Set;

import org.activiti.crystalball.processengine.wrapper.ProcessDiagramCanvasInterface;
import org.activiti.crystalball.processengine.wrapper.ProcessDiagramService;
import org.activiti.crystalball.processengine.wrapper.RepositoryServiceWrapper;
import org.activiti.crystalball.processengine.wrapper.queries.*;

/**
 * write counts into nodes  
 *
 */
public class WriteNodeDescriptionDiagramLayer extends AbstractProcessDiagramLayerGenerator {

	public static String PROCESS_DEFINITION_ID = "processDefinitionId";
	
	public WriteNodeDescriptionDiagramLayer() {
	}

	public WriteNodeDescriptionDiagramLayer( RepositoryServiceWrapper repositoryService, ProcessDiagramService diagramService) {
		this.repositoryService = repositoryService;
        this.diagramService = diagramService;
	}

	@Override
	public byte[] generateLayer( String imageType,
			Map<String, Object> params) {
		// get parameters
		final String processDefinitionKey = (String) params.get( PROCESS_DEFINITION_ID );
		final String processDefinitionId  = repositoryService.createProcessDefinitionQuery().processDefinitionKey( processDefinitionKey ).singleResult().getId();
	    
	    // get process activities to write count
		Set<String> writeableActivities = (Set<String>) params.keySet();
	    writeableActivities.remove( PROCESS_DEFINITION_ID);
	    
		// get process definition entity
	    ProcessDefinitionWrapper pde =  repositoryService.getDeployedProcessDefinition( processDefinitionId );
	    
		ProcessDiagramCanvasInterface canvas = initProcessDiagramCanvas(diagramService, pde );

		for (ActivityWrapper activity : pde.getActivities()) {
	      // Draw count into activities
	      if (writeableActivities.contains(activity.getId())) {
	    	  canvas.drawStringToNode( params.get( activity.getId() ).toString(), activity.getX(), activity.getY(), activity.getWidth(), activity.getHeight());	      
	      }
	    }
	    return convertToByteArray( imageType, canvas.generateImage(imageType));
	}
	
	  protected static ProcessDiagramCanvasInterface initProcessDiagramCanvas(ProcessDiagramService diagramService, ProcessDefinitionWrapper processDefinition) {
		    int minX = Integer.MAX_VALUE;
		    int maxX = 0;
		    int minY = Integer.MAX_VALUE;
		    int maxY = 0;
		    
		    if(processDefinition.getParticipantProcess() != null) {
		      ParticipantProcessWrapper pProc = processDefinition.getParticipantProcess();
		      
		      minX = pProc.getX();
		      maxX = pProc.getX() + pProc.getWidth();
		      minY = pProc.getY();
		      maxY = pProc.getY() + pProc.getHeight();
		    }
		    
		    for (ActivityWrapper activity : processDefinition.getActivities()) {

		      // width
		      if (activity.getX() + activity.getWidth() > maxX) {
		        maxX = activity.getX() + activity.getWidth();
		      }
		      if (activity.getX() < minX) {
		        minX = activity.getX();
		      }
		      // height
		      if (activity.getY() + activity.getHeight() > maxY) {
		        maxY = activity.getY() + activity.getHeight();
		      }
		      if (activity.getY() < minY) {
		        minY = activity.getY();
		      }

		      for (PvmTransitionWrapper sequenceFlow : activity.getOutgoingTransitions()) {
		        List<Integer> waypoints = ((PvmTransitionWrapper) sequenceFlow).getWaypoints();
		        for (int i = 0; i < waypoints.size(); i += 2) {
		          // width
		          if (waypoints.get(i) > maxX) {
		            maxX = waypoints.get(i);
		          }
		          if (waypoints.get(i) < minX) {
		            minX = waypoints.get(i);
		          }
		          // height
		          if (waypoints.get(i + 1) > maxY) {
		            maxY = waypoints.get(i + 1);
		          }
		          if (waypoints.get(i + 1) < minY) {
		            minY = waypoints.get(i + 1);
		          }
		        }
		      }
		    }
		    
		    if(processDefinition.getLaneSets() != null && processDefinition.getLaneSets().size() > 0) {
		      for(LaneSetWrapper laneSet : processDefinition.getLaneSets()) {
		        if(laneSet.getLanes() != null && laneSet.getLanes().size() > 0) {
		          for(LaneWrapper lane : laneSet.getLanes()) {
		            // width
		            if (lane.getX() + lane.getWidth() > maxX) {
		              maxX = lane.getX() + lane.getWidth();
		            }
		            if (lane.getX() < minX) {
		              minX = lane.getX();
		            }
		            // height
		            if (lane.getY() + lane.getHeight() > maxY) {
		              maxY = lane.getY() + lane.getHeight();
		            }
		            if (lane.getY() < minY) {
		              minY = lane.getY();
		            }
		          }
		        }
		      }
		    }
		    
		    return diagramService.createProcessDiagramCanvas(maxX + 10, maxY + 10, minX, minY);
		  }
}
