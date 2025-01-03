package com.example.tpjad_project_backend.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class AuthenticationController {

    private final AuthenticationService service;

    @RequestMapping(value = "/register",method = RequestMethod.POST)
    public ResponseEntity<AuthenticationResponse> register(@RequestBody RegisterRequest request)
    {
        return ResponseEntity.ok(service.register(request));
    }

    @RequestMapping(value = "/login",method = RequestMethod.POST)
    public ResponseEntity<AuthenticationResponse> login(@RequestBody LoginRequest request)
    {
        return ResponseEntity.ok(service.login(request));
    }
}
