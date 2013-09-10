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

import org.activiti.crystalball.simulator.ActivitiException;
import org.activiti.crystalball.simulator.delegate.VariableScope;
import org.activiti.crystalball.simulator.impl.context.SimulationContext;
import org.activiti.crystalball.simulator.impl.delegate.ExpressionGetInvocation;
import org.activiti.crystalball.simulator.impl.delegate.ExpressionSetInvocation;
import org.activiti.crystalball.simulator.impl.javax.el.*;


/**
 * Expression implementation backed by a JUEL {@link ValueExpression}.
 *
 * @author Frederik Heremans
 * @author Joram Barrez
 */
public class JuelExpression implements Expression {

  protected String expressionText;
  protected ValueExpression valueExpression;
  protected ExpressionManager expressionManager;
  
  public JuelExpression(ValueExpression valueExpression, ExpressionManager expressionManager, String expressionText) {
    this.valueExpression = valueExpression;
    this.expressionManager = expressionManager;
    this.expressionText = expressionText;
  }

  public Object getValue(VariableScope variableScope) {
    ELContext elContext = expressionManager.getElContext(variableScope);
    try {
      ExpressionGetInvocation invocation = new ExpressionGetInvocation(valueExpression, elContext);
      SimulationContext.getSimulationEngineConfiguration()
        .getDelegateInterceptor()
        .handleInvocation(invocation);
      return invocation.getInvocationResult();      
    } catch (PropertyNotFoundException pnfe) {
      throw new ActivitiException("Unknown property used in expression: " + expressionText, pnfe);
    } catch (MethodNotFoundException mnfe) {
      throw new ActivitiException("Unknown method used in expression: " + expressionText, mnfe);
    } catch(ELException ele) {
      throw new ActivitiException("Error while evaluating expression: " + expressionText, ele);
    } catch (Exception e) {
      throw new ActivitiException("Error while evaluating expression: " + expressionText, e);
    }
  }
    
  public void setValue(Object value, VariableScope variableScope) {
    ELContext elContext = expressionManager.getElContext(variableScope);
    try {
      ExpressionSetInvocation invocation = new ExpressionSetInvocation(valueExpression, elContext, value);
      SimulationContext.getSimulationEngineConfiguration()
        .getDelegateInterceptor()
        .handleInvocation(invocation);
    } catch (Exception e) {
      throw new ActivitiException("Error while evaluating expression: " + expressionText, e);
    }
  }
  
  @Override
  public String toString() {
    if(valueExpression != null) {
      return valueExpression.getExpressionString();
    }
    return super.toString();
  }
  
  public String getExpressionText() {
    return expressionText;
  }
}
