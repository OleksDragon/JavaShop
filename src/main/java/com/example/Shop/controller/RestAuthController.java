package com.example.Shop.controller;

import com.example.Shop.Service.JwtService;
import com.example.Shop.model.Role;
import com.example.Shop.model.User;
import com.example.Shop.repository.RoleRepository;
import com.example.Shop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/api/auth")
public class RestAuthController {
    private UserRepository userRepository;
    private JwtService jwtService;
    private PasswordEncoder passwordEncoder;
    private RoleRepository roleRepository;

    @Autowired
    public RestAuthController(UserRepository userRepository,
                              JwtService jwtService,
                              PasswordEncoder passwordEncoder,
                              RoleRepository roleRepository
    ) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody User user){
        Map<String, Object> response = new HashMap<>();
        var foundUser = userRepository.findByUsername(user.getUsername());
        if(foundUser.isPresent()){
            response.put("success",false);
            response.put("message", "Username is already exist");
            return ResponseEntity.badRequest().body(response);
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        Optional<Role> role = roleRepository.findByName("ROLE_USER");
        if(role.isEmpty()){
            Role newUserRole = new Role();
            newUserRole.setName("ROLE_USER");
            roleRepository.save(newUserRole);
            role=Optional.of(newUserRole);
        }

        user.setRoles(Set.of(role.get()));

        userRepository.save(user);
        response.put("success",true);
        return ResponseEntity.ok(response);
    }

    @PostMapping("login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody User user){
        Map<String, Object> response = new HashMap<>();
        var foundUser = userRepository.findByUsername(user.getUsername());
        if(foundUser.isEmpty()
                || !passwordEncoder.matches(user.getPassword(),foundUser.get().getPassword())){
            response.put("success",false);
            response.put("message", "Username or password is incorrect");
            return ResponseEntity.badRequest().body(response);
        }

        String token = jwtService.generateJwtToken(foundUser.get());
        response.put("success", true);
        response.put("message", "Login Successfull");
        response.put("token", token);
        return ResponseEntity.ok(response);

    }
}
