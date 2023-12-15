
package com.tqz.multithread.transaction.demo.controller;

import com.tqz.multithread.transaction.demo.entity.Employee;
import com.tqz.multithread.transaction.demo.exception.ServiceException;
import com.tqz.multithread.transaction.demo.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/sys")
public class EmployeeController {

    @Autowired
    EmployeeService employeeService;

    @Autowired
    @Qualifier("employeeServiceImplV2")
    private EmployeeService employeeServiceV2;

    @GetMapping("/saveThread1")
    public String saveThread1() {
        try {
            List<Employee> employeeDOList = getList();
            employeeService.saveThread1(employeeDOList);
            return System.currentTimeMillis() + " 添加成功";
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceException("添加失败", 500);
        }
    }

    @GetMapping("/saveThread2")
    public String saveThread2() {
        try {
            List<Employee> employeeDOList = getList();
            employeeService.saveThread2(employeeDOList);
            return System.currentTimeMillis() + " 添加成功";
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceException("添加失败", 500);
        }
    }

    @GetMapping("/saveThread3")
    public String saveThread3() {
        try {
            List<Employee> employeeDOList = getList();
            employeeService.saveThread3(employeeDOList);
            return System.currentTimeMillis() + " 添加成功";
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceException("添加失败", 500);
        }
    }

    @RequestMapping("insertAndUpdateV2")
    public String insertAndUpdateV2() {
        try {
            employeeServiceV2.insertAndUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return System.currentTimeMillis() + "";
    }

    private List<Employee> getList() {
        int size = 10;
        List<Employee> employeeDOList = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            Employee employee = new Employee();
            employee.setEmployeeName("lol" + i);
            employee.setCreateTime(Calendar.getInstance().getTime());
            employeeDOList.add(employee);
        }

        return employeeDOList;
    }
}