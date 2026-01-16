package de.aschwartz.camunda7demo.realestatefinancing.camunda.worker;

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
	public void handle(Map<String, Object> variables) {
		String correlationId = VariableMapper.getString(variables, "correlationId");
		log.info("[{}] Sending the contract to the client.", correlationId);
	}
}
