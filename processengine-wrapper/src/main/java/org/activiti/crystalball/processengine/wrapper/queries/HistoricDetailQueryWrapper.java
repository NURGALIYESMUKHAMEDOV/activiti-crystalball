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

package org.activiti.crystalball.processengine.wrapper.queries;

/**
 * Programmatic querying for {@link HistoricDetailWrapper}s.
 * 
 * @author Tom Baeyens
 */
public interface HistoricDetailQueryWrapper extends Query<HistoricDetailQueryWrapper, HistoricDetailWrapper> {

  /** Only select historic variable updates with the given process instance.
   * {@#link ProcessInstance) ids and {@#link HistoricProcessInstance} ids match. */
  HistoricDetailQueryWrapper processInstanceId(String processInstanceId);
  
  /** Only select historic variable updates with the given execution.
   * Note that {@#link Execution} ids are not stored in the history as first class citizen,
   * only process instances are.*/
  HistoricDetailQueryWrapper executionId(String executionId);

  /** Only select historic variable updates associated to the given {@#link HistoricActivityInstance activity instance}.
   * @deprecated since 5.2, use {@link #activityInstanceId(String)} instead */
  HistoricDetailQueryWrapper activityId(String activityId);

  /** Only select historic variable updates associated to the given {@#link HistoricActivityInstance activity instance}. */
  HistoricDetailQueryWrapper activityInstanceId(String activityInstanceId);

  /** Only select historic variable updates associated to the given {@l#ink HistoricTaskInstance historic task instance}. */
  HistoricDetailQueryWrapper taskId(String taskId);

  /** Only select {@#link HistoricFormProperty}s. */
  HistoricDetailQueryWrapper formProperties();

  /** Only select {@#link HistoricVariableUpdate}s. */
  HistoricDetailQueryWrapper variableUpdates();
  
  /** Exclude all task-related {@link HistoricDetailWrapper}s, so only items which have no
   * task-id set will be selected. When used togheter with {@link #taskId(String)}, this
   * call is ignored task details are NOT excluded.
   */
  HistoricDetailQueryWrapper excludeTaskDetails();

  HistoricDetailQueryWrapper orderByProcessInstanceId();
  
  HistoricDetailQueryWrapper orderByVariableName();
  
  HistoricDetailQueryWrapper orderByFormPropertyId();
  
  HistoricDetailQueryWrapper orderByVariableType();
  
  HistoricDetailQueryWrapper orderByVariableRevision();
  
  HistoricDetailQueryWrapper orderByTime();
}
