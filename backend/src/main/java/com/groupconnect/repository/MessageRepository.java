package com.groupconnect.repository;

import com.groupconnect.model.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByGroupIdOrderByCreatedAtAsc(Long groupId);

    List<Message> findByGroupIdOrderByCreatedAtDesc(Long groupId, Pageable pageable);
}
