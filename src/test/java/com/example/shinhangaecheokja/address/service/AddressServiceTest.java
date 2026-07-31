package com.example.shinhangaecheokja.address.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.shinhangaecheokja.address.dto.request.AddressCreateRequest;
import com.example.shinhangaecheokja.address.dto.request.AddressUpdateRequest;
import com.example.shinhangaecheokja.address.entity.Address;
import com.example.shinhangaecheokja.address.repository.AddressRepository;
import com.example.shinhangaecheokja.common.exception.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

  @Mock private AddressRepository addressRepository;
  @InjectMocks private AddressService addressService;

  @Test
  @DisplayName("특정 회원의 주소 목록을 조회한다")
  void getAddressesByMemberIdSuccess() {
    Address addr1 =
        Address.builder()
            .id(1L)
            .memberId(10L)
            .alias("집")
            .address("서울시 강남구")
            .detailAddress("101호")
            .pickupGuide("문 앞")
            .build();
    when(addressRepository.findByMemberId(10L)).thenReturn(List.of(addr1));

    List<Address> result = addressService.getAddresses(10L);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getAlias()).isEqualTo("집");
  }

  @Test
  @DisplayName("신규 주소를 생성한다")
  void createAddressNewAddressSuccess() {
    AddressCreateRequest request = new AddressCreateRequest("회사", "서울시 서초구", "202호", "경비실 맡김");
    Address saved =
        Address.builder()
            .id(2L)
            .memberId(10L)
            .alias("회사")
            .address("서울시 서초구")
            .detailAddress("202호")
            .pickupGuide("경비실 맡김")
            .build();

    when(addressRepository.save(any(Address.class))).thenReturn(saved);

    Address response = addressService.createAddress(10L, request);

    assertThat(response.getId()).isEqualTo(2L);
    assertThat(response.getAlias()).isEqualTo("회사");
  }

  @Test
  @DisplayName("주소를 수정한다")
  void updateAddressModifySuccess() {
    Address existing =
        Address.builder()
            .id(1L)
            .memberId(10L)
            .alias("집")
            .address("서울시 강남구")
            .detailAddress("101호")
            .pickupGuide("문 앞")
            .build();

    AddressUpdateRequest request = new AddressUpdateRequest("우리집", "서울시 강남구 역삼동", "303호", "직접 전달");

    when(addressRepository.findByIdAndMemberId(1L, 10L)).thenReturn(Optional.of(existing));

    Address response = addressService.updateAddress(1L, 10L, request);

    assertThat(response.getAlias()).isEqualTo("우리집");
    assertThat(response.getDetailAddress()).isEqualTo("303호");
  }

  @Test
  @DisplayName("주소를 삭제한다")
  void deleteAddressDeleteSuccess() {
    Address existing = Address.builder().id(1L).memberId(10L).alias("집").address("서울시 강남구").build();
    when(addressRepository.findByIdAndMemberId(1L, 10L)).thenReturn(Optional.of(existing));

    addressService.deleteAddress(1L, 10L);

    verify(addressRepository).delete(existing);
  }

  @Test
  @DisplayName("존재하지 않거나 권한없는 주소 수정시 EntityNotFoundException을 던진다")
  void updateAddressNotFoundOrUnauthorizedShouldThrowException() {
    AddressUpdateRequest request = new AddressUpdateRequest("우리집", "서울시 강남구 역삼동", "303호", "직접 전달");
    when(addressRepository.findByIdAndMemberId(999L, 10L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> addressService.updateAddress(999L, 10L, request))
        .isInstanceOf(EntityNotFoundException.class);
  }
}
