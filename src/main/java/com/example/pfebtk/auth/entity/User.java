package com.example.pfebtk.auth.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@SuperBuilder
@Table(name = "utilisateurs")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "CUTI", length = 10)
    private String cuti;

    @Column(name = "LIB", length = 30)
    private String lib;

    @Column(name = "PUTI", length = 10)
    private String puti;

    @Column(name = "EMAIL", length = 50)
    private String email;

    @Column(name = "UNIX", length = 50)
    private String unix;

    @Column(name = "AGE", length = 5)
    private String age;

    @Column(name = "SUS", length = 1)
    private String sus; // 'n' = actif, 'o' = suspendu
    @JsonIgnore
    @Column(name = "PWD", length = 100)
    private String pwd;


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + puti.trim()));
    }
    @JsonIgnore
    @Override
    public String getPassword() {
        return pwd;
    }

    @Override
    public String getUsername() {
        return unix;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return "n".equalsIgnoreCase(sus); // true si actif
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return "n".equalsIgnoreCase(sus);
    }
}
