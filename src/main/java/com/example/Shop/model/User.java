package com.example.Shop.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Setter
@Getter
@Entity
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Size(min = 2, max = 255, message = "Name min: 2, max: 255")
    @Size(min = 2, max = 255, message = "Name min: 2, max: 255")
    @Column(nullable = false, length = 255)
    private String username;
    @Size(min = 2, max = 512, message = "Password min: 2, max: 512")
    @Size(min = 2, max = 512, message = "Password min: 2, max: 512")
    @Column(nullable = false, length = 255)
    private String password;
    @Email
    @Size(min = 2, max = 255, message = "Email min: 2, max: 255")
    @Email
    @Size(min = 2, max = 255, message = "Email min: 2, max: 255")
    @Column(nullable = false, length = 255)
    private String email;

    @OneToMany(mappedBy = "user")
    @JsonIgnore
    private List<OrderDemo> orders;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    //If add to database
    //1. Ініціалізація Datainitializer
    //2. AuthController -> Register
    private Set<Role> roles;
    private boolean enabled = true; //Is the account active?
    private boolean accountNonExpired = true; //Isn't it too user-friendly?
    private LocalDateTime accountExpirationDate; //Account expiration date
    private LocalDateTime credentialsExpirationDate; //Password expiration date

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if(roles == null || roles.isEmpty()) {
            return Set.of(new SimpleGrantedAuthority("ROLE_USER"));
        }
        return roles.stream().map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toSet());
    }

    @Override
    public boolean isAccountNonExpired() {
        if(accountExpirationDate == null) {
            return true;
        }
        return LocalDateTime.now().isBefore(accountExpirationDate);
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonExpired;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        if(credentialsExpirationDate == null) {
            return true;
        }
        return LocalDateTime.now().isBefore(credentialsExpirationDate);
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
