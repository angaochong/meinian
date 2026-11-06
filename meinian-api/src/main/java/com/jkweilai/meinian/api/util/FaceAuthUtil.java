package com.jkweilai.meinian.api.util;

import cn.hutool.core.util.StrUtil;
import com.jkweilai.meinian.api.exception.HisException;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.iai.v20200303.IaiClient;
import com.tencentcloudapi.iai.v20200303.models.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class FaceAuthUtil {

    // 腾讯云API密钥配置
    @Value("${tencent.cloud.secretId}")
    private String secretId;

    @Value("${tencent.cloud.secretKey}")
    private String secretKey;

    @Value("${tencent.cloud.face.groupId}")
    private String groupId;

    @Value("${tencent.cloud.face.region}")
    private String region;

    /**
     * 人脸识别+活体验证综合验证
     * 验证流程：人脸比对 → 活体检测 → 人员库管理（待实现）
     *
     * @param name    体检人姓名
     * @param pid     身份证号码
     * @param sex     性别
     * @param photo_1 身份证上的照片(base64编码)
     * @param photo_2 现场摄像头拍摄照片(base64编码)
     * @return boolean 验证结果：true-验证通过，false-验证失败
     */
    public boolean verifyFaceModel(String name, String pid, String sex, String photo_1, String photo_2) {
        boolean result;

        // 1. 初始化腾讯云客户端
        Credential cred = new Credential(secretId, secretKey);
        IaiClient client = new IaiClient(cred, region);

        // 2. 执行人脸比对 - 验证两张照片是否为同一人
        CompareFaceRequest compareFaceRequest = new CompareFaceRequest();
        compareFaceRequest.setImageA(photo_1);  // 设置身份证上的照片
        compareFaceRequest.setImageB(photo_2);  // 设置现场拍摄照片

        CompareFaceResponse compareFaceResponse = null;
        try {
            // 调用腾讯云人脸比对接口
            compareFaceResponse = client.CompareFace(compareFaceRequest);
        } catch (TencentCloudSDKException e) {
            log.error("人脸比对失败 - 姓名: {}, 身份证: {}", name, pid, e);
            throw new HisException("人脸比对失败");
        }

        // 3. 获取人脸相似度分数并判断
        Float score = compareFaceResponse.getScore();
        log.info("人脸比对分数: {} - 姓名: {}, 身份证: {}", score, name, pid);

        if (score >= 50) {
            // 4. 人脸比对通过后，执行静态活体检测 - 防止照片攻击
            DetectLiveFaceRequest detectLiveFaceRequest = new DetectLiveFaceRequest();
            detectLiveFaceRequest.setImage(photo_2);  // 对现场拍摄照片进行活体检测

            DetectLiveFaceResponse detectLiveFaceResponse = null;
            try {
                // 调用腾讯云静态活体检测接口
                detectLiveFaceResponse = client.DetectLiveFace(detectLiveFaceRequest);
            } catch (TencentCloudSDKException e) {
                log.error("静态活体识别失败 - 姓名: {}, 身份证: {}", name, pid, e);
                throw new HisException("静态活体识别失败");
            }

            // 5. 获取活体检测结果
            result = detectLiveFaceResponse.getIsLiveness();
            log.info("活体检测结果: {} - 姓名: {}, 身份证: {}", result, name, pid);
        } else {
            // 人脸比对分数低于50分，直接判定验证失败
            log.warn("人脸比对分数过低: {} - 姓名: {}, 身份证: {}", score, name, pid);
            result = false;
            return result;
        }

        // 6. 待实现功能 - 人员库管理
        // TODO 判断人员库是否有该体检人
        // TODO 把体检人添加到人员库

        // 这里是新添加的代码
        if (result) {
            // 查询人员库中是否有该体检人 - 使用身份证号作为人员ID进行查询
            GetPersonBaseInfoRequest getPersonBaseInfoRequest = new GetPersonBaseInfoRequest();
            getPersonBaseInfoRequest.setPersonId(pid); // 设置要查询的人员ID（身份证号）
            GetPersonBaseInfoResponse getPersonBaseInfoResponse = null;
            try {
                // 调用腾讯云接口查询人员基本信息
                getPersonBaseInfoResponse = client.GetPersonBaseInfo(getPersonBaseInfoRequest);
            } catch (TencentCloudSDKException e) {
                // 异常处理：如果错误码不是"人员不存在"，则抛出异常
                // "InvalidParameterValue.PersonIdNotExist"表示要查询的人员在库中不存在，这是正常情况
                if (!e.getErrorCode().equals("InvalidParameterValue.PersonIdNotExist")) {
                    log.error("查询人员库失败", e);
                    throw new HisException("查询人员库失败");
                }
                // 如果是"人员不存在"的错误，不抛出异常，继续执行后续逻辑
            }

            // 判断查询结果是否为null，如果为null说明人员库中不存在该体检人
            if (getPersonBaseInfoResponse == null) {
                // 创建人员请求对象，准备将体检人添加到人员库
                CreatePersonRequest createPersonRequest = new CreatePersonRequest();
                createPersonRequest.setGroupId(groupId); // 设置人员所属的组ID
                createPersonRequest.setPersonId(pid); // 设置人员唯一ID（身份证号）

                // 性别转换：将"男"/"女"转换为腾讯云需要的数字格式（1-男，2-女）
                long gender = sex.equals("男") ? 1L : 2L;
                createPersonRequest.setGender(gender);

                createPersonRequest.setQualityControl(4L); // 设置图片质量控制为最高级别（4-很高的质量要求）
                createPersonRequest.setUniquePersonControl(4L); // 设置人员去重控制为最高级别（4-很高的同一人判断要求）
                createPersonRequest.setPersonName(name); // 设置人员姓名
                createPersonRequest.setImage(photo_1); // 设置人员照片（身份证照片）

                CreatePersonResponse createPersonResponse = null;
                try {
                    // 调用腾讯云接口创建人员记录
                    createPersonResponse = client.CreatePerson(createPersonRequest);
                } catch (TencentCloudSDKException e) {
                    log.error("添加体检人到人员库失败", e);
                    throw new HisException("添加体检人到人员库失败");
                }

                // 判断人员是否添加成功：通过检查返回的faceId是否为空
                if (StrUtil.isNotBlank(createPersonResponse.getFaceId())) {
                    log.debug("体检人成功添加到人员库"); // 添加成功日志
                } else {
                    log.error("添加体检人到人员库失败"); // 添加失败日志
                    throw new HisException("添加体检人到人员库失败"); // 抛出业务异常
                }
            }
            // 如果getPersonBaseInfoResponse不为null，说明人员已存在，不需要重复添加
        }

        return result;
    }
}