// 验证字符串是否为空
export function stringIsEmpty(s: string){
    return s == null || s.trim().length == 0;
}

// 验证邮箱地址是否合法
export function isEmail(s: string){
    return /^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$/.test(s);
}

// 验证手机号是否合法
export function isPhone(s: string){
    return /^1(?:3\d|4[5-9]|5[0-35-9]|6[567]|7[0-8]|8\d|9[0-35-9])\d{8}$/.test(s);
}

// 验证URL地址是否合法
export function isURL(s: string){
    return /^http[s]?:\/\/.*/.test(s);
}

// 验证用户名
export function isUsername(s: string){
    return /^[a-zA-Z0-9]{5,50}$/.test(s);
}

// 验证密码
export function isPassword(s: string){
    return /^[a-zA-Z0-9]{6,20}$/.test(s);
}

// 验证手机短信验证码
export function isSmsCode(s: string){
    return /^\d{6}$/.test(s);
}