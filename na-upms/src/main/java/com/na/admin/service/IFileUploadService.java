package com.na.admin.service;

import com.na.common.result.Result;
import org.springframework.web.multipart.MultipartFile;

public interface IFileUploadService {

    Result upload(MultipartFile file);
}
