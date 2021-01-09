package company.victoria.ecool.service;

import company.victoria.ecool.exception.BadRequestException;
import company.victoria.ecool.exception.ResourceNotFoundException;
import company.victoria.ecool.model.course.*;
import company.victoria.ecool.model.user.RoleName;
import company.victoria.ecool.model.user.User;
import company.victoria.ecool.payload.KeyIdClass;
import company.victoria.ecool.payload.PagedResponse;
import company.victoria.ecool.payload.course.*;
import company.victoria.ecool.payload.user.UserProfile;
import company.victoria.ecool.payload.user.UserSummary;
import company.victoria.ecool.repository.course.*;
import company.victoria.ecool.repository.user.RoleRepository;
import company.victoria.ecool.repository.user.UserRepository;
import company.victoria.ecool.security.UserPrincipal;
import company.victoria.ecool.util.AppConstants;
import company.victoria.ecool.util.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CourseService {
    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    CourseRepository courseRepository;

    @Autowired
    PartRepository partRepository;

    @Autowired
    SectionRepository sectionRepository;

    @Autowired
    ArticleRepository articleRepository;

    @Autowired
    ParagraphRepository paragraphRepository;

    @Autowired
    FollowCourseRepository followCourseRepository;

    public PagedResponse<CourseSummary> getAllCourses(int page, int size) {
        validatePageNumberAndSize(page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
        Page<Course> courses = courseRepository.findAllCompletedCourses(pageable);

        if (courses.getNumberOfElements() == 0) {
            return new PagedResponse<>(Collections.emptyList(), courses.getNumber(),
                    courses.getSize(), courses.getTotalElements(), courses.getTotalPages(), courses.isLast());
        }

        Map<Long, User> creatorMap = getCourseCreatorMap(courses.getContent());

        List<CourseSummary> courseResponses = courses.map(course -> {
            return ModelMapper.mapCourseToCourseSummary(course, creatorMap.get(course.getCreatedBy()));
        }).getContent();

        return new PagedResponse<>(courseResponses, courses.getNumber(),
                courses.getSize(), courses.getTotalElements(), courses.getTotalPages(), courses.isLast());
    }

    public PagedResponse<?> getAllCoursesFiltered(String keyword, int page, int size) {
        validatePageNumberAndSize(page, size);

        keyword = keyword.replace(' ', '%');

        List<Course> courses1 = courseRepository.search(keyword);
        List<Part> parts = partRepository.search(keyword);
        List<Section> sections = sectionRepository.search(keyword);
        List<Article> articles = articleRepository.search(keyword);
        List<Paragraph> paragraphs = paragraphRepository.search(keyword);

        for (Part part : parts){
            if(!courses1.contains(part.getCourse())){
                courses1.add(part.getCourse());
            }
        }

        for (Section section : sections){
            if(!parts.contains(section.getPart())){
                courses1.add(section.getPart().getCourse());
            }
        }

        for (Article article : articles){
            if(!sections.contains(article.getSection())){
                courses1.add(article.getSection().getPart().getCourse());
            }
        }

        for (Paragraph paragraph : paragraphs){
            if(!articles.contains(paragraph.getArticle())){
                courses1.add(paragraph.getArticle().getSection().getPart().getCourse());
            }
        }

        List<Long> courseIds = courses1.stream().map(course -> {
            return course.getId();
        }).collect(Collectors.toList());

        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
        Page<Course> courses = courseRepository.findAllByIdIn(courseIds, pageable);


        if (courses.getNumberOfElements() == 0) {
            return new PagedResponse<>(Collections.emptyList(), courses.getNumber(),
                    courses.getSize(), courses.getTotalElements(), courses.getTotalPages(), courses.isLast());
        }

        Map<Long, User> creatorMap = getCourseCreatorMap(courses.getContent());

        List<CourseSummary> courseResponses = courses.map(course -> {
            return ModelMapper.mapCourseToCourseSummary(course, creatorMap.get(course.getCreatedBy()));
        }).getContent();

        return new PagedResponse<>(courseResponses, courses.getNumber(),
                courses.getSize(), courses.getTotalElements(), courses.getTotalPages(), courses.isLast());
    }

    public PagedResponse<CourseSummary> getCoursesCreatedBy(String username, Boolean isOwner, int page, int size){
        validatePageNumberAndSize(page, size);

        User creator = userRepository.findByUsername(username).orElseThrow(() -> {
            return new ResourceNotFoundException("User", "Username", username);
        });

        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
        Page<Course> courses;

        if (isOwner){
            courses = courseRepository.findByCreatedBy(creator.getId(), pageable);
        } else {
            courses = courseRepository.findCompletedByCreatedBy(creator.getId(), pageable);
        }

        if (courses.getNumberOfElements() == 0) {
            return new PagedResponse<>(Collections.emptyList(), courses.getNumber(),
                    courses.getSize(), courses.getTotalElements(), courses.getTotalPages(), courses.isLast());
        }

        List<CourseSummary> courseSummaries = courses.map(course -> {
            return ModelMapper.mapCourseToCourseSummary(course, creator);
        }).getContent();

        return new PagedResponse<>(courseSummaries, courses.getNumber(),
                courses.getSize(), courses.getTotalElements(), courses.getTotalPages(), courses.isLast());
    }

    public CourseSummary getCourseById(Long courseId, UserPrincipal currentUser) {
        Course course = courseRepository.findById(courseId).orElseThrow(
                () -> new ResourceNotFoundException("Course", "id", courseId));

        User creator = userRepository.findById(course.getCreatedBy())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", course.getCreatedBy()));

        boolean userFollow = false;
        FollowCourse followCourse = followCourseRepository.findByUserIdAndCourseId(currentUser.getId(), courseId);
        if(followCourse != null){
            userFollow = true;
        }

        if(userFollow || creator.getId() == currentUser.getId()){
            Map<KeyIdClass, Long> count = getCountsMap(course);

            return ModelMapper.mapCourseToCourseResponse(course, creator, count);
        } else if (course.getCompleted()){
            return ModelMapper.mapCourseToCourseSummary(course, creator);
        } else return null;

    }

    public List<CourseSummary> getCoursesByCreator(Long creatorId, Boolean isOwner){
        User creator = userRepository.findById(creatorId).orElseThrow(() -> {
            return new ResourceNotFoundException("User", "Id", creatorId);
        });

        List<Course> courses;

        if (isOwner){
            courses = courseRepository.findAllCourseByCreatedBy(creatorId);
        } else {
            courses = courseRepository.findCompletedCourseByCreatedBy(creatorId);
        }

        List<CourseSummary> courseSummaries = courses.stream().map(course -> {
            return ModelMapper.mapCourseToCourseSummary(course, creator);
        }).collect(Collectors.toList());

        return courseSummaries;
    }

    public Course createCourse(CourseRequest courseRequest) {
        Course course = new Course();
        course.setTitle(courseRequest.getTitle());
        course.setDescription(courseRequest.getDescription());
        course.setGoals(courseRequest.getGoals());
        course.setPrerequisites(courseRequest.getPrerequisites());

        for(PartRequest partRequest : courseRequest.getParts()){
            Part part = new Part(partRequest.getTitle());
            course.addPart(part);
            for(SectionRequest sectionRequest : partRequest.getSections()){
                Section section = new Section(sectionRequest.getTitle());
                part.addSection(section);
                for(ArticleRequest articleRequest : sectionRequest.getArticles()){
                    Article article = new Article(articleRequest.getTitle());
                    section.addArticle(article);
                    articleRequest.getParagraphs().forEach(paragraphRequest -> {
                        article.addParagraph(new Paragraph(paragraphRequest.getContent()));
                    });
                }
            }
        }

        return courseRepository.save(course);
    }

    public boolean followCourse(Long courseId, UserPrincipal currentUser){

        FollowCourse followCourse = followCourseRepository.findByUserIdAndCourseId(currentUser.getId(), courseId);

        Course course = courseRepository.findById(courseId).orElseThrow(() -> {
            return new ResourceNotFoundException("Course", "Id", courseId);
        });

        if(followCourse != null || !course.getCompleted()){
            return false;
        } else {

            User user = userRepository.findById(currentUser.getId()).get();

            followCourse = new FollowCourse(course, user);

            followCourseRepository.save(followCourse);

            return true;
        }

    }

    public PagedResponse<?> getFollowersByCourseId(Long courseId, int page, int size) {
        validatePageNumberAndSize(page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
        Page<User> users = followCourseRepository.findFollowersByCourseId(courseId, pageable);


        if (users.getNumberOfElements() == 0) {
            return new PagedResponse<>(Collections.emptyList(), users.getNumber(),
                    users.getSize(), users.getTotalElements(), users.getTotalPages(), users.isLast());
        }

        List<UserSummary> userSummaries = users.map(user -> {
            return ModelMapper.mapUserToUserSummary(user);
        }).getContent();

        return new PagedResponse<>(userSummaries, users.getNumber(),
                users.getSize(), users.getTotalElements(), users.getTotalPages(), users.isLast());

    }

    public PagedResponse<CourseSummary> getFollowedCoursesByUserId(UserPrincipal userPrincipal, int page, int size) {
        validatePageNumberAndSize(page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
        Page<Course> courses = followCourseRepository.findFollowedCourseByUserId(userPrincipal.getId(), pageable);


        if (courses.getNumberOfElements() == 0) {
            return new PagedResponse<>(Collections.emptyList(), courses.getNumber(),
                    courses.getSize(), courses.getTotalElements(), courses.getTotalPages(), courses.isLast());
        }

        List<CourseSummary> courseSummaries = courses.map(course -> {
            User creator = userRepository.findById(course.getCreatedBy()).get();
            return ModelMapper.mapCourseToCourseSummary(course, creator);
        }).getContent();

        return new PagedResponse<>(courseSummaries, courses.getNumber(),
                courses.getSize(), courses.getTotalElements(), courses.getTotalPages(), courses.isLast());
    }

    public Paragraph createParagraph(ParagraphRequest paragraphRequest, Long articleId, UserPrincipal currentUser) {
        Paragraph paragraph = new Paragraph();
        paragraph.setContent(paragraphRequest.getContent());
        paragraph.setImageUrl(paragraphRequest.getImageUrl());

        Article article = articleRepository.findById(articleId).orElseThrow(() -> {
            return new ResourceNotFoundException("Article", "Id", articleId);
        });

        if(article.getCreatedBy() == currentUser.getId()){
            paragraph.setArticle(article);

            return paragraphRepository.save(paragraph);
        } else {
            return null;
        }
    }

    public Article createArticle(ArticleRequest articleRequest, Long sectionId, UserPrincipal currentUser) {
        Article article = new Article();
        article.setTitle(articleRequest.getTitle());

        Section section = sectionRepository.findById(sectionId).orElseThrow(() -> {
            return new ResourceNotFoundException("Section", "Id", sectionId);
        });

        if(section.getCreatedBy() == currentUser.getId()) {
            article.setSection(section);

            for (ParagraphRequest paragraphRequest : articleRequest.getParagraphs()){
                Paragraph paragraph = new Paragraph();
                paragraph.setContent(paragraphRequest.getContent());
                paragraph.setImageUrl(paragraphRequest.getImageUrl());
                paragraph.setArticle(article);
                article.addParagraph(paragraph);
            }

            return articleRepository.save(article);
        } else return null;
    }

    public Section createSection(SectionRequest sectionRequest, Long partId, UserPrincipal currentUser) {
        Section section = new Section();
        section.setTitle(sectionRequest.getTitle());

        Part part = partRepository.findById(partId).orElseThrow(() -> {
            return new ResourceNotFoundException("Part", "Id", partId);
        });

        if(part.getCreatedBy() == currentUser.getId()) {
            section.setPart(part);

            for (ArticleRequest articleRequest : sectionRequest.getArticles()){
                Article article = new Article();
                article.setTitle(articleRequest.getTitle());
                article.setSection(section);
                section.addArticle(article);

                for (ParagraphRequest paragraphRequest : articleRequest.getParagraphs()){
                    Paragraph paragraph = new Paragraph(paragraphRequest.getContent(), paragraphRequest.getImageUrl());
                    paragraph.setArticle(article);
                    article.addParagraph(paragraph);
                }
            }

            return sectionRepository.save(section);
        } else return null;
    }

    public Part createPart(PartRequest partRequest, Long courseId, UserPrincipal currentUser) {
        Part part = new Part();
        part.setTitle(partRequest.getTitle());

        Course course = courseRepository.findById(courseId).orElseThrow(() -> {
            return new ResourceNotFoundException("Course", "Id", courseId);
        });

        if(course.getCreatedBy() == currentUser.getId()) {
            part.setCourse(course);

            for (SectionRequest sectionRequest : partRequest.getSections()){
                Section section = new Section(sectionRequest.getTitle());
                section.setPart(part);
                part.addSection(section);

                for (ArticleRequest articleRequest : sectionRequest.getArticles()){
                    Article article = new Article(articleRequest.getTitle());
                    article.setSection(section);
                    section.addArticle(article);

                    for (ParagraphRequest paragraphRequest : articleRequest.getParagraphs()){
                        Paragraph paragraph = new Paragraph(paragraphRequest.getContent(), paragraphRequest.getImageUrl());
                        paragraph.setArticle(article);
                        article.addParagraph(paragraph);
                    }
                }
            }

            return partRepository.save(part);
        } else return null;
    }

    public Boolean modifyParagraphById(UserPrincipal currentUser, ParagraphRequest paragraphRequest, Long paragraphId) {
        Paragraph paragraph = paragraphRepository.findById(paragraphId).orElseThrow(() -> {
            return new ResourceNotFoundException("Paragraph", "Id", paragraphId);
        });

        if(paragraph.getCreatedBy() == currentUser.getId()){
            if(paragraphRequest.getImageUrl() != null)
                paragraph.setImageUrl(paragraphRequest.getImageUrl());
            if(paragraphRequest.getContent() != null)
                paragraph.setContent(paragraphRequest.getContent());

            paragraphRepository.save(paragraph);

            return true;
        } else {
            return false;
        }
    }

    public Boolean modifyArticleById(UserPrincipal currentUser, ArticleRequest articleRequest, Long articleId) {
        Article article = articleRepository.findById(articleId).orElseThrow(() -> {
            return new ResourceNotFoundException("Article", "Id", articleId);
        });

        if(article.getCreatedBy() == currentUser.getId() && articleRequest.getTitle() != null){
            article.setTitle(articleRequest.getTitle());

            articleRepository.save(article);

            return true;
        } else {
            return false;
        }
    }

    public Boolean modifySectionById(UserPrincipal currentUser, SectionRequest sectionRequest, Long sectionId) {
        Section section = sectionRepository.findById(sectionId).orElseThrow(() -> {
            return new ResourceNotFoundException("Section", "Id", sectionId);
        });

        if(section.getCreatedBy() == currentUser.getId() && sectionRequest.getTitle() != null){
            section.setTitle(sectionRequest.getTitle());

            sectionRepository.save(section);

            return true;
        } else {
            return false;
        }
    }

    public Boolean modifyPartById(UserPrincipal currentUser, PartRequest partRequest, Long partId) {
        Part part = partRepository.findById(partId).orElseThrow(() -> {
            return new ResourceNotFoundException("Part", "Id", partId);
        });

        if(part.getCreatedBy() == currentUser.getId() && partRequest.getTitle() != null){
            part.setTitle(partRequest.getTitle());

            partRepository.save(part);

            return true;
        } else {
            return false;
        }
    }

    public Boolean modifyCourseById(UserPrincipal currentUser, CourseRequest courseRequest, Long courseId) {
        Course course = courseRepository.findById(courseId).orElseThrow(() -> {
            return new ResourceNotFoundException("Course", "Id", courseId);
        });

        if(course.getCreatedBy() == currentUser.getId() && courseRequest.getTitle() != null){
            course.setTitle(courseRequest.getTitle());

            courseRepository.save(course);

            return true;
        } else {
            return false;
        }
    }

    public Boolean deleteCourseById(UserPrincipal currentUser, Long courseId) {
        Course course = courseRepository.findById(courseId).orElseThrow(() -> {
            return new ResourceNotFoundException("Course", "Id", courseId);
        });

        if(currentUser.getAuthorities().contains(new SimpleGrantedAuthority(RoleName.ROLE_ADMIN.toString())) || currentUser.getId() == course.getCreatedBy()){

            List<Part> parts = course.getParts();
            for (Part part : parts){
                List<Section> sections = part.getSections();
                for (Section section : sections){
                    List<Article> articles = section.getArticles();
                    for (Article article : articles){
                        List<Paragraph> paragraphs = article.getParagraphs();
                        for (Paragraph paragraph : paragraphs){
                            paragraphs.remove(paragraph);
                            paragraphRepository.delete(paragraph);

                            if(paragraphs.isEmpty())
                                break;
                        }

                        articles.remove(article);
                        articleRepository.delete(article);

                        if(articles.isEmpty())
                            break;
                    }

                    sections.remove(section);
                    sectionRepository.delete(section);

                    if(sections.isEmpty())
                        break;
                }

                parts.remove(part);
                partRepository.delete(part);

                if(parts.isEmpty())
                    break;
            }

            List<FollowCourse> followCourses = followCourseRepository.findAllByCourseId(courseId);

            for (FollowCourse followCourse : followCourses){
                followCourseRepository.delete(followCourse);
            }

            courseRepository.delete(course);

            return true;
        } else {
            return false;
        }
    }

    public Boolean deletePartById(UserPrincipal currentUser, Long partId) {
        Part part = partRepository.findById(partId).orElseThrow(() -> {
            return new ResourceNotFoundException("Part", "Id", partId);
        });

        if(currentUser.getAuthorities().contains(new SimpleGrantedAuthority(RoleName.ROLE_ADMIN.toString())) || currentUser.getId() == part.getCreatedBy()){

            List<Section> sections = part.getSections();
            for (Section section : sections){
                List<Article> articles = section.getArticles();
                for (Article article : articles){
                    List<Paragraph> paragraphs = article.getParagraphs();
                    for (Paragraph paragraph : paragraphs){
                        paragraphs.remove(paragraph);
                        paragraphRepository.delete(paragraph);

                        if(paragraphs.isEmpty())
                            break;
                    }

                    articles.remove(article);
                    articleRepository.delete(article);

                    if(articles.isEmpty())
                        break;
                }

                sections.remove(section);
                sectionRepository.delete(section);

                if(sections.isEmpty())
                    break;
            }

            partRepository.delete(part);

            return true;
        } else {
            return false;
        }
    }

    public Boolean deleteSectionById(UserPrincipal currentUser, Long sectionId) {
        Section section = sectionRepository.findById(sectionId).orElseThrow(() -> {
            return new ResourceNotFoundException("Section", "Id", sectionId);
        });

        if(currentUser.getAuthorities().contains(new SimpleGrantedAuthority(RoleName.ROLE_ADMIN.toString())) || currentUser.getId() == section.getCreatedBy()){

            List<Article> articles = section.getArticles();
            for (Article article : articles){
                List<Paragraph> paragraphs = article.getParagraphs();
                for (Paragraph paragraph : paragraphs){
                    paragraphs.remove(paragraph);
                    paragraphRepository.delete(paragraph);

                    if(paragraphs.isEmpty())
                        break;
                }

                articles.remove(article);
                articleRepository.delete(article);

                if(articles.isEmpty())
                    break;
            }

            sectionRepository.delete(section);

            return true;
        } else {
            return false;
        }
    }

    public Boolean deleteArticleById(UserPrincipal currentUser, Long articleId) {
        Article article = articleRepository.findById(articleId).orElseThrow(() -> {
            return new ResourceNotFoundException("Article", "Id", articleId);
        });

        if(currentUser.getAuthorities().contains(new SimpleGrantedAuthority(RoleName.ROLE_ADMIN.toString())) || currentUser.getId() == article.getCreatedBy()){

            List<Paragraph> paragraphs = article.getParagraphs();
            for (Paragraph paragraph : paragraphs){
                paragraphs.remove(paragraph);
                paragraphRepository.delete(paragraph);

                if(paragraphs.isEmpty())
                    break;
            }

            articleRepository.delete(article);

            return true;
        } else {
            return false;
        }
    }

    public Boolean deleteParagraphById(UserPrincipal currentUser, Long paragraphId) {
        Paragraph paragraph = paragraphRepository.findById(paragraphId).orElseThrow(() -> {
            return new ResourceNotFoundException("Paragraph", "Id", paragraphId);
        });

        if(currentUser.getAuthorities().contains(new SimpleGrantedAuthority(RoleName.ROLE_ADMIN.toString())) || currentUser.getId() == paragraph.getCreatedBy()){

            paragraphRepository.delete(paragraph);

            return true;
        } else {
            return false;
        }
    }

    private Map<Long, User> getCourseCreatorMap(List<Course> courses) {
        List<Long> creatorIds = courses.stream()
                .map(Course::getCreatedBy)
                .distinct()
                .collect(Collectors.toList());

        List<User> creators = userRepository.findByIdIn(creatorIds);
        Map<Long, User> creatorMap = creators.stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        return creatorMap;
    }

    private void validatePageNumberAndSize(int page, int size) {
        if (page < 0) {
            throw new BadRequestException("Page number cannot be less than zero.");
        }

        if (size > AppConstants.MAX_PAGE_SIZE) {
            throw new BadRequestException("Page size must not be greater than " + AppConstants.MAX_PAGE_SIZE);
        }
    }

    private Map<KeyIdClass, Long> getCountsMap(Course course){
        Map<KeyIdClass, Long> count = new HashMap<>();
        List<Part> parts = course.getParts();
        KeyIdClass key = new KeyIdClass(course.getId(), course.getClass());
        count.put(key, Long.valueOf(parts.size()));
        for(Part part : parts){
            List<Section> sections = part.getSections();
            key = new KeyIdClass(part.getId(), part.getClass());
            count.put(key, Long.valueOf(sections.size()));
            for(Section section : sections){
                List<Article> articles = section.getArticles();
                key = new KeyIdClass(section.getId(), section.getClass());
                count.put(key, Long.valueOf(articles.size()));
                for(Article article : articles){
                    List<Paragraph> paragraphs = article.getParagraphs();
                    key = new KeyIdClass(article.getId(), article.getClass());
                    count.put(key, Long.valueOf(paragraphs.size()));
                }
            }
        }


        return count;
    }

    public boolean completeCourse(Long courseId, UserPrincipal currentUser) {

        Course course = courseRepository.findById(courseId).orElseThrow(() -> {
            return new ResourceNotFoundException("Course", "Id", courseId);
        });

        if (course.getCreatedBy() != currentUser.getId()){
            return false;
        } else {

            if (!course.getCompleted()) {
                course.setCompleted(true);
                courseRepository.save(course);
            }

            return true;

        }

    }
}
