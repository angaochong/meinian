package com.jkweilai.meinian.api.common;

import lombok.Data;

import java.util.List;

/**
 * 分页数据
 *
 * @param <T>
 */
@Data
public class PageResult<T> {
    // 总记录条数
    private Long total;
    // 每页显示多少条(这个属性的存在并不是为了返回给前端，而是为了计算总页数。)
    private Integer pageSize;
    // 当前页码(这个属性的存在并不是为了返回给前端，而是为了计算总页数。)
    private Integer pageNo;
    // 总页数(其实这个总页数也不需要提供，因为我们前端使用的是ElementPlus提供的分页组件，它是可以自动计算总页数的。)
    // 咱们这里编写的分页对象是较为通用的分页对象。
    private Integer pages;
    // 当前页数据
    private List<T> records;

    // 虽然有5个属性，但是只需要传递4个参数，因为总页数可以动态计算。
    public PageResult(Long total, Integer pageSize, Integer pageNo, List<T> records) {
        this.total = total;
        this.pageSize = pageSize;
        this.pageNo = pageNo;
        this.pages = (int) (total % pageSize == 0 ? total / pageSize : total / pageSize + 1);
        this.records = records;
    }

    public PageResult(Long total, List<T> records) {
        this.total = total;
        this.records = records;
    }
}

// 最小化写法
/*@Data
public class PageResult<T> {
    private List<T> records;
    private Long total;

    public PageResult(Long total, List<T> records) {
        this.total = total;
        this.records = records;
    }
}*/
