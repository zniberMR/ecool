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
@Table(name = "courses")
@Data
@NoArgsConstructor
public class Course extends UserDateAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
	private String description;

    @Column(nullable = false)
    private String goals;

    private String prerequisites;

    private String imageUrl;

    private Boolean isCompleted;

    @OneToMany(
            mappedBy = "course",
            cascade = CascadeType.ALL,
            fetch = FetchType.EAGER,
            orphanRemoval = true
    )
    @Fetch(FetchMode.SELECT)
    private List<Part> parts = new ArrayList<>();

    public void addPart(Part part){
        parts.add(part);
        part.setCourse(this);
    }

    public void setCompleted(Boolean isCompleted){
        this.isCompleted = isCompleted;
    }

    public Boolean getCompleted(){
        return this.isCompleted;
    }
}
