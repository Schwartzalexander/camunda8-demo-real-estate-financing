package de.aschwartz.camunda8demo.realestatefinancing.logic;

import io.camunda.zeebe.client.ZeebeClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Service that starts Camunda 8 process instances by key.
 */
@Service
@Slf4j
public class CreateProcessService {
	private final ZeebeClient zeebeClient;

	/**
	 * Creates a new instance.
	 *
	 * @param zeebeClient Camunda 8 Zeebe client
	 */
	public CreateProcessService(ZeebeClient zeebeClient) {
		this.zeebeClient = zeebeClient;
	}

	/**
	 * Starts a process instance by process definition key.
	 *
	 * @param processId process definition key
	 * @param variables initial variables
	 * @return the process instance id
	 */
	public String createProcess(String processId, Map<String, Object> variables) {
		try {
			Map<String, Object> payload = variables != null ? new HashMap<>(variables) : new HashMap<>();
			String correlationId = payload.containsKey("correlationId")
					? String.valueOf(payload.get("correlationId"))
					: null;
			if (correlationId == null || correlationId.isBlank()) {
				correlationId = UUID.randomUUID().toString();
			}
			payload.put("correlationId", correlationId);
			long processInstanceKey = zeebeClient
					.newCreateInstanceCommand()
					.bpmnProcessId(processId)
					.latestVersion()
					.variables(payload)
					.send()
					.join()
					.getProcessInstanceKey();

			log.info("[{}] Process {} was started.", correlationId, processId);
			return correlationId;
		} catch (Exception e) {
			log.error("Process {} could not be started.", processId, e);
			throw e;
		}
	}

	/**
	 * Starts a process instance without initial variables.
	 *
	 * @param processId process definition key
	 * @return the process instance id
	 */
	public String createProcess(String processId) {
		return createProcess(processId, Map.of());
	}
}
