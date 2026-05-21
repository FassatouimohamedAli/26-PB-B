package com.example.pfebtk.auth.service;

import com.example.pfebtk.auth.dto.MdpOReq;
import com.example.pfebtk.auth.entity.ChangeType;
import com.example.pfebtk.auth.dto.ChangepwdReq;
import com.example.pfebtk.auth.entity.PasswordHistory;
import com.example.pfebtk.auth.entity.User;
import com.example.pfebtk.auth.repository.PasswordHistoryRepo;
import com.example.pfebtk.auth.repository.UserRepo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class EmpService {

    @Autowired
    private UserRepo userRepo ;

    @Autowired
    private PasswordEncoder passwordEncoder ;
    @Autowired
    private PasswordHistoryRepo passwordHistoryRepo ;
    @Autowired
    private UserRepo userRepository ;

// change pwd apres generation de code de responsable
    @Transactional
    public void changePwd(String unix , ChangepwdReq changepwdReq) throws Exception {
        User u = userRepo.findByUnix(unix).orElseThrow(() -> new Exception("utilisateur introuvable"));


        u.setPwd(passwordEncoder.encode(changepwdReq.newPwd()));
        userRepo.save(u);
        // cherche ADMIN + changedAt = NULL
        Optional<PasswordHistory> pwdTemp = passwordHistoryRepo
                .findTopByUserOrderByCreatedAtDesc(u);



        if (pwdTemp.isPresent() && pwdTemp.get().isTemp() && !pwdTemp.get().hasChanged()) {
            pwdTemp.get().setChangedAt(LocalDateTime.now());
            pwdTemp.get().setChangeType(ChangeType.EMP);
            passwordHistoryRepo.save(pwdTemp.get());
        } else {
            throw new Exception("aucun mot de passe temporaire en attente !");
        }

    }

    //mdp oublier ( le employer modifer leur pwd
    @Transactional
    public User passwordChange(String unix, MdpOReq mdpo) {

        User u = userRepository.findByUnix(unix)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        // 🔐 vérif ancien
        if (!passwordEncoder.matches(mdpo.oldPwd(), u.getPwd())) {
            throw new RuntimeException("Ancien mot de passe incorrect");
        }

        // 🚫 éviter même mdp
        if (passwordEncoder.matches(mdpo.newPwd(), u.getPwd())) {
            throw new RuntimeException("Le nouveau mot de passe doit être différent");
        }

        // 🔒 update user
        String encoded = passwordEncoder.encode(mdpo.newPwd());
        u.setPwd(encoded);

        // 📜 INSERT nouveau historique (IMPORTANT)
        PasswordHistory ph = new PasswordHistory();
        ph.setUser(u);
        ph.setCreatedAt(LocalDateTime.now());
        ph.setChangedAt(LocalDateTime.now());
        ph.setChangeType(ChangeType.EMP);

        passwordHistoryRepo.save(ph);

        return userRepository.save(u);
    }

public User profile(String unix) throws Exception {
    User u = userRepository.findByUnix(unix)
            .orElseThrow(() -> new RuntimeException("utilisateur n'existe pas avec ce identifiant"));
    return u ;
    }

    public User changeUsername(String unix, String newLib) {
        User u = userRepository.findByUnix(unix)
                .orElseThrow(() -> new RuntimeException("utilisateur n'existe pas avec ce identifiant"));
        u.setLib(newLib);
        return userRepository.save(u);
    }

        public User changeEmail(String unix , String e){
        User u = userRepository.findByUnix(unix)
                .orElseThrow(() -> new RuntimeException("utilisateur n'existe pas avec ce identifiant"));
        u.setEmail(e);
        return userRepository.save(u);
    }





}
