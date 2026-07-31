package com.example.shinhangaecheokja.vehicle.controller;

import com.example.shinhangaecheokja.vehicle.dto.request.VehicleCreateRequest;
import com.example.shinhangaecheokja.vehicle.dto.request.VehicleUpdateRequest;
import com.example.shinhangaecheokja.vehicle.dto.response.VehicleResponse;
import com.example.shinhangaecheokja.vehicle.entity.Vehicle;
import com.example.shinhangaecheokja.vehicle.service.VehicleService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Vehicle CRUD API를 제공하는 컨트롤러. */
@RestController
@RequestMapping("/api/v1/vehicles")
@RequiredArgsConstructor
public class VehicleController {

  private final VehicleService vehicleService;

  /** 운송수단을 등록한다. */
  @PostMapping
  public ResponseEntity<VehicleResponse> registerVehicle(
      @RequestBody @Valid VehicleCreateRequest request) {
    Vehicle created = vehicleService.registerVehicle(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(VehicleResponse.from(created));
  }

  /** 운송수단 단건을 조회한다. */
  @GetMapping("/{vehicleId}")
  public ResponseEntity<VehicleResponse> getVehicle(@PathVariable Long vehicleId) {
    return ResponseEntity.ok(VehicleResponse.from(vehicleService.getVehicle(vehicleId)));
  }

  /** 운송수단 전체 목록을 조회한다. */
  @GetMapping
  public ResponseEntity<List<VehicleResponse>> getVehicles() {
    List<VehicleResponse> responses =
        vehicleService.getVehicles().stream().map(VehicleResponse::from).toList();
    return ResponseEntity.ok(responses);
  }

  /** 운송수단 정보를 수정한다. */
  @PutMapping("/{vehicleId}")
  public ResponseEntity<VehicleResponse> updateVehicle(
      @PathVariable Long vehicleId, @RequestBody @Valid VehicleUpdateRequest request) {
    Vehicle updated = vehicleService.updateVehicle(vehicleId, request);
    return ResponseEntity.ok(VehicleResponse.from(updated));
  }

  /** 운송수단을 삭제한다. */
  @DeleteMapping("/{vehicleId}")
  public ResponseEntity<Void> deleteVehicle(@PathVariable Long vehicleId) {
    vehicleService.deleteVehicle(vehicleId);
    return ResponseEntity.noContent().build();
  }
}
