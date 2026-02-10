package com.cinebook.backend.services.interfaces;

import com.cinebook.backend.dtos.auth.AuthResponseDTO;
import com.cinebook.backend.dtos.auth.LoginRequestDTO;
import com.cinebook.backend.dtos.auth.RegisterRequestDTO;

public interface IAuthService {
    AuthResponseDTO register(RegisterRequestDTO request);
    AuthResponseDTO login(LoginRequestDTO request);
}

