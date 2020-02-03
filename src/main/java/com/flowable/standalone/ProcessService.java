package com.flowable.standalone;

import org.flowable.engine.*;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ProcessService {
    private ProcessEngine processEngine;
    private Optional<RepositoryService> repositoryService;
    private Optional<RuntimeService> runtimeService;
    private Optional<TaskService> taskService;
    private Optional<HistoryService> historyService;

    public ProcessService() {
        ProcessEngineConfiguration conf = new StandaloneProcessEngineConfiguration()
                .setJdbcUrl("jdbc:h2:mem:flowable;DB_CLOSE_DELAY=-1")
                .setJdbcUsername("sa")
                .setJdbcPassword("")
                .setJdbcDriver("org.h2.Driver")
                .setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);
        processEngine = conf.buildProcessEngine();  //Starting point
        repositoryService = Optional.empty();
        runtimeService = Optional.empty();
        taskService = Optional.empty();
        historyService = Optional.empty();
    }

    public Deployment createDeployment(String procDefFileName){
        ifNotPresentCreateRepositoryService();

        return repositoryService.map(
                rs -> rs.createDeployment()
                .addClasspathResource(procDefFileName)
                .deploy()
        ).get();
    }

    public ProcessDefinition getProcessDefinition(String deploymentId){
        ifNotPresentCreateRepositoryService();

        return repositoryService.map(rs -> rs.createProcessDefinitionQuery()
                .deploymentId(deploymentId)
                .singleResult()).get();
    }

    public ProcessInstance startProcessInstance(String processName, Map<String, Object> variables){
        ifNotPresentCreateRuntimeService();

        return runtimeService.map(rs -> rs.startProcessInstanceByKey(processName, variables)).get();
    }

    public List<Task> getTasksForCandidateGroup(String candidateGroup){
        ifNotPresentCreateTaskService();
        return taskService.map(ts -> ts.createTaskQuery().taskCandidateGroup(candidateGroup).list()).get();
    }

    private void ifNotPresentCreateTaskService() {
        taskService = taskService.isPresent() ? taskService : Optional.of(processEngine.getTaskService());
    }

    private void ifNotPresentCreateRuntimeService() {
        runtimeService = runtimeService.isPresent() ? runtimeService : Optional.of(processEngine.getRuntimeService());
    }

    private void ifNotPresentCreateRepositoryService() {
        repositoryService = repositoryService.isPresent() ? repositoryService : Optional.of(processEngine.getRepositoryService());
    }

    public Map<String, Object> getTaskVariables(String taskId) {
        ifNotPresentCreateTaskService();
        return taskService.map(ts -> ts.getVariables(taskId)).get();
    }

    public void completeTask(String taskId, Map<String, Object> approvalParams) {
        ifNotPresentCreateTaskService();
        taskService.ifPresent(ts -> ts.complete(taskId, approvalParams));
    }

    public List<HistoricActivityInstance> getFinishedHistoricTasks(String processInstanceId) {
        ifNotPresentCreateHistoryService();
        return historyService.map(hs ->
                hs.createHistoricActivityInstanceQuery()
                  .processInstanceId(processInstanceId)
                  .finished()
                  .orderByHistoricActivityInstanceEndTime().asc()
                  .list()).get();
    }

    private void ifNotPresentCreateHistoryService() {
        historyService = historyService.isPresent() ? historyService : Optional.of(processEngine.getHistoryService());
    }
}
