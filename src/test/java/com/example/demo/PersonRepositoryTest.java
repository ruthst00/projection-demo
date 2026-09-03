package com.example.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class PersonRepositoryTest {

    @Autowired
    private PersonRepository personRepository;

    @BeforeEach
    void setUp() {
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

    @Test
    void findByNameContains_matchesSubstring() {
        List<Person> results = personRepository.findByNameContains("ir");
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Dirk");
    }

    @Test
    void findByNameContains_matchesMultiple() {
        // "i" matches "Alice" and "Dirk"
        List<Person> results = personRepository.findByNameContains("i");
        assertThat(results).extracting(Person::getName)
                .containsExactlyInAnyOrder("Alice", "Dirk");
    }

    @Test
    void findByNameContains_noMatch_returnsEmpty() {
        List<Person> results = personRepository.findByNameContains("xyz");
        assertThat(results).isEmpty();
    }

    @Test
    void save_andFindById_works() {
        Person person = new Person();
        person.setName("Eve");
        person.setSurname("White");
        person.setAge(22L);
        Person saved = personRepository.save(person);

        assertThat(saved.getId()).isNotNull();
        assertThat(personRepository.findById(saved.getId())).isPresent()
                .get()
                .extracting(Person::getName, Person::getSurname, Person::getAge)
                .containsExactly("Eve", "White", 22L);
    }

    @Test
    void findAll_returnsAllPersons() {
        List<Person> all = personRepository.findAll();
        assertThat(all).hasSize(3);
    }

    @Test
    void delete_removesEntity() {
        List<Person> all = personRepository.findAll();
        personRepository.delete(all.get(0));
        assertThat(personRepository.findAll()).hasSize(2);
    }
}
