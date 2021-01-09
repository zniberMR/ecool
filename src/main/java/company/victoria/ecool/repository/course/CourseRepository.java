package company.victoria.ecool.repository.course;

import company.victoria.ecool.model.course.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface CourseRepository  extends JpaRepository<Course, Long> {

    Page<Course> findByCreatedBy(Long creatorId, Pageable pageable);

    List<Course> findAllCourseByCreatedBy(Long creatorId);

    @Query("SELECT c FROM Course c WHERE c.id IN :courseIds")
    Page<Course> findAllByIdIn(@Param("courseIds") List<Long> courseIds, Pageable pageable);

    @Query("SELECT c FROM Course c WHERE c.isCompleted = true AND c.title LIKE CONCAT ('%', :keyword, '%') OR c.description LIKE CONCAT ('%', :keyword, '%') OR c.goals LIKE CONCAT ('%', :keyword, '%') OR c.prerequisites LIKE CONCAT ('%', :keyword, '%') ")
    List<Course> search(@Param("keyword") String keyword);

    Boolean existsByCreatedBy(Long createdBy);

    @Query("SELECT c FROM Course c WHERE c.createdBy = :creatorId AND c.isCompleted = true")
    List<Course> findCompletedCourseByCreatedBy(@Param("creatorId") Long creatorId);

    @Query("SELECT c FROM Course c WHERE c.createdBy = :creatorId AND c.isCompleted = true")
    Page<Course> findCompletedByCreatedBy(@Param("creatorId") Long creatorId, Pageable pageable);

    @Query("SELECT c FROM Course c WHERE c.isCompleted = true")
    Page<Course> findAllCompletedCourses(Pageable pageable);
}
