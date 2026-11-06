package com.jkweilai.meinian.api.front.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.qrcode.QrConfig;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jkweilai.meinian.api.common.PageResult;
import com.jkweilai.meinian.api.db.dao.GoodsSnapshotDao;
import com.jkweilai.meinian.api.db.mapper.MedExamPackageMapper;
import com.jkweilai.meinian.api.db.mapper.TradeOrderMapper;
import com.jkweilai.meinian.api.db.pojo.GoodsSnapshotEntity;
import com.jkweilai.meinian.api.db.pojo.TradeOrderEntity;
import com.jkweilai.meinian.api.exception.HisException;
import com.jkweilai.meinian.api.front.service.PaymentService;
import com.jkweilai.meinian.api.front.service.TradeOrderService;
import com.ql.util.express.DefaultContext;
import com.ql.util.express.ExpressRunner;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service("FrontTradeOrderServiceImpl")
@Slf4j
public class TradeOrderServiceImpl implements TradeOrderService {


    @Resource
    private TradeOrderMapper tradeOrderMapper;

    @Resource
    private MedExamPackageMapper medExamPackageMapper;

    @Resource(name = "FrontPaymentServiceImpl")
    private PaymentService paymentService;

    @Value("${wechat.pay.v3.meinian-vue.domain}")
    private String domain;

    @Resource
    private RedisTemplate redisTemplate;

    @Autowired
    private GoodsSnapshotDao goodsSnapshotDao;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createPayment(Map<String, Object> params) {

        //取出 顾客id，商品id，商品数量
        Integer customerId = MapUtil.getInt(params, "customerId");
        Integer buyCount = MapUtil.getInt(params, "buyCount");
        Integer goodsId = MapUtil.getInt(params, "goodsId");

        //判断顾客是否达到限制
        if(tradeOrderMapper.isCustomerReachedDailyLimit(customerId)){
            return null;
        }

        //执行到这里证明顾客没有达到限制
        // TODO 查找商品详细信息
        Map<String, Object> map = medExamPackageMapper.selectPackageAndPromotionById(goodsId);
        String packageCode = MapUtil.getStr(map, "packageCode");
        String packageName = MapUtil.getStr(map, "packageName");
        String description = MapUtil.getStr(map, "description");
        String coverImage = MapUtil.getStr(map, "coverImage");
        String originalPrice = MapUtil.getStr(map, "originalPrice");
        String currentPrice = MapUtil.getStr(map, "currentPrice");
        String packageType = MapUtil.getStr(map, "packageType");
        String md5Hash = MapUtil.getStr(map, "md5Hash");
        String ruleContent = MapUtil.getStr(map, "ruleContent");
        String ruleName = MapUtil.getStr(map, "ruleName");

        String temp = MapUtil.getStr(map, "departmentExam");
        // 如果不进行格式转换的话，从数据库中查询出来的json字符串直接传递给mongodb，
        // mongodb会认为它只是一个普通的字符串，不会将其视为mongodb的文档对象
        // 记得做判空处理。
        List<Map> departmentExam = temp != null || !temp.isBlank() ? JSONUtil.parseArray(temp).toList(Map.class) : null;

        temp = MapUtil.getStr(map, "labExam");
        List<Map> labExam = temp != null || !temp.isBlank()? JSONUtil.parseArray(temp).toList(Map.class) : null;

        temp = MapUtil.getStr(map, "medicalExam");
        List<Map> medicalExam = temp != null || !temp.isBlank()? JSONUtil.parseArray(temp).toList(Map.class) : null;

        temp = MapUtil.getStr(map, "otherExam");
        List<Map> otherExam = temp != null || !temp.isBlank()? JSONUtil.parseArray(temp).toList(Map.class) : null;

        temp = MapUtil.getStr(map, "examItems");
        List<Map> examItems = temp != null || !temp.isBlank()? JSONUtil.parseArray(temp).toList(Map.class) : null;

        temp = MapUtil.getStr(map, "tags");
        List<String> tags = temp != null || !temp.isBlank()? JSONUtil.parseArray(temp).toList(String.class) : null;
        log.info("付款单对应的商品信息已经查询出来了：{}", map);
        // TODO 计算订单金额
        BigDecimal goodsCurrentPrice = new BigDecimal(currentPrice);
        BigDecimal goodsBuyCount = new BigDecimal(buyCount);
        String amount = null;
        if(ruleContent == null || ruleContent.isBlank()){
            amount = goodsCurrentPrice.multiply(goodsBuyCount).toString();
        }else{
            try{
                ExpressRunner runner = new ExpressRunner();
                DefaultContext<String, Object> context = new DefaultContext<>();
                context.put("number", buyCount.intValue());
                context.put("price", goodsCurrentPrice.toString());
                amount = runner.execute(ruleContent, context, null, true, false).toString();
            }catch (Exception e){
                throw new HisException("执行规则引擎错误", e);
            }
        }
        log.info("付款单的总金额：{}", amount);
        // TODO 创建微信支付订单
        // 创建微信支付单
        // 交易流水号
        String outTradeNo = IdUtil.simpleUUID().toUpperCase();
        // 订单总金额微信要求必须转换为分
        int total = NumberUtil.round(NumberUtil.mul(amount, "100"), 0).intValue();
        // 描述信息
        String desc = "购买体检套餐";
        // 回调url (notifyUrl：将来订单支付之后，微信支付平台会调用这个url，给我们传递支付成功/失败的信息)
        String notifyUrl = domain + "/front/order/paymentCallback";
        // 交易过期时间
        // 第一种写法
        /*
        DateTime dateTime = new DateTime(); // 当前时间
        dateTime.offset(DateField.MINUTE, 20); // 向后偏移20分钟
        String timeExpire = dateTime.toString("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"); // 这个日期格式是微信官方要求的。
        */
        // 第二种写法
        DateTime dateTime = DateUtil.offsetMinute(new Date(), 20);
        String timeExpire = dateTime.toInstant().toString();
        // 创建微信支付单
        ObjectNode objectNode = paymentService.unifiedOrder(outTradeNo, total, desc, notifyUrl, timeExpire);
        // 获取 code_url
        String codeUrl = objectNode.get("code_url").textValue();
        log.info("生成付款单成功，code_url={}", codeUrl);
        // TODO 创建支付单缓存，设置缓存过期时间
        // 创建支付单缓存，设置缓存过期时间
        String key = "codeUrl_" + customerId + "_" + outTradeNo;
        redisTemplate.opsForValue().set(key, codeUrl); // 存储到缓存中
        redisTemplate.expireAt(key, dateTime); // 设置过期时间
        log.info("redis中创建支付单缓存成功，支付单过期时间：{}", dateTime);
        // TODO 如果不存在商品快照，就创建快照记录
        if(codeUrl != null){
            String _id = goodsSnapshotDao.hasGoodsSnapshot(md5Hash);
            if(_id == null){
                log.info("【快照新增】商品ID:{}，MD5值:{}，无历史快照，开始插入",goodsId,md5Hash);
                GoodsSnapshotEntity goodsSnapshot = new GoodsSnapshotEntity();
                goodsSnapshot.setId(goodsId);
                goodsSnapshot.setDescription(description);
                goodsSnapshot.setCoverImage(coverImage);
                goodsSnapshot.setCurrentPrice(goodsCurrentPrice);
                goodsSnapshot.setDepartmentExam(departmentExam);
                goodsSnapshot.setLabExam(labExam);
                goodsSnapshot.setMedicalExam(medicalExam);
                goodsSnapshot.setOtherExam(otherExam);
                goodsSnapshot.setExamItems(examItems);
                goodsSnapshot.setPackageCode(packageCode);
                goodsSnapshot.setPackageName(packageName);
                goodsSnapshot.setPackageType(packageType);
                goodsSnapshot.setTags(tags);
                goodsSnapshot.setOriginalPrice(new BigDecimal(originalPrice));
                goodsSnapshot.setMd5Hash(md5Hash);
                goodsSnapshot.setRuleContent(ruleContent);
                goodsSnapshot.setRuleName(ruleName);

                _id = goodsSnapshotDao.insert(goodsSnapshot);
            }
            log.info("商品快照保存成功，快照 _id={}", _id);
            // 程序走到这里，商品快照的 _id 已经就绪。
            // TODO 保存订单记录
            TradeOrderEntity tradeOrder = new TradeOrderEntity();
            tradeOrder.setOutTradeNo(outTradeNo); // 交易流水号
            tradeOrder.setCustomerId(customerId); // 客户id
            tradeOrder.setGoodsId(goodsId); // 商品id
            tradeOrder.setGoodsTitle(packageName); // 商品名称
            tradeOrder.setGoodsPrice(goodsCurrentPrice); // 商品价格
            tradeOrder.setGoodsImage(coverImage); // 商品封面
            tradeOrder.setGoodsDescription(description); // 商品描述
            tradeOrder.setQuantity(buyCount); // 购买数量
            tradeOrder.setTotalAmount(new BigDecimal(amount)); // 订单总价
            tradeOrder.setSnapshotId(_id); // 关联商品快照id
            int rows = tradeOrderMapper.insert(tradeOrder);
            if(rows != 1){
                log.error("保存订单失败");
                throw new HisException("保存订单失败");
            }
            log.info("保存订单成功，订单流水号是：{}", outTradeNo);

            // TODO 更新商品销量
            // 底层对应的update语句是一条。属于原子性操作，并且隔离级别采用RR级别。
            // MySQL的RR使用MVCC（多版本并发控制） + Next-Key Lock
            // 是当前读（读取最新已提交值）、会加行锁、保证原子性
            rows = medExamPackageMapper.updateSalesVolume(goodsId, buyCount);
            if(rows != 1){
                log.error("更新销量失败");
                throw new HisException("更新商品销量失败");
            }


            // TODO 付款二维码图片转换成base64字符串返回给前端
            // 创建二维码配置对象
            QrConfig qrConfig = new QrConfig();
            qrConfig.setWidth(230);
            qrConfig.setHeight(230);
            // 设置二维码边距为2像素
            qrConfig.setMargin(2);
            // 生成二维码并转换为Base64字符串，格式为JPG
            String qrCodeBase64 = QrCodeUtil.generateAsBase64(codeUrl, qrConfig, "jpg");

            // 封装返回数据
            Map<String, Object> result = new HashMap<>();
            result.put("qrCodeBase64", qrCodeBase64);
            // 为什么要返回交易流水号，因为前端要通过交易流水号来轮询订单的支付状态。
            result.put("outTradeNo", outTradeNo);

            log.info("成功返回付款二维码：{}", qrCodeBase64);
            log.info("成功生成付款单，订单交易流水号：{}", tradeOrder);

            return result;

        }else{
            log.error("创建支付订单失败", objectNode);
            throw new HisException("创建订单失败！");
        }
    }

    @Override
    @Transactional
    public boolean updatePayment(Map<String, Object> params) {
        return tradeOrderMapper.updatePayment(params) == 1;
    }

    @Override
    public void clearRedisPayment(String outTradeNo) {
        int customerId = tradeOrderMapper.selectCustomerIdByOutTradeNo(outTradeNo);
        String key = "codeUrl_" + customerId + "_" + outTradeNo;
        if(redisTemplate.hasKey(key)){
            redisTemplate.delete(key);
            log.info("清除redis中的付款单：{}", key);
        }
    }

    @Override
    public Integer findCustomerIdByOutTradeNo(String outTradeNo) {
        return tradeOrderMapper.selectCustomerIdByOutTradeNo(outTradeNo);
    }

    @Override
    public boolean getPaymentResult(String outTradeNo) {
        String transactionId = paymentService.getPaymentResult(outTradeNo);
        if(transactionId != null){
            Map<String, Object> param = new HashMap<>();
            param.put("transactionId", transactionId);
            param.put("outTradeNo", outTradeNo);
            updatePayment(param);
            return true;
        }
        return false;
    }

    @Override
    public PageResult<Map<String, Object>> pageQueryByCondition(Map<String, Object> param) {
        List<Map<String, Object>> records = new ArrayList<>();
        long total = tradeOrderMapper.selectCountByQueryCondition(param);
        if(total > 0){
            records = tradeOrderMapper.selectPageByCondition(param);
        }
        Integer pageSize = MapUtil.getInt(param, "pageSize");
        Integer pageNo = MapUtil.getInt(param, "pageNo");
        PageResult<Map<String, Object>> pageResult = new PageResult<>(total, pageSize, pageNo, records);
        return pageResult;
    }

    @Override
    @Transactional
    public boolean refund(Map<String, Object> param) {
        Integer orderId = MapUtil.getInt(param, "orderId");
        String outRefundNo = tradeOrderMapper.selectOutRefundNoByOrderId(orderId);
        if(outRefundNo != null){
            return false;
        }

        Map<String, Object> map = tradeOrderMapper.selectTranIdAndAmountByOrderId(param);
        String transactionId = MapUtil.getStr(map, "transactionId");
        String totalAmount = MapUtil.getStr(map, "totalAmount");
        // 将金额转换成分
        //int total = NumberUtil.mul(totalAmount, "100").intValue();
        int total = 1;
        //int refund = total;
        int refund = 1;
        if(transactionId == null){
            log.error("transactionId is null");
            return false;
        }

        String notifyUrl = domain + "/front/order/refundCallback";
        outRefundNo = paymentService.refund(transactionId, refund, total, notifyUrl);
        param.put("outRefundNo", outRefundNo);
        if(outRefundNo != null){
            int rows = tradeOrderMapper.updateOutRefundNo(param);
            if(rows == 1){
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean modifyStatusByOutTradeNo(String outTradeNo) {
        return tradeOrderMapper.updateStatusByOutTradeNo(outTradeNo) == 1;
    }

    @Override
    public boolean hasOwnOrder(Map<String, Object> param) {
        return tradeOrderMapper.hasOwnOrder(param) != null;
    }


}
