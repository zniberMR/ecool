package company.victoria.ecool.controller;

import company.victoria.ecool.exception.BadRequestException;
import company.victoria.ecool.exception.ResourceNotFoundException;
import company.victoria.ecool.model.user.FollowUser;
import company.victoria.ecool.model.user.Role;
import company.victoria.ecool.model.user.RoleName;
import company.victoria.ecool.model.user.User;
import company.victoria.ecool.payload.ApiResponse;
import company.victoria.ecool.payload.PagedResponse;
import company.victoria.ecool.payload.SearchResponse;
import company.victoria.ecool.payload.course.CourseSummary;
import company.victoria.ecool.payload.user.*;
import company.victoria.ecool.repository.course.CourseRepository;
import company.victoria.ecool.repository.course.FollowCourseRepository;
import company.victoria.ecool.repository.user.FollowUserRepository;
import company.victoria.ecool.repository.user.RoleRepository;
import company.victoria.ecool.repository.user.UserRepository;
import company.victoria.ecool.security.CurrentUser;
import company.victoria.ecool.security.UserPrincipal;
import company.victoria.ecool.service.CourseService;
import company.victoria.ecool.util.AppConstants;
import company.victoria.ecool.util.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.function.EntityResponse;

import javax.jws.soap.SOAPBinding;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/ecool/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CourseService courseService;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private FollowCourseRepository followCourseRepository;

    @Autowired
    private FollowUserRepository followUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/me")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> getCurrentUser(@CurrentUser UserPrincipal userPrincipal){
        User user = userRepository.findById(userPrincipal.getId()).orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));

        Role formerRole = roleRepository.findByName(RoleName.ROLE_FORMER).get();

        UserProfile userResponse = null;

        Long followersCount = followUserRepository.countFollowersByFollowingId(userPrincipal.getId());
        Long followingCount = followUserRepository.countFollowingByFollowerId(userPrincipal.getId());

        if(!user.getRoles().contains(formerRole)){
            userResponse = ModelMapper.mapUserToUserProfile(user, true, followersCount, followingCount);
        } else {
            List<CourseSummary> courses = courseService.getCoursesByCreator(user.getId(), true);

            Long courseFollowersCount = 0L;
            for (CourseSummary course : courses){
                courseFollowersCount += followCourseRepository.countByCourseId(course.getId());
            }

            userResponse = ModelMapper.mapUserToFormerProfile(user, courses, courseFollowersCount, true, followersCount, followingCount);
        }

        return ResponseEntity.ok(userResponse);

    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> getProfile(@CurrentUser UserPrincipal currentUser, @PathVariable Long userId){
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Role formerRole = roleRepository.findByName(RoleName.ROLE_FORMER).get();

        UserProfile userResponse = null;

        Long followersCount = followUserRepository.countFollowersByFollowingId(userId);
        Long followingCount = followUserRepository.countFollowingByFollowerId(userId);
        Boolean followed    = followUserRepository.existsByFollowerIdAndFollowingId(currentUser.getId(), userId);

        if(!user.getRoles().contains(formerRole)){
            userResponse = ModelMapper.mapUserToUserProfile(user, followed, followersCount, followingCount);
        } else {
            List<CourseSummary> courses = courseService.getCoursesByCreator(user.getId(), false);

            Long courseFollowersCount = 0L;
            for (CourseSummary course : courses){
                courseFollowersCount += followCourseRepository.countByCourseId(course.getId());
            }

            userResponse = ModelMapper.mapUserToFormerProfile(user, courses, courseFollowersCount, followed, followersCount, followingCount);
        }

        return ResponseEntity.ok(userResponse);

    }

    @GetMapping("/checkUsernameAvailability")
    public ResponseEntity<?> checkUsernameAvailability(@RequestParam(value = "username") String username) {
        Boolean isAvailable = !userRepository.existsByUsername(username);
        return ResponseEntity.ok(new UserIdentityAvailability(isAvailable));
    }

    @GetMapping("/checkEmailAvailability")
    public ResponseEntity<?> checkEmailAvailability(@RequestParam(value = "email") String email) {
        Boolean isAvailable = !userRepository.existsByEmail(email);
        return ResponseEntity.ok(new UserIdentityAvailability(isAvailable));
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ROLE_USER')")
    public PagedResponse<?> getUsers(@RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) Integer page,
                                                @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) Integer size) {
        validatePageNumberAndSize(page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
        Page<User> users = userRepository.findAll(pageable);

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

    @GetMapping("/users/{keyword}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public PagedResponse<?> getUsersFiltered(@PathVariable String keyword,
                                     @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) Integer page,
                                     @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) Integer size) {
        validatePageNumberAndSize(page, size);

        keyword = keyword.replace(' ', '%');

        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
        Page<User> users = userRepository.search(keyword, pageable);

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

    @GetMapping("/formers")
    @PreAuthorize("hasRole('ROLE_USER')")
    public PagedResponse<?> getFormers(@RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) Integer page,
                                       @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) Integer size) {
        validatePageNumberAndSize(page, size);

        Role formerRole = roleRepository.findByName(RoleName.ROLE_FORMER).get();

        List<User> users1 = userRepository.findAll();

        List<Long> userIds = new ArrayList<>();

        users1.forEach(user -> {
            if(user.getRoles().contains(formerRole)){
                userIds.add(user.getId());
            }
        });

        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
        Page<User> users = userRepository.findAllByIdIn(userIds, pageable);

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

    @GetMapping("/formers/{keyword}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public PagedResponse<?> getFormersFiltered(@PathVariable String keyword,
                                       @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) Integer page,
                                       @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) Integer size) {
        validatePageNumberAndSize(page, size);

        keyword = keyword.replace(' ', '%');

        Role formerRole = roleRepository.findByName(RoleName.ROLE_FORMER).get();

        List<User> users1 = userRepository.search(keyword);

        List<Long> userIds = new ArrayList<>();

        users1.forEach(user -> {
            if(user.getRoles().contains(formerRole)){
                userIds.add(user.getId());
            }
        });

        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
        Page<User> users = userRepository.findAllByIdIn(userIds, pageable);

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

    @GetMapping("/verifiedFormers")
    public PagedResponse<?> getVerifiedFormers(@RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) Integer page,
                                                            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) Integer size){
        validatePageNumberAndSize(page, size);

        Role formerRole = roleRepository.findByName(RoleName.ROLE_FORMER).get();

        List<User> users1 = userRepository.findAll();

        List<Long> userIds = new ArrayList<>();

        users1.forEach(user -> {
            if(user.getRoles().contains(formerRole) && user.getVerified()){
                userIds.add(user.getId());
            }
        });

        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
        Page<User> users = userRepository.findAllByIdIn(userIds, pageable);

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

    @GetMapping("/verifiedFormers/{keyword}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public PagedResponse<?> getVerifiedFormersFiltered(@PathVariable String keyword,
                                                       @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) Integer page,
                                                       @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) Integer size){
        validatePageNumberAndSize(page, size);

        keyword = keyword.replace(' ', '%');

        Role formerRole = roleRepository.findByName(RoleName.ROLE_FORMER).get();

        List<User> users1 = userRepository.search(keyword);

        List<Long> userIds = new ArrayList<>();

        users1.forEach(user -> {
            if(user.getRoles().contains(formerRole) && user.getVerified()){
                userIds.add(user.getId());
            }
        });

        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
        Page<User> users = userRepository.findAllByIdIn(userIds, pageable);

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

    @PutMapping("/verified/{userId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> verifiedUser(@PathVariable Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        if(user.getVerified()){
            return new ResponseEntity(new ApiResponse(false, "User is already Verified"), HttpStatus.BAD_REQUEST);
        } else {
            user.setVerified(true);
            userRepository.save(user);
            return ResponseEntity.ok(new ApiResponse(true, "User Verified Successfully"));
        }
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> deleteUser(@CurrentUser UserPrincipal currentUser, @PathVariable Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN).get();

        if(!user.getRoles().contains(adminRole)) {

            if(followCourseRepository.existsByUserId(userId)){
                followCourseRepository.deleteByUserId(userId);
            }

            if(courseRepository.existsByCreatedBy(userId)){
                courseRepository.findAllCourseByCreatedBy(userId).forEach(course -> {
                    courseService.deleteCourseById(currentUser, course.getId());
                });
            }

            userRepository.deleteById(userId);
            return ResponseEntity.ok(new ApiResponse(true, "Delete User Successfully"));
        } else {
            return new ResponseEntity(new ApiResponse(false, "You can't delete user with role ADMIN"), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> modifyUser(@CurrentUser UserPrincipal currentUser, @RequestBody UserRequest userRequest) {
        User user = userRepository.findById(currentUser.getId()).orElseThrow(() -> {
            return new ResourceNotFoundException("User", "Id", currentUser.getId());
        });

        if (!userRequest.getName().isEmpty()){
            user.setName(userRequest.getName());
        }

        if (!userRequest.getPassword().isEmpty()){
            user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        }

        if (!userRequest.getAbout().isEmpty()){
            user.setAbout(userRequest.getAbout());
        }

        if (!userRequest.getCountry().isEmpty()){
            user.setCountry(userRequest.getCountry());
        }

        if (!userRequest.getCity().isEmpty()){
            user.setCity(userRequest.getCity());
        }

        if (!userRequest.getSkills().isEmpty()){
            user.setSkills(userRequest.getSkills());
        }

        if (!userRequest.getImageUrl().isEmpty()){
            user.setImageUrl(userRequest.getImageUrl());
        }

        if (!userRequest.getBannerUrl().isEmpty()){
            user.setBannerUrl(userRequest.getBannerUrl());
        }

        userRepository.save(user);

        return ResponseEntity.ok(new ApiResponse(true, "User Changed Successfully"));
    }

    @GetMapping("/{username}/courses")
    @PreAuthorize("hasRole('ROLE_USER')")
    public PagedResponse<?> getCoursesCreatedBy(@PathVariable String username,
                                                            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) Integer page,
                                                            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) Integer size) {
        return courseService.getCoursesCreatedBy(username, false, page, size);
    }

    @GetMapping("/courses")
    @PreAuthorize("hasRole('ROLE_FORMER')")
    public PagedResponse<?> getCourses(@CurrentUser UserPrincipal userPrincipal,
                                                            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) Integer page,
                                                            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) Integer size) {
        return courseService.getCoursesCreatedBy(userPrincipal.getUsername(), true, page, size);
    }

    /*
    @GetMapping("/search/{keyword}")
    public PagedResponse<?> getFiltered(@PathVariable String keyword,
                                        @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) Integer page,
                                        @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) Integer size){

        validatePageNumberAndSize(page, size);

        keyword = keyword.replace(' ', '%');

        PagedResponse<?> courses = courseService.getAllCoursesFiltered(keyword, page, size);

        Role formerRole = roleRepository.findByName(RoleName.ROLE_FORMER).get();

        List<User> users = userRepository.search(keyword);

        List<Long> userIds = new ArrayList<>();

        users.forEach(user -> {
            if(user.getRoles().contains(formerRole)){
                userIds.add(user.getId());
            }
        });

        Long totalElements = courses.getTotalElements() + userIds.size();
        Integer totalPages = courses.getTotalPages() + userIds.size() / size;

        if (courses.getTotalElements() == 0 && userIds.isEmpty()){

            return new PagedResponse<>(Collections.emptyList(), page, size, 0l, 0, true);

        } else if (courses.getTotalElements() == 0){

            Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
            Page<User> users1 = userRepository.findAllByIdIn(userIds, pageable);

            if (users1.getNumberOfElements() == 0) {
                return new PagedResponse<>(Collections.emptyList(), page,
                        size, users1.getTotalElements(), users1.getTotalPages(), users1.isLast());
            }

            List<FormerProfile> formerProfiles = users1.map(user -> {
                List<CourseSummary> courses1 = courseService.getCoursesByCreator(user.getId());

                Long courseFollowersCount = 0L;
                for (CourseSummary course : courses1){
                    courseFollowersCount += followCourseRepository.countByCourseId(course.getId());
                }

                return ModelMapper.mapUserToFormerResponse(user, courses1, courseFollowersCount);
            }).getContent();

            List<SearchResponse> searchResponses = formerProfiles.stream().map(former -> {
                return new SearchResponse("former", former);
            }).collect(Collectors.toList());

            return new PagedResponse<>(searchResponses, page,
                    size, users1.getTotalElements(), users1.getTotalPages(), users1.isLast());
        } else if (userIds.isEmpty()){

            List<SearchResponse> searchResponses = courses.getContent().stream().map(course -> {
                return new SearchResponse("course", course);
            }).collect(Collectors.toList());

            return new PagedResponse<>(searchResponses, page,
                    size, courses.getTotalElements(), courses.getTotalPages(), courses.getLast());

        } else {

            List<SearchResponse> searchResponses = courses.getContent().stream().map(course -> {
                return new SearchResponse("course", course);
            }).collect(Collectors.toList());

            if (courses.getContent().size() < size){

                Pageable pageable = PageRequest.of(0, size - courses.getContent().size(), Sort.Direction.DESC, "createdAt");
                Page<User> users1 = userRepository.findAllByIdIn(userIds, pageable);

                List<FormerProfile> formerProfiles = users1.map(user -> {
                    List<CourseSummary> courses1 = courseService.getCoursesByCreator(user.getId());

                    Long courseFollowersCount = 0L;
                    for (CourseSummary course : courses1){
                        courseFollowersCount += followCourseRepository.countByCourseId(course.getId());
                    }

                    return ModelMapper.mapUserToFormerResponse(user, courses1, courseFollowersCount);
                }).getContent();

                formerProfiles.forEach(former -> {
                    searchResponses.add(new SearchResponse("former", former));
                });

                return new PagedResponse<>(searchResponses, page, size,
                        totalElements, totalPages, users1.isLast());

            }

            if (courses.getContent().isEmpty()) {

                Pageable pageable = PageRequest.of(page - courses.getTotalPages() + 1, size, Sort.Direction.DESC, "createdAt");
                Page<User> users1 = userRepository.findAllByIdIn(userIds, pageable);

                List<FormerProfile> formerProfiles = users1.map(user -> {
                    List<CourseSummary> courses1 = courseService.getCoursesByCreator(user.getId());

                    Long courseFollowersCount = 0L;
                    for (CourseSummary course : courses1){
                        courseFollowersCount += followCourseRepository.countByCourseId(course.getId());
                    }

                    return ModelMapper.mapUserToFormerResponse(user, courses1, courseFollowersCount);
                }).getContent();

                List<SearchResponse> searchResponses1 = formerProfiles.stream().map(former -> {
                    return new SearchResponse("former", former);
                }).collect(Collectors.toList());

                return new PagedResponse<>(searchResponses1, page, size,
                        totalElements, totalPages, users1.isLast());

            }

            return new PagedResponse<>(searchResponses, page,
                    size, totalElements, totalPages, page == totalPages - 1);

        }


    }
    */

    @PutMapping("/followUser/{userId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> followUserById(@PathVariable Long userId, @CurrentUser UserPrincipal userPrincipal,
                                            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) Integer page,
                                            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) Integer size){
        Boolean bool = followUserRepository.existsByFollowerIdAndFollowingId(userPrincipal.getId(), userId);

        if (userId == userPrincipal.getId()){

            return ResponseEntity.badRequest().body(new ApiResponse(false, "You can not follow yourself"));

        } else if (bool){

            return ResponseEntity.badRequest().body(new ApiResponse(false, "User Already followed"));

        } else {

            User follower  = userRepository.findById(userPrincipal.getId()).orElseThrow(() -> {
                return new ResourceNotFoundException("User", "Id", userId);
            });

            User following = userRepository.findById(userId).orElseThrow(() -> {
                return new ResourceNotFoundException("User", "Id", userId);
            });

            FollowUser followUser = new FollowUser(follower, following);

            followUserRepository.save(followUser);

            return ResponseEntity.ok(new ApiResponse(true, "User followed Successfully"));
        }
    }

    @GetMapping("/{userId}/followers")
    @PreAuthorize("hasRole('ROLE_USER')")
    public PagedResponse<?> getFollowersByUserId(@PathVariable Long userId,
                                                 @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) Integer page,
                                                 @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) Integer size){
        validatePageNumberAndSize(page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
        Page<FollowUser> users  = followUserRepository.findAllByFollowingId(userId, pageable);

        List<UserSummary> userSummaries = users.map(user -> {
            return ModelMapper.mapUserToUserSummary(user.getFollower());
        }).getContent();

        return new PagedResponse<>(userSummaries, users.getNumber(),
                users.getSize(), users.getTotalElements(), users.getTotalPages(), users.isLast());
    }

    @GetMapping("/{userId}/following")
    @PreAuthorize("hasRole('ROLE_USER')")
    public PagedResponse<?> getFollowingByUserId(@PathVariable Long userId,
                                                 @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) Integer page,
                                                 @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) Integer size){
        validatePageNumberAndSize(page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
        Page<FollowUser> users  = followUserRepository.findAllByFollowerId(userId, pageable);

        List<UserSummary> userSummaries = users.map(user -> {
            return ModelMapper.mapUserToUserSummary(user.getFollowing());
        }).getContent();

        return new PagedResponse<>(userSummaries, users.getNumber(),
                users.getSize(), users.getTotalElements(), users.getTotalPages(), users.isLast());
    }

    private void validatePageNumberAndSize(Integer page, Integer size) {
        if (page < 0) {
            throw new BadRequestException("Page number cannot be less than zero.");
        }

        if (size > AppConstants.MAX_PAGE_SIZE) {
            throw new BadRequestException("Page size must not be greater than " + AppConstants.MAX_PAGE_SIZE);
        }
    }

}
