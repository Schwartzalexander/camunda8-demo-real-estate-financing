package de.aschwartz.camunda7demo.realestatefinancing.camunda.usertask;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Base class for Camunda user-task helpers.
 */
@Service
@Slf4j
@Getter
public class GenericUserTaskService {

	private final TaskService taskService;
	private final RuntimeService runtimeService;
	private final HistoryService historyService;

	/**
	 * Creates the service.
	 *
	 * @param taskService task service
	 * @param runtimeService runtime service
	 * @param historyService history service
	 */
	public GenericUserTaskService(TaskService taskService, RuntimeService runtimeService, HistoryService historyService) {
		this.taskService = taskService;
		this.runtimeService = runtimeService;
		this.historyService = historyService;
	}

	/**
	 * Finds the newest active task by definition key.
	 *
	 * @param processInstanceId process instance id
	 * @param taskDefinitionKey task definition key
	 * @return optional task
	 */
	Optional<Task> findTask(String processInstanceId, String taskDefinitionKey) {
		return taskService.createTaskQuery()
				.taskDefinitionKey(taskDefinitionKey)
				.active()
				.orderByTaskCreateTime()
				.desc()
				.listPage(0, 1)
				.stream().findFirst();
	}

	/**
	 * Reads a String variable from runtime or history.
	 *
	 * @param processInstanceId process instance id
	 * @param name variable name
	 * @return String value or {@code null}
	 */
	String readStringVar(String processInstanceId, String name) {
		Object o = readVar(processInstanceId, name);
		return (o instanceof String s) ? s : null;
	}

	/**
	 * Reads a Boolean variable from runtime or history.
	 *
	 * @param processInstanceId process instance id
	 * @param name variable name
	 * @return Boolean value or {@code null}
	 */
	Boolean readBooleanVar(String processInstanceId, String name) {
		Object o = readVar(processInstanceId, name);
		if (o instanceof Boolean b) return b;
		if (o instanceof String s) return Boolean.parseBoolean(s); // string fallback
		return null;
	}

	/**
	 * Checks if the process instance is still active.
	 *
	 * @param processInstanceId process instance id
	 * @return true if active
	 */
	private boolean isProcessInstanceActive(String processInstanceId) {
		ProcessInstance pi = runtimeService.createProcessInstanceQuery()
				.processInstanceId(processInstanceId)
				.active()
				.singleResult();
		return pi != null;
	}

	/**
	 * Reads a variable from runtime if active, otherwise from history.
	 *
	 * @param processInstanceId process instance id
	 * @param name variable name
	 * @return variable value or {@code null}
	 */
	private Object readVar(String processInstanceId, String name) {
		// 1) try runtime (if still running)
		if (isProcessInstanceActive(processInstanceId)) {
			Object value = runtimeService.getVariable(processInstanceId, name);
			if (value != null)
				return value;
		}

		// 2) history (works after end)
		HistoricVariableInstance hvi = historyService.createHistoricVariableInstanceQuery()
				.processInstanceId(processInstanceId)
				.variableName(name)
				.singleResult();

		return hvi != null ? hvi.getValue() : null;
	}
}
