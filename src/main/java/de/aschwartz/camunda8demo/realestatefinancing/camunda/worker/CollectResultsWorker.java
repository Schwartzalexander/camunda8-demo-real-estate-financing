package de.aschwartz.camunda8demo.realestatefinancing.camunda.worker;

import de.aschwartz.camunda8demo.realestatefinancing.camunda.store.ProcessStateStore;
import de.aschwartz.camunda8demo.realestatefinancing.model.Offer;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.annotation.VariablesAsMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Aggregates interest rates into a list of offers.
 */
@Component
@Slf4j
public class CollectResultsWorker {

	private final ProcessStateStore processStateStore;

	public CollectResultsWorker(ProcessStateStore processStateStore) {
		this.processStateStore = processStateStore;
	}

	@JobWorker(type = "collect-results")
	public Map<String, Object> handle(@VariablesAsMap Map<String, Object> variables) {
		BigDecimal interestRateA = de.aschwartz.camunda8demo.realestatefinancing.camunda.worker.VariableMapper.getBigDecimal(variables, "interestRateA");
		BigDecimal interestRateB = de.aschwartz.camunda8demo.realestatefinancing.camunda.worker.VariableMapper.getBigDecimal(variables, "interestRateB");
		BigDecimal interestRateC = de.aschwartz.camunda8demo.realestatefinancing.camunda.worker.VariableMapper.getBigDecimal(variables, "interestRateC");

		List<Offer> offers = List.of(
				new Offer("Hyperbank", interestRateA),
				new Offer("Bank of Scottsdale", interestRateB),
				new Offer("Equity Bank", interestRateC)
		);

		String correlationId = de.aschwartz.camunda8demo.realestatefinancing.camunda.worker.VariableMapper.getString(variables, "correlationId");
		if (correlationId != null) {
			processStateStore.storeOffers(correlationId, offers);
		} else {
			log.warn("No correlationId available for offer caching.");
		}

		return Map.of("creditOffers", offers);
	}
}
