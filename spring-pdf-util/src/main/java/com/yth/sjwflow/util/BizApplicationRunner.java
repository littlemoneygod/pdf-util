package com.yth.sjwflow.util;

import com.aspose.pdf.License;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.lang.reflect.Modifier;

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
        getPdfLicense();
        getWordLicense();
    }

    public void getPdfLicense(){
        try {
            FileInputStream fis = new FileInputStream("src/main/resources/license.xml");
            License license=new License();
            license.setLicense(fis);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void getWordLicense() {
        try{Class<?> aClass = Class.forName("com.aspose.words.zzXyu");
            java.lang.reflect.Field zzYAC = aClass.getDeclaredField("zzZXG");
            zzYAC.setAccessible(true);

            java.lang.reflect.Field modifiersField = zzYAC.getClass().getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(zzYAC, zzYAC.getModifiers() & ~Modifier.FINAL);
            zzYAC.set(null,new byte[]{76, 73, 67, 69, 78, 83, 69, 68});
        }catch (Exception e){
            System.out.println("破解失败");
        }
    }
}
