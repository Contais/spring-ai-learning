package com.learn.ai.controller;

import com.learn.ai.enums.ChatScene;
import com.learn.ai.model.vo.Result;
import com.learn.ai.service.ChatOrchestratorService;
import com.learn.ai.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;


@RequiredArgsConstructor
@RestController
@RequestMapping("/pdf")
public class PdfController {

    private final ChatOrchestratorService chatOrchestratorService;

    private final FileService fileService;

    /**
     * 文件上传
     */
    @RequestMapping("/file/upload/{conversationId}")
    public Result<Void> uploadPdf(@PathVariable("conversationId") String conversationId, @RequestParam("file") MultipartFile file) {
        try {
            // 1. 校验文件是否为PDF格式
            if (!Objects.equals(file.getContentType(), "application/pdf")) {
                return Result.fail("只能上传PDF文件！");
            }
            // 2.保存文件
            boolean success = fileService.save(conversationId, file.getResource());
            if (!success) {
                return Result.fail("保存文件失败！");
            }
            return Result.ok();
        } catch (Exception e) {
//            log.error("Failed to upload PDF.", e);
            return Result.fail("上传文件失败！");
        }
    }

    /**
     * 文件下载
     */
    @GetMapping("/file/{conversationId}")
    public ResponseEntity<Resource> download(@PathVariable("conversationId") String conversationId) {
        // 1.读取文件
        Resource resource = fileService.getFile(conversationId);
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }
        // 2.文件名编码，写入响应头
        String filename = URLEncoder.encode(Objects.requireNonNull(resource.getFilename()), StandardCharsets.UTF_8);
        // 3.返回文件
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }

    @PostMapping(value = "/chat", produces = "text/html;charset=UTF-8")
    public Flux<String> streamChatOrchestrated(@RequestParam("prompt") String prompt,
                                               @RequestParam("conversationId") Long conversationId) {
        return chatOrchestratorService.streamMessage(ChatScene.PDF, conversationId, prompt);
    }
}
