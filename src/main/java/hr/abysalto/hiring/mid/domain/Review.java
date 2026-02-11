package hr.abysalto.hiring.mid.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    private int rating;
    private String comment;
    private String date;
    private String reviewerName;
    private String reviewerEmail;
}
