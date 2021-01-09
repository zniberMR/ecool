package company.victoria.ecool.repository.user;

import company.victoria.ecool.model.user.FollowUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FollowUserRepository extends JpaRepository<FollowUser, Long> {
    Boolean existsByFollowerIdAndFollowingId(Long followerId, Long followingId);

    Page<FollowUser> findAllByFollowerId(Long followerId, Pageable pageable);

    Page<FollowUser> findAllByFollowingId(Long followingId, Pageable pageable);

    @Query("SELECT COUNT(f.id) from FollowUser f where f.following.id = :followingId")
    Long countFollowersByFollowingId(@Param("followingId") Long followingId);

    @Query("SELECT COUNT(f.id) from FollowUser f where f.follower.id = :followerId")
    Long countFollowingByFollowerId(@Param("followerId") Long followerId);
}
