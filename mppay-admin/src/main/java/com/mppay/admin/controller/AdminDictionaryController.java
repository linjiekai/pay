package com.mppay.admin.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mppay.core.utils.ResponseUtil;
import com.mppay.service.dto.DictionaryDTO;
import com.mppay.service.entity.Dictionary;
import com.mppay.service.service.IDictionaryService;

import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

/**
 * @author Administrator
 * @title: AdminDictionaryController
 * @projectName xfhl-mppay-api
 * @description: 字典信息
 * @date 2019/10/16 16:23
 */
@Slf4j
@RestController
@RequestMapping("/admin/dictionary")
public class AdminDictionaryController {

    @Resource
    private IDictionaryService dictionaryService;

    /**
     * 分页查询
     *
     * @param dictionaryDTO
     * @return
     */
    @PostMapping("/page")
    public Object page(@RequestBody DictionaryDTO dictionaryDTO) {
        log.info("|字典信息|分页|接收到请求报文:{}", dictionaryDTO);
        Page<Dictionary> page = new Page<>(dictionaryDTO.getPage(), dictionaryDTO.getLimit());
        dictionaryService.page(page, dictionaryDTO);
        Map<String, Object> data = new HashMap<>();
        data.put("total", page.getTotal());
        data.put("items", page.getRecords());
        return ResponseUtil.ok(data);
    }


    /**
     * 列表查询
     *
     * @param dictionaryDTO
     * @return
     */
    @PostMapping("/list")
    public Object list(@RequestBody DictionaryDTO dictionaryDTO) {
        log.info("|字典信息|列表|接收到请求报文:{}", dictionaryDTO);
        List<Dictionary> list = dictionaryService.list(dictionaryDTO);
        return ResponseUtil.ok(list);
    }

    /**
     * 修改字典
     *
     * @param dictionary
     * @return
     */
    @PostMapping("/update")
    public Object update(@RequestBody Dictionary dictionary) {
        log.info("|字典信息|更新|接收到请求报文:{}", dictionary);
        dictionaryService.updateById(dictionary);
        return ResponseUtil.ok();
    }

}
