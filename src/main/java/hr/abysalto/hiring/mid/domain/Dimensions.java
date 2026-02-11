package hr.abysalto.hiring.mid.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Dimensions {

    private Double width;
    private Double height;
    private Double depth;
}
