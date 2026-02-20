package fr._42.cinema.services;

import fr._42.cinema.models.*;

public interface UsersService 
{
    void signUp(String firstName, String lastName, String phone, String password);
    User signIn(String phone, String password);

}