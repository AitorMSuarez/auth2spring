package com.ams.authorizationserver.entity;

import org.springframework.security.oauth2.core.user.OAuth2User;

import jakarta.persistence.Entity;
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
@Data
@Builder
public class GoogleUser {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private String email;
	private String name;
	private String givenName;
	private String familyName;
	private String pictureUrl;

	public static GoogleUser fromOauth2User(OAuth2User user) {
		GoogleUser googleUser = GoogleUser.builder().email(user.getName())
				.name(user.getAttributes().get("name").toString())
				.givenName(user.getAttributes().get("given_name").toString())
				.familyName(user.getAttributes().get("family_name").toString())
				.pictureUrl(user.getAttributes().get("picture").toString()).build();
		return googleUser;
	}

}