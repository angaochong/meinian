package com.jkweilai.meinian.api.mis.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.json.JSONUtil;
import com.jkweilai.meinian.api.common.PageResult;
import com.jkweilai.meinian.api.common.R;
import com.jkweilai.meinian.api.db.pojo.SysUserEntity;
import com.jkweilai.meinian.api.mis.controller.form.*;
import com.jkweilai.meinian.api.mis.service.SysUserService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/mis/user")
public class UserController {

    @GetMapping("/findDoctorById")
    @SaCheckLogin
    public R findDoctorById(){
        int userId = StpUtil.getLoginIdAsInt();
        Map<String, Object> map = sysUserService.findDoctorById(userId);
        return  R.ok().put("result", map);
    }

    @PutMapping("/dismiss")
    @SaCheckPermission(value = {"ROOT", "USER:UPDATE"}, mode = SaMode.OR)
    public R dismiss(@RequestBody @Valid DismissUserForm dismissUserForm){
        Integer userId = dismissUserForm.getUserId();
        int rows = sysUserService.dismiss(userId);
        if(rows > 0){
            StpUtil.logout(dismissUserForm.getUserId());
        }
        return R.ok().put("rows", rows);
    }


    @DeleteMapping("/remove")
    @SaCheckPermission(value = {"ROOT", "USER:DELETE"}, mode = SaMode.OR)
    public R remove(@RequestBody @Valid RemoveUserByIdsForm removeUserByIdsForm) {
        int id = StpUtil.getLoginIdAsInt();
        if(ArrayUtil.contains(removeUserByIdsForm.getUserIds(), id)){
            return R.error("您不能删除自己");
        }
        int rows = sysUserService.removeByIds(removeUserByIdsForm.getUserIds());
        if(rows > 0){
            for(Integer userId: removeUserByIdsForm.getUserIds()){
                StpUtil.logout(userId);
            }
        }
        return R.ok().put("rows", rows);
    }

    @PostMapping("/modify")
    @SaCheckPermission(value = {"ROOT", "USER.UPDATE"}, mode = SaMode.OR)
    public R modify(@RequestBody @Valid ModifyUserForm modifyUserForm) {
        //前端传来的数据校验后，这里参数转为Map类型的，将来写mapper接口的时候不用写@Param("userId") Long userId 给参数起名字了
        Map<String, Object> user = BeanUtil.beanToMap(modifyUserForm);
        //前端传过来的是Integer[]，后端要存json串数组
        user.replace("roleIds", JSONUtil.toJsonStr(modifyUserForm.getRoleIds()));
        int rows = sysUserService.modify(user);
        if(rows == 1){
            //修改了用户信息之后要踢下线，让他重新登录才能拥有新的角色和权限
            StpUtil.logout(modifyUserForm.getUserId());
        }
        return R.ok().put("rows", rows);
    }

    @PostMapping("/findUserById")
    @SaCheckPermission(value = {"ROOT", "USER.SELECT"}, mode = SaMode.OR)
    public R findUserById(@RequestBody @Valid FindUserByIdForm findUserByIdForm){
        Map<String, Object> result = sysUserService.findById(findUserByIdForm.getUserId());
        return R.ok().put("result", result);
    }

    @PostMapping("/save")
    @SaCheckPermission(value = {"ROOT", "USER:INSERT"}, mode = SaMode.OR)
    public R save(@RequestBody @Valid SaveUserForm saveUserForm){
        SysUserEntity user = BeanUtil.toBean(saveUserForm, SysUserEntity.class);

        //前端传过来roleIds数组，form用Integer[]数组接收了，但是往数据库里存放得用json数组字符串
        Integer[] roleIds = saveUserForm.getRoleIds();
        String jsonStr = JSONUtil.toJsonStr(roleIds);
        user.setRoleIds(jsonStr);

        int rows = sysUserService.save(user);
        return R.ok().put("rows", rows);
    }

    @PostMapping("/pageQuery")
    @SaCheckPermission(value = {"ROOT", "USER:SELECT"}, mode = SaMode.OR)
    public R pageQuery(@RequestBody @Valid UserPageQueryForm userPageQueryForm){
        Map<String, Object> param = BeanUtil.beanToMap(userPageQueryForm);
        Integer pageSize = userPageQueryForm.getPageSize();
        Integer pageNo = userPageQueryForm.getPageNo();
        //当前页的起始序号
        Integer startIndex = (pageNo - 1) * pageSize;
        param.put("startIndex", startIndex);
        PageResult<Map<String, Object>> pageResult = sysUserService.pageQueryByCondition(param);

        return R.ok().put("pageResult", pageResult);
    }

    @PostMapping("/modifyPassword")
    @SaCheckLogin
    public R modifyPassword(@RequestBody @Valid ModifyPasswordForm modifyPasswordForm){
        int userId = StpUtil.getLoginIdAsInt();
        String oldPassword = modifyPasswordForm.getOldPassword();
        String newPassword = modifyPasswordForm.getNewPassword();

        Map<String, Object> param = new HashMap<>() {
            {
                put("oldPassword", oldPassword);
                put("newPassword", newPassword);
                put("userId", userId);
            }
        };

        boolean ok = sysUserService.modifyPassword(param);

        //响应记录条数1代表成功
        return R.ok().put("result", ok);
    }

    @Resource
    private SysUserService sysUserService;

    @PostMapping("/login")
    //添加@Valid,单独写一个校验类LoginForm，前端传过来数据后，先校验LoginForm类中的属性
    //@RequestBody将前端数据转换为json
    public R login(@RequestBody @Valid LoginForm loginForm) {
        // 将loginForm转换成Map
        Map<String, Object> loginMap = BeanUtil.beanToMap(loginForm);
        // 调用service登录
        Integer userId = sysUserService.login(loginMap);
        if(userId != null){
            // 登录成功，生成令牌，使用同端互斥,id,token都会存储到redis中
            StpUtil.login(userId, "web");
            // 获取令牌
            String token = StpUtil.getTokenValueByLoginId(userId, "web");
            // 获取权限列表
            List<String> permissions = StpUtil.getPermissionList();
            // 响应
            return R.ok().put("result", true).put("token", token).put("permissions", permissions);
        }
        // 登录失败
        return R.ok().put("Result", false);
    }

    @GetMapping("/logout")
    //登录才能退出
    @SaCheckLogin
    public R logout(){
        int userId = StpUtil.getLoginIdAsInt();
        StpUtil.logout(userId, "Web");
        return R.ok();
    }
}
