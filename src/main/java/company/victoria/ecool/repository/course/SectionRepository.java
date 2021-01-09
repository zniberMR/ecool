package company.victoria.ecool.repository.course;

import company.victoria.ecool.model.course.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SectionRepository  extends JpaRepository<Section, Long> {

    @Query("SELECT s FROM Section s where s.part.id = :partId")
    List<Section> findByPartId(@Param("partId") Long partId);

    @Query("SELECT s FROM Section s WHERE s.part.course.isCompleted = true AND s.title LIKE CONCAT ('%', :keyword, '%')")
    List<Section> search(@Param("keyword") String keyword);

}
