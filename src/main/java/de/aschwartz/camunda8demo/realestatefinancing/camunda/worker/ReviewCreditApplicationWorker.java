package de.aschwartz.camunda8demo.realestatefinancing.camunda.worker;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.aschwartz.camunda8demo.realestatefinancing.camunda.store.ProcessStateStore;
import de.aschwartz.camunda8demo.realestatefinancing.model.Offer;
import de.aschwartz.camunda8demo.realestatefinancing.model.ReviewResult;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Reviews a selected offer and sets acceptance decision variables.
 */
@Component
@Slf4j
public class ReviewCreditApplicationWorker {

	private final ProcessStateStore processStateStore;
	private final ObjectMapper objectMapper;

	public ReviewCreditApplicationWorker(ProcessStateStore processStateStore, ObjectMapper objectMapper) {
		this.processStateStore = processStateStore;
		this.objectMapper = objectMapper;
	}

	@JobWorker(type = "review-credit-application")
	public Map<String, Object> handle(final ActivatedJob job) {
		Map<String, Object> variables = job.getVariablesAsMap();
		List<Offer> offers = readOffers(variables.get("creditOffers"));
		String bankName = de.aschwartz.camunda8demo.realestatefinancing.camunda.worker.VariableMapper.getString(variables, "bankName");
		BigDecimal monthlyNetIncome = de.aschwartz.camunda8demo.realestatefinancing.camunda.worker.VariableMapper.getBigDecimal(variables, "monthlyNetIncome");
		BigDecimal propertyValue = de.aschwartz.camunda8demo.realestatefinancing.camunda.worker.VariableMapper.getBigDecimal(variables, "propertyValue");
		BigDecimal equity = de.aschwartz.camunda8demo.realestatefinancing.camunda.worker.VariableMapper.getBigDecimal(variables, "equity");

		Offer selectedOffer = offers.stream()
				.filter(it -> it.getBankName().equals(bankName))
				.findFirst()
				.orElseThrow(() -> new RuntimeException("Invalid Offer. Bankname %s not found".formatted(bankName)));

		ReviewResult result = reviewApplication(monthlyNetIncome, propertyValue, equity, selectedOffer);

		String correlationId = de.aschwartz.camunda8demo.realestatefinancing.camunda.worker.VariableMapper.getString(variables, "correlationId");
		if (correlationId != null) {
			processStateStore.storeReviewResult(correlationId, result);
		}

		Map<String, Object> vars = new HashMap<>();
		vars.put("applicationAccepted", result.isAccepted());
		vars.put("contractNumber", result.getContractNumber());
		vars.put("rejectionReason", result.getRejectionReason());
		return vars;

	}

	@SuppressWarnings("unchecked")
	private List<Offer> readOffers(Object offersObject) {
		if (offersObject == null) {
			throw new IllegalStateException("No offers available");
		}
		if (offersObject instanceof List<?> list && !list.isEmpty() && list.get(0) instanceof Offer) {
			return (List<Offer>) list;
		}
		return objectMapper.convertValue(offersObject, objectMapper.getTypeFactory().constructCollectionType(List.class, Offer.class));
	}

	private ReviewResult reviewApplication(
			BigDecimal monthlyNetIncome,
			BigDecimal propertyValue,
			BigDecimal equity,
			Offer selectedOffer
	) {
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

		BigDecimal loanAmount = propertyValue.subtract(equity);
		BigDecimal equityRatio = equity.divide(propertyValue, 6, RoundingMode.HALF_UP);
		BigDecimal ltv = loanAmount.divide(propertyValue, 6, RoundingMode.HALF_UP);

		BigDecimal annualRate = selectedOffer.getInterestRate().divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP);
		BigDecimal monthlyRate = annualRate.divide(new BigDecimal("12"), 10, RoundingMode.HALF_UP);

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

		if (equityRatio.compareTo(new BigDecimal("0.10")) < 0)
			return ReviewResult.rejected("Equity ratio too low (< 10%)");
		if (ltv.compareTo(new BigDecimal("0.90")) > 0)
			return ReviewResult.rejected("Loan-to-value too high (> 90%)");
		if (dti.compareTo(new BigDecimal("0.35")) > 0)
			return ReviewResult.rejected("Monthly payment too high (> 35% of net income)");

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
