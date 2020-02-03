package com.flowable.standalone;

import com.flowable.process.HolidayBookProcess;
import com.flowable.process.UserInteraction;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;

import java.util.List;
import java.util.Map;

public class ProcessStart {
    public static void main(String[] args) {
        ProcessService ps = new ProcessService();
        UserInteraction pp = new UserInteraction();
        HolidayBookProcess hbp = new HolidayBookProcess(pp);

        Deployment deploy = ps.createDeployment("holiday-request.bpmn");
        ProcessDefinition processDefinition = ps.getProcessDefinition(deploy.getId());
        System.out.println(processDefinition.getName());

        Map<String, Object> userInput = hbp.scanUserVariables();
        ProcessInstance holidayRequest = ps.startProcessInstance("holidayRequest", userInput);
        System.out.println(holidayRequest.getName());

        List<Task> tasks = ps.getTasksForCandidateGroup("managers");
        hbp.printTasks(tasks);
        String taskId = hbp.chooseTaskAndReturnId(tasks);
        Map<String,Object> taskVars = ps.getTaskVariables(taskId);
        hbp.presentRequest(taskVars);
        Map<String, Object> approvalParams = hbp.presentApprovalQuestion();
        ps.completeTask(taskId, approvalParams);

        List<HistoricActivityInstance> finishedTasks = ps.getFinishedHistoricTasks(holidayRequest.getId());
        hbp.presentFinishedHistoricTasks(finishedTasks);
    }
}
