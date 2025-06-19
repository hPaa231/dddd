package jeju.bear.plan.controller;

import jakarta.validation.Valid;
import jeju.bear.plan.dto.CreateTripPlanRequest;
import jeju.bear.plan.dto.TripPlanDto;
import jeju.bear.plan.dto.TripDayWithDestinationsDto;
import jeju.bear.plan.service.TripPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trip-plans")
@RequiredArgsConstructor
public class TripPlanController {
    private final TripPlanService tripPlanService;

    // 1) 여행 계획 생성
    @PostMapping
    public ResponseEntity<TripPlanDto> create(
            @RequestBody @Valid CreateTripPlanRequest request
    ) {
        Long fakeUserId = 1L;  // 테스트용 하드코딩
        TripPlanDto dto = tripPlanService.createTripPlan(request, fakeUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    // 2) 단일 여행 계획 조회
    @GetMapping("/{planId}")
    public ResponseEntity<TripPlanDto> get(
            @PathVariable Long planId
    ) {
        Long fakeUserId = 1L;
        TripPlanDto dto = tripPlanService.getTripPlan(planId, fakeUserId);
        return ResponseEntity.ok(dto);
    }

    // 3) 일자별 목적지 목록 조회
    @GetMapping("/{planId}/days-with-dests")
    public ResponseEntity<List<TripDayWithDestinationsDto>> listDaysWithDestinations(
            @PathVariable Long planId
    ) {
        Long fakeUserId = 1L;
        List<TripDayWithDestinationsDto> list = tripPlanService.getDaysWithDestinations(planId, fakeUserId);
        return ResponseEntity.ok(list);
    }
}
