package de.aschwartz.camunda7demo.realestatefinancing.camunda.executionlistener;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;

/**
 * Execution listener that logs when a DMN credit application is declined.
 */
@Slf4j
public class LogDecline implements ExecutionListener {
	/**
	 * Logs the decline event for the process instance.
	 *
	 * @param delegateExecution Camunda delegate execution
	 * @throws Exception when logging fails
	 */
	@Override
	public void notify(DelegateExecution delegateExecution) throws Exception {
		log.info("[{}] DMN credit application was declined.", delegateExecution.getProcessInstance().getProcessInstanceId());
	}
}
