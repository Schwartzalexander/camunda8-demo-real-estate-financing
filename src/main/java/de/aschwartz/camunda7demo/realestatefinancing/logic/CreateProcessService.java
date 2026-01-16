package de.aschwartz.camunda7demo.realestatefinancing.logic;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RuntimeService;
import org.springframework.stereotype.Service;

/**
 * Service that starts Camunda process instances by key.
 */
@Service
@Slf4j
public class CreateProcessService {
	private final RuntimeService runtimeService;

	/**
	 * Creates a new instance.
	 *
	 * @param runtimeService Camunda runtime service
	 */
	public CreateProcessService(RuntimeService runtimeService) {
		this.runtimeService = runtimeService;
	}

	/**
	 * Starts a process instance by process definition key.
	 *
	 * @param processId process definition key
	 * @return the process instance id
	 */
	public String createProcess(String processId) {
		try {
			var pi = runtimeService
					.createProcessInstanceByKey(processId)
					.executeWithVariablesInReturn();
			String processInstanceId = pi.getProcessInstanceId();
			log.info("[{}] Process {} was started.", processInstanceId, processId);
			return processInstanceId;
		} catch (Exception e) {
			log.error("Process {} could not be started.", processId, e);
			throw e;
		}
	}
}
