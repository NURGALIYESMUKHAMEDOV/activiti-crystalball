/* Licensed under the Apache License, Version 2.0 (the "License");
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
 */
package org.activiti.crystalball.simulator.impl.el;

import org.activiti.crystalball.simulator.delegate.Expression;
import org.activiti.crystalball.simulator.delegate.VariableScope;
import org.activiti.crystalball.simulator.impl.javax.el.*;
import org.activiti.crystalball.simulator.impl.juel.ExpressionFactoryImpl;
import org.activiti.crystalball.simulator.impl.persistence.entity.VariableScopeImpl;

import java.util.Map;


/**
 * <p>
 * Central manager for all expressions.
 * </p>
 * <p>
 * Process parsers will use this to build expression objects that are stored in
 * the process definitions.
 * </p>
 * <p>
 * Then also this class is used as an entry point for runtime evaluation of the
 * expressions.
 * </p>
 * 
 * @author Tom Baeyens
 * @author Dave Syer
 * @author Frederik Heremans
 */
public class ExpressionManager {


  protected ExpressionFactory expressionFactory;
  // Default implementation (does nothing)
  protected ELContext parsingElContext = new ParsingElContext();
  protected Map<Object, Object> beans;
  
  
  public ExpressionManager() {
    this(null);
  }
  
  public ExpressionManager(Map<Object, Object> beans) {
    // Use the ExpressionFactoryImpl in activiti build in version of juel, with parametrised method expressions enabled
    expressionFactory = new ExpressionFactoryImpl();
    this.beans = beans;
  }

 
  
  public Expression createExpression(String expression) {
    ValueExpression valueExpression = expressionFactory.createValueExpression(parsingElContext, expression, Object.class);
    return new JuelExpression(valueExpression, this, expression);
  }

  public void setExpressionFactory(ExpressionFactory expressionFactory) {
    this.expressionFactory = expressionFactory;
  }

  public ELContext getElContext(VariableScope variableScope) {
    ELContext elContext = null;
    if (variableScope instanceof VariableScopeImpl) {
      VariableScopeImpl variableScopeImpl = (VariableScopeImpl) variableScope;
      elContext = variableScopeImpl.getCachedElContext();
    }
    
    if (elContext==null) {
      elContext = createElContext(variableScope);
      if (variableScope instanceof VariableScopeImpl) {
        ((VariableScopeImpl)variableScope).setCachedElContext(elContext);
      }
    }

    return elContext;
  }

  protected ActivitiElContext createElContext(VariableScope variableScope) {
    ELResolver elResolver = createElResolver(variableScope);
    return new ActivitiElContext(elResolver);
  }

  protected ELResolver createElResolver(VariableScope variableScope) {
    CompositeELResolver elResolver = new CompositeELResolver();
    elResolver.add(new VariableScopeElResolver(variableScope));
    
    if(beans != null) {
      // ACT-1102: Also expose all beans in configuration when using standalone activiti, not
      // in spring-context
      elResolver.add(new ReadOnlyMapELResolver(beans));
    }
    
    elResolver.add(new ArrayELResolver());
    elResolver.add(new ListELResolver());
    elResolver.add(new MapELResolver());
//    elResolver.add(new DynamicBeanPropertyELResolver(ItemInstance.class, "getFieldValue", "setFieldValue")); //TODO: needs verification
    elResolver.add(new BeanELResolver());
    return elResolver;
  }
}
