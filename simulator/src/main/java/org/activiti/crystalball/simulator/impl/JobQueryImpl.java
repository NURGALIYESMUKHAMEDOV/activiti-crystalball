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

import org.activiti.crystalball.simulator.ActivitiException;
import org.activiti.crystalball.simulator.impl.interceptor.CommandContext;
import org.activiti.crystalball.simulator.impl.interceptor.CommandExecutor;
import org.activiti.crystalball.simulator.runtime.Job;
import org.activiti.crystalball.simulator.runtime.JobQuery;
import org.activiti.engine.impl.util.ClockUtil;

import java.io.Serializable;
import java.util.Date;
import java.util.List;


/**
 * @author Joram Barrez
 * @author Tom Baeyens
 * @author Falko Menge
 */
public class JobQueryImpl extends AbstractQuery<JobQuery, Job> implements JobQuery, Serializable {
  
  private static final long serialVersionUID = 1L;
  protected String id;
  protected String processInstanceId;
  protected String executionId;
  protected String processDefinitionId;
  protected boolean retriesLeft;
  protected boolean executable;
  protected boolean onlyTimers;
  protected boolean onlyMessages;
  protected Date duedateHigherThan;
  protected Date duedateLowerThan;
  protected Date duedateHigherThanOrEqual;
  protected Date duedateLowerThanOrEqual;
  protected boolean withException;
  protected String exceptionMessage;
  
  public JobQueryImpl() {
  }

  public JobQueryImpl(CommandContext commandContext) {
    super(commandContext);
  }

  public JobQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }
  
  public JobQuery jobId(String jobId) {
    if (jobId == null) {
      throw new ActivitiException("Provided job id is null");
    }
    this.id = jobId;
    return this;
  }

  public JobQueryImpl processInstanceId(String processInstanceId) {
    if (processInstanceId == null) {
      throw new ActivitiException("Provided process instance id is null");
    }
    this.processInstanceId = processInstanceId;
    return this;
  }
  
  public JobQueryImpl processDefinitionId(String processDefinitionId) {
    if (processDefinitionId == null) {
      throw new ActivitiException("Provided process definition id is null");
    }
    this.processDefinitionId = processDefinitionId;
    return this;
  }
  
  public JobQueryImpl executionId(String executionId) {
    if (executionId == null) {
      throw new ActivitiException("Provided execution id is null");
    }
    this.executionId = executionId;
    return this;
  }

  public JobQuery withRetriesLeft() {
    retriesLeft = true;
    return this;
  }

  public JobQuery executable() {
    executable = true;
    return this;
  }
  
  public JobQuery timers() {
    if (onlyMessages) {
      throw new ActivitiException("Cannot combine onlyTimers() with onlyMessages() in the same query");
    }
    this.onlyTimers = true;
    return this;
  }
  
  public JobQuery messages() {
    if (onlyTimers) {
      throw new ActivitiException("Cannot combine onlyTimers() with onlyMessages() in the same query");
    }
    this.onlyMessages = true;
    return this;
  }
  
  public JobQuery duedateHigherThan(Date date) {
    if (date == null) {
      throw new ActivitiException("Provided date is null");
    }
    this.duedateHigherThan = date;
    return this;
  }
  
  public JobQuery duedateLowerThan(Date date) {
    if (date == null) {
      throw new ActivitiException("Provided date is null");
    }
    this.duedateLowerThan = date;
    return this;
  }
  
  public JobQuery duedateHigherThen(Date date) {
    return duedateHigherThan(date);
  }
  
  public JobQuery duedateHigherThenOrEquals(Date date) {
    if (date == null) {
      throw new ActivitiException("Provided date is null");
    }
    this.duedateHigherThanOrEqual = date;
    return this;
  }
  
  public JobQuery duedateLowerThen(Date date) {
    return duedateLowerThan(date);
  }
  
  public JobQuery duedateLowerThenOrEquals(Date date) {
    if (date == null) {
      throw new ActivitiException("Provided date is null");
    }
    this.duedateLowerThanOrEqual = date;
    return this;
  }
  
  public JobQuery withException() {
    this.withException = true;
    return this;
  }

  public JobQuery exceptionMessage(String exceptionMessage) {
    if (exceptionMessage == null) {
      throw new ActivitiException("Provided exception message is null");
    }
    this.exceptionMessage = exceptionMessage;
    return this;
  }
  
  //sorting //////////////////////////////////////////
  
  public JobQuery orderByJobDuedate() {
    return orderBy(JobQueryProperty.DUEDATE);
  }
  
  public JobQuery orderByExecutionId() {
    return orderBy(JobQueryProperty.EXECUTION_ID);
  }
  
  public JobQuery orderByJobId() {
    return orderBy(JobQueryProperty.JOB_ID);
  }
  
  public JobQuery orderByProcessInstanceId() {
    return orderBy(JobQueryProperty.PROCESS_INSTANCE_ID);
  }
  
  public JobQuery orderByJobRetries() {
    return orderBy(JobQueryProperty.RETRIES);
  }
  
  //results //////////////////////////////////////////

  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    return 0L;
//    return commandContext
//      .getJobEntityManager()
//      .findJobCountByQueryCriteria(this);
  }

  public List<Job> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    return null;
//    return commandContext
//      .getJobManager()
//      .findJobsByQueryCriteria(this, page);
  }
  
  //getters //////////////////////////////////////////
  
  public String getProcessInstanceId() {
    return processInstanceId;
  }
  public String getExecutionId() {
    return executionId;
  }
  public boolean getRetriesLeft() {
    return retriesLeft;
  }
  public boolean getExecutable() {
    return executable;
  }
  public Date getNow() {
    return ClockUtil.getCurrentTime();
  }
  public boolean isWithException() {
    return withException;
  }
  public String getExceptionMessage() {
    return exceptionMessage;
  }
}
