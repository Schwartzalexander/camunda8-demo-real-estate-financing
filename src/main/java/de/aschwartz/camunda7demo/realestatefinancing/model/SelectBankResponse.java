package de.aschwartz.camunda7demo.realestatefinancing.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Response containing input values after bank selection.
 */
@Data
@AllArgsConstructor
public class SelectBankResponse {
	/**
	 * Monthly net income.
	 */
	BigDecimal monthlyNetIncome;
	/**
	 * Property value.
	 */
	BigDecimal propertyValue;
	/**
	 * Equity amount.
	 */
	BigDecimal equity;
}
