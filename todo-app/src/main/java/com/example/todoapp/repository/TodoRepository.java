package com.example.todoapp.repository;

import com.example.todoapp.model.Todo;
import com.example.todoapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TodoRepository extends JpaRepository<Todo, Long> {
	List<Todo> findByOwner(User owner);

	Optional<Todo> findByIdAndOwner(Long id, User owner);
}