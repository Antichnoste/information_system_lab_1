package org.example.lab_1.model;
import jakarta.persistence.*;
@Entity @Table(name="app_user")
public class AppUser {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Column(nullable=false,unique=true,length=100) public String username;
 @Column(name="password_hash",nullable=false,length=512) public String passwordHash;
}
