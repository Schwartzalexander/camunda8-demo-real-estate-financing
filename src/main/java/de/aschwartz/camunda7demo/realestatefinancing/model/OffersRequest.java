package de.aschwartz.camunda7demo.realestatefinancing.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request payload for the external offers API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OffersRequest implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	private Kunde kunde;
	private Immobilie immobilie;
	private Finanzierung finanzierung;

	/**
	 * Customer information payload.
	 */
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class Kunde implements Serializable {
		@Serial
		private static final long serialVersionUID = 1L;

		// Example value: "ANGESTELLTER".
		@JsonProperty("anstellungsVerhaeltnis")
		private String anstellungsVerhaeltnis;
	}

	/**
	 * Property information payload.
	 */
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class Immobilie implements Serializable {
		@Serial
		private static final long serialVersionUID = 1L;

		// Example uses int; we use BigDecimal to preserve scale in JSON.
		private BigDecimal kaufPreis;
		private String postleitzahl;
	}

	/**
	 * Financing information payload.
	 */
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class Finanzierung implements Serializable {
		@Serial
		private static final long serialVersionUID = 1L;

		private String finanzierungszweck;
		private BigDecimal tilgungsSatz;
		private Integer zinsBindungInJahren;
		private BigDecimal kreditbetrag;
		private LocalDate auszahlungsTermin;
	}
}
