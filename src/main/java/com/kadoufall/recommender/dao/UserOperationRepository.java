package com.kadoufall.recommender.dao;

import com.kadoufall.recommender.model.UserOperation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserOperationRepository extends JpaRepository<UserOperation, Long> {

    UserOperation findById(long id);

    List<UserOperation> findByUserId(long userId);

}