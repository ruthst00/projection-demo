package com.example.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class PersonRestTest {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private PersonRepository personRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();

        personRepository.deleteAll();

        Person alice = new Person();
        alice.setName("Alice");
        alice.setSurname("Smith");
        alice.setAge(30L);

        Person bob = new Person();
        bob.setName("Bob");
        bob.setSurname("Jones");
        bob.setAge(25L);

        Person dirk = new Person();
        dirk.setName("Dirk");
        dirk.setSurname("Deyne");
        dirk.setAge(20L);

        personRepository.saveAll(List.of(alice, bob, dirk));
    }

    // -----------------------------------------------------------------------
    // Spring Data REST: /persons
    // -----------------------------------------------------------------------

    @Test
    void getPersons_returnsAllPersons() throws Exception {
        mockMvc.perform(get("/persons").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.persons", hasSize(3)));
    }

    @Test
    void getPersons_withProjection_returnsNameOnly() throws Exception {
        mockMvc.perform(get("/persons")
                        .param("projection", "nameProjection")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.persons[*].name", everyItem(notNullValue())))
                .andExpect(jsonPath("$._embedded.persons[0].surname").doesNotExist())
                .andExpect(jsonPath("$._embedded.persons[0].age").doesNotExist());
    }

    @Test
    void getPersonById_returnsCorrectPerson() throws Exception {
        Long id = personRepository.findAll().get(0).getId();

        mockMvc.perform(get("/persons/{id}", id).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
    }

    @Test
    void getPersonById_notFound_returns404() throws Exception {
        mockMvc.perform(get("/persons/99999").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // -----------------------------------------------------------------------
    // Spring Data REST search: /persons/search/findByNameContains
    // -----------------------------------------------------------------------

    @Test
    void searchFindByNameContains_matchesSubstring() throws Exception {
        mockMvc.perform(get("/persons/search/findByNameContains")
                        .param("name", "ir")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.persons", hasSize(1)))
                .andExpect(jsonPath("$._embedded.persons[0].name").value("Dirk"));
    }

    @Test
    void searchFindByNameContains_noMatch_returnsEmptyList() throws Exception {
        mockMvc.perform(get("/persons/search/findByNameContains")
                        .param("name", "xyz")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.persons", hasSize(0)));
    }

    @Test
    void searchFindByNameContains_multipleMatches() throws Exception {
        mockMvc.perform(get("/persons/search/findByNameContains")
                        .param("name", "i")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.persons", hasSize(2)))
                .andExpect(jsonPath("$._embedded.persons[*].name",
                        containsInAnyOrder("Alice", "Dirk")));
    }

    // -----------------------------------------------------------------------
    // Custom REST endpoint: /test/findByNameContains
    // -----------------------------------------------------------------------

    @Test
    void customEndpoint_findByNameContains_returnsMatchingPersons() throws Exception {
        mockMvc.perform(get("/test/findByNameContains")
                        .param("name", "ob")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Bob"));
    }

    @Test
    void customEndpoint_findByNameContains_noMatch_returnsEmptyArray() throws Exception {
        mockMvc.perform(get("/test/findByNameContains")
                        .param("name", "xyz")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void customEndpoint_findByNameContains_multipleMatches() throws Exception {
        mockMvc.perform(get("/test/findByNameContains")
                        .param("name", "i")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].name", containsInAnyOrder("Alice", "Dirk")));
    }
}
