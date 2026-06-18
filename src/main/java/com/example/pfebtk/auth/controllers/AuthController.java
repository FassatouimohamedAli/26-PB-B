package com.example.pfebtk.auth.controllers;

import com.example.pfebtk.auth.dto.Authreq;
import com.example.pfebtk.auth.dto.Authresp;
import com.example.pfebtk.auth.dto.Registerreq;
import com.example.pfebtk.auth.entity.PasswordHistory;
import com.example.pfebtk.auth.entity.User;
import com.example.pfebtk.auth.repository.PasswordHistoryRepo;
import com.example.pfebtk.auth.repository.UserRepo;
import com.example.pfebtk.auth.service.AuthService;
import com.example.pfebtk.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
//@CrossOrigin
public class AuthController {
    @Autowired
    private AuthService authService;
    @Autowired
    private JwtUtil jwtUtil;
@Autowired
private UserRepo userRepo;
@Autowired
private PasswordHistoryRepo passwordHistoryRepo;

    @PostMapping("/login")
    public ResponseEntity<Authresp> login(@RequestBody Authreq request) {
        Authresp response = authService.authenticate(request);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/refresh")
    public Authresp refresh(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");

        if (!jwtUtil.validateToken(refreshToken) || !jwtUtil.isRefreshToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }
        String unix = jwtUtil.extractUnix(refreshToken);
        User u = userRepo.findByUnix(unix).get();
        if(!u.isEnabled()){
            return null;
        }



        String newAccesToken = jwtUtil.generateToken(unix);
        boolean passwordMustChange = false;
        Optional<PasswordHistory> phop = passwordHistoryRepo.findTopByUserOrderByCreatedAtDesc(u);
        if (phop.isPresent()) {
            PasswordHistory ph = phop.get();
            if (ph.isTemp() && !ph.hasChanged()) {
                passwordMustChange = true;
            }
        }
        return  new Authresp(u.getPuti(),newAccesToken,refreshToken,passwordMustChange);
    }




}
