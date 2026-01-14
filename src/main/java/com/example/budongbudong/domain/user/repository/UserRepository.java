package com.example.budongbudong.domain.user.repository;

import com.example.budongbudong.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,Long> {
}
