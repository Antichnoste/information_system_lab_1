package org.example.lab_1.dto;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HumanPageResponse {
    private List<HumanResponse> items;
    private long total;
    private int page;
    private int size;
}
