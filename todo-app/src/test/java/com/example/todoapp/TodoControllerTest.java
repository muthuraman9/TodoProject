package com.example.todoapp;

import com.example.todoapp.model.Todo;
import com.example.todoapp.model.User;
import com.example.todoapp.repository.TodoRepository;
import com.example.todoapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TodoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User alice;
    private User bob;

    @BeforeEach
    void setUp() {
        // Seed demo users for the test (idempotent)
        alice = userRepository.findByUsername("alice").orElseGet(() -> {
            User u = new User();
            u.setUsername("alice");
            u.setPassword(passwordEncoder.encode("password123"));
            return userRepository.save(u);
        });

        bob = userRepository.findByUsername("bob").orElseGet(() -> {
            User u = new User();
            u.setUsername("bob");
            u.setPassword(passwordEncoder.encode("password123"));
            return userRepository.save(u);
        });

        // Clean up todos between test runs
        todoRepository.deleteAll();
    }

    // ---------------------------------------------------------
    // TEST 1: Successful todo operation (create + fetch own todo)
    // ---------------------------------------------------------
    @Test
    void userCanCreateAndFetchOwnTodo() throws Exception {
        // Alice creates a todo
        mockMvc.perform(post("/api/todos")
                .with(user("alice").roles("USER"))
                .contentType("application/json")
                .content("{\"title\":\"Buy milk\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Buy milk"))
                .andExpect(jsonPath("$.completed").value(false));

        // Alice fetches her list — should contain exactly one todo
        mockMvc.perform(get("/api/todos")
                .with(user("alice").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Buy milk"))
                .andExpect(jsonPath("$[0].completed").value(false));
    }

    // ---------------------------------------------------------
    // TEST 2: A user cannot delete another user's todo
    // ---------------------------------------------------------
    @Test
    void userCannotDeleteAnotherUsersTodo() throws Exception {
        // Bob has a todo
        Todo bobTodo = new Todo();
        bobTodo.setTitle("Bob's secret");
        bobTodo.setCompleted(false);
        bobTodo.setOwner(bob);
        bobTodo = todoRepository.save(bobTodo);

        Long bobTodoId = bobTodo.getId();

        // Alice tries to delete Bob's todo → should get 404 (not 403, not 200)
        mockMvc.perform(delete("/api/todos/" + bobTodoId)
                .with(user("alice").roles("USER")))
                .andExpect(status().isNotFound());

        // Bob's todo must still exist
        mockMvc.perform(get("/api/todos")
                .with(user("bob").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(bobTodoId))
                .andExpect(jsonPath("$[0].title").value("Bob's secret"));
    }
}