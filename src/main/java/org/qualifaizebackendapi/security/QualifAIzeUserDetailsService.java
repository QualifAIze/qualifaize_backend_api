package org.qualifaizebackendapi.security;

import org.qualifaizebackendapi.model.User;
import org.qualifaizebackendapi.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class QualifAIzeUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public QualifAIzeUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            String message = "Username " + username + " not found";
            throw new UsernameNotFoundException(message);
        } else if (user.isDeleted()) {
            String message = "Username '" + username + "' was found but account is deleted!";
            throw new UsernameNotFoundException(message);
        }

        return new QualifAIzeUserDetails(user);
    }
}
