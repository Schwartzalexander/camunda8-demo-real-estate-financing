package de.aschwartz.camunda7demo.realestatefinancing.camunda.usertask;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * User-task handler for signing the credit contract.
 */
@Service
@Slf4j
public class UserSignContract extends GenericUserTaskService {

	/**
	 * Creates the service.
	 *
	 * @param taskService task service
	 * @param runtimeService runtime service
	 * @param historyService history service
	 */
	public UserSignContract(TaskService taskService, RuntimeService runtimeService, HistoryService historyService) {
		super(taskService, runtimeService, historyService);
	}

	/**
	 * Completes the sign-contract user task.
	 *
	 * @param processInstanceId Camunda process instance id
	 */
	public void signContract(String processInstanceId) {
		Optional<Task> taskOpt = super.findTask(processInstanceId, "Task_SignContract");
		if (taskOpt.isEmpty()) {
			throw new RuntimeException("[%s] No active Task_SignContract was found.".formatted(processInstanceId));

		}
		Task task = taskOpt.get();
		getTaskService().complete(task.getId());
	}

}
