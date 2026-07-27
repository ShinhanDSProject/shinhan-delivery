package com.example.shinhangaecheokja.repository;

import com.example.shinhangaecheokja.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);   //데이터베이스에서 이메일이 똑같은 회원을 찾아서 가져와
                                                //optimal덕분에 없어도 에러가 안남
    boolean existsByEmail(String email);    //데이터베이스에 이메일이 똑같은 외원이 존재하는지 확인
}
//회원가입할때 중복 이메일이 있는지 스캔(existByEmail)하고 로그인할떄 회원의 정보를 안전하게 꺼내오는(findByEmail) 역할