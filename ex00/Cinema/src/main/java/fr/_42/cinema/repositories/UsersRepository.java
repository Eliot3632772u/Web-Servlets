package fr._42.cinema.repositories;

import java.util.*;
import fr._42.cinema.repositories.*;
import fr._42.cinema.models.*;

public interface UsersRepository extends CrudRepository<User> 
{
    Optional<User> findByPhone(String phone);
}