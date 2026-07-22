package com.ruoyi.web.controller.system;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.dev33.satoken.annotation.SaIgnore;
import com.ruoyi.system.service.ISysFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/common/file/onlyoffice")
public class OnlyOfficeCallbackController {

    private final ISysFileService fileService;

//    @SaIgnore
    @PostMapping("/callback")
    public Map<String, Object> callback(@RequestBody String body) {
        JSONObject json = JSONUtil.parseObj(body);
        int status = json.getInt("status", 0);
        String downloadUrl = json.getStr("url");
        String key = json.getStr("key");

        log.info("OnlyOffice 回调: status={}, key={}", status, key);

        Map<String, Object> response = new HashMap<>();
        response.put("error", 0);

        if ((status == 2 || status == 6) && downloadUrl != null) {
            try {
                Long fileId = Long.parseLong(key.split("_")[0]);
                byte[] fileBytes = HttpUtil.downloadBytes(downloadUrl);
                fileService.overwriteFile(fileId, fileBytes);
                log.info("OnlyOffice 保存成功: fileId={}", fileId);
            } catch (Exception e) {
                log.error("OnlyOffice 保存失败", e);
                response.put("error", 1);
            }
        }

        return response;
    }
}
