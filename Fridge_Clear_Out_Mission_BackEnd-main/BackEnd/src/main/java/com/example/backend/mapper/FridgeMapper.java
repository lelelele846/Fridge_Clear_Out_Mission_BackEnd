package com.example.backend.mapper;

import com.example.backend.pojo.Fridge;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface FridgeMapper {
    @Insert("insert into fridge (id, user_id, create_time) values (#{id},#{userId},#{createTime})")
    void add(Fridge fridge);

    @Select("select id from fridge where user_id = #{currentUserId};")
    Integer selectByUserId(Integer currentUserId);
}
