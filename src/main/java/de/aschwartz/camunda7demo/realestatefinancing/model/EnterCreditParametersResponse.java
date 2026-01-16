package de.aschwartz.camunda7demo.realestatefinancing.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * Response wrapper for credit parameter entry.
 */
@Data
@AllArgsConstructor
public class EnterCreditParametersResponse {
	/**
	 * List of offers produced by the comparison process.
	 */
	List<Offer> offers;
}
