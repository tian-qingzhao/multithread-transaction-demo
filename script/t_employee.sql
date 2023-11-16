-- 创建表
create table t_employee
(
    id            int(11) not null AUTO_INCREMENT,
    employee_name varchar(32) default null,
    create_time   datetime,
    PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC COMMENT='员工表';

-- 插入一条测试数据
insert into t_employee(employee_name, create_time) values ('zs', now());

-- 查询表数据
select * from t_employee;