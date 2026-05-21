package com.example.pfebtk.auth.controllers.emp;

import com.example.pfebtk.auth.dto.Authresp;
import com.example.pfebtk.auth.dto.ChangepwdReq;
import com.example.pfebtk.auth.dto.MdpOReq;
import com.example.pfebtk.auth.entity.User;
import com.example.pfebtk.auth.service.EmpService;
import com.example.pfebtk.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/emp")
public class EmpController {

    @Autowired
    private EmpService empService ;
    @Autowired
    private JwtUtil jwtUtil ;

    @PatchMapping("/changePassword")
    public ResponseEntity<?> changePassword(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody  ChangepwdReq changepwdReq
    ) {

        try{
if(authHeader == null || !authHeader.startsWith("Bearer ")){
    return  ResponseEntity.badRequest().body("problem de Header");
}

String token = authHeader.substring(7);
            System.out.println(token);
String unix = jwtUtil.extractUnix(token);
System.out.println(unix);

empService.changePwd(unix,changepwdReq);
return  ResponseEntity.ok("pwd change avec succes");

        }catch(Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@RequestHeader("Authorization") String authHeader) {
        try {
            // Vérifier et extraire le token du header "Bearer <token>"
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body("Problème de header d'autorisation");
            }

            String token = authHeader.substring(7);
            String unix = jwtUtil.extractUnix(token);

            User user = empService.profile(unix);
            return ResponseEntity.ok(user);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Utilisateur non trouvé"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Token invalide ou expiré"));
        }
    }


    @PatchMapping("/changePasswordemp")
    public ResponseEntity<?> changePasswordEmp(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody MdpOReq mdpo
    ) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body("Problème de header d'autorisation");
            }

            String token = authHeader.substring(7);
            String unix = jwtUtil.extractUnix(token);

            empService.passwordChange(unix, mdpo);
            return ResponseEntity.ok("Mot de passe changé avec succès");

        } catch (Exception e) {
            e.printStackTrace(); // Pour le debug
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PatchMapping("/changeUsername")
    public ResponseEntity<?> changeUsername(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> body
    ) {
        String token = authHeader.substring(7);
        String unix = jwtUtil.extractUnix(token);
        String lib = body.get("lib");
        empService.changeUsername(unix, lib);
        return ResponseEntity.ok("Nom modifié avec succès");
    }

    @PatchMapping("/changeEmail")
    public ResponseEntity<?> changeEmail(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> body
    ) {
        String token = authHeader.substring(7);
        String unix = jwtUtil.extractUnix(token);
        String email = body.get("email");
        empService.changeEmail(unix, email);
        return ResponseEntity.ok("Email modifié avec succès");
    }



}
