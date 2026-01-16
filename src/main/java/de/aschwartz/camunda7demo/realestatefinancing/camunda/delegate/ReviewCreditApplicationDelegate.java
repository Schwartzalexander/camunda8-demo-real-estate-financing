package de.aschwartz.camunda7demo.realestatefinancing.camunda.delegate;

import de.aschwartz.camunda7demo.realestatefinancing.model.Offer;
import de.aschwartz.camunda7demo.realestatefinancing.model.ReviewResult;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

/**
 * Reviews a selected offer and sets acceptance decision variables.
 */
@Component("reviewCreditApplicationDelegate")
@Slf4j
public class ReviewCreditApplicationDelegate implements JavaDelegate {

	/**
	 * Executes the credit application review.
	 *
	 * @param execution Camunda delegate execution
	 */
	@Override
	public void execute(DelegateExecution execution) {

		@SuppressWarnings("unchecked")
		List<Offer> offers = (List<Offer>) execution.getVariable("creditOffers");
		String bankName = (String) execution.getVariable("bankName");
		BigDecimal monthlyNetIncome = (BigDecimal) execution.getVariable("monthlyNetIncome");
		BigDecimal propertyValue = (BigDecimal) execution.getVariable("propertyValue");
		BigDecimal equity = (BigDecimal) execution.getVariable("equity");

		Offer selectedOffer = offers.stream().filter(it -> it.getBankName().equals(bankName)).findFirst().orElseThrow(() ->
				new RuntimeException("Invalid Offer. Bankname %s not found".formatted(bankName))
		);

		ReviewResult result = reviewApplication(monthlyNetIncome, propertyValue, equity, selectedOffer);

		execution.setVariable("applicationAccepted", result.isAccepted());
		execution.setVariable("contractNumber", result.getContractNumber());
		execution.setVariable("rejectionReason", result.getRejectionReason());
	}

	/**
	 * Applies basic validation and scoring to determine acceptance.
	 *
	 * @param monthlyNetIncome monthly net income
	 * @param propertyValue property value
	 * @param equity equity amount
	 * @param selectedOffer selected offer
	 * @return review result
	 */
	private ReviewResult reviewApplication(
			BigDecimal monthlyNetIncome,
			BigDecimal propertyValue,
			BigDecimal equity,
			Offer selectedOffer
	) {
		// --- Basic validation (Demo-safe) ---
		if (monthlyNetIncome == null || propertyValue == null || equity == null || selectedOffer == null)
			return ReviewResult.rejected("Missing input");
		if (monthlyNetIncome.compareTo(BigDecimal.ZERO) <= 0)
			return ReviewResult.rejected("Monthly net income must be > 0");
		if (propertyValue.compareTo(BigDecimal.ZERO) <= 0)
			return ReviewResult.rejected("Property value must be > 0");
		if (equity.compareTo(BigDecimal.ZERO) < 0)
			return ReviewResult.rejected("Equity must be >= 0");
		if (equity.compareTo(propertyValue) >= 0)
			return ReviewResult.rejected("Equity must be < property value");
		if (selectedOffer.getInterestRate() == null || selectedOffer.getInterestRate().compareTo(BigDecimal.ZERO) <= 0)
			return ReviewResult.rejected("Interest rate must be > 0");

		// --- Derived values ---
		BigDecimal loanAmount = propertyValue.subtract(equity);
		BigDecimal equityRatio = equity.divide(propertyValue, 6, RoundingMode.HALF_UP);
		BigDecimal ltv = loanAmount.divide(propertyValue, 6, RoundingMode.HALF_UP);

		// interestRate is percent (e.g. 3.5 -> 3.5%)
		BigDecimal annualRate = selectedOffer.getInterestRate().divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP);
		BigDecimal monthlyRate = annualRate.divide(new BigDecimal("12"), 10, RoundingMode.HALF_UP);

		// --- Monthly payment approximation (annuity) ---
		int months = 30 * 12;

		BigDecimal onePlusR = BigDecimal.ONE.add(monthlyRate);
		BigDecimal pow = BigDecimal.ONE;
		for (int i = 0; i < months; i++) {
			pow = pow.multiply(onePlusR);
		}

		BigDecimal denominator = BigDecimal.ONE.subtract(BigDecimal.ONE.divide(pow, 20, RoundingMode.HALF_UP));
		if (denominator.compareTo(BigDecimal.ZERO) <= 0) {
			return ReviewResult.rejected("Interest configuration not plausible");
		}

		BigDecimal monthlyPayment = loanAmount
				.multiply(monthlyRate)
				.divide(denominator, 2, RoundingMode.HALF_UP);

		BigDecimal dti = monthlyPayment.divide(monthlyNetIncome, 6, RoundingMode.HALF_UP);

		// --- Hard rejects with reasons ---
		if (equityRatio.compareTo(new BigDecimal("0.10")) < 0)
			return ReviewResult.rejected("Equity ratio too low (< 10%)");
		if (ltv.compareTo(new BigDecimal("0.90")) > 0)
			return ReviewResult.rejected("Loan-to-value too high (> 90%)");
		if (dti.compareTo(new BigDecimal("0.35")) > 0)
			return ReviewResult.rejected("Monthly payment too high (> 35% of net income)");

		// --- Soft scoring (demo) ---
		int score = 0;

		if (equityRatio.compareTo(new BigDecimal("0.20")) >= 0) score += 2;
		else score += 1;

		if (selectedOffer.getInterestRate().compareTo(new BigDecimal("4.5")) <= 0) score += 2;
		else if (selectedOffer.getInterestRate().compareTo(new BigDecimal("6.0")) <= 0) score += 1;

		if (dti.compareTo(new BigDecimal("0.25")) <= 0) score += 2;
		else score += 1;

		if (score >= 4) {
			return ReviewResult.accepted(generateContractNumber(selectedOffer.getBankName()));
		}

		return ReviewResult.rejected("Score too low (risk too high)");
	}

	/**
	 * Generates a contract number using the bank name and a random suffix.
	 *
	 * @param bankName bank name
	 * @return generated contract number
	 */
	private String generateContractNumber(String bankName) {
		String bankCode = bankName == null
				? "BANK"
				: bankName.replaceAll("[^A-Z]", "")
				.toUpperCase();

		return bankCode + "-"
				+ System.currentTimeMillis()
				+ "-"
				+ UUID.randomUUID().toString().substring(0, 8).toUpperCase();
	}

}
