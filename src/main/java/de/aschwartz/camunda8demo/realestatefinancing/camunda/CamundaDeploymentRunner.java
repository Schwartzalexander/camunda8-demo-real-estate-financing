package de.aschwartz.camunda8demo.realestatefinancing.camunda;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.command.DeployResourceCommandStep1;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

@Component
public class CamundaDeploymentRunner implements ApplicationRunner {
	private static final Logger log = LoggerFactory.getLogger(CamundaDeploymentRunner.class);
	private static final List<String> RESOURCE_PATTERNS = List.of(
			"classpath*:processes/**/*.bpmn",
			"classpath*:processes/**/*.dmn"
	);

	private final ZeebeClient zeebeClient;
	private final ResourcePatternResolver resourcePatternResolver;

	public CamundaDeploymentRunner(ZeebeClient zeebeClient, ResourcePatternResolver resourcePatternResolver) {
		this.zeebeClient = zeebeClient;
		this.resourcePatternResolver = resourcePatternResolver;
	}

	@Override
	public void run(ApplicationArguments args) throws IOException {
		List<Resource> resources = new ArrayList<>();
		for (String pattern : RESOURCE_PATTERNS) {
			resources.addAll(Arrays.asList(resourcePatternResolver.getResources(pattern)));
		}

		if (resources.isEmpty()) {
			log.warn("No BPMN/DMN resources found for deployment.");
			return;
		}

		DeployResourceCommandStep1 deployCommand = zeebeClient.newDeployResourceCommand();
		for (Resource resource : resources) {
			String filename = resource.getFilename();
			if (filename == null) {
				filename = resource.getDescription();
			}
			deployCommand.addResourceBytes(resource.getInputStream().readAllBytes(), filename);
		}

		deployCommand.send().join();
		log.info("Deployed {} BPMN/DMN resources to Camunda 8.", resources.size());
	}
}
