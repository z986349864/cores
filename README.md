# cores
redis、rabbitmq和redisson Lock公司使用：
    redis 封装的好处：
    1.获取缓存都自动加redisson锁
    2.注入自己创建(UserAllCache)后，根据编写的代码自动查询如无缓存，则查询自己创建的(UserAllCacheProvider)返回的数据，
      缓存中有则使用缓存中的。
    rabbitMQ 封装的好处：
    1.提供二种生产者(无事务提交和事务完成后提交)
    2.生产者失败后自动保存到失败记录表中t_vos_common_fail_retry
    3.消费者消费失败后保存到失败记录表中t_vos_common_fail_retry
    4.提供重试接口，用于XXL-JOB使用。重试次数 5次。
    excle导入导出功能：
    1. 导出，根据VO上@ExcelProperty，设置导出的类。
    2. 导入，导入失败后会自动导出失败的条目以及失败原因。(需要上传导出的模板)
    