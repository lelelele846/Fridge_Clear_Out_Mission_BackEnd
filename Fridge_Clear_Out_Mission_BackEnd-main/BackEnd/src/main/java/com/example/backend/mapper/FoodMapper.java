package com.example.backend.mapper;

import com.example.backend.pojo.Food;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface FoodMapper {
    @Insert("insert into foods (id, name, emoji, kcal, carb, protein, fat, number, expired, expSoon, expiryDuration,manufactureDate, expiryDate, expiryStatus,fridge_id, create_time, update_time) values (#{id},#{name},#{emoji},#{kcal},#{carb},#{protein},#{fat},#{number},#{expired},#{expSoon},#{expiryDuration},#{manufactureDate},#{expiryDate},#{ expiryStatus},#{fridgeId},#{createTime},#{updateTime});")
    void add(Food food);
    @Select("select * from foods where fridge_id = #{fridgeId};")
    List<Food> getFoodList(Integer fridgeId);

    void deleteFood(Integer fridgeId, List<Integer> ids);

    @Select("select * from foods where fridge_id = #{fridgeId} and expSoon = 1")
    List<Food> getExpiringSoonFoods(Integer fridgeId);

    @Select("select * from foods where fridge_id = #{fridgeId} and expired = 1")
    List<Food> getExpiredFoods(Integer fridgeId);


    @Update("update foods set name=#{name}, emoji=#{emoji}, kcal=#{kcal}, carb=#{carb}, protein=#{protein}, fat=#{fat}, number=#{number}, expired=#{expired}, expSoon=#{expSoon}, expiryDuration=#{expiryDuration}, expiryDate=#{expiryDate}, expiryStatus=#{expiryStatus},update_time=#{updateTime} where id=#{id} and fridge_id=#{fridgeId}")
    void updateFood(Food food);
}
