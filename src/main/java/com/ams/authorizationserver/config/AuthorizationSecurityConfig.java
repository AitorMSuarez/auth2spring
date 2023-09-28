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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

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

	private final PasswordEncoder passwordEncoder;

	// Configura el filtro de seguridad para el servidor de autorización OAuth2
	@Bean
	@Order(1)
	public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
		OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
		http.getConfigurer(OAuth2AuthorizationServerConfigurer.class).oidc(Customizer.withDefaults()); // Habilitar
																										// OpenID
																										// Connect 1.0
		http.exceptionHandling(
				exception -> exception.authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login")))
				.oauth2ResourceServer((oauth2) -> oauth2.jwt(Customizer.withDefaults())); // Aceptar tokens de acceso
																							// para User Info y/o Client
																							// Registration
		return http.build();
	}

	// Configura el filtro de seguridad predeterminado para la aplicación
	@Bean
	@Order(2)
	public SecurityFilterChain webSecurityFilterChain(HttpSecurity http) throws Exception {

		// Se le dice que como requisito tiene que estar autenticado.
		http.authorizeHttpRequests(auth -> auth.requestMatchers("/auth/**").permitAll().anyRequest().authenticated())
				.formLogin(Customizer.withDefaults());
		http.csrf().ignoringRequestMatchers("/auth/**");
		return http.build();
	}

	// Configura los clientes registrados y sus detalles, como ID de cliente,
	// secreto, alcance, etc.
	@Bean
	public RegisteredClientRepository registeredClientRepository() {
		RegisteredClient registeredOidcClient = RegisteredClient.withId(UUID.randomUUID().toString()).clientId("client") // oidc-client
				// se autentica el cliente y el usuario.
				.clientSecret(passwordEncoder.encode("secret"))
				// método de autenticación
				.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
				// RESPONSE TYPE en OAUTHDEBUGGER
				.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
				.authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
				.authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
//                web para probarlo
				.redirectUri("https://oauthdebugger.com/debug")
//                web para implementar de ejemplo
//				.redirectUri("http://127.0.0.1:8080/login/oauth2/code/oidc-client")
//                lo mismo para el logout
//				.postLogoutRedirectUri("http://127.0.0.1:8080/")
				.scope(OidcScopes.OPENID)

//				.scope(OidcScopes.OPENID).scope(OidcScopes.PROFILE)
				.clientSettings(clientSettings()).build();
// TODO
		return new InMemoryRegisteredClientRepository(registeredOidcClient);
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

	private ClientSettings clientSettings() {
		return ClientSettings.builder().requireAuthorizationConsent(true).build();
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
