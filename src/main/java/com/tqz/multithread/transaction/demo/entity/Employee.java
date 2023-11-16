package com.tqz.multithread.transaction.demo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

/**
 * 员工对象
 *
 * @author tianqingzhao
 * @since 2023/11/16 9:53
 */
@ToString
@Data
@TableName("t_employee")
@NoArgsConstructor
@AllArgsConstructor
public class Employee {

    private Integer id;

    private String employeeName;

    private Date createTime;

}