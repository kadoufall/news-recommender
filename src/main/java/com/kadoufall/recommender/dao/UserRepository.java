package com.kadoufall.recommender.dao;

import com.kadoufall.recommender.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

    User findById(long id);


    @Query("from User u where u.id=:id")
    User findUser(@Param("id") long id);


}
