package company.victoria.ecool.controller;

import company.victoria.ecool.model.course.*;
import company.victoria.ecool.payload.ApiResponse;
import company.victoria.ecool.payload.PagedResponse;
import company.victoria.ecool.payload.course.*;
import company.victoria.ecool.security.CurrentUser;
import company.victoria.ecool.security.UserPrincipal;
import company.victoria.ecool.service.CourseService;
import company.victoria.ecool.util.AppConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;

@RestController
@RequestMapping("/ecool/course")
public class CourseController {
    @Autowired
    private CourseService courseService;

    @GetMapping("/{courseId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> getCourseById(@CurrentUser UserPrincipal currentUser, @PathVariable Long courseId) {
        return ResponseEntity.ok(courseService.getCourseById(courseId, currentUser));
    }

    @GetMapping("/courses")
    public PagedResponse<?> getAllCourses(@RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
                                                      @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        return courseService.getAllCourses(page, size);
    }
    @GetMapping("/courses/{keyword}")
    public PagedResponse<?> getAllCoursesFiltered(@PathVariable(value = "keyword") String keyword,
                                          @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
                                          @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        return courseService.getAllCoursesFiltered(keyword, page, size);
    }

    @GetMapping("/{courseId}/followers")
    @PreAuthorize("hasRole('ROLE_FORMER')")
    public PagedResponse<?> getFollowers(@PathVariable Long courseId,
                                                    @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
                                                    @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size){
        return courseService.getFollowersByCourseId(courseId, page, size);
    }

    @GetMapping("/followedCourses")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public PagedResponse<?> getFollowedCourses(@CurrentUser UserPrincipal userPrincipal,
                                                     @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
                                                     @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size){
        return courseService.getFollowedCoursesByUserId(userPrincipal, page, size);
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_FORMER')")
    public ResponseEntity<?> uploadCourse(@Valid @RequestBody CourseRequest courseRequest){
        Course course = courseService.createCourse(courseRequest);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{courseId}")
                .buildAndExpand(course.getId()).toUri();

        return ResponseEntity.created(location)
                .body(new ApiResponse(true, "Course Uploaded Successfully"));
    }

    @PutMapping("/completedCourse/{courseId}")
    @PreAuthorize("hasRole('ROLE_FORMER')")
    public ResponseEntity<?> completedCourse(@CurrentUser UserPrincipal currentUser, @PathVariable Long courseId){
        boolean bool = courseService.completeCourse(courseId, currentUser);

        if(bool){
            return ResponseEntity.ok(new ApiResponse(true, "Course Completed Successfully"));
        } else {
            return new ResponseEntity(new ApiResponse(false, "You can not complete this course"), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/follow/{courseId}")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public ResponseEntity<?> followCourse(@PathVariable Long courseId, @CurrentUser UserPrincipal currentUser){
        boolean bool = courseService.followCourse(courseId, currentUser);

        if(bool){
            return ResponseEntity.ok(new ApiResponse(true, "Course Followed Successfully"));
        } else {
            return new ResponseEntity(new ApiResponse(false, "Course already has been Followed"), HttpStatus.BAD_REQUEST);
        }

    }

    @PostMapping("/paragraph/{articleId}")
    @PreAuthorize("hasRole('ROLE_FORMER')")
    public ResponseEntity<?> uploadParagraph(@Valid @RequestBody ParagraphRequest paragraphRequest, @PathVariable Long articleId, @CurrentUser UserPrincipal currentUser){
        Paragraph paragraph = courseService.createParagraph(paragraphRequest, articleId, currentUser);

        if(paragraph == null){
            return ResponseEntity.badRequest().body(new ApiResponse(false, "You can't upload this paragraph in this article"));
        }

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{paragraphId}")
                .buildAndExpand(paragraph.getId()).toUri();

        return ResponseEntity.created(location)
                .body(new ApiResponse(true, "Paragraph Uploaded Successfully"));
    }

    @PostMapping("/article/{sectionId}")
    @PreAuthorize("hasRole('ROLE_FORMER')")
    public ResponseEntity<?> uploadArticle(@Valid @RequestBody ArticleRequest articleRequest, @PathVariable Long sectionId, @CurrentUser UserPrincipal currentUser){
        Article article = courseService.createArticle(articleRequest, sectionId, currentUser);

        if(article == null){
            return ResponseEntity.badRequest().body(new ApiResponse(false, "You can't upload this article in this section"));
        }

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{articleId}")
                .buildAndExpand(article.getId()).toUri();

        return ResponseEntity.created(location)
                .body(new ApiResponse(true, "Article Uploaded Successfully"));
    }

    @PostMapping("/section/{partId}")
    @PreAuthorize("hasRole('ROLE_FORMER')")
    public ResponseEntity<?> uploadSection(@Valid @RequestBody SectionRequest sectionRequest, @PathVariable Long partId, @CurrentUser UserPrincipal currentUser){
        Section section = courseService.createSection(sectionRequest, partId, currentUser);

        if(section == null){
            return ResponseEntity.badRequest().body(new ApiResponse(false, "You can't upload this section in this part"));
        }

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{sectionId}")
                .buildAndExpand(section.getId()).toUri();

        return ResponseEntity.created(location)
                .body(new ApiResponse(true, "Section Uploaded Successfully"));
    }

    @PostMapping("/part/{courseId}")
    @PreAuthorize("hasRole('ROLE_FORMER')")
    public ResponseEntity<?> uploadPart(@Valid @RequestBody PartRequest partRequest, @PathVariable Long courseId, @CurrentUser UserPrincipal currentUser){
        Part part = courseService.createPart(partRequest, courseId, currentUser);

        if(part == null){
            return ResponseEntity.badRequest().body(new ApiResponse(false, "You can't upload this part in this course"));
        }

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{partId}")
                .buildAndExpand(part.getId()).toUri();

        return ResponseEntity.created(location)
                .body(new ApiResponse(true, "Part Uploaded Successfully"));
    }

    @PutMapping("/paragraph/{paragraphId}")
    @PreAuthorize("hasRole('ROLE_FORMER')")
    public ResponseEntity<?> modifyParagraph(@CurrentUser UserPrincipal currentUser, @RequestBody ParagraphRequest paragraphRequest, @PathVariable Long paragraphId){
        Boolean bool = courseService.modifyParagraphById(currentUser, paragraphRequest, paragraphId);

        if(bool){

            return ResponseEntity.ok(new ApiResponse(true, "Paragraph Modify Successfully"));
        } else {
            return new ResponseEntity(new ApiResponse(false, "You can't modify this paragraph"), HttpStatus.BAD_REQUEST);
        }

    }

    @PutMapping("/article/{articleId}")
    @PreAuthorize("hasRole('ROLE_FORMER')")
    public ResponseEntity<?> modifyArticle(@CurrentUser UserPrincipal currentUser, @RequestBody ArticleRequest articleRequest, @PathVariable Long articleId){
        Boolean bool = courseService.modifyArticleById(currentUser, articleRequest, articleId);

        if(bool){
            return ResponseEntity.ok(new ApiResponse(true, "Article Modify Successfully"));
        } else {
            return new ResponseEntity(new ApiResponse(false, "You can't modify this paragraph"), HttpStatus.BAD_REQUEST);
        }

    }

    @PutMapping("/section/{sectionId}")
    @PreAuthorize("hasRole('ROLE_FORMER')")
    public ResponseEntity<?> modifySection(@CurrentUser UserPrincipal currentUser, @RequestBody SectionRequest sectionRequest, @PathVariable Long sectionId){
        Boolean bool = courseService.modifySectionById(currentUser, sectionRequest, sectionId);

        if(bool){
            return ResponseEntity.ok(new ApiResponse(true, "Section Modify Successfully"));
        } else {
            return new ResponseEntity(new ApiResponse(false, "You can't modify this paragraph"), HttpStatus.BAD_REQUEST);
        }

    }

    @PutMapping("/part/{partId}")
    @PreAuthorize("hasRole('ROLE_FORMER')")
    public ResponseEntity<?> modifyPart(@CurrentUser UserPrincipal currentUser, @RequestBody PartRequest partRequest, @PathVariable Long partId){
        Boolean bool = courseService.modifyPartById(currentUser, partRequest, partId);

        if(bool){
            return ResponseEntity.ok(new ApiResponse(true, "Part Modify Successfully"));
        } else {
            return new ResponseEntity(new ApiResponse(false, "You can't modify this paragraph"), HttpStatus.BAD_REQUEST);
        }

    }

    @PutMapping("/{courseId}")
    @PreAuthorize("hasRole('ROLE_FORMER')")
    public ResponseEntity<?> modifyCourse(@CurrentUser UserPrincipal currentUser, @RequestBody CourseRequest courseRequest, @PathVariable Long courseId){
        Boolean bool = courseService.modifyCourseById(currentUser, courseRequest, courseId);

        if(bool){
            return ResponseEntity.ok(new ApiResponse(true, "Course Modify Successfully"));
        } else {
            return new ResponseEntity(new ApiResponse(false, "You can't modify this paragraph"), HttpStatus.BAD_REQUEST);
        }

    }

    @DeleteMapping("/{courseId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_FORMER')")
    public ResponseEntity<?> deleteCourse(@CurrentUser UserPrincipal currentUser, @PathVariable Long courseId){
        Boolean result = courseService.deleteCourseById(currentUser, courseId);
        if(result){
            return ResponseEntity.ok(new ApiResponse(true, "Course Delete Successfully"));
        } else {
            return new ResponseEntity(new ApiResponse(false, "You can not delete the course"), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/part/{partId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_FORMER')")
    public ResponseEntity<?> deletePart(@CurrentUser UserPrincipal currentUser, @PathVariable Long partId){
        Boolean result = courseService.deletePartById(currentUser, partId);
        if(result){
            return ResponseEntity.ok(new ApiResponse(true, "Part Delete Successfully"));
        } else {
            return new ResponseEntity(new ApiResponse(false, "You can not delete the part"), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/section/{sectionId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_FORMER')")
    public ResponseEntity<?> deleteSection(@CurrentUser UserPrincipal currentUser, @PathVariable Long sectionId){
        Boolean result = courseService.deleteSectionById(currentUser, sectionId);
        if(result){
            return ResponseEntity.ok(new ApiResponse(true, "Section Delete Successfully"));
        } else {
            return new ResponseEntity(new ApiResponse(false, "You can not delete the section"), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/article/{articleId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_FORMER')")
    public ResponseEntity<?> deleteArticle(@CurrentUser UserPrincipal currentUser, @PathVariable Long articleId){
        Boolean result = courseService.deleteArticleById(currentUser, articleId);
        if(result){
            return ResponseEntity.ok(new ApiResponse(true, "Article Delete Successfully"));
        } else {
            return new ResponseEntity(new ApiResponse(false, "You can not delete the article"), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/paragraph/{paragraphId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_FORMER')")
    public ResponseEntity<?> deleteParagraph(@CurrentUser UserPrincipal currentUser, @PathVariable Long paragraphId){
        Boolean result = courseService.deleteParagraphById(currentUser, paragraphId);
        if(result){
            return ResponseEntity.ok(new ApiResponse(true, "Paragraph Delete Successfully"));
        } else {
            return new ResponseEntity(new ApiResponse(false, "You can not delete the paragraph"), HttpStatus.BAD_REQUEST);
        }
    }


}
