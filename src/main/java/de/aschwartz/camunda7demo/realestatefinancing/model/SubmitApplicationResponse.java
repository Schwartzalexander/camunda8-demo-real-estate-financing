package de.aschwartz.camunda7demo.realestatefinancing.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Response containing the application decision.
 */
@Data
@AllArgsConstructor
public class SubmitApplicationResponse {
	/**
	 * Whether the application was accepted.
	 */
	Boolean accepted;
	/**
	 * Contract number when accepted.
	 */
	String contractNumber;
	/**
	 * Reason for rejection when declined.
	 */
	String rejectionReason;
}
