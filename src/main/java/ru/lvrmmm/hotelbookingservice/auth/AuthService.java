package ru.lvrmmm.hotelbookingservice.auth;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.lvrmmm.hotelbookingservice.auth.dto.JwtResponse;
import ru.lvrmmm.hotelbookingservice.auth.dto.LoginRequest;
import ru.lvrmmm.hotelbookingservice.security.CustomUserDetailsService;
import ru.lvrmmm.hotelbookingservice.security.JwtTokenProvider;
import ru.lvrmmm.hotelbookingservice.security.UserDetailsImpl;
import ru.lvrmmm.hotelbookingservice.user.dto.request.UserCreateRequest;
import ru.lvrmmm.hotelbookingservice.user.dto.response.UserResponse;
import ru.lvrmmm.hotelbookingservice.user.service.UserService;

@Service
public class AuthService {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;

    public AuthService(UserService userService, JwtTokenProvider jwtTokenProvider, AuthenticationManager authenticationManager, CustomUserDetailsService userDetailsService) {
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
    }

    @Transactional
    public JwtResponse register(UserCreateRequest request){

        UserResponse user = userService.createUser(request);

        UserDetailsImpl userDetails = (UserDetailsImpl) userDetailsService.loadUserByUsername(request.username());

        String token = jwtTokenProvider.generateToken(userDetails);

        return new JwtResponse(token, user);
    }

    public JwtResponse login (LoginRequest request){
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        UserDetailsImpl userDetails = (UserDetailsImpl) userDetailsService.loadUserByUsername(request.username());

        String token = jwtTokenProvider.generateToken(userDetails);
        return new JwtResponse(token, UserResponse.from(userDetails.getUser()));
    }
}
