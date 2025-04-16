package com.fourt.RailSkylines.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.fourt.RailSkylines.domain.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail (String email); 
} 
