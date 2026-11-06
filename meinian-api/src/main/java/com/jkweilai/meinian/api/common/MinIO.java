package com.jkweilai.meinian.api.common;

import cn.hutool.core.codec.Base64;
import com.jkweilai.meinian.api.exception.HisException;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;


@Component
@Slf4j
public class MinIO {

    //ip
    @Value("${minio.endpoint}")
    private String endpoint;

    //账号名
    @Value("${minio.access-key}")
    private String accessKey;

    //密码
    @Value("${minio.secret-key}")
    private String secretKey;

    //数据桶名字
    @Value("${minio.bucket}")
    private String bucket;

    //MinIO的客户端
    private MinioClient client;

    //自动创建并初始化全局 MinIO 客户端 client
    @PostConstruct
    public void init() {
        // 初始化MinIO客户端连接
        // 使用建造者模式配置服务端点和管理员凭证
        this.client = new MinioClient.Builder()
                .endpoint(endpoint)          // 设置MinIO服务器地址
                .credentials(accessKey, secretKey)  // 设置访问密钥
                .build();                    // 构建MinIO客户端实例
    }

    /**
     * 上传图片到MinIO对象存储
     * @param path 文件在MinIO中的存储路径（包含文件名）
     * @param file Spring MultipartFile文件对象
     * @throws HisException 当文件上传失败时抛出业务异常
     */
    public void uploadImage(String path, MultipartFile file) {
        try {
            // 使用MinIO客户端上传文件到指定存储桶
            this.client.putObject(PutObjectArgs.builder()
                    .bucket(bucket)                    // 设置存储桶名称
                    .object(path)                      // 设置文件在MinIO中的存储路径
                    .stream(file.getInputStream(),     // 文件输入流
                            -1,                        // 文件大小：-1表示自动检测
                            5 * 1024 * 1024)           // 分片大小：5MB，用于大文件分片上传（这个工具类不只是给上传图片用，还有大文件）
                    .contentType("image/jpeg")         // 设置文件内容类型为JPEG图片
                    .build());                         // 构建上传参数并执行上传

            // 记录上传成功日志
            log.debug("向" + path + "保存了文件");

        } catch (Exception e) {
            // 记录错误日志并抛出业务异常
            log.error("保存文件失败", e);
            throw new HisException("保存文件失败");
        }
    }

    //上传excle工具方法
    public void uploadExcel(String path, MultipartFile file) {
        try {
            // Excel文件的MIME类型
            String mime = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

            // 使用MinIO客户端上传文件到对象存储
            // bucket: 存储桶名称
            // path: 文件在存储桶中的路径
            // file.getInputStream(): 获取上传文件的输入流
            // -1: 表示流大小未知，由SDK自动计算
            // 20 * 1024 * 1024: Excel文件大小限制为20MB
            // contentType: 设置文件内容类型为Excel格式
            this.client.putObject(PutObjectArgs.builder()
                    .bucket(bucket)                    // 指定存储桶
                    .object(path)                      // 指定文件路径
                    .stream(file.getInputStream(), -1, 20 * 1024 * 1024)  // 文件流，自动计算大小，限制20MB
                    .contentType(mime)                 // 设置内容类型为Excel
                    .build());                         // 构建上传参数并执行上传

            // 记录调试日志，文件上传成功
            log.debug("向" + path + "保存了文件");

        } catch (Exception e) {
            // 记录错误日志并抛出业务异常
            log.error("保存文件失败", e);
            throw new HisException("保存文件失败");
        }
    }
    /**
     * 下载文件
     * 从MinIO对象存储中获取指定路径的文件输入流
     *
     * @param path 文件在MinIO中的存储路径（对象键）
     * @return InputStream 文件输入流，可直接用于文件下载或读取
     * @throws HisException 当文件下载失败时抛出业务异常
     * @example
     * <pre>
     * // 使用示例：
     * InputStream inputStream = downloadFile("user/avatar/12345.jpg");
     * // 将inputStream写入HttpServletResponse输出流实现文件下载
     * </pre>
     */
    public InputStream downloadFile(String path) {
        try {
            // 构建MinIO文件获取参数
            GetObjectArgs args = GetObjectArgs.builder()
                    .bucket(bucket)      // 设置存储桶名称
                    .object(path)        // 设置文件路径（对象键）
                    .build();

            // 从MinIO获取文件输入流
            return client.getObject(args);
        } catch (Exception e) {
            log.error("文件下载失败，文件路径：{}", path, e);
            throw new HisException("文件下载失败");
        }
    }

    /**
     * 删除文件
     * 从MinIO对象存储中删除指定路径的文件
     *
     * @param path 文件在MinIO中的存储路径（对象键）
     * @throws HisException 当文件删除失败时抛出业务异常
     * @example
     * <pre>
     * // 使用示例：
     * deleteFile("user/avatar/12345.jpg"); // 删除用户头像文件
     * deleteFile("documents/report.pdf");   // 删除文档文件
     * </pre>
     */
    public void deleteFile(String path) {
        try {
            // 构建MinIO文件删除参数
            this.client.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucket)      // 设置存储桶名称
                    .object(path)        // 设置要删除的文件路径（对象键）
                    .build());

            // 记录删除操作日志（调试级别）
            log.debug("删除了" + path + "路径下的文件");
        } catch (Exception e) {
            // 记录错误日志并抛出业务异常
            log.error("文件删除失败，文件路径：{}", path, e);
            throw new HisException("文件删除失败");
        }
    }

    public void uploadImage(String path, String base64Image) {
        try {
            // 移除Base64字符串中的Data URI前缀，保留纯Base64数据
            base64Image = base64Image.replace("data:image/jpeg;base64,", "");
            base64Image = base64Image.replace("data:image/png;base64,", "");

            // 将Base64字符串解码为字节数组
            byte[] decode = Base64.decode(base64Image);

            // 将字节数组包装为输入流，供MinIO客户端读取
            ByteArrayInputStream in = new ByteArrayInputStream(decode);

            // 使用MinIO客户端上传对象到存储桶
            this.client.putObject(PutObjectArgs.builder()
                    .bucket(bucket)          // 指定存储桶名称
                    .object(path)            // 指定对象存储路径
                    .stream(in, -1, 5 * 1024 * 1024) // 设置输入流，-1表示未知流大小，5MB为分片大小
                    .contentType("image/jpeg") // 设置内容类型为JPEG格式
                    .build());

            // 记录调试日志，确认文件保存成功
            log.debug("向" + path + "保存了文件");

        } catch (Exception e) {
            // 记录错误日志并抛出业务异常
            log.error("保存文件失败", e);
            throw new HisException("保存文件失败");
        }
    }

    //保存体检报告
    public void uploadWord(String path, InputStream in){
        try{
            // 当用户下载文件时，MinIO服务器在HTTP响应头中设置的Content-Type值，如果不设置浏览器不知道如何解释和处理收到的文件数据，导致下载后的文件无法打开
            String mime = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

            this.client.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(path)
                    .stream(in, -1, 50 * 1024 * 1024)
                    .contentType(mime)
                    .build()
            );

            in.close();
        }catch(Exception e){
            throw new HisException("保存体检报告文件失败");
        }
    }

}

