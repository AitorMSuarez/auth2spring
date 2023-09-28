package com.ams.authorizationserver.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ams.authorizationserver.dto.CreateAppUserDto;
import com.ams.authorizationserver.dto.MessageDto;
import com.ams.authorizationserver.service.AppUserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AppUserService appUserService;

	@PostMapping("/create")
	public ResponseEntity<MessageDto> createUser(@RequestBody CreateAppUserDto dto) {
		return ResponseEntity.status(HttpStatus.CREATED).body(appUserService.createUser(dto));
	}
}