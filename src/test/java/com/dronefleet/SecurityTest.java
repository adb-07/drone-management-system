package com.dronefleet;
import com.dronefleet.security.PasswordHasher;import org.junit.jupiter.api.Test;import static org.junit.jupiter.api.Assertions.*;
class SecurityTest{@Test void passwordHashIsVerifiable(){String h=PasswordHasher.hash("secret");assertNotEquals("secret",h);assertTrue(PasswordHasher.verify("secret",h));assertFalse(PasswordHasher.verify("wrong",h));}}
