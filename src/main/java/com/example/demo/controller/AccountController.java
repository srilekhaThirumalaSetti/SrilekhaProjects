package com.example.demo.controller;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.demo.model.Appuser;
import com.example.demo.model.LoginDto;
import com.example.demo.model.RegisterDto;
import com.example.demo.repository.AppuserRepository;
import com.nimbusds.jose.jwk.source.ImmutableSecret;

@RestController
@RequestMapping("/account")
public class AccountController {

	@Value("${security.jwt.secret-key}")
	private String jwtSecretKey;

	@Value("${security.jwt.issuer}")
	private String jwtIssuer;

	@Autowired
	private AppuserRepository appUserRepository;

	@Autowired
	private AuthenticationManager authenticationManager;

	@GetMapping("/profile")
	public String userProfile() {
		return "Welconme to jwt";
	}

	@PostMapping("/register")
	public ResponseEntity<Object> register(@RequestBody RegisterDto registerDto, BindingResult result) {
		if (result.hasErrors()) {
			var errorsList = result.getAllErrors();
			var errorsMap = new HashMap<String, String>();

			for (int i = 0; i < errorsList.size(); i++) {
				var error = (FieldError) errorsList.get(i);
				errorsMap.put(error.getField(), error.getDefaultMessage());
			}
			return ResponseEntity.badRequest().body(errorsMap);
		}

		var bCryptEncoder = new BCryptPasswordEncoder();
		Appuser appUser = new Appuser();
		appUser.setFirstName(registerDto.getFirstName());
		appUser.setLastName(registerDto.getLastName());
		appUser.setUsername(registerDto.getUsername());
		appUser.setEmail(registerDto.getEmail());
		appUser.setRole("client");
		appUser.setCreatedAt(new Date());
		appUser.setPassword(bCryptEncoder.encode(registerDto.getPassword()));

		try {
			var otherUser = appUserRepository.findByUsername(registerDto.getUsername());
			if (otherUser != null) {
				return ResponseEntity.badRequest().body("Username already used");
			}
			otherUser = appUserRepository.findByEmail(registerDto.getEmail());

			if (otherUser != null) {
				return ResponseEntity.badRequest().body("Email address already used");
			}
			appUserRepository.save(appUser);
			String jwtToken = createJwtToken(appUser);

			var response = new HashMap<String, Object>();
			response.put("token", jwtToken);
			response.put("user", appUser);

			return ResponseEntity.ok(response);

		} catch (Exception ex) {
			System.out.println("There is an Exception :");
			ex.printStackTrace();
		}
		return ResponseEntity.badRequest().body("Error");
	}

	@PostMapping("/login")
	public ResponseEntity<Object> login(@Value(value = "") @RequestBody LoginDto loginDto, BindingResult result) {

		if (result.hasErrors()) {
			var errorsList = result.getAllErrors();
			var errorsMap = new HashMap<String, String>();

			for (int i = 0; i < errorsList.size(); i++) {
				var error = (FieldError) errorsList.get(i);
				errorsMap.put(error.getField(), error.getDefaultMessage());
			}
			return ResponseEntity.badRequest().body(errorsMap);
		}
		try {
			authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword()));

			Appuser appUser = appUserRepository.findByUsername(loginDto.getUsername());

			String jwtToken = createJwtToken(appUser);

			var response = new HashMap<String, Object>();
			response.put("token", jwtToken);
			response.put("user", jwtToken);

			return ResponseEntity.ok(response);

		} catch (Exception ex) {
			System.out.println("There is a Exception:");
			ex.printStackTrace();

		}
		return ResponseEntity.badRequest().body("Bad username or password");

	}

	private String createJwtToken(Appuser appUser) {
		Instant now = Instant.now();
		JwtClaimsSet claims = JwtClaimsSet.builder().issuer(jwtIssuer).issuedAt(now)
				.expiresAt(now.plusSeconds(24 * 3600)).subject(appUser.getUsername()).claim("role", appUser.getRole())
				.build();

		var encoder = new NimbusJwtEncoder(new ImmutableSecret<>(jwtSecretKey.getBytes()));
		var params = JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims);

		return encoder.encode(params).getTokenValue();
	}
}