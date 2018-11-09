#include <jni.h>
#include "dp_api.h"
#include "Fp16Convert.h"
#include "mv_types.h"
#include "interpret_output.h"
#include "Common.h"
#include "Region.h"

#include <android/log.h>
#define LOG_TAG     "Deepano"
#define ALOGE(...)      __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

// jni extern
EXTERN {
JNIEXPORT jint JNICALL
Java_com_deepano_dpnandroidsample_DeepanoApiFactory_initDevice(
        JNIEnv *env,
        jobject /* this */,jint fd);

JNIEXPORT jint JNICALL
Java_com_deepano_dpnandroidsample_DeepanoApiFactory_startCamera(
        JNIEnv *env,
        jobject /* this */);

JNIEXPORT jint JNICALL
Java_com_deepano_dpnandroidsample_DeepanoApiFactory_getFrameBuffer(
        JNIEnv *env,
        jobject /* this */,jbyteArray frameBuffer);

JNIEXPORT jint JNICALL
Java_com_deepano_dpnandroidsample_DeepanoApiFactory_netProc(
        JNIEnv *env,
        jobject /* this */,jstring blobPath);
};

typedef enum NET_CAFFE_TENSFLOW
{
    DP_AGE_NET=0,
    DP_ALEX_NET,
    DP_GOOGLE_NET,
    DP_GENDER_NET,
    DP_TINI_YOLO_NET,
    DP_SSD_MOBILI_NET,
    DP_RES_NET,
    DP_SQUEEZE_NET,
    DP_MNIST_NET,
    DP_INCEPTION_V1,
    DP_INCEPTION_V2,
    DP_INCEPTION_V3,
    DP_INCEPTION_V4,
    DP_MOBILINERS_NET,
    DP_ALI_FACENET,
    DP_TINY_YOLO_V2_NET,
    DP_CAFFE_NET,
} DP_MODEL_NET;

//
dp_img_t* g_img = static_cast<dp_img_t *>(malloc(sizeof(dp_img_t)));;
jint devStatus = -1;
dp_image_box_t BLOB_IMAGE_SIZE={0,1280,0,960};
dp_image_box_t box_demo[100];
char categoles[100][20];
int num_box_demo=0;

// common extern
extern void video_callback(dp_img_t *img, void *param);
extern void box_callback_model_demo(void *result,void *param);

JNIEXPORT jint JNICALL
Java_com_deepano_dpnandroidsample_DeepanoApiFactory_initDevice(
        JNIEnv *env,
        jobject /* this */,jint fd){
    int ret;
    ret = dp_init(fd);
    if(ret == 0){
        ALOGE("init device successfully\n");
    }else{
        ALOGE("init device failed\n");
    }
    return ret;
}

JNIEXPORT jint JNICALL
Java_com_deepano_dpnandroidsample_DeepanoApiFactory_startCamera(
        JNIEnv *env,
        jobject /* this */){
    if(devStatus == -1){
        ALOGE("Please init device first!\n");
        return -1;
    }else{
        int ret;
        int param = 15;
        dp_register_video_frame_cb(video_callback,&param);
        ret = dp_start_camera_video();
        if(ret == 0){
            ALOGE("start video successfully\n");
        } else{
            ALOGE("start video failed!\n");
            return -1;
        }
    }
    return 0;
}

JNIEXPORT jint JNICALL
Java_com_deepano_dpnandroidsample_DeepanoApiFactory_getFrameBuffer(
        JNIEnv *env,
        jobject /* this */,jbyteArray frameBuffer){
    if(g_img && g_img->img){
        ALOGE("\nyuvBuffer :: first Y = %d\n",g_img->img[0]);
        env->SetByteArrayRegion(frameBuffer, 0,1280*960*3/2,
                                reinterpret_cast<const jbyte *>(g_img->img));
    }else{
        return -1;
    }
    return 0;
}

JNIEXPORT jint JNICALL
Java_com_deepano_dpnandroidsample_DeepanoApiFactory_netProc(
        JNIEnv *env,
        jobject /* this */,jstring blobPath){
    jint ret ;
    jint blob_nums = 1; //numbers of the blobs；
    dp_blob_parm_t parms = {0,300,300,707*2}; // NN image input size
    dp_netMean mean={127.5,127.5,127.5,127.5}; //average && std

    const char* path = env->GetStringUTFChars(blobPath, 0);
    ALOGE("Blob Path = %s\n",path);

    dp_set_blob_image_size(&BLOB_IMAGE_SIZE); //here is 1280*960;it can be modified to a customization size
    dp_set_blob_parms(blob_nums,&parms); // transfer blob params
    dp_set_blob_mean_std(blob_nums,&mean); //transfer average && std


    ret = dp_update_model(path); // transfer blob model
    if (ret == 0) {
        ALOGE("Test dp_update_model sucessfully!\n");
    }
    else {
        ALOGE("Test dp_update_model failed !\n");
        return -1;
    }

    DP_MODEL_NET net = DP_SSD_MOBILI_NET;
    dp_register_video_frame_cb(video_callback, &net); // video callback
    dp_register_box_device_cb(box_callback_model_demo,&net); //receive the output buffer of the first NN
    //dp_register_fps_device_cb(fps_callback,&net); // fps
    //dp_register_parse_blob_time_device_cb(blob_parse_callback,NULL); // model parsing-time


    ret = dp_start_camera_video(); // NN will start to work if camera is on
    if (ret == 0) {
        ALOGE("Test test_start_video successfully!\n");
    }
    else {
        ALOGE("Test test_start_video failed! ret=%d\n", ret);
        return -1;
    }

    env->ReleaseStringUTFChars(blobPath,path);
    return 0;
}

void video_callback(dp_img_t *img, void *param){
    memset(g_img,0, sizeof(dp_img_t));
    memcpy(g_img,img, sizeof(dp_img_t));
}

void box_callback_model_demo(void *result,void *param){
    DP_MODEL_NET model = *((DP_MODEL_NET*)param);
    // i have a bug here,can not fetch the right param,this is a movidius system bug,i will fix it later.
    //if( model == DP_SSD_MOBILI_NET){
        char* category[] = {"background","aeroplane","bicycle","bird","boat","bottle","bus","car","cat","chair","cow","diningtable",
                            "dog","horse","motorbike","person","pottedplant","sheep","sofa","train","tvmonitor"};
        u16* probabilities = (u16*)result;
        unsigned int resultlen = 707;
        float* resultfp32;
        resultfp32 = (float*)malloc(resultlen * sizeof(*resultfp32));
        int img_width = 1280;
        int img_height = 960;
        for (u32 i = 0; i < resultlen; i++)
            resultfp32[i] = f16Tof32(probabilities[i]);
        int num_valid_boxes = int(resultfp32[0]);
        int index = 0;
        ALOGE("num_valid_bxes:%d\n",num_valid_boxes);
        for(int box_index = 0; box_index < num_valid_boxes; box_index ++)
        {
            int base_index = 7*box_index + 7;
            if(resultfp32[base_index + 6] < 0
               ||resultfp32[base_index + 6] >= 1
               ||resultfp32[base_index + 5] < 0
               ||resultfp32[base_index + 5] >= 1
               ||resultfp32[base_index + 4] < 0
               ||resultfp32[base_index + 4] >= 1
               ||resultfp32[base_index + 3] < 0
               ||resultfp32[base_index + 3] >= 1
               ||resultfp32[base_index + 2] >= 1
               ||resultfp32[base_index + 2] < 0
               ||resultfp32[base_index + 1] < 0)
            {
                continue;
            }
            ALOGE(":::::%d %f %f %f %f %f\n",
                  int(resultfp32[base_index + 1]),
                  resultfp32[base_index + 2],
                  resultfp32[base_index + 3],
                  resultfp32[base_index + 4],
                  resultfp32[base_index + 5],
                  resultfp32[base_index + 6]);
            box_demo[index].x1=(int(resultfp32[base_index + 3] * img_width) > 0) ? int(resultfp32[base_index + 3] * img_width): 0;
            box_demo[index].x2=(int(resultfp32[base_index + 5] * img_width) < img_width) ? int(resultfp32[base_index + 5] * img_width):img_width;
            box_demo[index].y1=(int(resultfp32[base_index+ 4 ] * img_height) > 0 ) ? int(resultfp32[base_index + 4] * img_height): 0;
            box_demo[index].y2=(int(resultfp32[base_index+ 6 ] * img_height) < img_height) ? int(resultfp32[base_index + 6] * img_height):img_height;
            memcpy(categoles[index],category[int(resultfp32[base_index + 1])], 20);
            index++;
        }
        num_box_demo=index;
        free(resultfp32);
//    }else{
//        ALOGE("param is not DP_SSD_MOBILI_NET,param = %d\n",param);
//    }
}
