// src/main/java/com/jeju/bear/plan/controller/DestinationController.java
package jeju.bear.plan.controller;

import jeju.bear.plan.dto.CreateDestinationRequest;
import jeju.bear.plan.dto.DestinationDto;
import jeju.bear.plan.dto.UpdateSequenceRequest;
import jeju.bear.plan.service.DestinationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trip-days/{dayId}/destinations")
@RequiredArgsConstructor
public class DestinationController {
    private final DestinationService destinationService;

    @GetMapping
    public List<DestinationDto> list(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long dayId
    ) {
        return destinationService.getDestinations(dayId, userId);
    }

    @PostMapping
    public DestinationDto add(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long dayId,
            @RequestBody CreateDestinationRequest req
    ) {
        req.setTripDayId(dayId);
        return destinationService.addDestination(req, userId);
    }

    @PutMapping("/sequence")
    public void reorder(
            @AuthenticationPrincipal Long userId,
            @RequestBody UpdateSequenceRequest req
    ) {
        destinationService.updateSequence(req.getTripDayId(), req.getOrderedDestinationIds(), userId);
    }

    @DeleteMapping("/{destId}")
    public void remove(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long dayId,
            @PathVariable Long destId
    ) {
        destinationService.removeDestination(dayId, destId, userId);
    }
}
