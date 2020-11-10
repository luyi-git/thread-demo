package com.zhaoxi.lock.demo.lockdemo.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@Slf4j
public class OrderController1 {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 为了演示方便，我这里就简单定义了一个常量作为商品的id
     * 实际工作中，这个商品id是前端进行下单操作传递过来的参数
     */
    public static final String PRODUCT_ID = "100001";

    //@RequestMapping("/submitOrder")
    public String submitOrder(){
        //通过stringRedisTemplate来调用Redis的SETNX命令，key为商品的id，value为字符串“zhaoxi”
        //实际上，value可以为任意的字符换
        Boolean isLocked = stringRedisTemplate.opsForValue()
                .setIfAbsent(PRODUCT_ID, "zhaoxi",30, TimeUnit.SECONDS);
        //没有拿到锁，返回下单失败
        if(!isLocked){
            return "failure";
        }

        System.out.println("test push");
        try {
            // 设置加锁key的过期时间
            // stringRedisTemplate.expire(PRODUCT_ID,30, TimeUnit.SECONDS);

            int stock = Integer.parseInt(stringRedisTemplate.opsForValue().get("stock"));
            if(stock > 0){
                stock -= 1;
                stringRedisTemplate.opsForValue().set("stock", String.valueOf(stock));
                log.info("库存扣减成功，当前库存为：{}", stock);
                // throw new RuntimeException("制造异常造成死锁");
            }else{
                log.info("库存不足，扣减库存失败");
                throw new RuntimeException("库存不足，扣减库存失败");
            }
        } finally {
            //业务执行完成，删除PRODUCT_ID key
            stringRedisTemplate.delete(PRODUCT_ID);
        }

        return "success";
    }
}
