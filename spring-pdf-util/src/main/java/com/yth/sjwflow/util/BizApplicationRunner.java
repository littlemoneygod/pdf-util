package com.yth.sjwflow.util;

import com.aspose.pdf.License;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;

/**
 * 启动项配置
 *
 * @author littlemoneygod
 * @since 2022/6/15 13:55
 */
@Component
public class BizApplicationRunner implements ApplicationRunner {
    @Override
    public void run(ApplicationArguments args) throws Exception {
        try {
            FileInputStream fis = new FileInputStream("src/main/resources/license.xml");
            License license=new License();
            license.setLicense(fis);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
