package com.grh.grh.usuario;


import jakarta.persistence.*;

import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.SecureRandom;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name="usuarios")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "password")
public class Usuario implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 200)
    private String email;

    @Column(nullable = false, length = 60)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationUserRole role;

    @Builder.Default  // sin esto, el builder dejaría active = false
    @Column(nullable = false)
    private boolean active=true;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = role.getPermissions()
                .stream()
                .map(p -> new SimpleGrantedAuthority(p.getPermission()))
                .collect(Collectors.toSet());

        // Añadimos también el rol con prefijo ROLE_ para poder usar hasRole()
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role.name()));

        return authorities;
    }

    public static Usuario create(String email, String password,ApplicationUserRole role, PasswordEncoder passwordEncoder) {
        Usuario u = new Usuario();
        u.setEmail(email.trim().toLowerCase());
        u.setPassword(passwordEncoder.encode(password));
        u.setActive(true);
        u.setRole(role);
        return u;
    }

    private static final String PASSWORD_CHARS =
            "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";   // sin 0/O ni 1/l/I
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generatePassword() {
        StringBuilder sb = new StringBuilder(12);
        for (int i = 0; i < 12; i++) {
            sb.append(PASSWORD_CHARS.charAt(RANDOM.nextInt(PASSWORD_CHARS.length())));
        }
        return sb.toString();
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isEnabled() { return active; } // Significa que las credenciales se pueden usar para acceder al sistema
    // Cuando des de baja a un empleado, desactivas también su Usuario. Así el token deja de funcionar automáticamente.

    @Override
    public boolean isAccountNonExpired() { return true; } //no expiran

    @Override
    public boolean isAccountNonLocked() { return true; } // no tiene sistema de bloqueo por intentos fallidos.

    @Override
    public boolean isCredentialsNonExpired() { return true; } // las credenciales no caducan.

}
