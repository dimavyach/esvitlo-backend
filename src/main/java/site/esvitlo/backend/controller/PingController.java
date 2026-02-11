package site.esvitlo.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import site.esvitlo.backend.domain.Device;
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

        String auth = request.getHeader("Authorization");
        String deviceKey = auth.substring(7).trim(); // "Device "

        Device device = deviceService.findByKeyOrThrow(deviceKey);
        deviceService.registerPing(device);

        return ResponseEntity.ok().build();
    }

}