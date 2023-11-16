package com.tqz.multithread.transaction.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tqz.multithread.transaction.demo.entity.Employee;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeMapper extends BaseMapper<Employee> {

    @Insert("<script>" +
            "insert into t_employee (employee_name,create_time) " +
            "values " +
            "<foreach collection='employeeList' item='employee' index='index' separator=','>" +
            "(#{employee.employeeName}, #{employee.createTime})" +
            "</foreach>" +
            "</script>")
    int saveBatchEmployee(@Param("employeeList") List<Employee> employeeList);

}