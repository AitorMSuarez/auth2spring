package com.ams.authorizationserver.entity;

import org.springframework.security.core.GrantedAuthority;

import com.ams.authorizationserver.constant.RoleName;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role implements GrantedAuthority {
	private static final long serialVersionUID = -4451344554028816705L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Enumerated(EnumType.STRING)
	private RoleName role;

	@Override
	public String getAuthority() {
		return role.name();
	}
}
