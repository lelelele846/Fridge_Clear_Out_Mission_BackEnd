package com.example.backend.pojo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.LocalDate;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class Food implements Serializable {
    private Integer id;
    private String name;
    private String emoji;
    private Double kcal;
    private Double carb;
    private Double protein;
    private Double fat;
    private Integer quantity;
    private Integer number; // 数量
    private Integer expiryDuration;//保质期
    private LocalDateTime manufactureDate;//生产日期
    private LocalDate expiryDate;    // 过期日期
    private Boolean isExpired;      // 已过期
    private Boolean isExpiringSoon; // 即将过期
    private String expiryStatus;    // 过期状态
    private Integer fridgeId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime; // 更新时间

    // 自动计算过期时间
    public LocalDateTime calculateExpiryDate() {
        if (manufactureDate != null && expiryDuration != null) {
            return manufactureDate.plusDays(expiryDuration);
        }
        return null;
    }

    // 快速判断状态（可选）
    public boolean isFresh() {
        return !isExpired && !isExpiringSoon;
    }

    public Boolean getExpired() {
        return isExpired;
    }

    public void setExpired(Boolean expired) {
        this.isExpired = expired;
    }

    public Boolean getExpSoon() {
        return isExpiringSoon;
    }

    public void setExpSoon(Boolean expSoon) {
        this.isExpiringSoon = expSoon;
    }

    // 获取剩余天数
    public long getRemainingDays() {
        if (manufactureDate == null || expiryDuration == null) {
            return -1;
        }
        LocalDate expiryDate = manufactureDate.plusDays(expiryDuration).toLocalDate();
        return ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
    }

    // 设置过期日期
    public void setExpiryDateFromManufacture() {
        if (manufactureDate != null && expiryDuration != null) {
            this.expiryDate = manufactureDate.plusDays(expiryDuration).toLocalDate();
        }
    }

}


