package de.aschwartz.camunda7demo.realestatefinancing.camunda.delegate;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Calculates interest rates for Bank C using a loan-to-value curve.
 */
@Component("requestConditionsBankCDelegate")
@Slf4j
public class RequestConditionsBankCDelegate implements JavaDelegate {

	/**
	 * Executes the delegate and sets the calculated interest rate.
	 *
	 * @param execution Camunda delegate execution
	 */
	@Override
	public void execute(DelegateExecution execution) {
		BigDecimal monthlyNetIncome = (BigDecimal) execution.getVariable("monthlyNetIncome");
		BigDecimal propertyValue = (BigDecimal) execution.getVariable("propertyValue");
		BigDecimal equity = (BigDecimal) execution.getVariable("equity");

		BigDecimal interestRate = calculateInterestRate(
				monthlyNetIncome, propertyValue, equity
		);

		execution.setVariable("interestRateC", interestRate);
	}

	/**
	 * Calculates an interest rate based on income, equity, and loan-to-value.
	 *
	 * @param income monthly net income
	 * @param propertyValue property value
	 * @param equity equity amount
	 * @return interest rate
	 */
	private BigDecimal calculateInterestRate(
			BigDecimal income,
			BigDecimal propertyValue,
			BigDecimal equity
	) {
		if (propertyValue == null || propertyValue.signum() <= 0) {
			// Without a property value, LTV cannot be calculated: very expensive rate.
			return new BigDecimal("12.00");
		}

		if (equity == null) equity = BigDecimal.ZERO;
		if (income == null) income = BigDecimal.ZERO;

		// loan = propertyValue - equity, min 0
		BigDecimal loan = propertyValue.subtract(equity);
		if (loan.signum() < 0) loan = BigDecimal.ZERO;

		// LTV in [0..] (1.0 = 100%)
		BigDecimal ltv = loan.divide(propertyValue, 8, RoundingMode.HALF_UP);

		// Core idea:
		// - With low LTV the rate should be very small.
		// - Around 80% it should rise noticeably.
		// - At >=100% it becomes very expensive.
		//
		// Build a smooth curve:
		// rate = nearZero + A * (ltv^p) / (1 - ltv + eps)   (blows up as ltv -> 1)
		// plus a surcharge when ltv >= 1
		BigDecimal nearZero = new BigDecimal("0.15");     // near zero
		BigDecimal A = new BigDecimal("1.40");      // base scaling
		BigDecimal p = new BigDecimal("2.30");      // stronger weighting at high LTV
		BigDecimal eps = new BigDecimal("0.03");      // prevents division by zero

		// ltv^p (using double, because non-integer BigDecimal pow is cumbersome)
		double ltvD = ltv.doubleValue();
		double curve = Math.pow(ltvD, p.doubleValue()) / (Math.max(1e-9, (1.0 - ltvD + eps.doubleValue())));

		BigDecimal rate = nearZero.add(A.multiply(BigDecimal.valueOf(curve)));

		// Bonus/penalty: income lightly influences the rate.
		if (income.compareTo(new BigDecimal("5000")) >= 0) {
			rate = rate.subtract(new BigDecimal("0.10"));
		} else if (income.compareTo(new BigDecimal("2500")) < 0) {
			rate = rate.add(new BigDecimal("0.25"));
		}

		// Hard risk surcharges for very high LTV.
		if (ltv.compareTo(new BigDecimal("0.90")) >= 0) {
			rate = rate.add(new BigDecimal("1.50")); // noticeably more expensive at 90%+
		}

		// When >=100%: very high.
		if (ltv.compareTo(BigDecimal.ONE) >= 0) {
			BigDecimal over = ltv.subtract(BigDecimal.ONE); // e.g., 1.10 -> 0.10
			// fast growth: extra uplift up to +10% LTV
			rate = rate.add(new BigDecimal("6.00"))
					.add(over.multiply(new BigDecimal("20.0"))); // 10% over -> +2.0
		}

		// Clamp to a plausible range.
		BigDecimal min = new BigDecimal("0.05");  // near zero
		BigDecimal max = new BigDecimal("18.00"); // very high
		if (rate.compareTo(min) < 0) rate = min;
		if (rate.compareTo(max) > 0) rate = max;

		return rate.setScale(2, RoundingMode.HALF_UP);
	}

}
