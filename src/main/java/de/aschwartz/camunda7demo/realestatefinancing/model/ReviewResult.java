package de.aschwartz.camunda7demo.realestatefinancing.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Result of the credit application review.
 */
@Data
@AllArgsConstructor
public class ReviewResult {
	private boolean accepted;
	private String contractNumber;
	private String rejectionReason;

	/**
	 * Creates an accepted result.
	 *
	 * @param contractNumber contract number
	 * @return accepted result
	 */
	public static ReviewResult accepted(String contractNumber) {
		return new ReviewResult(true, contractNumber, null);
	}

	/**
	 * Creates a rejected result.
	 *
	 * @param reason rejection reason
	 * @return rejected result
	 */
	public static ReviewResult rejected(String reason) {
		return new ReviewResult(false, null, reason);
	}
}
