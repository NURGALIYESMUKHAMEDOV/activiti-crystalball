package org.activiti.crystalball.simulator.delegate.event;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;

/**
 * This class...
 */
public class RecordableProcessEngineFactory extends DefaultSimulationProcessEngineFactory {

  private ActivitiEventListener listener;

  public RecordableProcessEngineFactory(String resourceToDeploy, ActivitiEventListener listener) {
    super(resourceToDeploy);
    this.listener = listener;
  }

  @Override
  public ProcessEngine getObject() {
    ProcessEngine processEngine = super.getObject();

    //add eventListener
    final ProcessEngineConfigurationImpl processEngineConfiguration = ((ProcessEngineImpl) processEngine).getProcessEngineConfiguration();
    processEngineConfiguration.getEventDispatcher().addEventListener(listener);

    return processEngine;
  }
}