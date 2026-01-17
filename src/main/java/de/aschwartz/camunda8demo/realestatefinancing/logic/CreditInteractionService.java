package de.aschwartz.camunda8demo.realestatefinancing.logic;

import io.camunda.zeebe.client.ZeebeClient;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Service to advance the credit process via messages.
 */
@Service
public class CreditInteractionService {

	private final ZeebeClient zeebeClient;

	public CreditInteractionService(ZeebeClient zeebeClient) {
		this.zeebeClient = zeebeClient;
	}

	public void publishBankSelected(String correlationId, String bankName) {
		zeebeClient.newPublishMessageCommand()
				.messageName("bank-selected")
				.correlationKey(correlationId)
				.variables(Map.of("bankName", bankName))
				.send()
				.join();
	}

	public void publishApplicationSubmitted(String correlationId) {
		zeebeClient.newPublishMessageCommand()
				.messageName("application-submitted")
				.correlationKey(correlationId)
				.send()
				.join();
	}

	public void publishContractSigned(String correlationId) {
		zeebeClient.newPublishMessageCommand()
				.messageName("contract-signed")
				.correlationKey(correlationId)
				.send()
				.join();
	}
}
