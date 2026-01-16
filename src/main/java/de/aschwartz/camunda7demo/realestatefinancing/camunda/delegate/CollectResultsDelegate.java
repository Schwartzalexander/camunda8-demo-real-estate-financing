package de.aschwartz.camunda7demo.realestatefinancing.camunda.delegate;

import de.aschwartz.camunda7demo.realestatefinancing.model.Offer;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Collects offer results from the three bank calculations.
 */
@Component("collectResultsDelegate")
@Slf4j
public class CollectResultsDelegate implements JavaDelegate {

	/**
	 * Aggregates interest rates into a list of offers.
	 *
	 * @param execution Camunda delegate execution
	 */
	@Override
	public void execute(DelegateExecution execution) {
		BigDecimal interestRateA = (BigDecimal) execution.getVariable("interestRateA");
		BigDecimal interestRateB = (BigDecimal) execution.getVariable("interestRateB");
		BigDecimal interestRateC = (BigDecimal) execution.getVariable("interestRateC");

		List<Offer> offers = List.of(
				new Offer("Hyperbank", interestRateA),
				new Offer("Bank of Scottsdale", interestRateB),
				new Offer("Equity Bank", interestRateC)
		);

		execution.setVariable("creditOffers", offers);
	}

}
