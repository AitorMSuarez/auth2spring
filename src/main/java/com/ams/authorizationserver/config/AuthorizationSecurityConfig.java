package com.ams.authorizationserver.config;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.HttpSessionEventPublisher;

import com.ams.authorizationserver.federation.FederatedIdentityConfigurer;
import com.ams.authorizationserver.federation.UserRepositoryOAuth2UserHandler;
import com.ams.authorizationserver.repository.GoogleUserRepository;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
@DependsOn("passwordEncoder")
@RequiredArgsConstructor
public class AuthorizationSecurityConfig {

	private final GoogleUserRepository googleUserRepository;


	// Configura el filtro de seguridad para el servidor de autorización OAuth2
	@Bean
	@Order(1)
	public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
		OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
		http.getConfigurer(OAuth2AuthorizationServerConfigurer.class).oidc(Customizer.withDefaults()); // Enable OpenID
																										// Connect 1.0
		http.oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt);
		http.apply(new FederatedIdentityConfigurer());
		return http.build();
	}

	// Configura el filtro de seguridad predeterminado para la aplicación
	@Bean
	@Order(2)
	public SecurityFilterChain webSecurityFilterChain(HttpSecurity http) throws Exception {

		   FederatedIdentityConfigurer federatedIdentityConfigurer = new FederatedIdentityConfigurer()
	                .oauth2UserHandler(new UserRepositoryOAuth2UserHandler(googleUserRepository));
		   
		http.authorizeHttpRequests(authorize -> authorize.requestMatchers("/auth/**", "/client/**", "/login")
				.permitAll().anyRequest().authenticated()).formLogin(Customizer.withDefaults())
				.apply(federatedIdentityConfigurer);

		http.csrf(csrf -> csrf.ignoringRequestMatchers("/auth/**", "/client/**"));

		return http.build();
	}

	@Bean
	public OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer() {
		return context -> {
			Authentication principal = context.getPrincipal();
			if ("id_token".equals(context.getTokenType().getValue())) {
				context.getClaims().claim("token_type", "id token");
			}
			if ("access_token".equals(context.getTokenType().getValue())) {
				context.getClaims().claim("token_type", "access token");
				Set<String> roles = principal.getAuthorities().stream().map(GrantedAuthority::getAuthority)
						.collect(Collectors.toSet());
				context.getClaims().claim("roles", roles).claim("Authorized Company", "Gertek");
			}
		};
	}

	@Bean
	public SessionRegistry sessionRegistry() {
		return new SessionRegistryImpl();
	}

	@Bean
	public HttpSessionEventPublisher httpSessionEventPublisher() {
		return new HttpSessionEventPublisher();
	}

	@Bean
	public OAuth2AuthorizationService authorizationService() {
		return new InMemoryOAuth2AuthorizationService();
	}

	@Bean
	public OAuth2AuthorizationConsentService authorizationConsentService() {
		return new InMemoryOAuth2AuthorizationConsentService();
	}

	// Configura las propiedades del servidor de autorización OAuth2
	public AuthorizationServerSettings authorizationServerSettings() {
//		issuer la URL que el Servidor de Autorización utiliza como su Identificador de Emisor
		return AuthorizationServerSettings.builder().issuer("http://localhost:9000").build();
	}

	// Configura un decodificador JWT para validar los tokens de acceso
	@Bean
	public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
		return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
	}

	// Configura las claves públicas y privadas utilizadas para firmar y verificar
	// tokens JWT
	@Bean
	public JWKSource<SecurityContext> jwkSource() {
		RSAKey rsaKey = generateRSAKey();
		JWKSet jwkSet = new JWKSet(rsaKey);
		return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
	}

	private RSAKey generateRSAKey() {
		KeyPair keyPair = generateKeyPair();
		RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
		RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
		return new RSAKey.Builder(publicKey).privateKey(privateKey).keyID(UUID.randomUUID().toString()).build();
	}

	private KeyPair generateKeyPair() {
		KeyPair keyPair;
		try {
			KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
			keyPairGenerator.initialize(2048);
			keyPair = keyPairGenerator.generateKeyPair();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e.getMessage());
		}

		return keyPair;

	}
}
