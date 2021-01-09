package company.victoria.ecool.util;

import company.victoria.ecool.model.course.*;
import company.victoria.ecool.model.user.*;
import company.victoria.ecool.repository.course.CourseRepository;
import company.victoria.ecool.repository.course.FollowCourseRepository;
import company.victoria.ecool.repository.user.FollowUserRepository;
import company.victoria.ecool.repository.user.RoleRepository;
import company.victoria.ecool.repository.user.UserRepository;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class FirstTimeInitializer implements CommandLineRunner {

    private final Log logger = LogFactory.getLog(FirstTimeInitializer.class);

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private FollowCourseRepository followCourseRepository;

    @Autowired
    private FollowUserRepository followUserRepository;

    @Override
    public void run(String... strings) throws Exception {
        Role userRole    = new Role(RoleName.ROLE_USER);
        Role studentRole = new Role(RoleName.ROLE_STUDENT);
        Role formerRole  = new Role(RoleName.ROLE_FORMER);
        Role adminRole   = new Role(RoleName.ROLE_ADMIN);

        if(roleRepository.findAll().isEmpty()){
            logger.info("Create some roles...");
            userRole    = roleRepository.save(userRole);
            studentRole = roleRepository.save(studentRole);
            formerRole  = roleRepository.save(formerRole);
            adminRole   = roleRepository.save(adminRole);

            Set<Role> adminRoles = new HashSet<>();
            adminRoles.add(userRole);
            adminRoles.add(adminRole);

            Set<Role> formerRoles = new HashSet<>();
            formerRoles.add(userRole);
            formerRoles.add(studentRole);
            formerRoles.add(formerRole);

            Set<Role> studentRoles = new HashSet<>();
            studentRoles.add(userRole);
            studentRoles.add(studentRole);

            logger.info("Create some users...");

            User admin = new User();
            admin.setName("Admin");
            admin.setUsername("admin");
            admin.setEmail("admin");
            admin.setVerified(true);
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setProvider(AuthProvider.local);
            admin.setRoles(adminRoles);
            admin = userRepository.save(admin);
            logger.info("Create Admin account: " + admin.toString());

            User former1 = new User();
            former1.setName("Former 1");
            former1.setUsername("former1");
            former1.setEmail("former1");
            former1.setVerified(true);
            former1.setPassword(passwordEncoder.encode("former1"));
            former1.setProvider(AuthProvider.local);
            former1.setRoles(formerRoles);
            former1 = userRepository.save(former1);
            logger.info("Create Former 1 account: " + former1.toString());

            User former2 = new User();
            former2.setName("Former 2");
            former2.setUsername("former2");
            former2.setEmail("former2");
            former2.setVerified(true);
            former2.setPassword(passwordEncoder.encode("former2"));
            former2.setProvider(AuthProvider.local);
            former2.setRoles(formerRoles);
            former2 = userRepository.save(former2);
            logger.info("Create Former 2 account: " + former2.toString());

            User former3 = new User();
            former3.setName("Former 3");
            former3.setUsername("former3");
            former3.setEmail("former3");
            former3.setVerified(true);
            former3.setPassword(passwordEncoder.encode("former3"));
            former3.setProvider(AuthProvider.local);
            former3.setRoles(formerRoles);
            former3 = userRepository.save(former3);
            logger.info("Create Former 3 account: " + former3.toString());

            User student1 = new User();
            student1.setName("Student 1");
            student1.setUsername("student1");
            student1.setEmail("student1");
            student1.setVerified(true);
            student1.setPassword(passwordEncoder.encode("student1"));
            student1.setProvider(AuthProvider.local);
            student1.setRoles(studentRoles);
            student1 = userRepository.save(student1);
            logger.info("Create Student 1 account: " + student1.toString());

            User student2 = new User();
            student2.setName("Student 2");
            student2.setUsername("student2");
            student2.setEmail("student2");
            student2.setVerified(true);
            student2.setPassword(passwordEncoder.encode("student2"));
            student2.setProvider(AuthProvider.local);
            student2.setRoles(studentRoles);
            student2 = userRepository.save(student2);
            logger.info("Create Student 2 account: " + student2.toString());

            User student3 = new User();
            student3.setName("Student 3");
            student3.setUsername("student3");
            student3.setEmail("student3");
            student3.setVerified(true);
            student3.setPassword(passwordEncoder.encode("student3"));
            student3.setProvider(AuthProvider.local);
            student3.setRoles(studentRoles);
            student3 = userRepository.save(student3);
            logger.info("Create Student 3 account: " + student3.toString());

            logger.info("Create some courses...");

            Course course1 = new Course();
            course1.setCreatedBy(former1.getId());
            course1.setUpdatedBy(former1.getId());
            course1.setTitle("Course 1");
            course1.setDescription("Cum competition prarere, omnes nixes imperium audax, fidelis rumores.");
            course1.setGoals("Pol, a bene adiurator, sectam!");
            course1.setPrerequisites("Congregabo foris ducunt ad flavum secula.");

            Part part1 = new Part("Part 1 in course 1");
            part1.setCreatedBy(former1.getId());
            part1.setUpdatedBy(former1.getId());
            part1.setCourse(course1);
            course1.addPart(part1);

            Section section1 = new Section("Section 1 in part 1 in course 1");
            section1.setCreatedBy(former1.getId());
            section1.setUpdatedBy(former1.getId());
            section1.setPart(part1);
            part1.addSection(section1);

            Article article1 = new Article("Article 1 in section 1 in part 1 in course 1");
            article1.setCreatedBy(former1.getId());
            article1.setUpdatedBy(former1.getId());
            article1.setSection(section1);
            section1.addArticle(article1);

            Paragraph paragraph1 = new Paragraph("Paragraph 1 in article 1 in section 1 in part 1 in course 1");
            paragraph1.setCreatedBy(former1.getId());
            paragraph1.setUpdatedBy(former1.getId());
            paragraph1.setArticle(article1);
            article1.addParagraph(paragraph1);

            course1.setCompleted(true);

            courseRepository.save(course1);

            Course course2 = new Course();
            course2.setCreatedBy(former2.getId());
            course2.setUpdatedBy(former2.getId());
            course2.setTitle("Course 2");
            course2.setDescription("Nunquam locus verpa.");
            course2.setGoals("Compaters sunt abnobas de emeritis luna.");
            course2.setPrerequisites("Est peritus eleates, cesaris.");

            part1 = new Part("Part 1 in course 2");
            part1.setCreatedBy(former2.getId());
            part1.setUpdatedBy(former2.getId());
            part1.setCourse(course2);
            course2.addPart(part1);

            section1 = new Section("Section 1 in part 1 in course 2");
            section1.setCreatedBy(former2.getId());
            section1.setUpdatedBy(former2.getId());
            section1.setPart(part1);
            part1.addSection(section1);

            article1 = new Article("Article 1 in section 1 in part 1 in course 2");
            article1.setCreatedBy(former2.getId());
            article1.setUpdatedBy(former2.getId());
            article1.setSection(section1);
            section1.addArticle(article1);

            paragraph1 = new Paragraph("Paragraph 1 in article 1 in section 1 in part 1 in course 2");
            paragraph1.setCreatedBy(former2.getId());
            paragraph1.setUpdatedBy(former2.getId());
            paragraph1.setArticle(article1);
            article1.addParagraph(paragraph1);

            Part part2 = new Part("Part 2 in course 2");
            part2.setCreatedBy(former2.getId());
            part2.setUpdatedBy(former2.getId());
            part2.setCourse(course2);
            course2.addPart(part2);

            section1 = new Section("Section 1 in part 2 in course 2");
            section1.setCreatedBy(former2.getId());
            section1.setUpdatedBy(former2.getId());
            section1.setPart(part2);
            part2.addSection(section1);

            article1 = new Article("Article 1 in section 1 in part 2 in course 2");
            article1.setCreatedBy(former2.getId());
            article1.setUpdatedBy(former2.getId());
            article1.setSection(section1);
            section1.addArticle(article1);

            paragraph1 = new Paragraph("Paragraph 1 in article 1 in section 1 in part 2 in course 2");
            paragraph1.setArticle(article1);
            paragraph1.setCreatedBy(former2.getId());
            paragraph1.setUpdatedBy(former2.getId());
            article1.addParagraph(paragraph1);

            course2.setCompleted(true);

            courseRepository.save(course2);

            Course course3 = new Course();
            course3.setCreatedBy(former3.getId());
            course3.setUpdatedBy(former3.getId());
            course3.setTitle("Course 3");
            course3.setDescription("Cannabis peritus resistentia est.");
            course3.setGoals("Mensa peregrinationess, tanquam grandis gabalium.");
            course3.setPrerequisites("Adelphis, solem, et classis.");

            part1 = new Part("Part 1 in course 3");
            part1.setCreatedBy(former3.getId());
            part1.setUpdatedBy(former3.getId());
            part1.setCourse(course3);
            course3.addPart(part1);

            section1 = new Section("Section 1 in part 1 in course 3");
            section1.setCreatedBy(former3.getId());
            section1.setUpdatedBy(former3.getId());
            section1.setPart(part1);
            part1.addSection(section1);

            article1 = new Article("Article 1 in section 1 in part 1 in course 3");
            article1.setCreatedBy(former3.getId());
            article1.setUpdatedBy(former3.getId());
            article1.setSection(section1);
            section1.addArticle(article1);

            paragraph1 = new Paragraph("Paragraph 1 in article 1 in section 1 in part 1 in course 3");
            paragraph1.setCreatedBy(former3.getId());
            paragraph1.setUpdatedBy(former3.getId());
            paragraph1.setArticle(article1);
            article1.addParagraph(paragraph1);

            Article article2 = new Article("Article 2 in section 1 in part 1 in course 3");
            article2.setCreatedBy(former3.getId());
            article2.setUpdatedBy(former3.getId());
            article2.setSection(section1);
            section1.addArticle(article2);

            paragraph1 = new Paragraph("Paragraph 1 in article 2 in section 1 in part 1 in course 3");
            paragraph1.setArticle(article2);
            paragraph1.setCreatedBy(former3.getId());
            paragraph1.setUpdatedBy(former3.getId());
            article2.addParagraph(paragraph1);

            course3.setCompleted(true);

            courseRepository.save(course3);

            logger.info("Create some follows courses...");

            FollowCourse followCourse11 = new FollowCourse(course1, student1);
            FollowCourse followCourse21 = new FollowCourse(course2, student1);
            FollowCourse followCourse31 = new FollowCourse(course3, student1);
            FollowCourse followCourse22 = new FollowCourse(course2, student2);
            FollowCourse followCourse32 = new FollowCourse(course3, student2);
            FollowCourse followCourse33 = new FollowCourse(course3, student3);

            followCourseRepository.save(followCourse11);
            followCourseRepository.save(followCourse21);
            followCourseRepository.save(followCourse31);
            followCourseRepository.save(followCourse22);
            followCourseRepository.save(followCourse32);
            followCourseRepository.save(followCourse33);

            logger.info("Create some follows users...");

            FollowUser followUserS1S2 = new FollowUser(student1, student2);
            FollowUser followUserS1S3 = new FollowUser(student1, student3);
            FollowUser followUserS1F1 = new FollowUser(student1, former1);
            FollowUser followUserS2F2 = new FollowUser(student2, former2);
            FollowUser followUserS3F3 = new FollowUser(student3, former3);
            FollowUser followUserF1F3 = new FollowUser(former1, former3);

            followUserRepository.save(followUserS1S2);
            followUserRepository.save(followUserS1S3);
            followUserRepository.save(followUserS1F1);
            followUserRepository.save(followUserS2F2);
            followUserRepository.save(followUserS3F3);
            followUserRepository.save(followUserF1F3);
        }
    }
}
