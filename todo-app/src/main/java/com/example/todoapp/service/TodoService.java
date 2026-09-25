package com.example.todoapp.service;

import com.example.todoapp.exception.TodoNotFoundException;
import com.example.todoapp.model.Todo;
import com.example.todoapp.model.User;
import com.example.todoapp.repository.TodoRepository;
import com.example.todoapp.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TodoService {

    private final TodoRepository todoRepository;
    private final UserRepository userRepository;

    public TodoService(TodoRepository todoRepository, UserRepository userRepository) {
        this.todoRepository = todoRepository;
        this.userRepository = userRepository;
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    public List<Todo> getTodosForUser(String username) {
        User user = getUserByUsername(username);
        return todoRepository.findByOwner(user);
    }

    @Transactional
    public Todo createTodo(String username, String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        User user = getUserByUsername(username);
        Todo todo = new Todo();
        todo.setTitle(title.trim());
        todo.setCompleted(false);
        todo.setOwner(user);
        return todoRepository.save(todo);
    }

    /**
     * Ownership check happens HERE, in one place.
     * Any caller — controller, scheduled job, CLI — gets the same protection.
     */
    @Transactional
    public Todo updateTodo(String username, Long todoId, String newTitle, Boolean completed) {
        User user = getUserByUsername(username);

        Todo todo = todoRepository.findByIdAndOwner(todoId, user)
            .orElseThrow(() -> new TodoNotFoundException(todoId));

        if (newTitle != null) {
            if (newTitle.trim().isEmpty()) {
                throw new IllegalArgumentException("Title cannot be empty");
            }
            todo.setTitle(newTitle.trim());
        }
        if (completed != null) {
            todo.setCompleted(completed);
        }
        return todoRepository.save(todo);
    }

    @Transactional
    public void deleteTodo(String username, Long todoId) {
        User user = getUserByUsername(username);

        Todo todo = todoRepository.findByIdAndOwner(todoId, user)
            .orElseThrow(() -> new TodoNotFoundException(todoId));

        todoRepository.delete(todo);
    }
}
