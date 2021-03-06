package org.qk.pyq.water.util;

import org.qk.pyq.water.entity.Location;
import org.qk.pyq.water.service.LocationService;
import org.qk.pyq.water.service.RecordService;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/*
 * 定时给每个水表生成一条随机记录
 */
@Component
@EnableScheduling
public class RecordTimedGenerator {
    final LocationService locationService;
    final RecordService recordService;

    public RecordTimedGenerator(LocationService locationService,
                                RecordService recordService) {
        this.locationService = locationService;
        this.recordService = recordService;
    }

    // 每分钟的第0秒执行执行一次
    @Scheduled(cron = "0 * * * * ?")
    public void generateNewRandomRecord() {
        // 读取全部水表
        List<Location> locationList = locationService.retrieveList();
        // 遍历全部水表的位置信息并添加一条记录，瞬时用量随机生成
        for (Location location : locationList) {
            Integer waterId;
            String name;
            double instantUsage;
            StringBuilder stringInstantUsage = new StringBuilder();
            long recordDate;

            waterId = location.getWaterId(); // 获取水表编号
            name = location.getName(); //　获取水表名称

            // 随机生成一个瞬时用量，保留2位小数，四舍五入
            stringInstantUsage.append(String.format("%.2f", Math.random() * 10));
            instantUsage = Double.parseDouble(stringInstantUsage.toString());

            // 日期时间
            Instant instant = Instant.now();
            recordDate = instant.toEpochMilli();

            // 添加记录
            recordService.addOrModifyOne(null, waterId, recordDate, instantUsage);

            // 终端输出
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            ZonedDateTime zonedDateTime = instant.atZone(ZoneId.systemDefault());
            String recordDateToPrint = zonedDateTime.format(dateTimeFormatter);
            System.out.println(" --- " + recordDateToPrint
                + " --- \t已生成随机记录\t水表编号: " + waterId
                + "\t瞬时用量: " + instantUsage
                + "\t水表名称: " + name);
        }
    }
}
