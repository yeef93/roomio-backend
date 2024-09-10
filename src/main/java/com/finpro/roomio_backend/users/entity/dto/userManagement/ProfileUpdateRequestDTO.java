package com.finpro.roomio_backend.users.entity.dto.userManagement;

import com.finpro.roomio_backend.users.entity.Users;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Date;
import java.util.Optional;

@Data
@RequiredArgsConstructor
public class ProfileUpdateRequestDTO {

  private String firstname;
  private String lastname;
  private Long avatarId;
  private String phonenumbaer;
  private Date birthday;

  public Users dtoToEntity(Users user, ProfileUpdateRequestDTO requestDto) {
    Optional.ofNullable(requestDto.getFirstname()).ifPresent(user::setFirstname);
    Optional.ofNullable(requestDto.getLastname()).ifPresent(user::setLastname);
    Optional.ofNullable(requestDto.getPhonenumbaer()).ifPresent(user::setPhonenumber);
    Optional.ofNullable(requestDto.getBirthday()).ifPresent(user::setBirthdate);
    return user;
  }

}
