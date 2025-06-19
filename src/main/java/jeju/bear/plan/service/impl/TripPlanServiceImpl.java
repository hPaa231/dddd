package jeju.bear.plan.service.impl;

import jeju.bear.plan.dto.CreateTripPlanRequest;
import jeju.bear.plan.dto.TripPlanDto;
import jeju.bear.plan.dto.TripDayWithDestinationsDto;
import jeju.bear.plan.dto.DestinationDto;
import jeju.bear.plan.entity.TripDay;
import jeju.bear.plan.entity.TripPlan;
import jeju.bear.plan.repository.TripPlanRepository;
import jeju.bear.plan.service.TripPlanService;            // ← 추가
import jeju.bear.user.entity.User;
import jeju.bear.user.repository.UserRepository;
import jeju.bear.global.common.CustomException;
import jeju.bear.global.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Transactional
public class TripPlanServiceImpl implements TripPlanService {
    private final TripPlanRepository tripPlanRepository;
    private final UserRepository userRepository;

    @Override
    public TripPlanDto createTripPlan(CreateTripPlanRequest request, Long userId) {
        // 1) User 엔티티 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        // 2) TripPlan 엔티티 생성
        TripPlan plan = TripPlan.builder()
                .planName(request.getPlanName())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .user(user)
                .build();

        // 3) 날짜 수 계산 및 TripDay 생성
        int totalDays = (int) ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;
        IntStream.range(0, totalDays)
                .mapToObj(i -> TripDay.builder()
                        .dayNumber(i + 1)
                        .date(request.getStartDate().plusDays(i))
                        .tripPlan(plan)
                        .build())
                .forEach(plan.getDays()::add);

        // 4) 저장 & DTO 변환
        TripPlan saved = tripPlanRepository.save(plan);
        return TripPlanDto.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TripPlanDto getTripPlan(Long tripPlanId, Long userId) {
        TripPlan plan = tripPlanRepository.findById(tripPlanId)
                .filter(tp -> tp.getUser().getId().equals(userId))
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
        return TripPlanDto.from(plan);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TripDayWithDestinationsDto> getDaysWithDestinations(Long planId, Long userId) {
        TripPlan plan = tripPlanRepository.findById(planId)
                .filter(tp -> tp.getUser().getId().equals(userId))
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        return plan.getDays().stream()
                .map(day -> TripDayWithDestinationsDto.builder()
                        .tripDayId(day.getTripDayId())
                        .dayNumber(day.getDayNumber())
                        .date(day.getDate())
                        .destinations(day.getDestinations().stream()
                                .map(DestinationDto::from)
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());
    }
}
