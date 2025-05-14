package com.champsoft.DataAccess;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProfileDashboardRepository extends MongoRepository<UserProfileDashboardEntity, String> { // String is type of mongoId
    Optional<UserProfileDashboardEntity> findByUserId(String userId);
    void deleteByUserId(String userId);
}