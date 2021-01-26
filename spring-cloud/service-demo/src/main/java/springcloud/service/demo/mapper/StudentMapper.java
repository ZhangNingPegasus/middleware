package springcloud.service.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import springcloud.service.demo.entity.Student;

@Mapper
public interface StudentMapper extends BaseMapper<Student> {
}