# 失败重试表
CREATE TABLE `t_vos_common_fail_retry` (
                                           `id` varchar(50) NOT NULL,
                                           `business_type` int(10) DEFAULT NULL,
                                           `business_id` varchar(100) DEFAULT NULL,
                                           `params` varchar(255) DEFAULT NULL,
                                           `status` int(11) DEFAULT NULL,
                                           `fail_reason` varchar(255) DEFAULT NULL,
                                           `retry_times` int(11) DEFAULT NULL,
                                           `max_retry_times` int(11) DEFAULT NULL,
                                           `retry_time` datetime DEFAULT NULL,
                                           `is_active` int(11) DEFAULT NULL,
                                           `remark` varchar(255) DEFAULT NULL,
                                           `create_user_code` varchar(20) DEFAULT NULL,
                                           `create_time` datetime DEFAULT NULL,
                                           `modify_user_code` varchar(255) DEFAULT NULL,
                                           `modify_time` datetime DEFAULT NULL,
                                           PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

