package com.leyou.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyou.order.entity.TbOrder;
import org.apache.ibatis.annotations.Update;


/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author SYL
 * @since 2020-02-24
 */
public interface TbOrderMapper extends BaseMapper<TbOrder> {

    @Update("update tb_order set status=5 where status=1 and TIMESTAMPDIFF(MINUTE,create_time,NOW())>60")
    void updateOvertimeOreder();

}
