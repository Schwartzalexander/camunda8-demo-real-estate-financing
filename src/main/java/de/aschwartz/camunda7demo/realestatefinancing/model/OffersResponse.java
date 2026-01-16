package de.aschwartz.camunda7demo.realestatefinancing.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * Response payload for the external offers API.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OffersResponse implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	private List<Angebot> angebote;

	/**
	 * Single offer entry.
	 */
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class Angebot implements Serializable {

		@Serial
		private static final long serialVersionUID = 1L;

		private String vermittler;
		private Kondition kondition;
		private Anbieter anbieter;
		private Produktinformation produktinformation;

		private String actionUrl;

		/**
		 * Offer terms.
		 */
		@Data
		@NoArgsConstructor
		@AllArgsConstructor
		@Builder
		@JsonInclude(JsonInclude.Include.NON_NULL)
		public static class Kondition implements Serializable {
			@Serial
			private static final long serialVersionUID = 1L;

			private BigDecimal sollZins;
			private BigDecimal effektivZins;
			private BigDecimal monatlicheRate;
			private BigDecimal anfaenglicheTilgung;

			private BigDecimal zinskostenAmEndeDerZinsbindung;
			private BigDecimal restschuldAmEndeDerZinsbindung;

			private Integer zinsbindungInJahren;
			private Integer gesamtlaufzeitInMonaten;

			private BigDecimal beleihungsauslauf;
			private BigDecimal kaufpreis;
			private BigDecimal darlehensbetrag;
			private BigDecimal gesamtkosten;

			private BigDecimal grundbuchkosten;
		}

		/**
		 * Provider information.
		 */
		@Data
		@NoArgsConstructor
		@AllArgsConstructor
		@Builder
		@JsonInclude(JsonInclude.Include.NON_NULL)
		public static class Anbieter implements Serializable {
			@Serial
			private static final long serialVersionUID = 1L;

			private String id;
			private String name;
			private String kurzbezeichnung;
			private String ksId;
			private Anschrift anschrift;

			private String gegruendet;
			private Boolean beratungVorOrt;
			private Boolean unterstuetztKfw;
			private String unterstuetztSondertilgung;
			private String anbietertyp;
			private String informationstext;

			private Integer anzahlMitarbeiter;
			private Integer anzahlNiederlassungen;

			private String urlLogo;
			private Boolean regional;
			private String urlKlickout;

			private String leadId;
			private String leadmanagementId;

			/**
			 * Provider address.
			 */
			@Data
			@NoArgsConstructor
			@AllArgsConstructor
			@Builder
			@JsonInclude(JsonInclude.Include.NON_NULL)
			public static class Anschrift implements Serializable {
				@Serial
				private static final long serialVersionUID = 1L;

				private String ort;
				private String plz;
				private String strasseUndHausnummer;
			}
		}

		/**
		 * Product information metadata.
		 */
		@Data
		@NoArgsConstructor
		@AllArgsConstructor
		@Builder
		@JsonInclude(JsonInclude.Include.NON_NULL)
		public static class Produktinformation implements Serializable {
			@Serial
			private static final long serialVersionUID = 1L;

			private BigDecimal minimalerDarlehensbetrag;
			private BigDecimal maximalerDarlehensbetrag;
			private BigDecimal bearbeitungsgebuehr;

			private String bereitstellungsfreieZeitText;
			private String sondertilgung;
			private String tilgungsaussetzungText;
			private String kfwDarlehenText;

			private Integer produktId;
		}

		// --------- Optional: JSON serialize/deserialize helper ----------
		/**
		 * Serializes this offer to JSON.
		 *
		 * @param mapper object mapper
		 * @return JSON string
		 */
		public String toJson(ObjectMapper mapper) {
			try {
				return mapper.writeValueAsString(this);
			} catch (Exception e) {
				throw new RuntimeException("Failed to serialize Angebot to JSON", e);
			}
		}

		/**
		 * Deserializes a single offer from JSON.
		 *
		 * @param mapper object mapper
		 * @param json JSON payload
		 * @return deserialized offer
		 */
		public static Angebot fromJson(ObjectMapper mapper, String json) {
			try {
				return mapper.readValue(json, Angebot.class);
			} catch (Exception e) {
				throw new RuntimeException("Failed to deserialize Angebot from JSON", e);
			}
		}
	}

	// JSON helpers for the full response.
	/**
	 * Serializes this response to JSON.
	 *
	 * @param mapper object mapper
	 * @return JSON string
	 */
	public String toJson(ObjectMapper mapper) {
		try {
			return mapper.writeValueAsString(this);
		} catch (Exception e) {
			throw new RuntimeException("Failed to serialize OffersResponse to JSON", e);
		}
	}

	/**
	 * Deserializes a response from JSON.
	 *
	 * @param mapper object mapper
	 * @param json JSON payload
	 * @return deserialized response
	 */
	public static OffersResponse fromJson(ObjectMapper mapper, String json) {
		try {
			return mapper.readValue(json, OffersResponse.class);
		} catch (Exception e) {
			throw new RuntimeException("Failed to deserialize OffersResponse from JSON", e);
		}
	}
}
