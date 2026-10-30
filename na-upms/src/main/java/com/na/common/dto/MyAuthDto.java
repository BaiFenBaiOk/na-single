package com.na.common.dto;

import com.na.auth.model.AuthUserModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MyAuthDto extends AuthUserModel {
    private String token;
}
