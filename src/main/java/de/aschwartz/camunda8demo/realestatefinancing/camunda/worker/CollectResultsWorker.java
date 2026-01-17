package de.aschwartz.camunda8demo.realestatefinancing.camunda.worker;

import de.aschwartz.camunda8demo.realestatefinancing.camunda.store.ProcessStateStore;
import de.aschwartz.camunda8demo.realestatefinancing.model.Offer;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
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
	public Map<String, Object> handle(final ActivatedJob job) {
		Map<String, Object> variables = job.getVariablesAsMap();

		BigDecimal interestRateA = VariableMapper.getBigDecimal(variables, "interestRateA");
		BigDecimal interestRateB = VariableMapper.getBigDecimal(variables, "interestRateB");
		BigDecimal interestRateC = VariableMapper.getBigDecimal(variables, "interestRateC");

		List<Offer> offers = List.of(
				new Offer("Hyperbank", interestRateA),
				new Offer("Bank of Scottsdale", interestRateB),
				new Offer("Equity Bank", interestRateC)
		);

		String correlationId = VariableMapper.getString(variables, "correlationId");
		if (correlationId != null) {
			processStateStore.storeOffers(correlationId, offers);
		} else {
			log.warn("No correlationId available for offer caching.");
		}

		return Map.of("creditOffers", offers);
	}
}
