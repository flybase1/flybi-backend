package com.fly.springbootinit.mapper;

import cn.hutool.core.annotation.Alias;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fly.springbootinit.model.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

/**
 * 用户数据库操作
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {


    boolean updateAllUserLeftCountTo100();
}




