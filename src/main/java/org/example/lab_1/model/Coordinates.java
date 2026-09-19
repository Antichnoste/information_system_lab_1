package org.example.lab_1.model;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
@Entity @Table(name="coordinates")
public class Coordinates {
 @jakarta.validation.constraints.Positive @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Version public long version;
 @NotNull @Column(nullable=false) public Long x;
 @NotNull @DecimalMin(value="-629", inclusive=false) @Column(nullable=false) public Float y;
 @AssertTrue(message="Координата y должна быть конечным числом больше -629")
 public boolean isFiniteY() { return y == null || Float.isFinite(y); }
}
