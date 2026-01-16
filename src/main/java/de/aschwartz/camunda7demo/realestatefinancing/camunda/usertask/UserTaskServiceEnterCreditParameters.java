package de.aschwartz.camunda7demo.realestatefinancing.camunda.usertask;

import de.aschwartz.camunda7demo.realestatefinancing.model.EnterCreditParametersResponse;
import de.aschwartz.camunda7demo.realestatefinancing.model.Offer;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.variable.Variables;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * User-task handler for entering credit parameters in the comparison flow.
 */
@Service
@Slf4j
public class UserTaskServiceEnterCreditParameters extends GenericUserTaskService {

	/**
	 * Creates the service.
	 *
	 * @param taskService task service
	 * @param runtimeService runtime service
	 * @param historyService history service
	 */
	public UserTaskServiceEnterCreditParameters(TaskService taskService, RuntimeService runtimeService, HistoryService historyService) {
		super(taskService, runtimeService, historyService);
	}

	/**
	 * Completes the user task and reads the generated offers.
	 *
	 * @param monthlyNetIncome monthly net income
	 * @param propertyValue property value
	 * @param equity equity amount
	 * @param processInstanceId Camunda process instance id
	 * @return response containing offers
	 */
	public EnterCreditParametersResponse enterCreditParameters(BigDecimal monthlyNetIncome, BigDecimal propertyValue, BigDecimal equity, String processInstanceId) {

		Optional<Task> taskOpt = super.findTask(processInstanceId, "Task_EnterCreditParameters");
		if (taskOpt.isEmpty()) {
			throw new RuntimeException("[%s] No active Task_EnterCreditParameters was found.".formatted(processInstanceId));

		}
		Task task = taskOpt.get();

		getTaskService().complete(task.getId(),
				Variables.createVariables()
						.putValue("monthlyNetIncome", monthlyNetIncome)
						.putValue("propertyValue", propertyValue)
						.putValue("equity", equity));

		Object offersVar = getRuntimeService().getVariable(processInstanceId, "creditOffers");

		@SuppressWarnings("unchecked")
		List<Offer> offers = (List<Offer>) offersVar;

		return new EnterCreditParametersResponse(offers);
	}

}
