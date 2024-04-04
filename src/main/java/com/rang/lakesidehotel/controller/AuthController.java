package com.rang.lakesidehotel.controller;

import com.rang.lakesidehotel.exception.UserAlreadyExistsException;
import com.rang.lakesidehotel.model.User;
import com.rang.lakesidehotel.request.LoginRequest;
import com.rang.lakesidehotel.response.JwtResponse;
import com.rang.lakesidehotel.security.jwt.JwtUtils;
import com.rang.lakesidehotel.security.user.HotelUserDetails;
import com.rang.lakesidehotel.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

//allow for free
@RestController
@RequestMapping("/auth")
@AllArgsConstructor
//@CrossOrigin("*")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    @PostMapping("/register-user")
    public ResponseEntity<?> registerUser(@RequestBody User user){
        try {
            userService.registerUser(user);
            return new ResponseEntity<>("Registration successful!", HttpStatus.OK);
        }catch (UserAlreadyExistsException e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest){
        Authentication authentication =
                authenticationManager
                        .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtTokenForUser(authentication);

        HotelUserDetails userDetails = (HotelUserDetails) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities()
                .stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());

        //方便postman 测试
        return new ResponseEntity<>(new JwtResponse
                (userDetails.getId(),
                userDetails.getEmail(),
                jwt, roles),
                HttpStatus.OK);
    }
}
