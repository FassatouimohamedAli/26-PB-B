package com.example.pfebtk.auth.controllers.resp;

import com.example.pfebtk.auth.dto.Registerreq;
import com.example.pfebtk.auth.entity.User;
import com.example.pfebtk.auth.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/resp")
public class UserController {

    @Autowired
    private AuthService authService ;


    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody Registerreq request) {
        User user = authService.register(request);
        return ResponseEntity.ok("Utilisateur créé avec succès : " + user.getLib());
    }

    @GetMapping("/getall")
    public List<User> getAll() {
        try {

            return authService.getAllEmp();
        } catch (Exception e) {

            e.printStackTrace();

            return new ArrayList<>();
        }
    }


    @PatchMapping("/update/{unix}/{statut}")
    public ResponseEntity<String> updateStatut(@PathVariable String unix,@PathVariable String statut) {

       User user = authService.updateStatut(unix,statut);
        return ResponseEntity.ok("nouvelle status  de compte est " + user.getSus());
    }

    @PatchMapping("/update/{unix}")
    public ResponseEntity<String> updatePwd(@PathVariable String unix){
        User u = authService.generatePwdEmp(unix);
        return ResponseEntity.ok("mdp updated "+ u.getLib());
    }


}
