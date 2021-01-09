package company.victoria.ecool.model.course;

import company.victoria.ecool.model.audit.UserDateAudit;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "articles")
@Data
@NoArgsConstructor
public class Article extends UserDateAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;

    @OneToMany(
            mappedBy = "article",
            cascade = CascadeType.ALL,
            fetch = FetchType.EAGER,
            orphanRemoval = true
    )
    @Fetch(FetchMode.SELECT)
    private List<Paragraph> paragraphs = new ArrayList<>();

    public Article(String title){
        this.title = title;
    }

    public void addParagraph(Paragraph paragraph){
        paragraphs.add(paragraph);
        paragraph.setArticle(this);
    }
}
