package jeju.bear.place.controller;

import jeju.bear.place.dto.PlaceDto;
import jeju.bear.place.dto.RoomDto;
import jeju.bear.place.service.YeogiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/yeogi")
@RequiredArgsConstructor
public class YeogiController {

    private final YeogiService yeogiService;

    @GetMapping("/places")
    public List<PlaceDto> getPlaces(
            @RequestParam String region,
            @RequestParam String checkIn,
            @RequestParam String checkOut,
            @RequestParam int personal
    ) throws Exception {
        return yeogiService.fetchPlaceIds(region, checkIn, checkOut, personal);
    }

    @GetMapping("/rooms/{placeId}")
    public List<RoomDto> getRooms(
            @PathVariable String placeId,
            @RequestParam String checkIn,
            @RequestParam String checkOut,
            @RequestParam int personal
    ) throws Exception {
        return yeogiService.fetchRoomsWithPrices(placeId, checkIn, checkOut, personal);
    }
}