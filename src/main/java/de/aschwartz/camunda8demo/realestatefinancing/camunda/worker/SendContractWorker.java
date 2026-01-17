package de.aschwartz.camunda8demo.realestatefinancing.camunda.worker;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Simulates sending a contract to the client.
 */
@Component
@Slf4j
public class SendContractWorker {

	@JobWorker(type = "send-contract")
	public void handle(final ActivatedJob job) {
		Map<String, Object> variables = job.getVariablesAsMap();
		String correlationId = de.aschwartz.camunda8demo.realestatefinancing.camunda.worker.VariableMapper.getString(variables, "correlationId");
		log.info("[{}] Sending the contract to the client.", correlationId);
	}
}
