package com.grh.grh.auth;

import com.grh.grh.exception.ConflictException;
import com.grh.grh.security.JwtService;
import com.grh.grh.usuario.Usuario;
import com.grh.grh.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    public AuthResponseDTO login(LoginRequestDTO request) {
        authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
            var usuario = usuarioRepository.findByEmail(request.getEmail()).orElseThrow();

            var token = jwtService.generateToken(usuario);

            return new AuthResponseDTO(token,usuario.getRole().name());
    }
    @Transactional
    public void changePassword(ChangePasswordRequestDTO dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow();

        if (!passwordEncoder.matches(dto.getCurrentPassword(), usuario.getPassword())) {
            throw new ConflictException("La contraseña actual no es correcta");
        }
        if (passwordEncoder.matches(dto.getNewPassword(), usuario.getPassword())) {
            throw new ConflictException("La nueva contraseña debe ser distinta de la actual");
        }
        usuario.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        usuarioRepository.save(usuario);
    }

}
