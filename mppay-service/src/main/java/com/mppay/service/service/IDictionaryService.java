package com.mppay.service.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mppay.service.dto.DictionaryDTO;
import com.mppay.service.entity.Dictionary;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 数据字典表 服务类
 * </p>
 *
 * @author chenfeihang
 * @since 2019-08-23
 */
public interface IDictionaryService extends IService<Dictionary> {

    /**
     * 根据类别,名称,商户号查询字典信息
     *
     * @param category
     * @param name
     * @param mercId
     * @return
     */
    Dictionary findByCategoryAndNameAndMercId(String category, String name, String mercId);

    /**
     * 返回String value
     *
     * @param category
     * @param name
     * @return
     */
    @Deprecated
    String findForString(String category, String name);

    /**
     * 返回Long value
     *
     * @param category
     * @param name
     * @return
     */
    @Deprecated
    Long findForLong(String category, String name);

    /**
     * 返回String value
     *
     * @param category
     * @param name
     * @param mercId
     * @return
     */
    String findForString(String category, String name, String mercId);

    /**
     * 返回Long value
     *
     * @param category
     * @param name
     * @param mercId
     * @return
     */
    Long findForLong(String category, String name, String mercId);

    /**
     * 获取字典列表
     *
     * @param category
     * @param name
     * @param mercId
     * @param visible
     * @return
     */
    List<Dictionary> list(String category, String name, String mercId, Integer visible);

    /**
     * 分页查询
     *
     * @param page
     * @param dictionaryDTO
     */
    void page(Page<Dictionary> page, DictionaryDTO dictionaryDTO);

    /**
     * 列表查询
     *
     * @param dictionaryDTO
     */
    List<Dictionary> list(DictionaryDTO dictionaryDTO);

}
