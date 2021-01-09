package company.victoria.ecool.util;

import company.victoria.ecool.model.course.Course;
import company.victoria.ecool.model.user.User;
import company.victoria.ecool.payload.KeyIdClass;
import company.victoria.ecool.payload.course.*;
import company.victoria.ecool.payload.user.FormerProfile;
import company.victoria.ecool.payload.user.UserProfile;
import company.victoria.ecool.payload.user.UserSummary;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ModelMapper {

    public static CourseSummary mapCourseToCourseSummary(Course course, User creator) {
        CourseSummary courseSummary = new CourseSummary();
        courseSummary.setId(course.getId());
        courseSummary.setTitle(course.getTitle());
        courseSummary.setDescription(course.getDescription());
        courseSummary.setGoals(course.getGoals());
        courseSummary.setPrerequisites(course.getPrerequisites());
        courseSummary.setCreationDateTime(course.getCreatedAt());
        courseSummary.setImageUrl(course.getImageUrl());

        List<String> roles = creator.getRoles().stream().map(role -> {
            return new String(role.getName().name());
        }).collect(Collectors.toList());

        UserSummary creatorSummary = mapUserToUserSummary(creator);
        courseSummary.setCreatedBy(creatorSummary);

        return courseSummary;
    }

    public static CourseResponse mapCourseToCourseResponse(Course course, User creator, Map<KeyIdClass, Long> count) {
        CourseResponse courseResponse = new CourseResponse(mapCourseToCourseSummary(course, creator));
        courseResponse.setFollowed(true);

        KeyIdClass key = new KeyIdClass();

        List<PartResponse> partResponses = course.getParts().stream().map(part -> {
            PartResponse partResponse = new PartResponse();
            partResponse.setId(part.getId());
            partResponse.setTitle(part.getTitle());

            List<SectionResponse> sectionResponses = part.getSections().stream().map(section -> {
                SectionResponse sectionResponse = new SectionResponse();
                sectionResponse.setId(section.getId());
                sectionResponse.setTitle(section.getTitle());

                List<ArticleResponse> articleResponses = section.getArticles().stream().map(article -> {
                    ArticleResponse articleResponse = new ArticleResponse();
                    articleResponse.setId(article.getId());
                    articleResponse.setTitle(article.getTitle());

                    List<ParagraphResponse> paragraphResponses = article.getParagraphs().stream().map(paragraph -> {
                        ParagraphResponse paragraphResponse = new ParagraphResponse();
                        paragraphResponse.setId(paragraph.getId());
                        paragraphResponse.setContent(paragraph.getContent());
                        paragraphResponse.setImageUrl(paragraph.getImageUrl());
                        return paragraphResponse;
                    }).collect(Collectors.toList());

                    articleResponse.setParagraphs(paragraphResponses);

                    key.setId(articleResponse.getId());
                    key.setClassType(article.getClass());
                    if (count.containsKey(key)) {
                        articleResponse.setParagraphsCount(count.get(key));
                    } else {
                        articleResponse.setParagraphsCount(0L);
                    }

                    return articleResponse;
                }).collect(Collectors.toList());

                sectionResponse.setArticles(articleResponses);

                key.setId(sectionResponse.getId());
                key.setClassType(section.getClass());
                if (count.containsKey(key)) {
                    sectionResponse.setArticlesCount(count.get(key));
                } else {
                    sectionResponse.setArticlesCount(0L);
                }

                return sectionResponse;
            }).collect(Collectors.toList());

            partResponse.setSections(sectionResponses);

            key.setId(partResponse.getId());
            key.setClassType(part.getClass());
            if (count.containsKey(key)) {
                partResponse.setSectionsCount(count.get(key));
            } else {
                partResponse.setSectionsCount(0L);
            }

            return partResponse;
        }).collect(Collectors.toList());

        courseResponse.setParts(partResponses);

        key.setId(courseResponse.getId());
        key.setClassType(course.getClass());
        if (count.containsKey(key)) {
            courseResponse.setPartsCount(count.get(key));
        } else {
            courseResponse.setPartsCount(0L);
        }

        return courseResponse;
    }

    public static UserSummary mapUserToUserSummary(User user){
        UserSummary userSummary = new UserSummary();
        userSummary.setId(user.getId());
        userSummary.setName(user.getName());
        userSummary.setUsername(user.getUsername());
        userSummary.setEmail(user.getEmail());
        userSummary.setImageUrl(user.getImageUrl());
        userSummary.setCompteVerified(user.getVerified());
        userSummary.setJoinedAt(user.getCreatedAt());

        return userSummary;
    }

    public static UserProfile mapUserToUserProfile(User user, Boolean followed, Long followersCount, Long followingCount) {
        UserProfile userProfile = new UserProfile(mapUserToUserSummary(user));
        userProfile.setAbout(user.getAbout());
        userProfile.setCountry(user.getCountry());
        userProfile.setCity(user.getCity());
        userProfile.setSkills(user.getSkills());
        userProfile.setBannerUrl(user.getBannerUrl());

        userProfile.setFollowed(followed);
        userProfile.setFollowersCount(followersCount);
        userProfile.setFollowingCount(followingCount);

        return userProfile;
    }

    public static FormerProfile mapUserToFormerProfile(User user, List<CourseSummary> courses, Long courseFollowersCount, Boolean followed, Long followersCount, Long followingCount) {
        FormerProfile formerProfile = new FormerProfile(mapUserToUserProfile(user, followed, followersCount, followingCount));

        formerProfile.setCours(courses);

        formerProfile.setCourseCount(Long.valueOf(courses.size()));
        formerProfile.setCourseFollowersCount(courseFollowersCount);

        return formerProfile;
    }
}
