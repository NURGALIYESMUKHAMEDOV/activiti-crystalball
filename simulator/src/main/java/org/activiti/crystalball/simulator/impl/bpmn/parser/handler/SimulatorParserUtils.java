package org.activiti.crystalball.simulator.impl.bpmn.parser.handler;

import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.ExtensionElement;
import org.activiti.crystalball.simulator.delegate.AbstractSimulationActivityBehavior;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.pvm.delegate.ActivityBehavior;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.activiti.engine.impl.pvm.process.ScopeImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;

/**
 * This class provides basic utilities for other simulation parsers
 */
final class SimulatorParserUtils {
  private static Logger LOG = LoggerFactory.getLogger(SimulatorParserUtils.class);

  /**
   * The namespace of the simulator custom BPMN extensions.
   */
  private static final String SIMULATION_BPMN_EXTENSIONS_NS = "http://crystalball.org/simulation";

  private static final String SIMULATION_BEHAVIOR = "behavior";
  private static final String SIMULATION_CLASS_NAME = "className";

  static void setSimulationBehavior(ScopeImpl scope, BaseElement baseElement) {

    String behaviorClassName = SimulatorParserUtils.getBehaviorClassName(baseElement);
    if (behaviorClassName != null) {
      ProcessDefinitionImpl processDefinition = scope.getProcessDefinition();
      ActivityImpl activity = processDefinition.findActivity(baseElement.getId());

      LOG.debug("Scripting task [" + activity.getId() + "] setting behavior to [" + behaviorClassName + "]");
      try {
        Class<AbstractSimulationActivityBehavior> behaviorClass = (Class<AbstractSimulationActivityBehavior>) Class.forName(behaviorClassName);
        Constructor<AbstractSimulationActivityBehavior> constructor = behaviorClass.getDeclaredConstructor(ScopeImpl.class, ActivityImpl.class);
        activity.setActivityBehavior((ActivityBehavior) constructor.newInstance( scope, activity));
      } catch (Throwable t) {
        LOG.error("unable to set simulation behavior class[" + behaviorClassName + "]", t);
        throw new ActivitiException("unable to set simulation behavior class[" + behaviorClassName + "]");
      }
    }
  }

  private static String getBehaviorClassName(BaseElement baseElement) {
    final Map<String, List<ExtensionElement>> extensionElements = baseElement.getExtensionElements();
    if (extensionElements != null && !extensionElements.isEmpty()) {
      List<ExtensionElement> behaviorExtensionElements = extensionElements.get( SIMULATION_BEHAVIOR);

      if (behaviorExtensionElements != null && !behaviorExtensionElements.isEmpty()) {
        for (ExtensionElement extension : behaviorExtensionElements) {
          if (SIMULATION_BPMN_EXTENSIONS_NS.equals(extension.getNamespace()))
            return extension.getAttributeValue(null, SIMULATION_CLASS_NAME);
        }
      }
    }
    return null;
  }

}
