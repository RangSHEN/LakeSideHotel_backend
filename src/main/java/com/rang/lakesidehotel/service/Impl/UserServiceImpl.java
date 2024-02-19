package com.rang.lakesidehotel.service.Impl;

import com.rang.lakesidehotel.exception.UserAlreadyExistsException;
import com.rang.lakesidehotel.model.Role;
import com.rang.lakesidehotel.model.User;
import com.rang.lakesidehotel.repository.RoleRepository;
import com.rang.lakesidehotel.repository.UserRepository;
import com.rang.lakesidehotel.security.jwt.AuthTokenFilter;
import com.rang.lakesidehotel.security.jwt.JwtUtils;
import com.rang.lakesidehotel.service.UserService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final RoleRepository roleRepository;


    @Override
    public User registerUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())){
            throw new UserAlreadyExistsException(user.getEmail()+ "already exists");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        Role userRole = roleRepository.findByName("ROLE_USER").get(); //自动变成user
        user.setRoles(Collections.singleton(userRole));
        return userRepository.save(user);
    }

    @Override
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    @Transactional
    @Override
    public void deleteUser(String email) {
        User theUser = getUser(email); //getUser this function will return UsernameNotFoundException("User not found")) to the frontend
        if (theUser != null){
            userRepository.deleteByEmail(email);
        }

    }

    @Override
    public User getUser(String email) {
        return userRepository.findByEmail(email).orElseThrow(()-> new UsernameNotFoundException("User not found"));
    }
}
