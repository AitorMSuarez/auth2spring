package com.ams.authorizationserver.dto;

import java.util.List;

//java 14/17
public record CreateAppUserDto(String username, String password, List<String> roles) {
}