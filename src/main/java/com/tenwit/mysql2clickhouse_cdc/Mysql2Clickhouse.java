package com.tenwit.mysql2clickhouse_cdc;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.SqlDialect;
import org.apache.flink.table.api.TableResult;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

public class Mysql2Clickhouse {
        public static void main(String[] args) throws Exception {
            EnvironmentSettings fsSettings = EnvironmentSettings.newInstance()
                    .useBlinkPlanner()
                    .inStreamingMode()
                    .build();
            StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
            env.setParallelism(1);
            StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env, fsSettings);



            tableEnv.getConfig().setSqlDialect(SqlDialect.DEFAULT);


            // 数据源表
            String sourceDDL =
                    "CREATE TABLE mysql_binlog (\n" +
                            " id INT NOT NULL,\n" +
                            " name STRING,\n" +
                            " description STRING\n" +
                            ") WITH (\n" +
                            " 'connector' = 'mysql-cdc',\n" +
                            " 'hostname' = 'localhost',\n" +
                            " 'port' = '3306',\n" +
                            " 'username' = 'flinkcdc',\n" +
                            " 'password' = 'dafei1288',\n" +
                            " 'database-name' = 'test',\n" +
                            " 'table-name' = 'test_cdc'\n" +
                            ")";


            String url = "jdbc:mysql://127.0.0.1:3306/test";
            String userName = "root";
            String password = "dafei1288";
            String mysqlSinkTable = "test_cdc_sink";
            // 输出目标表
            String sinkDDL =
                    "CREATE TABLE test_cdc_sink (\n" +
                            " id INT NOT NULL,\n" +
                            " name STRING,\n" +
                            " description STRING,\n" +
                            " PRIMARY KEY (id) NOT ENFORCED \n " +
                            ") WITH (\n" +
                            " 'connector' = 'jdbc',\n" +
                            " 'driver' = 'com.mysql.jdbc.Driver',\n" +
                            " 'url' = '" + url + "',\n" +
                            " 'username' = '" + userName + "',\n" +
                            " 'password' = '" + password + "',\n" +
                            " 'table-name' = '" + mysqlSinkTable + "'\n" +
                            ")";
            // 简单的聚合处理
            String transformSQL =
                    "insert into test_cdc_sink select * from mysql_binlog";

            tableEnv.executeSql(sourceDDL);
            tableEnv.executeSql(sinkDDL);
            TableResult result = tableEnv.executeSql(transformSQL);

            // 等待flink-cdc完成快照
            result.print();
            env.execute("sync-flink-cdc");
        }

    }



