package jeju.bear.place.service;

import jeju.bear.place.entity.Place;
import jeju.bear.place.repository.PlaceRepository;
import jeju.bear.place.client.VisitJejuApiClient;
import jeju.bear.place.dto.VisitJejuResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VisitJejuService {

    private final VisitJejuApiClient apiClient;
    private final PlaceRepository placeRepository;

    public List<Place> importAttractions(int page, int size) {
        VisitJejuResponse response = apiClient.getAttractions(page, size);
        List<Place> saved = response.getItems().stream()
                .map(this::convert)
                .filter(place -> place != null)
                .map(placeRepository::save)
                .toList();
        return saved;
    }

    private Place convert(VisitJejuResponse.Item item) {
        try {
            if (item.getLatitude() == null || item.getLongitude() == null) return null;

            double lat = new BigDecimal(item.getLatitude()).doubleValue();
            double lng = new BigDecimal(item.getLongitude()).doubleValue();

            String imageUrl = item.getRepPhoto() != null && item.getRepPhoto().getPhotoid() != null
                    ? item.getRepPhoto().getPhotoid().getImgpath()
                    : null;

            return new Place(
                    item.getContentsid(),
                    item.getTitle(),
                    item.getAddr1() != null ? item.getAddr1() : item.getAddress(),
                    lat,
                    lng,
                    item.getAlltag(),
                    item.getIntroduction(),
                    imageUrl,
                    "c1"
            );
        } catch (Exception e) {
            return null;
        }
    }
}