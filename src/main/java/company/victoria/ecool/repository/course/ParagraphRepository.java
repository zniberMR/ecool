package company.victoria.ecool.repository.course;

import company.victoria.ecool.model.course.Paragraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParagraphRepository  extends JpaRepository<Paragraph, Long> {

    @Query("SELECT p FROM Paragraph p where p.article.id = :articleId")
    List<Paragraph> findByArticleId(@Param("articleId") Long articleId);

    @Query("SELECT p FROM Paragraph p WHERE p.article.section.part.course.isCompleted = true AND p.content LIKE CONCAT ('%', :keyword, '%')")
    List<Paragraph> search(@Param("keyword") String keyword);

}
