package company.victoria.ecool.repository.course;

import company.victoria.ecool.model.course.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleRepository  extends JpaRepository<Article, Long> {

    @Query("SELECT a FROM Article a where a.section.id = :sectionId")
    List<Article> findBySectionId(@Param("sectionId") Long sectionId);

    @Query("SELECT a FROM Article a WHERE a.section.part.course.isCompleted = true AND a.title LIKE CONCAT ('%', :keyword, '%')")
    List<Article> search(@Param("keyword") String keyword);

}
