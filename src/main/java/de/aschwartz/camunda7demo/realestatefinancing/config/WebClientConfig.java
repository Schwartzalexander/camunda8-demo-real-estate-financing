package de.aschwartz.camunda7demo.realestatefinancing.config;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

/**
 * Configuration for a WebClient builder that trusts all SSL certificates.
 */
@Configuration
public class WebClientConfig {

	/**
	 * Builds a {@link WebClient.Builder} configured with an insecure SSL context.
	 *
	 * @return configured WebClient builder
	 */
	@Bean
	public WebClient.Builder webClientBuilder() {
		try {
			SslContext sslContext = SslContextBuilder
					.forClient()
					.trustManager(InsecureTrustManagerFactory.INSTANCE)
					.build();

			HttpClient httpClient = HttpClient.create()
					.secure(spec -> spec.sslContext(sslContext));

			return WebClient.builder()
					.clientConnector(new ReactorClientHttpConnector(httpClient));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
