package dev.renzo.crud.security.controller;

import java.text.ParseException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.renzo.crud.dto.MensajeDTO;
import dev.renzo.crud.security.dto.JwtDto;
import dev.renzo.crud.security.dto.NuevoUsuario;
import dev.renzo.crud.security.dto.LoginUsuario;
import dev.renzo.crud.security.entity.Rol;
import dev.renzo.crud.security.entity.Usuario;
import dev.renzo.crud.security.enums.RolNombre;
import dev.renzo.crud.security.jwt.JwtProvider;
import dev.renzo.crud.security.service.RolService;
import dev.renzo.crud.security.service.UsuarioService;

@RestController
@RequestMapping("/auth")
@CrossOrigin
public class AuthController {

    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    UsuarioService usuarioService;
    @Autowired
    RolService rolService;
    @Autowired
    JwtProvider jwtProvider;

    @PostMapping("")
    public ResponseEntity<MensajeDTO> register(@Valid @RequestBody NuevoUsuario nuevoUsuario, BindingResult bindingResult) {
        if (bindingResult.hasErrors())
            return createRequestResponse("Error: Invalid data", HttpStatus.BAD_REQUEST);
        if (usuarioService.existsByNombreUsuario(nuevoUsuario.getNombreUsuario()))
            return createRequestResponse("Error: Username is already taken", HttpStatus.BAD_REQUEST);
        if (usuarioService.existsByEmail(nuevoUsuario.getEmail()))
            return createRequestResponse("Error: Email is already taken", HttpStatus.BAD_REQUEST);

        Usuario user = new Usuario(nuevoUsuario.getNombre(), nuevoUsuario.getNombreUsuario(), nuevoUsuario.getEmail(),
                passwordEncoder.encode(nuevoUsuario.getPassword()));

        Set<Rol> roles = new HashSet<>();
        Optional<Rol> userRoleName = rolService.getByRolNombre(RolNombre.ROLE_USER);
        Optional<Rol> adminRoleName = rolService.getByRolNombre(RolNombre.ROLE_ADMIN);

        if (!userRoleName.isPresent())
            return createRequestResponse("Error: User role is not found", HttpStatus.BAD_REQUEST);

        if (!adminRoleName.isPresent())
            return createRequestResponse("Error: Admin role is not found", HttpStatus.BAD_REQUEST);

        roles.add(userRoleName.get());

        if (nuevoUsuario.getRoles().contains("admin"))
            roles.add(adminRoleName.get());

        user.setRoles(roles);
        usuarioService.save(user);

        return createRequestResponse("User registered successfully", HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginUsuario userLoginDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors())
            return createRequestResponse("Error: Invalid user", HttpStatus.UNAUTHORIZED);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userLoginDTO.getNombreUsuario(), userLoginDTO.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtProvider.generateToken(authentication);
        JwtDto jwtDto = new JwtDto(jwt);
        return new ResponseEntity<>(jwtDto, HttpStatus.ACCEPTED);
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtDto> refresh(@RequestBody JwtDto jwtDto) throws ParseException {
        String token = jwtProvider.refreshToken(jwtDto);
        JwtDto jwt = new JwtDto(token);
        return new ResponseEntity<>(jwt, HttpStatus.OK);

    }

    private ResponseEntity<MensajeDTO> createRequestResponse(String message, HttpStatus httpStatus) {
        return new ResponseEntity<>(new MensajeDTO(message), httpStatus);
    }

}