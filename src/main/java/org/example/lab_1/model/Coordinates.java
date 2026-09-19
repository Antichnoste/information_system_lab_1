package org.example.lab_1.model;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.Check;
@Entity
@Table(name="coordinates")
@Check(constraints = "y > -629 and y < 'Infinity'::real")
public class Coordinates {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    public Long id;

    @NotNull
    @Column(nullable=false)
    public Long x;

    @NotNull
    @DecimalMin(value="-629", inclusive=false)
    @Column(nullable=false)
    public Float y;

    @AssertTrue(message="Координата y должна быть конечным числом")
    public boolean isFiniteY() {
        return y == null || Float.isFinite(y);
    }
}
