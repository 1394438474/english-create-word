package com.smartvocab.module.upload.controller;

import com.smartvocab.common.R;
import com.smartvocab.common.SecurityContext;
import com.smartvocab.module.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class UploadController {

    private final UserService userService;

    @PostMapping("/avatar")
    public R<Map<String, String>> avatar(@RequestParam MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) return R.fail("文件为空");
        String ext = getExt(file.getOriginalFilename());
        String dir = "avatar/" + LocalDate.now() + "/";
        File target = new File("./upload/" + dir);
        if (!target.exists() && !target.mkdirs()) throw new IOException("无法创建目录");
        String name = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        File dest = new File(target, name);
        file.transferTo(dest);
        String url = "/api/static/" + dir + name;
        userService.updateProfile(SecurityContext.getUserId(), null, null, url);
        return R.ok(Map.of("url", url));
    }

    private static String getExt(String name) {
        if (name == null) return "png";
        int i = name.lastIndexOf('.');
        return i < 0 ? "png" : name.substring(i + 1);
    }
}
