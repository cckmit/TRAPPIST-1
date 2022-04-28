package com.t1.sys.base.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.t1.common.config.GlobalConfig;
import com.t1.common.utils.StrUtil;
import com.t1.sys.base.entity.DictData;
import com.t1.sys.base.mapper.DictDataMapper;
import com.t1.sys.base.service.DictDataService;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 字典数据表 服务实现类
 * </p>
 *
 * @author Bruce Lee ( copy )
 * @since 2019-01-30
 */
@Service
@AllArgsConstructor
public class DictDataServiceImpl extends ServiceImpl<DictDataMapper, DictData> implements DictDataService {

    private final String REDIS_DIR = "dictdata:";
    private final RedisTemplate redisTemplate;

    public List<DictData> getDictDataList(String dictType) {
        List<DictData> dictDataList = new ArrayList<>();
        //redis缓存
        String cacheKey = REDIS_DIR + dictType;
        if (GlobalConfig.isRedisSwitch()) {
            Object dicts = redisTemplate.opsForValue().get(cacheKey);
            if (!StrUtil.isEmptyIfStr(dicts)) {
                dictDataList = JSONUtil.toList(JSONUtil.parseArray(dicts.toString()), DictData.class);
            } else {
                dictDataList = baseMapper.selectList(new QueryWrapper<DictData>().eq("dict_type", dictType).orderByAsc("sort"));
                redisTemplate.opsForValue().set(cacheKey, JSONUtil.toJsonStr(dictDataList));
            }
        } else {
            dictDataList = baseMapper.selectList(new QueryWrapper<DictData>().eq("dict_type", dictType).orderByAsc("sort"));
        }
        return dictDataList;
    }
}