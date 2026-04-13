package com.photo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.photo.entity.SystemConfig;
import com.photo.mapper.SystemConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SystemConfigService extends ServiceImpl<SystemConfigMapper, SystemConfig> {

    private final SystemConfigMapper systemConfigMapper;

    public Map<String, String> getAllConfigs() {
        List<SystemConfig> configs = systemConfigMapper.selectList(null);
        Map<String, String> configMap = new HashMap<>();
        for (SystemConfig config : configs) {
            configMap.put(config.getConfigKey(), config.getConfigValue());
        }
        return configMap;
    }

    public String getConfig(String key) {
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemConfig::getConfigKey, key);
        SystemConfig config = systemConfigMapper.selectOne(wrapper);
        return config != null ? config.getConfigValue() : null;
    }

    @Transactional
    public void setConfig(String key, String value) {
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemConfig::getConfigKey, key);
        SystemConfig config = systemConfigMapper.selectOne(wrapper);

        if (config != null) {
            config.setConfigValue(value);
            systemConfigMapper.updateById(config);
        } else {
            config = new SystemConfig();
            config.setConfigKey(key);
            config.setConfigValue(value);
            systemConfigMapper.insert(config);
        }
    }

    @Transactional
    public void setConfigs(Map<String, String> configs) {
        for (Map.Entry<String, String> entry : configs.entrySet()) {
            setConfig(entry.getKey(), entry.getValue());
        }
    }
}
