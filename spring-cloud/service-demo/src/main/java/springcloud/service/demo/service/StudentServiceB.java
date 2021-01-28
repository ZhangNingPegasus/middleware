package springcloud.service.demo.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nepxion.discovery.common.constant.DiscoveryConstant;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import springcloud.service.demo.entity.Student;
import springcloud.service.demo.mapper.StudentMapper;

@Service
@ConditionalOnProperty(name = DiscoveryConstant.SPRING_APPLICATION_NAME, havingValue = "b")
public class StudentServiceB extends ServiceImpl<StudentMapper, Student> {
    //    @GlobalTransactional
    public void save(final String name) {
        Student student = new Student();
        student.setName(name);
        this.save(student);
    }
}
