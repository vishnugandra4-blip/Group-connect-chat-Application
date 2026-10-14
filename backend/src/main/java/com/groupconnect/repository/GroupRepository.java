package com.groupconnect.repository;

import com.groupconnect.model.ChatGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupRepository extends JpaRepository<ChatGroup, Long> {

    List<ChatGroup> findAllByOrderByCreatedAtDesc();

    List<ChatGroup> findByCreatedById(Long createdById);
}
