package com.mppay.service.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mppay.service.dto.DictionaryDTO;
import com.mppay.service.entity.Dictionary;
import com.mppay.service.mapper.DictionaryMapper;
import com.mppay.service.service.IDictionaryService;

/**
 * <p>
 * 数据字典表 服务实现类
 * </p>
 *
 * @author chenfeihang
 * @since 2019-08-23
 */
@Service
public class DictionaryServiceImpl extends ServiceImpl<DictionaryMapper, Dictionary> implements IDictionaryService {

    private final static String DEFAULT_MERC_ID = "0";

    /**
     * 根据类别,名称,商户号查询字典信息
     *
     * @param category
     * @param name
     * @param mercId
     * @return
     */
    @Override
    public Dictionary findByCategoryAndNameAndMercId(String category, String name, String mercId) {
        return this.getOne(new QueryWrapper<Dictionary>().eq("category", category)
                .eq("name", name).eq("merc_id", mercId));
    }

    /**
     * 返回String value
     *
     * @param category
     * @param name
     * @return
     */
    @Override
    public String findForString(String category, String name) {
        Dictionary dictionary = this.findByCategoryAndNameAndMercId(category, name, DEFAULT_MERC_ID);
        if (dictionary == null) {
            return null;
        }
        return dictionary.getStrVal();
    }

    /**
     * 返回String value
     *
     * @param category
     * @param name
     * @return
     */
    @Override
    public Long findForLong(String category, String name) {
        Dictionary dictionary = this.findByCategoryAndNameAndMercId(category, name, DEFAULT_MERC_ID);
        if (dictionary == null) {
            return null;
        }
        return dictionary.getLongVal();
    }

    /**
     * 返回String value
     *
     * @param category
     * @param name
     * @param mercId
     * @return
     */
    @Override
    public String findForString(String category, String name, String mercId) {
        Dictionary dictionary = this.findByCategoryAndNameAndMercId(category, name, mercId);
        if (dictionary == null) {
            return null;
        }
        return dictionary.getStrVal();
    }

    /**
     * 返回Long value
     *
     * @param category
     * @param name
     * @param mercId
     * @return
     */
    @Override
    public Long findForLong(String category, String name, String mercId) {
        Dictionary dictionary = this.findByCategoryAndNameAndMercId(category, name, mercId);
        if (dictionary == null) {
            return null;
        }
        return dictionary.getLongVal();
    }

    /**
     * 获取字典列表
     *
     * @param category
     * @param name
     * @param mercId
     * @param visible
     * @return
     */
    @Override
    public List<Dictionary> list(String category, String name, String mercId, Integer visible) {
        return null;
    }

    /**
     * 分页查询
     *
     * @param dictionaryDTO
     */
    @Override
    public void page(Page<Dictionary> page, DictionaryDTO dictionaryDTO) {
        List<Dictionary> orders = baseMapper.page(page, dictionaryDTO);
        page.setRecords(orders);
    }

    /**
     * 列表查询
     *
     * @param dictionaryDTO
     */
    @Override
    public List<Dictionary> list(DictionaryDTO dictionaryDTO) {
        Page<Dictionary> page = new Page<>(1, Integer.MAX_VALUE);
        return baseMapper.page(page, dictionaryDTO);
    }

}
