package com.yth.sjwflow.controller;

import com.yth.sjwflow.util.AsposePdfUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 功能模块-中文描述
 *
 * @author littlemoneygod
 * @since 2025/11/24 23:15
 */

@RestController
@RequestMapping("/api/pdf")
@CrossOrigin(origins = "*") // 允许跨域
public class PdfController {

    /**
     * 合并PDF文件接口
     *
     * @param files 上传的PDF文件列表
     * @return 合并后的PDF文件下载
     */
    @PostMapping(value = "/merge", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> mergePdfs(
            @RequestParam("files") List<MultipartFile> files) {

        try {
            // 1. 参数验证
            if (files == null || files.isEmpty()) {
                return ResponseEntity.badRequest().body("请上传PDF文件".getBytes());
            }

            if (files.size() < 2) {
                return ResponseEntity.badRequest().body("至少需要2个PDF文件进行合并".getBytes());
            }

            // 验证文件类型
            for (MultipartFile file : files) {
                if (!"application/pdf".equals(file.getContentType())) {
                    return ResponseEntity.badRequest().body("只能上传PDF文件".getBytes());
                }
            }
            String fileName = "mergeFile" + System.currentTimeMillis() + ".pdf";

            byte[] mergedPdfData = mergeExample(files);

            // 5. 设置响应头，触发文件下载
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", fileName);
            headers.setCacheControl("no-cache");
            headers.setContentLength(mergedPdfData.length);

            System.out.println("PDF合并完成，文件大小: " + mergedPdfData.length + " bytes");

            return new ResponseEntity<>(mergedPdfData, headers, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("合并失败: " + e.getMessage()).getBytes());
        }
    }


    @PostMapping(value = "/split", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> splitPdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam("splitPages") String splitPages) {

        String[] splitPageList = splitPages.split(",");
        List<Integer> pages = new ArrayList<>();
        for(int i=0;i<splitPageList.length;i++){
            pages.add(Integer.valueOf(splitPageList[i]));
        }
        // 这里应该是PDF拆分的实际逻辑
        // 模拟拆分过程
        byte[] splitPdfData = splitExample(file, pages);

        // 设置响应头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment",  "splitPdf.zip");
        headers.setCacheControl("no-cache");
        headers.setContentLength(splitPdfData.length);

        return new ResponseEntity<>(splitPdfData, headers, HttpStatus.OK);
    }

    private byte[] mergeExample(List<MultipartFile> files) {
        ByteArrayOutputStream fileStream = AsposePdfUtils.mergePdfFiles(files);
        return AsposePdfUtils.toByteArray(fileStream);
    }


    private byte[] splitExample(MultipartFile file, List<Integer> pages) {
        List<ByteArrayOutputStream> resultFiles = AsposePdfUtils.splitPdfByPages(file, pages);
        try {
            return AsposePdfUtils.packToZip(resultFiles);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}