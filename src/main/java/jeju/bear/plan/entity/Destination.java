package jeju.bear.plan.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "destinations")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Destination {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer sequence;

    @Column(length = 50)
    private String transportation;

    @Column
    private Integer duration;  // 분 단위

    @Column(nullable = false)
    private String placeId;

    @Column(nullable = false)
    private String type;

    @Column
    private Integer price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_day_id", nullable = false)
    private TripDay tripDay;
}