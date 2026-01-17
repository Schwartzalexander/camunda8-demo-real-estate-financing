package de.aschwartz.camunda8demo.realestatefinancing.camunda;

import io.camunda.zeebe.client.ZeebeClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class CamundaDeploymentRunner implements ApplicationRunner {

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

		// Step1 -> Step2 happens after the first addResource...
		Resource first = resources.get(0);
		var cmd = zeebeClient.newDeployResourceCommand()
				.addResourceBytes(first.getInputStream().readAllBytes(), filenameOf(first));

		for (int i = 1; i < resources.size(); i++) {
			Resource r = resources.get(i);
			cmd = cmd.addResourceBytes(r.getInputStream().readAllBytes(), filenameOf(r));
		}

		cmd.send().join();
		log.info("Deployed {} BPMN/DMN resources.", resources.size());
	}

	private static String filenameOf(Resource resource) throws IOException {
		String filename = resource.getFilename();
		return (filename != null) ? filename : resource.getDescription();
	}
}
