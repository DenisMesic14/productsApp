package hr.abysalto.hiring.mid.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Meta {

    private String createdAt;
    private String UpdatedAt;
    private String barcode;
    private String qrCode;
}
