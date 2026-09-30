package com.dronefleet.security;import com.dronefleet.domain.Role;public record Session(String username,Role role){public boolean admin(){return role==Role.ADMIN;}}
