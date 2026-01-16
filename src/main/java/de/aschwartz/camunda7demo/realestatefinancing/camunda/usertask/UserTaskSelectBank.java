package de.aschwartz.camunda7demo.realestatefinancing.camunda.usertask;

import de.aschwartz.camunda7demo.realestatefinancing.model.SelectBankResponse;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.variable.Variables;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * User-task handler for selecting a bank.
 */
@Service
@Slf4j
public class UserTaskSelectBank extends GenericUserTaskService {

	/**
	 * Creates the service.
	 *
	 * @param taskService task service
	 * @param runtimeService runtime service
	 * @param historyService history service
	 */
	public UserTaskSelectBank(TaskService taskService, RuntimeService runtimeService, HistoryService historyService) {
		super(taskService, runtimeService, historyService);
	}

	/**
	 * Completes the bank selection user task.
	 *
	 * @param bankName selected bank name
	 * @param processInstanceId Camunda process instance id
	 * @return response echoing key values
	 */
	public SelectBankResponse selectBank(String bankName, String processInstanceId) {
		Optional<Task> taskOpt = super.findTask(processInstanceId, "Task_BankSelection");
		if (taskOpt.isEmpty()) {
			throw new RuntimeException("[%s] No active Task_BankSelection was found.".formatted(processInstanceId));

		}
		Task task = taskOpt.get();

		getTaskService().complete(task.getId(),
				Variables.createVariables()
						.putValue("bankName", bankName));

		BigDecimal monthlyNetIncome = (BigDecimal) getRuntimeService().getVariable(processInstanceId, "monthlyNetIncome");
		BigDecimal propertyValue = (BigDecimal) getRuntimeService().getVariable(processInstanceId, "propertyValue");
		BigDecimal equity = (BigDecimal) getRuntimeService().getVariable(processInstanceId, "equity");

		return new SelectBankResponse(monthlyNetIncome, propertyValue, equity);

	}

}
