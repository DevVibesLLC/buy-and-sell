package am.devvibes.buyandsell.controller;

import am.devvibes.buyandsell.dto.priceStatistic.PriceStatisticsRequestDto;
import am.devvibes.buyandsell.dto.priceStatistic.PriceStatisticsResponseDto;
import am.devvibes.buyandsell.service.priceStatistic.PriceStatisticService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/public/priceStatistics")
@RequiredArgsConstructor
public class PriceStatisticsController {

	private final PriceStatisticService priceStatisticService;

	@PostMapping
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Get Price Statistic for Item")
	public ResponseEntity<PriceStatisticsResponseDto> getPriceStatistics(@RequestBody PriceStatisticsRequestDto priceStatisticsRequestDto){
		PriceStatisticsResponseDto priceStatistic = priceStatisticService.getPriceStatistic(priceStatisticsRequestDto);
		return ResponseEntity.ok(priceStatistic);
	}

}
