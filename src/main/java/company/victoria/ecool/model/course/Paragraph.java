package company.victoria.ecool.model.course;

import company.victoria.ecool.model.audit.UserDateAudit;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "paragraphs")
@Data
@NoArgsConstructor
public class Paragraph extends UserDateAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(nullable = false, columnDefinition = "LONGTEXT", length = 2147483647)
    private String content;

    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    public Paragraph(String content){
        this.content = content;
    }

    public Paragraph(String content, String imageUrl) {
        this.content = content;
        this.imageUrl = imageUrl;
    }
}
