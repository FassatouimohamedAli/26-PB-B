package com.example.pfebtk.auth.service;

import com.example.pfebtk.auth.dto.Authreq;
import com.example.pfebtk.auth.dto.Authresp;
import com.example.pfebtk.auth.entity.ChangeType;
import com.example.pfebtk.auth.dto.Registerreq;
import com.example.pfebtk.auth.entity.PasswordHistory;
import com.example.pfebtk.auth.entity.User;
import com.example.pfebtk.auth.repository.PasswordHistoryRepo;
import com.example.pfebtk.auth.repository.UserRepo;
import com.example.pfebtk.auth.service.email.EmailService;
import com.example.pfebtk.utils.JwtUtil;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import org.springframework.security.core.AuthenticationException;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private UserRepo userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private PasswordHistoryRepo passwordHistoryRepo ;

    public Authresp authenticate(Authreq jwtRequest) {
        try {
            // Authentification via le gestionnaire d'authentification
            System.out.println("aaaaaaaaaaa");
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            jwtRequest.unix(),
                            jwtRequest.pwd()
                    )
            );

            //System.out.println("bbbb");
            //verifier si le compte sus ou pas
            // Générer le token

            String token = jwtUtil.generateToken(jwtRequest.unix());
            String refreshToken = jwtUtil.generateRefreshToken(jwtRequest.unix());

            UserDetails userDetails = customUserDetailsService.loadUserByUsername(jwtRequest.unix());

            // Déterminer le rôle de l'utilisateur
            String role = determineRole(userDetails);

// Récupérer le User complet depuis UserDetails
            User user = (User) userDetails;
            boolean passwordMustChange;

            if ("RESPM1".equals(role)) {
                passwordMustChange = false; // admin jamais besoin de changer
            }else {
                // Vérifier si le mot de passe temporaire existe et doit être changé
                Optional<PasswordHistory> lastPwd = passwordHistoryRepo.findTopByUserOrderByCreatedAtDesc(user);
                passwordMustChange = lastPwd.isPresent() && lastPwd.get().isTemp() && !lastPwd.get().hasChanged();

            }


            // Retourner la réponse avec le token et les informations de l'utilisateur
            return new Authresp(role, token,refreshToken, passwordMustChange);


        } catch (AuthenticationException e) {
            throw new RuntimeException("Authentication failed: " + e.getMessage(), e);
        }
    }

    private String determineRole(UserDetails userDetails) {
        if (userDetails instanceof User u) {
            return u.getPuti() ;
        }
        throw new RuntimeException("Impossible de déterminer le rôle de l'utilisateur.");
    }



    @Transactional
    public User register(Registerreq request) {
        // Vérifier si l'email existe déjà
        if (userRepository.existsByEmail(request.email())) {
            throw new RuntimeException("Email déjà utilisé.");
        }

        String generatedPassword = generateRandomPassword();
        // Créer un utilisateur
        User user = User.builder()
                .cuti(request.cuti())
                .lib(request.lib())
                .puti(request.puti())
                .email(request.email())
                .unix(request.unix())
                .age(request.age())
                .sus("n") // actif par défaut
                .pwd(passwordEncoder.encode(generatedPassword))
                .build();

        User savedUser =  userRepository.save(user);

        // ── toujours insert un nouveau record ADMIN ──────────────
        passwordHistoryRepo.save(
                PasswordHistory.builder()
                        .user(user)
                        .changeType(ChangeType.RESP)
                        .createdAt(LocalDateTime.now())
                        .changedAt(null) // en attente ⏳
                        .build()
        );

        // Envoyer email avec username + password
        emailService.sendCredentials(
                savedUser.getEmail(),
                savedUser.getUnix(),
                generatedPassword
        );
        return savedUser ;
    }

    public List<User> getAllEmp() {
        return userRepository.findByPuti("EMP"); // ✅
    }

    public User updateStatut(String  unix , String statut){
        if (userRepository.findByUnix(unix).isEmpty()) {
            throw new RuntimeException("utilisateur nexiste pas avec ce identifiant");
        }
        User userUp =userRepository.findByUnix(unix).get();

        userUp.setSus(statut);  // sus = n updated to o <=>
        return userRepository.save(userUp);

    }


    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();

        for (int i = 0; i < 10; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }

        return password.toString();
    }

    @Transactional
    public User generatePwdEmp(String unix) {
        User u = userRepository.findByUnix(unix)
                .orElseThrow(() -> new RuntimeException("utilisateur n'existe pas avec ce identifiant"));

        String generatePwd = generateRandomPassword();
        u.setPwd(passwordEncoder.encode(generatePwd));
        User userNp = userRepository.save(u);

        // ── toujours insert un nouveau record ADMIN ──────────────
        passwordHistoryRepo.save(
                PasswordHistory.builder()
                        .user(userNp)
                        .changeType(ChangeType.RESP)
                        .createdAt(LocalDateTime.now())
                        .changedAt(null) // en attente ⏳
                        .build()
        );

        emailService.sendCredentials_2(userNp.getEmail(), generatePwd);
        return userNp;
    }

}
