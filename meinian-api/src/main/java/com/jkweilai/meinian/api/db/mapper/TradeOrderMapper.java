package com.jkweilai.meinian.api.db.mapper;

import com.jkweilai.meinian.api.db.pojo.TradeOrderEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
* @author admin
* @description 针对表【trade_order(交易订单表)】的数据库操作Mapper
* @createDate 2026-07-09 14:26:46
* @Entity com.jkweilai.meinian.api.db.pojo.TradeOrderEntity
*/
public interface TradeOrderMapper {

    //查询订单信息
    Map<String, Object> selectOrderStatistics(Integer id);

    //查看非法用户（10笔未付款或5笔退款）
    boolean isCustomerReachedDailyLimit(Integer customerId);

    //关闭订单
    int closeOrder();

    //保存订单
    int insert(TradeOrderEntity tradeOrderEntity);

    //支付成功后，插入订单编号，修改订单状态3（已付款）
    int updatePayment(Map<String, Object> params);

    //通过订单流水号返回客户id
    int selectCustomerIdByOutTradeNo(String outTradeNo);

    //分页查询我的订单数据
    List<Map<String, Object>> selectPageByCondition(Map<String, Object> params);

    //分页查询我的订单记录条数
    long selectCountByQueryCondition(Map<String, Object> params);

    //根据订单id查询退款流水号
    String selectOutRefundNoByOrderId(Integer orderId);

    //根据订单id查询交易订单id和总价
    Map<String, Object> selectTranIdAndAmountByOrderId(Map<String, Object> param);

    //用户点击退款后更新退款流水号、退款时间、退款日期
    int updateOutRefundNo(Map<String, Object> param);

    //退款成功后根据退款流水号，更新订单状态为已退款4
    int updateStatusByOutTradeNo(String outTradeNo);

    //mis订单管理模块分页查询数据
    List<Map<String, Object>> selectPage(Map<String, Object> params);

    //mis订单管理模块分页查询记录条数
    long selectCount(Map<String, Object> params);

    //删除订单记录
    int deleteById(Integer id);

    //线下退款更新订单状态
    int updateStatusOfflineRefundByOutTradeNo(Map<String, Object> params);

    //根据customerId和snapshotId查询订单id
    Integer selectByCustomerIdAndSnapshotId(@Param("snapshotId") String snapshotId, @Param("customerId") Integer customerId);

    //预约成功后修改订单状态
    int updateStatus(Map<String, Object> param);

    //判断用户是否有这个订单
    Integer hasOwnOrder(Map<String, Object> param);

    //出啊寻订单中套餐的数量 和 已经完成的体检数量
    Map<String, Object> selectOrderIsFinished(String appointmentNo);

}




