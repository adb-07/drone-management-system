package com.dronefleet.config;

import java.io.IOException;
import java.nio.file.*;
import java.sql.*;

public final class Database {
    private final String url;
    public Database(String url){ this.url=url; }
    public Connection connect() throws SQLException {
        if (url.startsWith("jdbc:sqlite:")) {
            String path=url.substring("jdbc:sqlite:".length());
            Path p=Path.of(path); if(p.getParent()!=null) try{Files.createDirectories(p.getParent());}catch(IOException e){throw new SQLException(e);}
        }
        Connection c=DriverManager.getConnection(url); try(Statement s=c.createStatement()){s.execute("PRAGMA foreign_keys = ON");} return c;
    }
    public void initialize(){
        try(Connection c=connect(); Statement s=c.createStatement(); var in=getClass().getClassLoader().getResourceAsStream("schema.sql")) {
            if(in==null) throw new IllegalStateException("schema.sql not found");
            String sql=new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            for(String stmt:sql.split(";")) if(!stmt.trim().isEmpty()) s.execute(stmt);
        }catch(Exception e){throw new IllegalStateException("Database initialization failed",e);}
    }
}
