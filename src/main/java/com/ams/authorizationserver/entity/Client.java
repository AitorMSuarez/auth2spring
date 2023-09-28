package com.ams.authorizationserver.entity;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Set;

import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
// TODO poner las relaciones bien para que se cree la tabla bien o directamente desde el modelo.
public class Client {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private String clientId;
	private String clientSecret;
	@ElementCollection(fetch = FetchType.EAGER)
	private Set<ClientAuthenticationMethod> authenticationMethods;
	@ElementCollection(fetch = FetchType.EAGER)
	private Set<AuthorizationGrantType> authorizationGrantTypes;
	@ElementCollection(fetch = FetchType.EAGER)
	// TODO manytoone apuntando a esta clase.
	private Set<String> redirectUris;
	@ElementCollection(fetch = FetchType.EAGER)
	private Set<String> scopes;
	private boolean requireProofKey;

	// Necesario para oauth2.
	public static RegisteredClient toRegisteredClient(Client client) {
		RegisteredClient.Builder builder = RegisteredClient.withId(client.getClientId()).clientId(client.getClientId())
				.clientSecret(client.getClientSecret())
				.clientIdIssuedAt(ZonedDateTime.now(ZoneId.systemDefault()).toInstant())
				.clientAuthenticationMethods(am -> am.addAll(client.getAuthenticationMethods()))
				.authorizationGrantTypes(agt -> agt.addAll(client.getAuthorizationGrantTypes()))
				.redirectUris(ru -> ru.addAll(client.getRedirectUris())).scopes(sc -> sc.addAll(client.getScopes()))
				.clientSettings(ClientSettings.builder().requireProofKey(client.isRequireProofKey()).build());
		return builder.build();
	}
}
