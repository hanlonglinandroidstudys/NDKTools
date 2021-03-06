//
// Created by hanlonglin on 2019/3/21.
//

/*
 * Copyright 2014 http://Bither.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include <string.h>
#include <android/bitmap.h>
#include <android/log.h>
#include <jni.h>
#include <stdio.h>
#include <setjmp.h>
#include <math.h>
#include <stdint.h>
#include <time.h>

extern "C" {    //这些一定要放在extern “C”里，不然会报错
    #include "jpeglib.h"
    #include "cdjpeg.h"        /* Common decls for cjpeg/djpeg applications */
    #include "jversion.h"        /* for version message */
    #include "config.h"
}


#define LOG_TAG "jni"
#define LOGW(...)  __android_log_write(ANDROID_LOG_WARN,LOG_TAG,__VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

#define true 1
#define false 0

typedef uint8_t BYTE;

char *error;
struct my_error_mgr {
    struct jpeg_error_mgr pub;
    jmp_buf setjmp_buffer;
};

typedef struct my_error_mgr *my_error_ptr;

METHODDEF(void)
my_error_exit(j_common_ptr cinfo) {
    my_error_ptr myerr = (my_error_ptr) cinfo->err;
    (*cinfo->err->output_message)(cinfo);
    error = (char *) myerr->pub.jpeg_message_table[myerr->pub.msg_code];
    LOGE("jpeg_message_table[%d]:%s", myerr->pub.msg_code,
         myerr->pub.jpeg_message_table[myerr->pub.msg_code]);
// LOGE("addon_message_table:%s", myerr->pub.addon_message_table);
//  LOGE("SIZEOF:%d",myerr->pub.msg_parm.i[0]);
//  LOGE("sizeof:%d",myerr->pub.msg_parm.i[1]);
    longjmp(myerr->setjmp_buffer, 1);
}


/**
 * 压缩图片主方法
 * @param data
 * @param w
 * @param h
 * @param quality
 * @param outfilename
 * @param optimize
 * @return
 */
int generateJPEG(BYTE *data, int w, int h, int quality,
                 const char *outfilename, jboolean optimize) {
    int nComponent = 3;

    struct jpeg_compress_struct jcs;

    struct my_error_mgr jem;

    jcs.err = jpeg_std_error(&jem.pub);
    jem.pub.error_exit = my_error_exit;
    if (setjmp(jem.setjmp_buffer)) {
        return 0;
    }
    jpeg_create_compress(&jcs);
    FILE *f = fopen(outfilename, "wb");
    if (f == NULL) {
        return 0;
    }
    jpeg_stdio_dest(&jcs, f);
    jcs.image_width = w;
    jcs.image_height = h;
    if (optimize) {
        LOGI("optimize==ture");
    } else {
        LOGI("optimize==false");
    }

    jcs.arith_code = false;
    jcs.input_components = nComponent;
    if (nComponent == 1)
        jcs.in_color_space = JCS_GRAYSCALE;
    else
        jcs.in_color_space = JCS_RGB;

    //全部设置默认参数
    jpeg_set_defaults(&jcs);
    //是否采用哈夫曼算法，质量相差5-10倍
    jcs.optimize_coding = optimize;
    //设置质量
    jpeg_set_quality(&jcs, quality, true);
    //开始压缩（是否写入全部像素）
    jpeg_start_compress(&jcs, TRUE);

    JSAMPROW row_pointer[1];
    int row_stride;
    //一行rgb的数量
    row_stride = jcs.image_width * nComponent;
    //一行行遍历
    while (jcs.next_scanline < jcs.image_height) {
        //得到一行的首地址
        row_pointer[0] = &data[jcs.next_scanline * row_stride];
        //此方法会将jcs.next_scanlines加1
        jpeg_write_scanlines(&jcs, row_pointer, 1);
    }

    if (jcs.optimize_coding) {
        LOGI("optimize==ture");
    } else {
        LOGI("optimize==false");
    }
    jpeg_finish_compress(&jcs);
    jpeg_destroy_compress(&jcs);
    fclose(f);

    return 1;
}

typedef struct {
    uint8_t r;
    uint8_t g;
    uint8_t b;
} rgb;

/**
 * byte转 char*
 * @param env
 * @param barr
 * @return
 */
char *jstrinTostring(JNIEnv *env, jbyteArray barr) {
    char *rtn = NULL;
    jsize alen = env->GetArrayLength(barr);
    jbyte *ba = env->GetByteArrayElements(barr, 0);
    if (alen > 0) {
        rtn = (char *) malloc(alen + 1);
        memcpy(rtn, ba, alen);
        rtn[alen] = 0;
    }
    env->ReleaseByteArrayElements(barr, ba, 0);
    return rtn;
}

jbyteArray stoJstring(JNIEnv *env, const char *pat, int len) {
    jbyteArray bytes = env->NewByteArray(len);
    env->SetByteArrayRegion(bytes, 0, len, (jbyte *) pat);
    jsize alen = env->GetArrayLength(bytes);
    return bytes;
}

jstring Java_net_bither_util_NativeUtil_compressBitmap(JNIEnv *env,
                                                       jobject thiz, jobject bitmapcolor, int w,
                                                       int h, int quality,
                                                       jbyteArray fileNameStr, jboolean optimize) {

    AndroidBitmapInfo infocolor;
    BYTE *pixelscolor;
    int ret;
    BYTE *data;
    BYTE *tmpdata;
    char *fileName = jstrinTostring(env, fileNameStr);
    if ((ret = AndroidBitmap_getInfo(env, bitmapcolor, &infocolor)) < 0) {
        LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
        return env->NewStringUTF("0");;
    }
    if ((ret = AndroidBitmap_lockPixels(env, bitmapcolor, (void **) &pixelscolor)) < 0) {
        LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
    }

    BYTE r, g, b;
    data = NULL;
    data = (BYTE *) malloc(w * h * 3);
    tmpdata = data;
    int j = 0, i = 0;
    int color;
    for (i = 0; i < h; i++) {
        for (j = 0; j < w; j++) {
            color = *((int *) pixelscolor);
            r = ((color & 0x00FF0000) >> 16);
            g = ((color & 0x0000FF00) >> 8);
            b = color & 0x000000FF;
            *data = b;
            *(data + 1) = g;
            *(data + 2) = r;
            data = data + 3;
            pixelscolor += 4;

        }

    }
    AndroidBitmap_unlockPixels(env, bitmapcolor);
    int resultCode = generateJPEG(tmpdata, w, h, quality, fileName, optimize);
    free(tmpdata);
    if (resultCode == 0) {
        jstring result = env->NewStringUTF(error);
        error = NULL;
        return result;
    }
    return env->NewStringUTF("1"); //success
}


void jstringTostring(JNIEnv *env, jstring jstr, char *output, int *de_len) {
    *output = NULL;
    jclass clsstring = env->FindClass("java/lang/String");
    jstring strencode = env->NewStringUTF("utf-8");
    jmethodID mid = env->GetMethodID(clsstring, "getBytes",
                                     "(Ljava/lang/String;)[B");
    jbyteArray barr = (jbyteArray) env->CallObjectMethod(jstr, mid,
                                                         strencode);
    jsize alen = env->GetArrayLength(barr);
    *de_len = alen;
    jbyte *ba = env->GetByteArrayElements(barr, JNI_FALSE);
    if (alen > 0) {
        output = (char *) malloc(alen + 1);
        memcpy(output, ba, alen);
        output[alen] = 0;
    }
    env->ReleaseByteArrayElements(barr, ba, 0);
}



/**
 * 压缩图片步骤
 * 1.将bitmap解码，并转换成RGB数据
 * 2.JPEG对象分配空间以及初始化
 * 3.指定压缩数据源
 * 4.获取文件信息
 * 5.为压缩设置参数，比如图片大小，类型，颜色空间
 * 6.开始压缩
 * 7.压缩结束
 * 8.释放资源
 */
extern "C"
JNIEXPORT jstring JNICALL
Java_hanlonglin_com_ndktools_ImageUtil_ImageCompressWithCPP_compressBitmap(JNIEnv *env,
                                                                           jobject instance,
                                                                           jobject bitmapcolor,
                                                                           jint w,
                                                                           jint h, jint quality,
                                                                           jbyteArray filename_,
                                                                           jboolean optimize) {
    char *filenameStr = jstrinTostring(env, filename_);
    BYTE *pixelscolor;

//    AndroidBitmapInfo infocolor;
//    int ret;
//    if ((ret = AndroidBitmap_getInfo(env, bitmapcolor, &infocolor)) < 0) {
//        LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
//        return env->NewStringUTF("0");
//    }
    //1.将bitmap里的所有信息读取出来,保存到二维byte数组里
    //处理bitmap图形信息的方法1 锁定画布
    AndroidBitmap_lockPixels(env, bitmapcolor, (void **) &pixelscolor);

    //2.解析每一个向所的rgb值（去掉alpha值），保存到一维数组data里面
    BYTE *data;
    BYTE a, r, g, b;
    data = (BYTE *) malloc(w * h * 3);
    BYTE *tempdata;
    tempdata = data; //临时保存data的首地址

    int i = 0, j = 0;
    int color;
    for (i = 0; i < h; i++) {
        for (j = 0; j < w; j++) {
            //解决掉alpha
            //获取二维数组的每一个像素信息（四个部分a/r/g/b）
            color = *((int *) pixelscolor);
//            a=((color & 0x00FF0000)>>24);
            r = ((color & 0x00FF0000) >> 16);
            g = ((color & 0x0000FF00) >> 8);
            b = ((color & 0x000000FF));

            //改值---保存到data一维数组里面
            *data = b;
            *(data + 1) = g;
            *(data + 2) = r;
            data = data + 3;
            //一个像素包括四个像素值  每一次+4就是取下一个像素点
            pixelscolor += 4;
        }
    }

    //处理bitmap图形信息的方法2 解锁
    AndroidBitmap_unlockPixels(env, bitmapcolor);

    //调用jpeg核心方法 实现压缩
    int ret = generateJPEG(tempdata, w, h, quality, filenameStr, optimize);

    if (ret == 0) {
        return env->NewStringUTF("0"); //失败
    } else {
        return env->NewStringUTF("1"); //成功
    }
}

extern "C"
JNIEXPORT jstring JNICALL
Java_hanlonglin_com_ndktools_ImageUtil_ImageCompressWithCPP_compressBitmap2(JNIEnv *env,
                                                                            jobject thiz,
                                                                            jobject bitmapcolor,
                                                                            jint w,
                                                                            jint h, jint quality,
                                                                            jbyteArray fileNameStr,
                                                                            jboolean optimize) {

    AndroidBitmapInfo infocolor;
    BYTE *pixelscolor;
    int ret;
    BYTE *data;
    BYTE *tmpdata;
    char *fileName = jstrinTostring(env, fileNameStr);
    if ((ret = AndroidBitmap_getInfo(env, bitmapcolor, &infocolor)) < 0) {
        LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
        return env->NewStringUTF("0");;
    }
    if ((ret = AndroidBitmap_lockPixels(env, bitmapcolor, (void **) &pixelscolor)) < 0) {
        LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
    }

    BYTE r, g, b;
    data = NULL;
    data = (BYTE *) malloc(w * h * 3);
    tmpdata = data;
    int j = 0, i = 0;
    int color;
    for (i = 0; i < h; i++) {
        for (j = 0; j < w; j++) {
            color = *((int *) pixelscolor);
            r = ((color & 0x00FF0000) >> 16);
            g = ((color & 0x0000FF00) >> 8);
            b = color & 0x000000FF;
            *data = b;
            *(data + 1) = g;
            *(data + 2) = r;
            data = data + 3;
            pixelscolor += 4;

        }

    }
    AndroidBitmap_unlockPixels(env, bitmapcolor);
    int resultCode = generateJPEG(tmpdata, w, h, quality, fileName, optimize);
    free(tmpdata);
    if (resultCode == 0) {
        jstring result = env->NewStringUTF(error);
        error = NULL;
        return result;
    }
    return env->NewStringUTF("1"); //success
}