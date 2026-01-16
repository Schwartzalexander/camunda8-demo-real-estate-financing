package de.aschwartz.camunda7demo.realestatefinancing.camunda.delegate;

import de.aschwartz.camunda7demo.realestatefinancing.model.OffersRequest;
import de.aschwartz.camunda7demo.realestatefinancing.model.OffersResponse;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.variable.Variables;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;

/**
 * Retrieves the cheapest offer from an external auto-credit API.
 */
@Component("getCheapestOfferDelegate")
@Slf4j
public class GetCheapestOfferDelegate implements JavaDelegate {

	private final WebClient webClient;
	private final String baseUrl;
	private final String apiPath;

	/**
	 * Creates the delegate with the configured API base URL and path.
	 *
	 * @param webClientBuilder web client builder
	 * @param baseUrl base URL for the API
	 * @param apiPath API path for offers
	 */
	public GetCheapestOfferDelegate(
			WebClient.Builder webClientBuilder,
			@Value("${camunda7demo.auto-credit.base-url}") String baseUrl,
			@Value("${camunda7demo.auto-credit.api-path}") String apiPath) {
		this.webClient = webClientBuilder.baseUrl(baseUrl).build();
		this.baseUrl = baseUrl;
		this.apiPath = apiPath;
	}

	/**
	 * Calls the external API and stores the cheapest offer as a process variable.
	 *
	 * @param execution Camunda delegate execution
	 */
	@Override
	public void execute(DelegateExecution execution) {

		BigDecimal propertyValue = (BigDecimal) execution.getVariable("propertyValue");
		BigDecimal equity = (BigDecimal) execution.getVariable("equity");

		BigDecimal kreditbetrag = propertyValue.subtract(equity);

		OffersRequest request = OffersRequest.builder()
				.kunde(OffersRequest.Kunde.builder()
						.anstellungsVerhaeltnis("ANGESTELLTER")
						.build())
				.immobilie(OffersRequest.Immobilie.builder()
						.kaufPreis(propertyValue)
						.postleitzahl("81925")
						.build())
				.finanzierung(OffersRequest.Finanzierung.builder()
						.finanzierungszweck("ANSCHLUSSFINANZIERUNG")
						.tilgungsSatz(new BigDecimal("2"))
						.zinsBindungInJahren(10)
						.kreditbetrag(kreditbetrag)
						.auszahlungsTermin(LocalDate.now())
						.build())
				.build();

		OffersResponse response;
		try {
			response = webClient
					.post()
					.uri(apiPath)
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON)
					.bodyValue(request)
					.retrieve()
					.bodyToMono(OffersResponse.class)
					.block();
		} catch (Exception e) {
			throw new RuntimeException("API not available: %s".formatted(baseUrl), e
			);
		}

		if (response == null || response.getAngebote() == null || response.getAngebote().isEmpty()) {
			throw new IllegalStateException("No offers returned from %s".formatted(baseUrl));
		}

		OffersResponse.Angebot cheapestOffer =
				response.getAngebote().stream()
						.filter(a -> a.getKondition() != null && a.getKondition().getMonatlicheRate() != null)
						.min(Comparator.comparing(a -> a.getKondition().getMonatlicheRate()))
						.orElseThrow();

		log.info(
				"Cheapest offer: vermittler={}, anbieter={}, rate={}",
				cheapestOffer.getVermittler(),
				cheapestOffer.getAnbieter() != null ? cheapestOffer.getAnbieter().getName() : "n/a",
				cheapestOffer.getKondition().getMonatlicheRate());

		execution.setVariable(
				"cheapestOffer",
				Variables
						.objectValue(cheapestOffer)
						.serializationDataFormat(Variables.SerializationDataFormats.JAVA)
						.create());
	}
}
