#include <jni.h>
#include <string>
#include <unistd.h>
#include "usbrw.h"
#include<android/log.h>

#define TAG "myDemo-jni"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,TAG ,__VA_ARGS__) // 定义LOGD类型

class NewGlobalRef;

long Read_Timeout = 50 * 1000;

static jobject device_handle;
static jobject EndPointRead;
static jobject EndPointWrite;
static jclass usb_class = NULL;
jmethodID method_id;


long usb_sent(JNIEnv *env, long len, unsigned char *WriteBuf) {

    int total_frame = len / 64;
    int left = len % 64;
    int i = 0, s_len = 0;

    for (; i < total_frame; i++) {
        jbyteArray jarrRV = env->NewByteArray(64);
        env->SetByteArrayRegion(jarrRV, 0, 64, (jbyte *) WriteBuf + i * 64);
        s_len += env->CallIntMethod(device_handle, method_id, EndPointWrite, jarrRV, 64,
                                    Read_Timeout);
        env->DeleteLocalRef(jarrRV);
    }
    if (left > 0) {
        jbyteArray jarrRV = env->NewByteArray(left);
        env->SetByteArrayRegion(jarrRV, 0, left, (jbyte *) WriteBuf + i * 64);
        s_len += env->CallIntMethod(device_handle, method_id, EndPointWrite, jarrRV, left,
                                    Read_Timeout);
        env->DeleteLocalRef(jarrRV);
    }
    return s_len;

}

long usb_read(JNIEnv *env, unsigned char ReadBuf[]) {
    unsigned char *r_ReadBuf = (unsigned char *) malloc(2048 * sizeof(char));
    memset(r_ReadBuf, 0, 2048);
    int length = 0;
    int index = 0;
    int left = 0;
    jbyteArray jarrRV = env->NewByteArray(64);
    int r_len = env->CallIntMethod(device_handle, method_id, EndPointRead, jarrRV, 64,
                                   Read_Timeout);
    if (r_len < 0) {
//        LOGD("first read");
        return -6;
    }

    jbyte *bytes = env->GetByteArrayElements(jarrRV, 0);
    memcpy(r_ReadBuf, bytes, r_len);
//    for (int i = 0; i < r_len; i++) {
//        LOGD("data:%x", r_ReadBuf[i]);
//    }
    length = r_ReadBuf[0] + 1;
//    LOGD("length:%d", length);
    int total = length / 64;
    if (length > 64) {
        left = length % 64;
    }

    index = 64;
    for (int count = 1; count < total; count++) {
        jbyteArray jarrRV = env->NewByteArray(64);
        r_len = env->CallIntMethod(device_handle, method_id, EndPointRead, jarrRV, 64,
                                   Read_Timeout);
        if (r_len < 0) {
            return -6;
        }
        jbyte *jbyte = env->GetByteArrayElements(jarrRV, 0);

        memcpy(&r_ReadBuf[(count * 64)], jbyte, r_len);
        index = (count + 1) * 64;
        env->ReleaseByteArrayElements(jarrRV, jbyte, 0);
        env->DeleteLocalRef(jarrRV);
    }
    if (left > 0) {
        jbyteArray jarrRV = env->NewByteArray(64);
        r_len = env->CallIntMethod(device_handle, method_id, EndPointRead, jarrRV, 64,
                                   Read_Timeout);
        if (r_len < 0) {
            return -6;
        }
        jbyte *jbyte = env->GetByteArrayElements(jarrRV, 0);
        memcpy(&r_ReadBuf[index], jbyte, r_len);
        env->ReleaseByteArrayElements(jarrRV, jbyte, 0);
        env->DeleteLocalRef(jarrRV);
    }
//    for (int i = 0; i < length; i++) {
//        LOGD("----------------data[%d]:%x", i, r_ReadBuf[i]);
//    }
    memcpy(ReadBuf, r_ReadBuf, length);
    free(r_ReadBuf);
    env->ReleaseByteArrayElements(jarrRV, bytes, 0);
    env->DeleteLocalRef(jarrRV);
    return length;
}

long Reader_Init(JNIEnv *env, jobject deviceConnection,
                 jobject usbEpIn,
                 jobject usbEPOut) {


    device_handle = env->NewGlobalRef(deviceConnection);
    EndPointRead = env->NewGlobalRef(usbEpIn);
    EndPointWrite = env->NewGlobalRef(usbEPOut);

    jclass usb_class = env->GetObjectClass(deviceConnection);
    method_id = env->GetMethodID(usb_class, "bulkTransfer",
                                 "(Landroid/hardware/usb/UsbEndpoint;[BII)I");
    if (usb_class != NULL) {
        env->DeleteLocalRef(usb_class);
        usb_class = NULL;
    }
    return 0;
}

long Reader_Close(JNIEnv *env) {
    if (device_handle != NULL) {
        env->DeleteLocalRef(device_handle);
        device_handle = NULL;
    }
    if (EndPointRead != NULL) {
        env->DeleteGlobalRef(EndPointRead);
        EndPointRead = NULL;
    }
    if (EndPointWrite != NULL) {
        env->DeleteGlobalRef(EndPointWrite);
        EndPointWrite = NULL;
    }
    return 0;
}

