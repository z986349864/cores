package com.zd.core.excel.service;

import com.zd.core.excel.vo.CoreImportResultVO;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    CoreImportResultVO batchUploadUserList(MultipartFile file);
}
