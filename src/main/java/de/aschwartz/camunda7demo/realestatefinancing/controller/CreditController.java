package de.aschwartz.camunda7demo.realestatefinancing.controller;

import de.aschwartz.camunda7demo.realestatefinancing.camunda.store.ProcessStateStore;
import de.aschwartz.camunda7demo.realestatefinancing.logic.CreditInteractionService;
import de.aschwartz.camunda7demo.realestatefinancing.logic.CreateProcessService;
import de.aschwartz.camunda7demo.realestatefinancing.model.EnterCreditParametersResponse;
import de.aschwartz.camunda7demo.realestatefinancing.model.Offer;
import de.aschwartz.camunda7demo.realestatefinancing.model.SubmitApplicationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

/**
 * MVC controller for the credit comparison flow.
 */
@Controller
@RequestMapping("/credit")
@Slf4j
public class CreditController {

	private final CreateProcessService createProcessService;
	private final CreditInteractionService creditInteractionService;
	private final ProcessStateStore processStateStore;

	/**
	 * Creates the controller with required services.
	 *
	 * @param createProcessService process starter service
	 * @param creditInteractionService service for publishing user events
	 * @param processStateStore process state store
	 */
	public CreditController(
			CreateProcessService createProcessService,
			CreditInteractionService creditInteractionService,
			ProcessStateStore processStateStore
	) {
		this.createProcessService = createProcessService;
		this.creditInteractionService = creditInteractionService;
		this.processStateStore = processStateStore;
	}

	/**
	 * Renders the credit comparison page with defaults.
	 *
	 * @param model Spring MVC model
	 * @return view name
	 */
	@GetMapping
	public String page(Model model) {
		Object monthlyNetIncome = model.getAttribute("monthlyNetIncome");
		model.addAttribute("monthlyNetIncome", monthlyNetIncome != null ? monthlyNetIncome : "2500");
		Object propertyValue = model.getAttribute("propertyValue");
		model.addAttribute("propertyValue", propertyValue != null ? propertyValue : "100000");
		Object equity = model.getAttribute("equity");
		model.addAttribute("equity", equity != null ? equity : "10000");
		return "credit";
	}

	/**
	 * Starts the comparison sub-process and fetches offers.
	 *
	 * @param monthlyNetIncome monthly net income
	 * @param propertyValue property value
	 * @param equity equity amount
	 * @param model Spring MVC model
	 * @return view name
	 */
	@PostMapping("/compare")
	public String compare(
			@RequestParam BigDecimal monthlyNetIncome,
			@RequestParam BigDecimal propertyValue,
			@RequestParam BigDecimal equity,
			Model model
	) {
		model.addAttribute("monthlyNetIncome", monthlyNetIncome);
		model.addAttribute("propertyValue", propertyValue);
		model.addAttribute("equity", equity);

		try {
			String processInstanceId = createProcessService.createProcess(
					"RealEstateCreditApplication",
					java.util.Map.of(
							"monthlyNetIncome", monthlyNetIncome,
							"propertyValue", propertyValue,
							"equity", equity
					)
			);
			EnterCreditParametersResponse response = processStateStore
					.awaitOffers(processInstanceId, Duration.ofSeconds(5))
					.map(EnterCreditParametersResponse::new)
					.orElseThrow(() -> new IllegalStateException("No offers received from comparison process."));

			List<Offer> offers = response.getOffers();
			model.addAttribute("offers", offers);
			model.addAttribute("processInstanceId", processInstanceId);
		} catch (Exception e) {

			model.addAttribute("statusType", "danger");
			model.addAttribute("statusTitle", "Info");
			model.addAttribute("statusMessage",
					"Could not read creditOffers from process. " + e.getMessage() + "");
			log.error(e.getMessage(), e);
		}
		return "credit";
	}

	/**
	 * Selects a bank and submits the application.
	 *
	 * @param bankName selected bank name
	 * @param processInstanceId Camunda process instance id
	 * @param model Spring MVC model
	 * @return view name
	 */
	@PostMapping("/select")
	public String selectBank(
			@RequestParam String bankName,
			@RequestParam BigDecimal monthlyNetIncome,
			@RequestParam BigDecimal propertyValue,
			@RequestParam BigDecimal equity,
			@RequestParam String processInstanceId,
			Model model
	) {
		creditInteractionService.publishBankSelected(processInstanceId, bankName);
		model.addAttribute("processInstanceId", processInstanceId);
		model.addAttribute("monthlyNetIncome", monthlyNetIncome);
		model.addAttribute("propertyValue", propertyValue);
		model.addAttribute("equity", equity);
		model.addAttribute("selectedBank", bankName);

		return "credit";
	}

	/**
	 * Submits the credit application after a bank was selected.
	 *
	 * @param processInstanceId Camunda process instance id
	 * @param model Spring MVC model
	 * @return view name
	 */
	@PostMapping("/submit")
	public String submit(@RequestParam String processInstanceId, Model model) {
		creditInteractionService.publishApplicationSubmitted(processInstanceId);
		SubmitApplicationResponse submitApplicationResponse = processStateStore
				.awaitReviewResult(processInstanceId, Duration.ofSeconds(5))
				.map(result -> new SubmitApplicationResponse(result.isAccepted(), result.getContractNumber(), result.getRejectionReason()))
				.orElseGet(() -> new SubmitApplicationResponse(false, null, "No review result received."));

		model.addAttribute("processInstanceId", processInstanceId);
		model.addAttribute("applicationAccepted", submitApplicationResponse.getAccepted());
		model.addAttribute("contractNumber", submitApplicationResponse.getContractNumber());
		model.addAttribute("rejectionReason", submitApplicationResponse.getRejectionReason());
		model.addAttribute("showSign", submitApplicationResponse.getAccepted());

		return "credit";
	}

	/**
	 * Completes the sign-contract user task.
	 *
	 * @param processInstanceId Camunda process instance id
	 * @param model Spring MVC model
	 * @return view name
	 */
	@PostMapping("/sign")
	public String sign(@RequestParam String processInstanceId, Model model) {
		try {
			creditInteractionService.publishContractSigned(processInstanceId);
			model.addAttribute("statusType", "success");
			model.addAttribute("statusTitle", "Done");
			model.addAttribute("statusMessage", "Contract signed. Credit contract concluded.");
			model.addAttribute("showSign", false);
		} catch (Exception e) {
			model.addAttribute("statusType", "danger");
			model.addAttribute("statusTitle", "Not found");
			model.addAttribute("statusMessage", e.getMessage());
			model.addAttribute("showSign", false);
		}

		return "credit";
	}

}
