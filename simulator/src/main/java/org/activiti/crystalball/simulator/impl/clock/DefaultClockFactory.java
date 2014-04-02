package org.activiti.crystalball.simulator.impl.clock;

import org.activiti.crystalball.simulator.FactoryBean;
import org.activiti.engine.impl.util.DefaultClockImpl;
import org.activiti.engine.runtime.Clock;

/**
 * This class provides factory for default clocks
 */
public class DefaultClockFactory implements FactoryBean<Clock> {
  @Override
  public Clock getObject() throws RuntimeException {
    return new DefaultClockImpl();
  }

  @Override
  public Class<?> getObjectType() {
    return DefaultClockImpl.class;
  }

  @Override
  public boolean isSingleton() {
    return false;
  }
}
