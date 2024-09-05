package com.miniproject.eventastic.users.entity.dto.userManagement;

import com.miniproject.eventastic.users.entity.Users;
import java.util.Date;
import java.util.Optional;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class ProfileUpdateRequestDTO {

  private String fullName;
  private Long avatarId;
  private String bio;
  private Date birthday;

  public Users dtoToEntity(Users user, ProfileUpdateRequestDTO requestDto) {
    Optional.ofNullable(requestDto.getFullName()).ifPresent(user::setFullName);
    Optional.ofNullable(requestDto.getBio()).ifPresent(user::setBio);
    Optional.ofNullable(requestDto.getBirthday()).ifPresent(user::setBirthday);
    return user;
  }

}
