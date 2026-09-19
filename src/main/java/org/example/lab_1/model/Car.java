package org.example.lab_1.model;
import jakarta.persistence.*;
@Entity @Table(name="car")
public class Car {
 @jakarta.validation.constraints.Positive @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Version public long version;
 @Column(columnDefinition="text") public String name;
 @Column(nullable=false) public boolean cool;
 @Column(columnDefinition="text") public String color;
}
