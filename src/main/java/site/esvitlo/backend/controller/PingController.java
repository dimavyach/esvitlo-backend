package site.esvitlo.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.server.ResponseStatusException;
import site.esvitlo.backend.domain.Device;
import site.esvitlo.backend.security.DeviceAuthFilter;
import site.esvitlo.backend.service.DeviceService;

@RestController
@RequestMapping("/api/v1")
public class PingController {

    private final DeviceService deviceService;

    public PingController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @PostMapping("/ping")
    public ResponseEntity<Void> ping(HttpServletRequest request) {

        Object attr = request.getAttribute(DeviceAuthFilter.DEVICE_KEY_ATTR);
        if (!(attr instanceof String deviceKey) || deviceKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        Device device = deviceService.findByKeyOrThrow(deviceKey);
        deviceService.registerPing(device);

        return ResponseEntity.ok().build();
    }

}