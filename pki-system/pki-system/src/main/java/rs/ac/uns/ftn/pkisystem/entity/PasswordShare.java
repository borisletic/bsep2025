package rs.ac.uns.ftn.pkisystem.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Entity
@Table(name = "password_shares")
public class PasswordShare {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "password_entry_id", nullable = false)
    private PasswordEntry passwordEntry;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String encryptedPassword; // Encrypted with user's public key

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Constructors
    public PasswordShare() {}

    public PasswordShare(PasswordEntry passwordEntry, User user, String encryptedPassword) {
        this.passwordEntry = passwordEntry;
        this.user = user;
        this.encryptedPassword = encryptedPassword;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public PasswordEntry getPasswordEntry() { return passwordEntry; }
    public void setPasswordEntry(PasswordEntry passwordEntry) { this.passwordEntry = passwordEntry; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getEncryptedPassword() { return encryptedPassword; }
    public void setEncryptedPassword(String encryptedPassword) { this.encryptedPassword = encryptedPassword; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}