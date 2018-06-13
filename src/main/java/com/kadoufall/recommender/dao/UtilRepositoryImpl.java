package com.kadoufall.recommender.dao;

import com.kadoufall.recommender.model.UserOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;


@Repository
public class UtilRepositoryImpl implements UtilRepositoryCustom {
    @Autowired
    private UserOperationRepository userOperationRepository;


    @Override
    public List<Long> findSeenNewsIds(long userId) {
        List<UserOperation> userOperations = this.userOperationRepository.findByUserId(userId);
        return userOperations.parallelStream().map(UserOperation::getNewsId).collect(Collectors.toList());
    }

    @Override
    public List<Long> findSeenNewsIdsBefore(long userId, Timestamp timestamp) {
        List<UserOperation> userOperations = this.userOperationRepository.findByUserId(userId);
        return userOperations
                .parallelStream()
                .filter(u -> u.getReadTime().before(timestamp))
                .map(UserOperation::getNewsId)
                .collect(Collectors.toList());
    }
}
