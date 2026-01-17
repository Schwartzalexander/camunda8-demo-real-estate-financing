package de.aschwartz.camunda8demo.realestatefinancing.camunda.worker;

import io.camunda.zeebe.spring.client.annotation.JobWorker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * Job workers that calculate interest rates for the demo banks.
 */
@Component
@Slf4j
public class BankConditionsWorker {

	@JobWorker(type = "request-conditions-bank-a")
	public Map<String, Object> handleBankA(Map<String, Object> variables) {
		BigDecimal interestRate = calculateInterestRate(variables, new BigDecimal("3.10"));
		return Map.of("interestRateA", interestRate);
	}

	@JobWorker(type = "request-conditions-bank-b")
	public Map<String, Object> handleBankB(Map<String, Object> variables) {
		BigDecimal interestRate = calculateInterestRate(variables, new BigDecimal("3.40"));
		return Map.of("interestRateB", interestRate);
	}

	@JobWorker(type = "request-conditions-bank-c")
	public Map<String, Object> handleBankC(Map<String, Object> variables) {
		BigDecimal interestRate = calculateInterestRate(variables, new BigDecimal("3.25"));
		return Map.of("interestRateC", interestRate);
	}

	private BigDecimal calculateInterestRate(Map<String, Object> variables, BigDecimal baseRate) {
		BigDecimal monthlyNetIncome = de.aschwartz.camunda8demo.realestatefinancing.camunda.worker.VariableMapper.getBigDecimal(variables, "monthlyNetIncome");
		BigDecimal propertyValue = de.aschwartz.camunda8demo.realestatefinancing.camunda.worker.VariableMapper.getBigDecimal(variables, "propertyValue");
		BigDecimal equity = de.aschwartz.camunda8demo.realestatefinancing.camunda.worker.VariableMapper.getBigDecimal(variables, "equity");

		BigDecimal equityRatio =
				(propertyValue != null && propertyValue.signum() > 0 && equity != null)
						? equity.divide(propertyValue, 6, RoundingMode.HALF_UP)
						: BigDecimal.ZERO;

		BigDecimal equityDiscount = equityRatio.multiply(new BigDecimal("1.20"));
		BigDecimal incomeBonus =
				(monthlyNetIncome != null && monthlyNetIncome.compareTo(new BigDecimal("4000")) >= 0)
						? new BigDecimal("0.20")
						: BigDecimal.ZERO;

		BigDecimal rate = baseRate
				.subtract(equityDiscount)
				.subtract(incomeBonus)
				.max(new BigDecimal("1.10"))
				.setScale(2, RoundingMode.HALF_UP);

		log.info("Calculated interest rate {} for base {}", rate, baseRate);
		return rate;
	}
}
