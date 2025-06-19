package jeju.bear.place.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlaceDto {
    private String id;
    private String name;
    private Double latitude;
    private Double longitude;
    private String imageUrl; // 대표 썸네일
}