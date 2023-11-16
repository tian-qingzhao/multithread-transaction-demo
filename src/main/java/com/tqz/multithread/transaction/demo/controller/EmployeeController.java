
package com.tqz.multithread.transaction.demo.controller;

import com.tqz.multithread.transaction.demo.entity.Employee;
import com.tqz.multithread.transaction.demo.exception.ServiceException;
import com.tqz.multithread.transaction.demo.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/sys")
public class EmployeeController {

    @Autowired
    EmployeeService employeeService;

    @GetMapping("/add")
    public String batchAddEmployee() {

        int size = 10;
        List<Employee> employeeDOList = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            Employee employee = new Employee();
            employee.setEmployeeName("lol" + i);
            employee.setCreateTime(Calendar.getInstance().getTime());
            employeeDOList.add(employee);
        }
        try {
            employeeService.saveThread1(employeeDOList);
            System.out.println("添加成功");
            return System.currentTimeMillis() + " 添加成功";
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceException("添加失败", 500);
        }
    }

}