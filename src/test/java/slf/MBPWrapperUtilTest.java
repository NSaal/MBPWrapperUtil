package slf;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.jupiter.api.Test;
import slf.entity.TestDao;

class MBPWrapperUtilTest {


    @Test
    void toSqlTemplate() {
        QueryWrapper<TestDao> wrapper = new QueryWrapper<>();
        wrapper.select("id")
                .eq("name", "abc")
                .or()
                .ge("id", 10)
                .and(w -> w.like("name", "cde")
                        .or()
                        .between("id", 1, 10))
                .apply("date_formate(dateColumn,'%Y-%m-%d')={0}", "2009-09-09")
                .groupBy("group1")
                .having("sum(id) > {0}", 100)
                .orderByDesc("id")
                .last("partition by id");
        MBPWrapperUtil<TestDao> testDaoMBPWrapperUtil = new MBPWrapperUtil<>(new TestDao());
        String sqlTemplate = testDaoMBPWrapperUtil.toSqlTemplate(wrapper);
        System.out.println(sqlTemplate);
        System.out.println("=====================================");

        MBPWrapperUtil.SimpleQueryWrapper wrapper2 = new MBPWrapperUtil.SimpleQueryWrapper();
        wrapper2.eq("name", "abc")
                .or()
                .ge("id", 10)
                .and(w -> w.like("name", "cde")
                        .or()
                        .between("id", 1, 10))
                .groupBy("group1")
                .orderByDesc("id");
        MBPWrapperUtil<Object> testDaoMBPWrapperUtil2 = new MBPWrapperUtil<>(new Object());
        String sqlTemplate2 = testDaoMBPWrapperUtil2.toSqlTemplate(wrapper2);
        System.out.println(sqlTemplate2);
        System.out.println("=====================================");

        String finalSqlTemplate = "select * from( ( {0} ) a join ( {1} ) b on a.id=b.id ) where a.name={2}";
        String finalSql = testDaoMBPWrapperUtil.format(finalSqlTemplate, sqlTemplate, sqlTemplate2, "abc");
        System.out.println(finalSql);
        System.out.println("=====================================");
    }
}