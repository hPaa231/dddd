package jeju.bear.place.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Place {

    @Id
    private String contentsId;

    private String name;
    private String address;
    private double latitude;
    private double longitude;

    @Column(length = 1000)
    private String tag;
    private String introduction;
    private String imageUrl;
    private String category;
}
