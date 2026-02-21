package fr._42.cinema.services;

import fr._42.cinema.repositories.*;
import fr._42.cinema.models.*;
import org.springframework.context.annotation.*;
import org.springframework.security.crypto.*;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.*;

@Component
public class UsersServiceImpl implements UsersService 
{
    private UsersRepository usersRepository;
    private PasswordEncoder passwordEncoder;

    public UsersServiceImpl(UsersRepository usersRepository, PasswordEncoder passwordEncoder)
    {
        this.usersRepository = usersRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void signUp(String firstName, String lastName, String phone, String password, String email) {

        String hash = passwordEncoder.encode(password);

        User user = new User(0L, firstName, lastName, phone, hash, email);

        usersRepository.save(user);

    }

    public User signIn(String phone, String password) {

        Optional<User> userOpt = usersRepository.findByPhone(phone);

        if (userOpt.isEmpty())
            return null;

        User user = userOpt.get();
        if(passwordEncoder.matches(password, user.getPassword()))
            return user;
        
        return null;
    }
}