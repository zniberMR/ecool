package company.victoria.ecool.repository.course;

import company.victoria.ecool.model.course.Course;
import company.victoria.ecool.model.course.FollowCourse;
import company.victoria.ecool.model.user.User;
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
public interface FollowCourseRepository extends JpaRepository<FollowCourse, Long> {

    @Query("SELECT f FROM FollowCourse f where f.user.id = :userId and f.course.id = :courseId")
    FollowCourse findByUserIdAndCourseId(@Param("userId") Long userId, @Param("courseId") Long courseId);

    @Query("SELECT COUNT(f.id) from FollowCourse f where f.course.id = :courseId")
    Long countByCourseId(@Param("courseId") Long courseId);

    @Query("SELECT f.course FROM FollowCourse f WHERE f.user.id = :userId")
    Page<Course> findFollowedCourseByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT f.user FROM FollowCourse f WHERE f.course.id = :courseId")
    Page<User> findFollowersByCourseId(@Param("courseId") Long courseId, Pageable pageable);

    List<FollowCourse> findAllByCourseId(Long courseId);
    
    Boolean existsByUserId(Long userId);

    @Transactional
    @Modifying
    @Query("DELETE FROM FollowCourse f WHERE f.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
