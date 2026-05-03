package com.Nook.backend.domain.faker;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/faker")
@RequiredArgsConstructor
public class FakerController {

    private final FakeDataService fakeDataService;

    @PostMapping("/start")
    public ResponseEntity<String> start(
            @RequestParam String roomId, // Added this parameter
            @RequestParam(defaultValue = "5") int intervalSeconds,
            @RequestParam(defaultValue = "3") int batchSize
    ) {
        // We pass roomId to the service method
        boolean started = fakeDataService.start(roomId, intervalSeconds, batchSize);
        return started
                ? ResponseEntity.ok("Faker started for room " + roomId)
                : ResponseEntity.badRequest().body("Faker is already running");
    }

    @PostMapping("/stop")
    public ResponseEntity<String> stop() {
        boolean stopped = fakeDataService.stop();
        return stopped
                ? ResponseEntity.ok("Faker stopped")
                : ResponseEntity.badRequest().body("Faker is not running");
    }

    @GetMapping("/status")
    public ResponseEntity<Boolean> status() {
        return ResponseEntity.ok(fakeDataService.isRunning());
    }
}