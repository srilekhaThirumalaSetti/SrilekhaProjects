package com.example.demo.repository;



import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.model.Appuser;



public interface AppuserRepository extends JpaRepository<Appuser, Integer> {
    Appuser findByUsername(String username);


	Appuser findByEmail(String email);

}

	