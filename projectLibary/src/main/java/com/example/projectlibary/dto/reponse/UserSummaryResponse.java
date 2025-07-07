package com.example.projectlibary.dto.reponse;

import lombok.*;

import java.io.Serializable;
@Builder
@Value
public class UserSummaryResponse implements Serializable {
     Long id;
     String username;
     String fullName;
      String avatarUrl;
}
