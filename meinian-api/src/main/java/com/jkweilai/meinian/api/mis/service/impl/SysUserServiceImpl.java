package com.jkweilai.meinian.api.mis.service.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.MD5;
import com.jkweilai.meinian.api.common.PageResult;
import com.jkweilai.meinian.api.db.mapper.SysUserMapper;
import com.jkweilai.meinian.api.db.pojo.SysUserEntity;
import com.jkweilai.meinian.api.mis.service.SysUserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SysUserServiceImpl implements SysUserService {

    @Resource
    private SysUserMapper sysUserMapper;

    @Override
    public Integer login(Map<String, Object> userMap) {
        //获取用户提交的用户名和密码
        String username = MapUtil.getStr(userMap, "username");
        String password = MapUtil.getStr(userMap, "password");
        //加密密码
        String pwd = generatePassword(username, password);
        // 验证用户名和密码
        return sysUserMapper.selectByUsernameAndPassword(username, pwd);
    }

    private String generatePassword(String username, String password) {
        MD5 md5 = MD5.create();
        String usernameMD5 = md5.digestHex(username);
        String usernameMD5Start = StrUtil.subWithLength(usernameMD5, 0, 6);
        String usernameMd5End = StrUtil.subSuf(usernameMD5, usernameMD5.length() - 3);
        password = usernameMD5Start + password + usernameMd5End;
        return md5.digestHex(password).toUpperCase();
    }

    @Override
    @Transactional
    public boolean modifyPassword(Map<String, Object> param) {
        int userId = MapUtil.getInt(param, "userId");
        String username = sysUserMapper.selectUserNameById(userId);
        String oldPassword = MapUtil.getStr(param, "oldPassword");
        String newPassword = MapUtil.getStr(param, "newPassword");

        String oldPwdMD5 = generatePassword(username, oldPassword);
        String newPwdMD5 = generatePassword(username, newPassword);

        return sysUserMapper.updatePassword(userId, oldPwdMD5, newPwdMD5) == 1;
    }

    @Override
    public PageResult<Map<String, Object>> pageQueryByCondition(Map<String, Object> param) {
        //获取总记录条数和当前页面数据，创建空集合
        long total = sysUserMapper.selectCountByQueryCondition(param);
        List<Map<String, Object>> records = sysUserMapper.selectPageByQueryCondition(param);
        Map<String, Object> result = new HashMap<>();

        if(total > 0){
            records = sysUserMapper.selectPageByQueryCondition(param);
        }
        //查询后，拿到pageSize和当前页码pageNo
        Integer pageSize = MapUtil.getInt(param, "pageSize");
        Integer pageNo = MapUtil.getInt(param, "pageNo");
        //封装分页对象
        PageResult<Map<String, Object>> pageResult = new PageResult<>(total, pageSize, pageNo, records);
        return pageResult;
    }

    @Override
    @Transactional
    public int save(SysUserEntity user) {
        String passwordMD5 = generatePassword(user.getUsername(), user.getPassword());
        user.setPassword(passwordMD5);
        return sysUserMapper.insert(user);
    }

    @Override
    public Map<String, Object> findById(int userId) {
        return  sysUserMapper.selectById(userId);
    }

    @Override
    @Transactional
    public int modify(Map<String, Object> user) {
        return  sysUserMapper.update(user);
    }

    @Override
    @Transactional
    public int removeByIds(Integer[] ids) {
        return sysUserMapper.deleteByIds(ids);
    }

    @Override
    @Transactional
    public int dismiss(int userId) {
        return sysUserMapper.updateStatus(userId);
    }

    @Override
    public Map<String, Object> findDoctorById(int id) {
        return sysUserMapper.selectDoctorById(id);
    }

}
