package de.aschwartz.camunda8demo.realestatefinancing.camunda.worker;

import de.aschwartz.camunda8demo.realestatefinancing.model.OffersRequest;
import de.aschwartz.camunda8demo.realestatefinancing.model.OffersResponse;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Map;

/**
 * Retrieves the cheapest offer from an external auto-credit API.
 */
@Component
@Slf4j
public class GetCheapestOfferWorker {

	private final WebClient webClient;
	private final String baseUrl;
	private final String apiPath;

	public GetCheapestOfferWorker(
			WebClient.Builder webClientBuilder,
			@Value("${camunda8demo.auto-credit.base-url}") String baseUrl,
			@Value("${camunda8demo.auto-credit.api-path}") String apiPath) {
		this.webClient = webClientBuilder.baseUrl(baseUrl).build();
		this.baseUrl = baseUrl;
		this.apiPath = apiPath;
	}

	@JobWorker(type = "get-cheapest-offer", timeout = 120_000)
	public Map<String, Object> handle(Map<String, Object> variables) {
		BigDecimal propertyValue = de.aschwartz.camunda8demo.realestatefinancing.camunda.worker.VariableMapper.getBigDecimal(variables, "propertyValue");
		BigDecimal equity = de.aschwartz.camunda8demo.realestatefinancing.camunda.worker.VariableMapper.getBigDecimal(variables, "equity");

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
			throw new RuntimeException("API not available: %s".formatted(baseUrl), e);
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

		return Map.of("cheapestOffer", cheapestOffer);
	}
}
