package com.example.pfebtk.auth.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@SuperBuilder
@Table(name = "Password_History")
public class PasswordHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "change_type", nullable = false)
    private ChangeType changeType; //ADMIN ou USER

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt; // date génération par admin

    @Column(name = "changed_at")
    private LocalDateTime changedAt;

    public boolean isTemp() {
        return this.changeType == ChangeType.RESP;
    }


    public boolean hasChanged() {
        return this.changedAt != null;
    }
}
