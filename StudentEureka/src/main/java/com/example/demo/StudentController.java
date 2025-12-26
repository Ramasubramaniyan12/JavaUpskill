package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class StudentController {
	
	   @Autowired
	    private StudentRespository studentRepository;

	@GetMapping("/student")
	public String getStudent() {
		return"Hi Students";
	}
	 @PostMapping
	    public StudentEntity addStudent(@RequestBody StudentEntity student) {
	        return studentRepository.save(student);
	    }
	
}
