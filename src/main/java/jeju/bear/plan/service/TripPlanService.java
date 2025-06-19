package jeju.bear.plan.service;

import jeju.bear.plan.dto.TripPlanDto;
import jeju.bear.plan.dto.TripDayWithDestinationsDto;
import jeju.bear.plan.dto.CreateTripPlanRequest;

import java.util.List;

public interface TripPlanService {
    TripPlanDto createTripPlan(CreateTripPlanRequest request, Long userId);
    TripPlanDto getTripPlan(Long tripPlanId, Long userId);
    // 이 메서드를 반드시 선언해야 구현체에서 찾아냅니다.
    List<TripDayWithDestinationsDto> getDaysWithDestinations(Long tripPlanId, Long userId);
}
