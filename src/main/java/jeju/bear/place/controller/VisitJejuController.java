package jeju.bear.place.controller;

import io.swagger.v3.oas.annotations.Operation;
import jeju.bear.place.entity.Place;
import jeju.bear.place.service.VisitJejuService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/visitjeju")
public class VisitJejuController {

    private final VisitJejuService visitJejuService;

    @Operation(summary = "제주 관광지 저장", description = "VisitJeju API에서 c1 관광지를 가져와 저장합니다.")
    @GetMapping("/import")
    public List<Place> importAttractions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return visitJejuService.importAttractions(page, size);
    }
}