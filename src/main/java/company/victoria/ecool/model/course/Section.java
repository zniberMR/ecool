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
@Table(name = "sections")
@Data
@NoArgsConstructor
public class Section extends UserDateAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "part_id", nullable = false)
    private Part part;

    @OneToMany(
            mappedBy = "section",
            cascade = CascadeType.ALL,
            fetch = FetchType.EAGER,
            orphanRemoval = true
    )
    @Fetch(FetchMode.SELECT)
    private List<Article> articles = new ArrayList<>();

    public Section(String title){
        this.title = title;
    }

    public void addArticle(Article article){
        articles.add(article);
        article.setSection(this);
    }
}
