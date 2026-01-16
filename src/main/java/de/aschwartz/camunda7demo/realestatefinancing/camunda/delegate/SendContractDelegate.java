package de.aschwartz.camunda7demo.realestatefinancing.camunda.delegate;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

/**
 * Simulates sending a contract to the client.
 */
@Component("sendContractDelegate")
@Slf4j
public class SendContractDelegate implements JavaDelegate {

	/**
	 * Logs the contract send action.
	 *
	 * @param execution Camunda delegate execution
	 */
	@Override
	public void execute(DelegateExecution execution) {
		String processInstanceId = execution.getProcessInstanceId();
		log.info("[{}] Sending the contract to the client.", processInstanceId);
	}

}
