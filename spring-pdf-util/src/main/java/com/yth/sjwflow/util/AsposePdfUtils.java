package com.yth.sjwflow.util;

import com.aspose.pdf.Document;
import com.aspose.pdf.facades.PdfFileEditor;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * PDF工具类 - 基于Aspose.PDF
 * 功能：PDF合并、按页数分割
 */
public class AsposePdfUtils {

    /**
     * 合并多个PDF文件为一个PDF
     * @param inputPaths 输入PDF文件路径列表
     * @return 是否合并成功
     */
    public static ByteArrayOutputStream mergePdfFiles(List<MultipartFile> inputPaths) {
        if (inputPaths == null || inputPaths.isEmpty()) {
            throw new IllegalArgumentException("输入文件列表不能为空");
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            InputStream[] files = convertMultipartFilesToInputStreamArray(inputPaths);
            PdfFileEditor editor = new PdfFileEditor();
            // 执行合并操作
            editor.concatenate(files, byteArrayOutputStream);
        } catch (Exception e) {
            System.err.println("PDF合并失败: " + e.getMessage());
            e.printStackTrace();
        }
        return byteArrayOutputStream;
    }

    public static InputStream[] convertMultipartFilesToInputStreamArray(List<MultipartFile> inputFiles) throws IOException {
        if (inputFiles == null || inputFiles.isEmpty()) {
            return new InputStream[0];
        }

        InputStream[] inputStreams = new InputStream[inputFiles.size()];

        for (int i = 0; i < inputFiles.size(); i++) {
            MultipartFile file = inputFiles.get(i);
            if (!file.isEmpty()) {
                inputStreams[i] = file.getInputStream();
            }
        }

        return inputStreams;
    }

    /**
     * 按页数分割PDF文件
     * @param pages 每个分割文件的页数
     * @return 分割后的文件路径列表
     */
    public static List<ByteArrayOutputStream> splitPdfByPages(MultipartFile file, List<Integer> pages) {
        List<ByteArrayOutputStream> results = new ArrayList<>();
        try {
            InputStream ins = file.getInputStream();
            // 缓存到字节数组
            byte[] cachedData = cacheInputStream(ins);
            PdfFileEditor editor = new PdfFileEditor();

            int totalPages = 0;
            try (InputStream newStream = new ByteArrayInputStream(cachedData)) {
                // 打开源文档获取总页数
                Document sourceDoc = new Document(newStream);
                totalPages = sourceDoc.getPages().size();
                sourceDoc.close();
                pages.add(totalPages);
            }

            if (totalPages == 0) {
                throw new IllegalArgumentException("PDF文件没有页面");
            }

            //从小到大排序
            Collections.sort(pages);
            for(int i=0; i< pages.size(); i++){
                if(i == 0){
                    try (InputStream newStream = new ByteArrayInputStream(cachedData)) {
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        editor.extract(newStream, 1, pages.get(i), byteArrayOutputStream);
                        results.add(byteArrayOutputStream);
                    }
                }else {
                    try (InputStream newStream = new ByteArrayInputStream(cachedData)) {
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        editor.extract(newStream, pages.get(i-1) + 1, pages.get(i), byteArrayOutputStream);
                        results.add(byteArrayOutputStream);
                    }
                }

            }
        } catch (Exception e) {
            System.err.println("PDF分割失败: " + e.getMessage());
            e.printStackTrace();
        }
        return results;
    }

    /**
     * 将 InputStream 转换为可重复读取的 byte[]
     */
    public static byte[] cacheInputStream(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return new byte[0];
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            return baos.toByteArray();
        } finally {
            inputStream.close();
        }
    }


    /**
     * 获取PDF文件页数
     * @param filePath PDF文件路径
     * @return 页数
     */
    public static int getPageCount(String filePath) {
        Document doc = null;
        try {
            doc = new Document(filePath);
            return doc.getPages().size();
        } catch (Exception e) {
            System.err.println("获取页数失败: " + e.getMessage());
            return 0;
        } finally {
            if (doc != null) {
                doc.close();
            }
        }
    }

    /**
     * 将 ByteArrayOutputStream 转换为 byte[]
     */
    public static byte[] toByteArray(ByteArrayOutputStream baos) {
        if (baos == null) {
            return new byte[0];
        }
        return baos.toByteArray();
    }

    /**
     * 将 ByteArrayOutputStream 数组打包为 ZIP 并转换为 byte[]
     * @param streams ByteArrayOutputStream 数组
     * @return ZIP 包的字节数组
     */
    public static byte[] packToZip(List<ByteArrayOutputStream> streams) throws IOException {
        try (ByteArrayOutputStream zipBaos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(zipBaos)) {

            for (int i = 0; i < streams.size(); i++) {
                ByteArrayOutputStream stream = streams.get(i);
                String filename = "splitPdf" + (i+1) + ".pdf";

                if (stream == null || filename == null || filename.trim().isEmpty()) {
                    continue; // 跳过无效条目
                }

                // 创建 ZIP 条目
                ZipEntry entry = new ZipEntry(filename);
                zos.putNextEntry(entry);

                // 写入数据
                byte[] data = stream.toByteArray();
                zos.write(data);

                // 关闭当前条目
                zos.closeEntry();
            }

            // 完成 ZIP 包
            zos.finish();

            // 返回 ZIP 包的字节数组
            return zipBaos.toByteArray();
        }
    }

}