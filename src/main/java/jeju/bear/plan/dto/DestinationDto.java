package jeju.bear.plan.dto;

import jeju.bear.plan.entity.Destination;
import lombok.Getter;

@Getter
public class DestinationDto {
    private Long id;
    private int sequence;
    private String transportation;
    private long duration;
    private String placeId;
    private String type;
    private int price;
    // currency 빼고 싶으시면 DTO에서도 삭제하세요.

    public static DestinationDto from(Destination d) {
        DestinationDto dto = new DestinationDto();
        dto.id = d.getId();
        dto.sequence = d.getSequence();
        dto.transportation = d.getTransportation();
        dto.duration = d.getDuration();
        dto.placeId = d.getPlaceId();
        dto.type = d.getType();
        dto.price = d.getPrice();
        return dto;
    }
}
