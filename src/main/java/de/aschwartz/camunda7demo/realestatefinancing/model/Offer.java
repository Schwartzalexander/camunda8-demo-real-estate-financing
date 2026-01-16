package de.aschwartz.camunda7demo.realestatefinancing.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Simple offer representation for the UI.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Offer implements Serializable {
	private static final long serialVersionUID = 1L;

	private String bankName;
	private BigDecimal interestRate;
}
