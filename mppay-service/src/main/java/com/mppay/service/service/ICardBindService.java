package com.mppay.service.service;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mppay.service.entity.CardBind;
import com.mppay.service.vo.CardBindLastBindVO;
import com.mppay.service.vo.CardBindVO;

/**
 * <p>
 * 绑卡表 服务类
 * </p>
 *
 * @author chenfeihang
 * @since 2019-05-29
 */
public interface ICardBindService extends IService<CardBind> {

    List<CardBindVO> withdrBankList(Map<String, Object> map);

    List<CardBindVO> cardBindList(Map<String, Object> map);

    /**
	 * 查询[提现]银行卡信息
     * @param map userId    用户id
     *            mobile    手机号
     *            page      页码
     *            limit     页面大小
     * @return object
     * @throws Exception
     */
    Object withdrCardBindList(Map<String, Object> map) throws Exception;

    /**
     * 根据用户id查询,用户最近的绑卡信息
     * @param map
     * @return
     */
    List<CardBindLastBindVO> lastBindByUserIds(Map<String, Object> map);

    /**
     * 根据协议号查询绑定信息
     *
     * @param cardBind
     * @return
     */
    CardBind getCardBindByAgrNo(CardBind cardBind);

}
