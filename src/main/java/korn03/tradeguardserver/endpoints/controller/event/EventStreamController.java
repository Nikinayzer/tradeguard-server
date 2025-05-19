package korn03.tradeguardserver.endpoints.controller.event;

import korn03.tradeguardserver.endpoints.dto.user.equity.UserEquityStateDTO;
import korn03.tradeguardserver.endpoints.dto.user.position.UserPositionsStateDTO;
import korn03.tradeguardserver.security.AuthUtil;
import korn03.tradeguardserver.service.equity.EquityService;
import korn03.tradeguardserver.service.sse.SseEmitterService;
import korn03.tradeguardserver.service.position.PositionService;
import korn03.tradeguardserver.service.bybit.BybitMarketDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Controller for server-sent events (SSE) stream endpoints.
 */
@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Slf4j
public class EventStreamController {

    private final SseEmitterService sseService;
    private final PositionService positionService;
    private final EquityService equityService;
    private final BybitMarketDataService marketDataService;

    /**
     * Main SSE stream endpoint that provides real-time updates for all data types.
     * 
     * @return SSE emitter for streaming events
     */
    @GetMapping("/stream")
    public SseEmitter getEventStream() {
        Long userId = AuthUtil.getCurrentUserId();
        
        log.info("Creating SSE stream for user {}", userId);
        SseEmitter emitter = sseService.createEmitter(userId);

        new Thread(() -> {
            try {
                Thread.sleep(500); // Give a small delay to ensure connection is established

                log.info("Sending initial market data to user {}", userId);
                sseService.sendUpdate(userId, "market_data", marketDataService.getCurrentMarketData());

                UserPositionsStateDTO positionsState = positionService.getUserPositionsState(userId);
                if (positionsState != null) {
                    log.info("Sending initial positions state to user {}", userId);
                    sseService.sendUpdate(userId, "positions", positionsState);
                } else {
                    log.info("No positions data available for user {}", userId);
                }

                UserEquityStateDTO equityState = equityService.getUserEquityState(userId);
                if (equityState != null) {
                    log.info("Sending initial equity state to user {}", userId);
                    sseService.sendUpdate(userId, "equity", equityState);
                } else {
                    log.info("No equity data available for user {}", userId);
                }
                
                log.info("Initial data sent to user {}", userId);
            } catch (Exception e) {
                log.error("Error sending initial data to user {}: {}", userId, e.getMessage(), e);
            }
        }).start();
        
        return emitter;
    }
} 