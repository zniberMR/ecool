package company.victoria.ecool.repository.course;

import company.victoria.ecool.model.course.Part;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PartRepository  extends JpaRepository<Part, Long> {

    @Query("SELECT p FROM Part p where p.course.id = :courseId")
    List<Part> findByCourseId(@Param("courseId") Long courseId);

    @Query("SELECT p FROM Part p WHERE p.course.isCompleted = true AND p.title LIKE CONCAT ('%', :keyword, '%')")
    List<Part> search(@Param("keyword") String keyword);
}
