package com.jkweilai.meinian.api.config.satoken;

import cn.dev33.satoken.stp.StpInterface;
import com.jkweilai.meinian.api.db.mapper.SysUserMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

// 纳入IoC容器的管理
@Component
public class StpInterfaceImpl implements StpInterface {

    @Resource
    private SysUserMapper sysUserMapper;

    /**
     * 获取用户的权限
     *
     * @param loginId   账号id （SaToken自动从Redis中获取到，然后给我们传过来了）
     * @param loginType 账号类型
     * @return
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        int userId = Integer.parseInt(String.valueOf(loginId));
        Set<String> permissions = sysUserMapper.getPermissionsByUserId(userId);
        return new ArrayList<>(permissions);
    }

    /**
     * 获取用户的角色
     * @param loginId  账号id
     * @param loginType 账号类型
     * @return
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return new ArrayList<>();
    }
}
