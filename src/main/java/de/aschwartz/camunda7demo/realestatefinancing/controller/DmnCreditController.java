package de.aschwartz.camunda7demo.realestatefinancing.controller;

import de.aschwartz.camunda7demo.realestatefinancing.camunda.usertask.UserTaskServiceEnterDmnCreditParameters;
import de.aschwartz.camunda7demo.realestatefinancing.logic.CreateProcessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

/**
 * MVC controller for the dmn-credit flow.
 */
@Controller
@RequestMapping("/dmncredit")
@Slf4j
public class DmnCreditController {

	private final CreateProcessService createProcessService;
	private final UserTaskServiceEnterDmnCreditParameters userTaskServiceEnterDmnCreditParameters;

	/**
	 * Creates the controller with required services.
	 *
	 * @param createProcessService                    process starter service
	 * @param userTaskServiceEnterDmnCreditParameters service for entering parameters
	 */
	public DmnCreditController(
			CreateProcessService createProcessService,
			UserTaskServiceEnterDmnCreditParameters userTaskServiceEnterDmnCreditParameters
	) {
		this.createProcessService = createProcessService;
		this.userTaskServiceEnterDmnCreditParameters = userTaskServiceEnterDmnCreditParameters;
	}

	/**
	 * Renders the dmn-credit page with defaults.
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
		return "dmncredit";
	}

	/**
	 * Starts the dmn-credit process.
	 *
	 * @param monthlyNetIncome monthly net income
	 * @param propertyValue    property value
	 * @param equity           equity amount
	 * @param model            Spring MVC model
	 * @return view name
	 */
	@PostMapping("/start")
	public String start(
			@RequestParam BigDecimal monthlyNetIncome,
			@RequestParam BigDecimal propertyValue,
			@RequestParam BigDecimal equity,
			Model model
	) {
		model.addAttribute("monthlyNetIncome", monthlyNetIncome);
		model.addAttribute("propertyValue", propertyValue);
		model.addAttribute("equity", equity);

		try {
			String processInstanceId = createProcessService.createProcess("RealEstateDmnCredit");
			userTaskServiceEnterDmnCreditParameters.enterCreditParameters(monthlyNetIncome, propertyValue, equity, processInstanceId);

			model.addAttribute("processInstanceId", processInstanceId);

			model.addAttribute("statusType", "success");
			model.addAttribute("statusTitle", "Success");
			model.addAttribute("statusMessage", "Process started successfully.");

		} catch (Exception e) {
			model.addAttribute("statusType", "danger");
			model.addAttribute("statusTitle", "Error");
			model.addAttribute("statusMessage", e.getMessage());
			log.error(e.getMessage(), e);
		}

		return "dmncredit";
	}
}
