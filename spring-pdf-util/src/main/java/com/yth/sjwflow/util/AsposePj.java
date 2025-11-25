package com.yth.sjwflow.util;

import javassist.*;

import java.io.*;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

/**
 * 功能模块-中文描述
 *
 * @author littlemoneygod
 * @since 2025/11/22 00:07
 */
public class AsposePj {
    private static final String Desktop="/Users/littlemoneygod/Downloads/";
    public static void crackAsposePdfJar(String jarName) {
        try {
            ClassPool.getDefault().insertClassPath(jarName);
            CtClass ctClass = ClassPool.getDefault().getCtClass("com.aspose.pdf.ADocument");
            CtMethod[] declaredMethods = ctClass.getDeclaredMethods();
            int num = 0;
            for (int i = 0; i < declaredMethods.length; i++) {
                if (num == 2) {
                    break;
                }
                CtMethod method = declaredMethods[i];
                CtClass[] ps = method.getParameterTypes();
                if (ps.length == 2
                        && method.getName().equals("lI")
                        && ps[0].getName().equals("com.aspose.pdf.ADocument")
                        && ps[1].getName().equals("int")) {
                    //源码ADocument类的这个方法限制页数：
                    // static boolean lI(ADocument var0, int var1) {
                    //        return !lb() && !lj() && !var0.lt() && var1 > 4;
                    //    }
                    // 最多只能转换4页，处理返回false，无限制页数
                    System.out.println(method.getReturnType());
                    System.out.println(ps[1].getName());
                    method.setBody("{return false;}");
                    num = 1;
                }
                if (ps.length == 0 && method.getName().equals("lt")) {
                    // 水印处理
                    method.setBody("{return true;}");
                    num = 2;
                }
            }
            //修改完，把类的输出到指定目录（桌面下）
            ctClass.writeFile(Desktop);
        } catch (NotFoundException e) {
            e.printStackTrace();
        } catch (CannotCompileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
