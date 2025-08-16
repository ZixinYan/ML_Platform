package com.ml.order.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("recipient_info")
public class RecipientInfo {

    private Long memberId;
    private String recipientName;
    private String recipientAddress;
    private String recipientPhone;
   //标签:家、公司、学校等
    private String label;

}
