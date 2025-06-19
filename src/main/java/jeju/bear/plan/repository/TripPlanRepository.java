package jeju.bear.plan.repository;

import jeju.bear.plan.entity.TripPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TripPlanRepository extends JpaRepository<TripPlan, Long> {
    List<TripPlan> findAllByUserId(Long userId);
}