package com.example.demo.service;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.demo.model.Appuser;
import com.example.demo.repository.AppuserRepository;



@Service
public class AppUserService implements UserDetailsService{
   @Autowired
	private AppuserRepository repo;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Appuser appUser=repo.findByUsername(username);
		if(appUser!=null) {
			var springUser=User.withUsername(appUser.getUsername())
			   .password(appUser.getPassword())
			   .roles(appUser.getRole())
			   .build();
			
			return springUser;
		}
		return null;
	}

}