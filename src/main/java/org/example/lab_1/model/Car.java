package org.example.lab_1.model;
import jakarta.persistence.*;
@Entity
@Table(name = "car")
public class Car {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String name;
    @Column(nullable = false)
    public boolean cool;
    public String color; // добавил так как нужна была операция со цветом
}
