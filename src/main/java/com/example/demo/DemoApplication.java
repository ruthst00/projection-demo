package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@SpringBootApplication
@RestController
public class DemoApplication implements CommandLineRunner {

    @Autowired
    private PersonRepository personRepository;

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}


    @Override
    public void run(String... args) throws Exception {
        Person person = new Person();
        person.setName("Dirk");
        person.setSurname("Deyne");
        person.setAge(20L);
        personRepository.save(person);
    }

    @RequestMapping("test/findByNameContains")
    public List<Person> findByNameContains(@Param("name") String name){
        return personRepository.findByNameContains(name);
    }

}
