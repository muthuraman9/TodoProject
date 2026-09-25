package com.example.todoapp.controller;

import com.example.todoapp.model.Todo;
import com.example.todoapp.exception.TodoNotFoundException;
import com.example.todoapp.exception.AccessDeniedException;
import com.example.todoapp.service.TodoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/todos")
public class TodoController {

    private final TodoService todoService;

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    @GetMapping
    public List<Todo> getTodos(Authentication authentication) {
        return todoService.getTodosForUser(authentication.getName());
    }

    @PostMapping
    public ResponseEntity<?> createTodo(@RequestBody Map<String, String> body,
                                        Authentication authentication) {
        try {
            Todo todo = todoService.createTodo(authentication.getName(), body.get("title"));
            return ResponseEntity.ok(todo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTodo(@PathVariable Long id,
                                        @RequestBody Map<String, Object> body,
                                        Authentication authentication) {
        try {
            Todo todo = todoService.updateTodo(
                authentication.getName(),
                id,
                (String) body.get("title"),
                (Boolean) body.get("completed")
            );
            return ResponseEntity.ok(todo);
        } catch (TodoNotFoundException e) {
            // Todo with this ID does not exist at all
            return ResponseEntity.notFound().build();
        } catch (AccessDeniedException e) {
            // Todo exists but belongs to another user
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTodo(@PathVariable Long id,
                                        Authentication authentication) {
        try {
            todoService.deleteTodo(authentication.getName(), id);
            return ResponseEntity.ok(Map.of("message", "Deleted"));
        } catch (TodoNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", e.getMessage()));
        }
    }
}