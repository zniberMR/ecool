package company.victoria.ecool.model.user;
import com.fasterxml.jackson.annotation.JsonIgnore;
import company.victoria.ecool.model.audit.DateAudit;
import lombok.Data;
import org.hibernate.annotations.NaturalId;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = {
                "username"
        }),
        @UniqueConstraint(columnNames = {
                "email"
        })
})
@Data
public class User extends DateAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String username;

    @NaturalId
    @Column(nullable = false)
    private String email;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(columnDefinition = "LONGTEXT", length = 2147483647)
    private String about;

    private String country;

    private String city;

    private String skills;

    private String imageUrl;

    private String bannerUrl;

    @Column(nullable = false)
    private Boolean isVerified = false;

    @JsonIgnore
    private String password;

    @Enumerated(EnumType.STRING)
    private AuthProvider provider;

    private String providerId;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    public Boolean getVerified(){
        return isVerified;
    }

    public void setVerified(Boolean isVerified){
        this.isVerified = isVerified;
    }
}
