package com.cyp.nanningworkloadreportsystem.common;

import lombok.Data;

import java.util.List;

/**
 * 分页查询结果封装类
 * 配合 MyBatis Plus 的 IPage 使用，统一分页响应格式
 *
 * @param <T> 数据记录类型
 */
@Data
public class PageResult<T> {

    /** 总记录数 */
    private Long total;

    /** 当前页码 */
    private Integer pageNum;

    /** 每页大小 */
    private Integer pageSize;

    /** 当前页数据列表 */
    private List<T> records;

    /**
     * 创建分页结果
     *
     * @param total    总记录数
     * @param pageNum  当前页码
     * @param pageSize 每页大小
     * @param records  数据列表
     * @param <T>      数据类型
     * @return 分页结果对象
     */
    public static <T> PageResult<T> of(Long total, Integer pageNum, Integer pageSize, List<T> records) {
        PageResult<T> result = new PageResult<>();
        result.setTotal(total);
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        result.setRecords(records);
        return result;
    }
}
