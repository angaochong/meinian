<template>
  <div id="app">
    <div class="home-TUIKit-main">
      <TUIKit 
        v-if="sdkAppId && userId && userSig" 
        :SDKAppID="Number(sdkAppId)" 
        :userID="userId" 
        :userSig="userSig" 
      />
    </div>
  </div>
</template>

<script lang="ts" setup>
    import { ref } from 'vue';
    import { TUIKit } from '../../TUIKit';
    import axios from 'axios';

    let sdkAppId = ref('');
    let userId = ref('');
    let userSig = ref('');

    // 发送ajax请求
    async function login(){
        // 准备配置
        let config = {
            headers: {
                satoken: localStorage.getItem('token')
            }
        };
        let response = await axios.get('/meinian-api/front/customer/im/createAccount', config);
        let result = response.data.result;
        sdkAppId.value = result.sdkAppId;
        userSig.value = result.userSig;
        userId.value = result.account;
    }
    login();
</script>

<!-- 不能添加scoped，因为样式中需要设置TUIKit内部组件的样式。需要编写全局样式。-->
<style lang="less">
    @import url("customer_im.less");
</style>