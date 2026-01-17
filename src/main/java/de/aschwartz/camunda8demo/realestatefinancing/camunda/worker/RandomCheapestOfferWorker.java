package de.aschwartz.camunda8demo.realestatefinancing.camunda.worker;

import de.aschwartz.camunda8demo.realestatefinancing.model.OffersResponse;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Generates a randomized cheapest offer for the DMN credit flow.
 */
@Component
public class RandomCheapestOfferWorker {

	@JobWorker(type = "generate-cheapest-offer")
	public Map<String, Object> handle() {
		return Map.of("cheapestOffer", createRandomOffer());
	}

	private OffersResponse.Angebot createRandomOffer() {
		ThreadLocalRandom rnd = ThreadLocalRandom.current();

		BigDecimal sollZins = bd(rnd.nextDouble(1.5, 6.5), 2);
		BigDecimal effektivZins = sollZins.add(bd(rnd.nextDouble(0.05, 0.6), 2));

		int zinsbindung = rnd.nextInt(5, 21);
		int gesamtlaufzeit = rnd.nextInt(120, 421);

		BigDecimal kaufpreis = bd(rnd.nextDouble(150_000, 1_500_000), 0);
		BigDecimal darlehensbetrag = bd(kaufpreis.doubleValue() * rnd.nextDouble(0.5, 0.95), 0);
		BigDecimal monatlicheRate = bd(rnd.nextDouble(500, 4500), 2);
		BigDecimal anfaenglicheTilgung = bd(rnd.nextDouble(1.0, 6.0), 2);

		BigDecimal gesamtkosten = bd(darlehensbetrag.doubleValue() * rnd.nextDouble(1.05, 1.6), 0);
		BigDecimal grundbuchkosten = bd(kaufpreis.doubleValue() * rnd.nextDouble(0.005, 0.02), 0);

		BigDecimal beleihungsauslauf = bd(darlehensbetrag.doubleValue() / kaufpreis.doubleValue() * 100.0, 2);

		OffersResponse.Angebot.Kondition kondition = OffersResponse.Angebot.Kondition.builder()
				.sollZins(sollZins)
				.effektivZins(effektivZins)
				.monatlicheRate(monatlicheRate)
				.anfaenglicheTilgung(anfaenglicheTilgung)
				.zinsbindungInJahren(zinsbindung)
				.gesamtlaufzeitInMonaten(gesamtlaufzeit)
				.beleihungsauslauf(beleihungsauslauf)
				.kaufpreis(kaufpreis)
				.darlehensbetrag(darlehensbetrag)
				.gesamtkosten(gesamtkosten)
				.grundbuchkosten(grundbuchkosten)
				.zinskostenAmEndeDerZinsbindung(bd(darlehensbetrag.doubleValue() * rnd.nextDouble(0.05, 0.25), 0))
				.restschuldAmEndeDerZinsbindung(bd(darlehensbetrag.doubleValue() * rnd.nextDouble(0.4, 0.9), 0))
				.build();

		String providerName = pickOne(rnd, List.of(
				"Münchner Bank", "Bavaria Finance", "Nordlicht Kredit", "AlpenHyp", "MainCapital",
				"RheinInvest", "Hansekredit", "IsarFunding"
		));

		OffersResponse.Angebot.Anbieter.Anschrift anschrift = OffersResponse.Angebot.Anbieter.Anschrift.builder()
				.ort(pickOne(rnd, List.of("München", "Frankfurt", "Hamburg", "Berlin", "Stuttgart", "Köln")))
				.plz(String.valueOf(rnd.nextInt(10000, 99999)))
				.strasseUndHausnummer(pickOne(rnd, List.of("Hauptstraße", "Bahnhofstraße", "Parkallee", "Isartorplatz", "Königsweg"))
						+ " " + rnd.nextInt(1, 200))
				.build();

		OffersResponse.Angebot.Anbieter anbieter = OffersResponse.Angebot.Anbieter.builder()
				.id(UUID.randomUUID().toString())
				.name(providerName)
				.kurzbezeichnung(providerName.replaceAll("\\s+", "").substring(0, Math.min(10, providerName.replaceAll("\\s+", "").length())))
				.ksId("KS-" + rnd.nextInt(100000, 999999))
				.anschrift(anschrift)
				.beratungVorOrt(rnd.nextBoolean())
				.unterstuetztKfw(rnd.nextBoolean())
				.unterstuetztSondertilgung(pickOne(rnd, List.of("Ja", "Nein", "Nach Vereinbarung")))
				.anbietertyp(pickOne(rnd, List.of("Bank", "Bausparkasse", "Vermittler")))
				.informationstext("Konditionen abhängig von Bonität und Objekt.")
				.anzahlMitarbeiter(rnd.nextInt(50, 5000))
				.anzahlNiederlassungen(rnd.nextInt(1, 200))
				.regional(rnd.nextBoolean())
				.urlKlickout("https://example.com/clickout/" + UUID.randomUUID())
				.leadId("LEAD-" + rnd.nextInt(100000, 999999))
				.leadmanagementId("LM-" + rnd.nextInt(100000, 999999))
				.build();

		OffersResponse.Angebot.Produktinformation produktinformation = OffersResponse.Angebot.Produktinformation.builder()
				.minimalerDarlehensbetrag(bd(rnd.nextDouble(25_000, 100_000), 0))
				.maximalerDarlehensbetrag(bd(rnd.nextDouble(500_000, 3_000_000), 0))
				.bearbeitungsgebuehr(bd(rnd.nextDouble(0, 1500), 2))
				.bereitstellungsfreieZeitText(pickOne(rnd, List.of("3 Monate", "6 Monate", "12 Monate")))
				.sondertilgung(pickOne(rnd, List.of("5% p.a.", "10% p.a.", "nach Absprache")))
				.tilgungsaussetzungText(pickOne(rnd, List.of("möglich", "nicht möglich", "teilweise möglich")))
				.kfwDarlehenText(pickOne(rnd, List.of("unterstützt", "nicht unterstützt")))
				.produktId(rnd.nextInt(1000, 9999))
				.build();

		return OffersResponse.Angebot.builder()
				.vermittler(pickOne(rnd, List.of("Vergleich.de", "TopKredit24", "FinanzScout", "HypoCheck", "RateFinder")))
				.kondition(kondition)
				.anbieter(anbieter)
				.produktinformation(produktinformation)
				.actionUrl("https://example.com/offer/" + UUID.randomUUID())
				.build();
	}

	private static BigDecimal bd(double value, int scale) {
		return BigDecimal.valueOf(value).setScale(scale, RoundingMode.HALF_UP);
	}

	private static <T> T pickOne(ThreadLocalRandom rnd, List<T> values) {
		return values.get(rnd.nextInt(values.size()));
	}
}
