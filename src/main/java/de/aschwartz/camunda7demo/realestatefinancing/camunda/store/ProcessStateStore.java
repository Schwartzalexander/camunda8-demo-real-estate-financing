package de.aschwartz.camunda7demo.realestatefinancing.camunda.store;

import de.aschwartz.camunda7demo.realestatefinancing.model.Offer;
import de.aschwartz.camunda7demo.realestatefinancing.model.ReviewResult;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple in-memory store for demo process results that are needed by the UI.
 */
@Component
public class ProcessStateStore {

	private final Map<String, List<Offer>> offersByCorrelationId = new ConcurrentHashMap<>();
	private final Map<String, ReviewResult> reviewByCorrelationId = new ConcurrentHashMap<>();

	/**
	 * Stores offers for a process instance.
	 *
	 * @param correlationId correlation id
	 * @param offers offers list
	 */
	public void storeOffers(String correlationId, List<Offer> offers) {
		offersByCorrelationId.put(correlationId, offers);
	}

	/**
	 * Retrieves offers for a process instance.
	 *
	 * @param correlationId correlation id
	 * @return offers list
	 */
	public Optional<List<Offer>> getOffers(String correlationId) {
		return Optional.ofNullable(offersByCorrelationId.get(correlationId));
	}

	/**
	 * Stores review result for a process instance.
	 *
	 * @param correlationId correlation id
	 * @param result review result
	 */
	public void storeReviewResult(String correlationId, ReviewResult result) {
		reviewByCorrelationId.put(correlationId, result);
	}

	/**
	 * Retrieves review result for a process instance.
	 *
	 * @param correlationId correlation id
	 * @return review result
	 */
	public Optional<ReviewResult> getReviewResult(String correlationId) {
		return Optional.ofNullable(reviewByCorrelationId.get(correlationId));
	}

	/**
	 * Waits for offers to become available with a timeout.
	 *
	 * @param correlationId correlation id
	 * @param timeout timeout duration
	 * @return offers list if available
	 */
	public Optional<List<Offer>> awaitOffers(String correlationId, Duration timeout) {
		Instant deadline = Instant.now().plus(timeout);
		while (Instant.now().isBefore(deadline)) {
			Optional<List<Offer>> offers = getOffers(correlationId);
			if (offers.isPresent()) {
				return offers;
			}
			sleep();
		}
		return Optional.empty();
	}

	/**
	 * Waits for review result to become available with a timeout.
	 *
	 * @param correlationId correlation id
	 * @param timeout timeout duration
	 * @return review result if available
	 */
	public Optional<ReviewResult> awaitReviewResult(String correlationId, Duration timeout) {
		Instant deadline = Instant.now().plus(timeout);
		while (Instant.now().isBefore(deadline)) {
			Optional<ReviewResult> result = getReviewResult(correlationId);
			if (result.isPresent()) {
				return result;
			}
			sleep();
		}
		return Optional.empty();
	}

	private void sleep() {
		try {
			Thread.sleep(150);
		} catch (InterruptedException ie) {
			Thread.currentThread().interrupt();
		}
	}
}
