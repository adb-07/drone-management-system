package com.dronefleet.repository;
import com.dronefleet.domain.Role;import java.sql.*;import java.util.*;
public final class UserRepository {private final com.dronefleet.config.Database db;public UserRepository(com.dronefleet.config.Database db){this.db=db;}
 public void save(String username,String hash,Role role){try(Connection c=db.connect();PreparedStatement p=c.prepareStatement("INSERT INTO users(username,password_hash,role,created_at) VALUES(?,?,?,?) ON CONFLICT(username) DO UPDATE SET password_hash=excluded.password_hash,role=excluded.role,active=1")){p.setString(1,username);p.setString(2,hash);p.setString(3,role.name());p.setString(4,java.time.LocalDateTime.now().toString());p.executeUpdate();}catch(SQLException e){throw new RuntimeException(e);}}
 public Optional<Record> find(String username){try(Connection c=db.connect();PreparedStatement p=c.prepareStatement("SELECT username,password_hash,role FROM users WHERE username=? AND active=1")){p.setString(1,username);try(ResultSet r=p.executeQuery()){return r.next()?Optional.of(new Record(r.getString(1),r.getString(2),Role.valueOf(r.getString(3)))):Optional.empty();}}catch(SQLException e){throw new RuntimeException(e);}}
 public record Record(String username,String hash,Role role){}
}
