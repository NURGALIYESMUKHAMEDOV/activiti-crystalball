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

package org.activiti.crystalball.simulator.impl;

import org.activiti.engine.query.QueryProperty;
import org.activiti.engine.runtime.ProcessInstanceQuery;

import java.util.HashMap;
import java.util.Map;


/**
 * Contains the possible properties that can be used in a {@link ProcessInstanceQuery}.
 * 
 * @author Joram Barrez
 */
public class SimulationInstanceQueryProperty implements QueryProperty {
  
  private static final long serialVersionUID = 1L;

  private static final Map<String, SimulationInstanceQueryProperty> properties = new HashMap<String, SimulationInstanceQueryProperty>();

  public static final SimulationInstanceQueryProperty SIMULATION_INSTANCE_ID = new SimulationInstanceQueryProperty("RES.ID_");
  public static final SimulationInstanceQueryProperty SIMULATION_DEFINITION_ID = new SimulationInstanceQueryProperty("SimulationDefinitionId");
  
  private String name;

  public SimulationInstanceQueryProperty(String name) {
    this.name = name;
    properties.put(name, this);
  }

  public String getName() {
    return name;
  }
  
  public static SimulationInstanceQueryProperty findByName(String propertyName) {
    return properties.get(propertyName);
  }

}
