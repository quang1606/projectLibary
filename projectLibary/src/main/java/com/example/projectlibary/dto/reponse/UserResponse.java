package com.example.projectlibary.dto.reponse;

import com.example.projectlibary.common.UserRole;
import lombok.*;

import java.io.Serializable;

@Builder
@Value
public class UserResponse implements Serializable {
     Long id;
     String username;
     String email;
     String fullName;
     String studentId;
     String phoneNumber;
     String avatar;
     UserRole role;
     boolean isActive;

}
