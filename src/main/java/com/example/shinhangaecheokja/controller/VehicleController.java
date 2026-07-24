package com.example.shinhangaecheokja.controller;

import com.example.shinhangaecheokja.dto.request.VehicleCreateRequest;
import com.example.shinhangaecheokja.dto.request.VehicleUpdateRequest;
import com.example.shinhangaecheokja.dto.response.VehicleResponse;
import com.example.shinhangaecheokja.service.VehicleService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

  private final VehicleService vehicleService;

  @PostMapping
  public ResponseEntity<VehicleResponse> registerVehicle(
      @RequestBody VehicleCreateRequest request) {
    return ResponseEntity.ok(vehicleService.registerVehicle(request));
  }

  @GetMapping("/{vehicleId}")
  public ResponseEntity<VehicleResponse> getVehicle(@PathVariable Long vehicleId) {
    return ResponseEntity.ok(vehicleService.getVehicle(vehicleId));
  }

  @GetMapping
  public ResponseEntity<List<VehicleResponse>> getVehicles() {
    return ResponseEntity.ok(vehicleService.getVehicles());
  }

  @PutMapping("/{vehicleId}")
  public ResponseEntity<VehicleResponse> updateVehicle(
      @PathVariable Long vehicleId, @RequestBody VehicleUpdateRequest request) {
    return ResponseEntity.ok(vehicleService.updateVehicle(vehicleId, request));
  }

  @DeleteMapping("/{vehicleId}")
  public ResponseEntity<Void> deleteVehicle(@PathVariable Long vehicleId) {
    vehicleService.deleteVehicle(vehicleId);
    return ResponseEntity.noContent().build();
  }
}
